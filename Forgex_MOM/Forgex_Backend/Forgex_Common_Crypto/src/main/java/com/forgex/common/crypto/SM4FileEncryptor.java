/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgex.common.crypto;

import cn.hutool.core.util.HexUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.security.Security;

/**
 * SM4 文件加密工具类（国密标准 GM/T 0002-2012）。
 * <p>
 * 提供文件级别的 SM4/CBC/PKCS7Padding 加密与解密功能，支持流式处理大文件。
 * <p>
 * 加密文件格式：{@code [16字节 IV] [SM4-CBC加密数据]}
 * <p>
 * 安全级别：国密二级，符合国家密码管理局标准。
 * <p>
 * 使用示例：
 * <pre>
 * // 生成密钥
 * byte[] key = SM4FileEncryptor.generateKey();
 * String keyHex = HexUtil.encodeHexStr(key);
 *
 * // 加密文件
 * SM4FileEncryptor.encryptFile(new File("input.txt"), new File("input.enc"), key);
 *
 * // 解密文件
 * SM4FileEncryptor.decryptFile(new File("input.enc"), new File("output.txt"), key);
 * </pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public final class SM4FileEncryptor {

    private static final String ALGORITHM = "SM4";
    private static final String TRANSFORMATION = "SM4/CBC/PKCS7Padding";
    private static final int KEY_SIZE = 16; // 128 bits
    private static final int IV_LENGTH = 16; // 128 bits
    private static final int BUFFER_SIZE = 8192;

    static {
        // 确保 BouncyCastle 提供者已注册
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private SM4FileEncryptor() {
        // 工具类不允许实例化
    }

    /**
     * 生成 SM4 密钥。
     *
     * @return 16 字节（128 位）的随机密钥
     */
    public static byte[] generateKey() {
        byte[] key = new byte[KEY_SIZE];
        new SecureRandom().nextBytes(key);
        return key;
    }

    /**
     * 加密文件。
     * <p>
     * 输出文件格式：{@code [16字节 IV] [SM4-CBC加密数据]}
     *
     * @param inputFile  输入文件（明文）
     * @param outputFile 输出文件（密文）
     * @param key        SM4 密钥（16 字节）
     * @throws IOException 文件读写异常
     */
    public static void encryptFile(File inputFile, File outputFile, byte[] key) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            encryptStream(fis, fos, key);
        }
    }

    /**
     * 解密文件。
     *
     * @param encryptedFile 加密文件
     * @param outputFile    输出文件（明文）
     * @param key           SM4 密钥（16 字节）
     * @throws IOException 文件读写异常
     */
    public static void decryptFile(File encryptedFile, File outputFile, byte[] key) throws IOException {
        try (FileInputStream fis = new FileInputStream(encryptedFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            decryptStream(fis, fos, key);
        }
    }

    /**
     * 加密流。
     * <p>
     * 先写入 16 字节随机 IV，再写入 SM4-CBC 加密数据。
     *
     * @param in  输入流（明文）
     * @param out 输出流（密文）
     * @param key SM4 密钥（16 字节）
     * @throws IOException 流读写异常
     */
    public static void encryptStream(InputStream in, OutputStream out, byte[] key) throws IOException {
        try {
            validateKey(key);

            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // 先写入 IV
            out.write(iv);

            // 写入加密数据
            try (CipherOutputStream cos = new CipherOutputStream(out, cipher)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    cos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("SM4-CBC encryption failed", e);
        }
    }

    /**
     * 解密流。
     * <p>
     * 先读取 16 字节 IV，再解密后续 SM4-CBC 加密数据。
     *
     * @param in  输入流（密文）
     * @param out 输出流（明文）
     * @param key SM4 密钥（16 字节）
     * @throws IOException 流读写异常
     */
    public static void decryptStream(InputStream in, OutputStream out, byte[] key) throws IOException {
        try {
            validateKey(key);

            // 读取 IV
            byte[] iv = new byte[IV_LENGTH];
            int totalRead = 0;
            while (totalRead < IV_LENGTH) {
                int read = in.read(iv, totalRead, IV_LENGTH - totalRead);
                if (read == -1) {
                    throw new IOException("Unexpected end of stream while reading IV");
                }
                totalRead += read;
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 读取并解密数据
            try (CipherInputStream cis = new CipherInputStream(in, cipher)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = cis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("SM4-CBC decryption failed", e);
        }
    }

    /**
     * 将十六进制密钥字符串转换为字节数组。
     *
     * @param keyHex 十六进制密钥字符串（32 字符 = 16 字节）
     * @return 密钥字节数组
     */
    public static byte[] keyFromHex(String keyHex) {
        return HexUtil.decodeHex(keyHex);
    }

    /**
     * 校验密钥长度。
     *
     * @param key SM4 密钥
     */
    private static void validateKey(byte[] key) {
        if (key == null || key.length != KEY_SIZE) {
            throw new IllegalArgumentException("SM4 key must be 16 bytes (128 bits), got: " + (key == null ? "null" : key.length));
        }
    }
}

