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

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;

/**
 * AES-256-GCM 文件加密工具。
 * <p>
 * 提供文件级别的 AES-256-GCM 加密与解密功能，支持流式处理大文件。
 * <p>
 * 加密文件格式：{@code [12字节 IV] [GCM加密数据（含128位认证Tag）]}
 * <p>
 * 安全级别：银行级/军事级（AES-256），GCM 模式同时提供机密性和完整性保护。
 * <p>
 * 使用示例：
 * <pre>
 * // 使用随机密钥
 * byte[] key = AESFileEncryptor.generateKey();
 * AESFileEncryptor.encryptFile(new File("input.txt"), new File("input.enc"), key);
 * AESFileEncryptor.decryptFile(new File("input.enc"), new File("output.txt"), key);
 *
 * // 使用流式 API
 * AESFileEncryptor.encryptStream(inputStream, outputStream, key);
 * AESFileEncryptor.decryptStream(inputStream, outputStream, key);
 * </pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public final class AESFileEncryptor {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits
    private static final int BUFFER_SIZE = 8192;

    private AESFileEncryptor() {
        // 工具类不允许实例化
    }

    /**
     * 生成 AES-256 密钥。
     *
     * @return 32 字节（256 位）的随机密钥
     */
    public static byte[] generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE);
            SecretKey secretKey = keyGen.generateKey();
            return secretKey.getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate AES-256 key", e);
        }
    }

    /**
     * 加密文件。
     * <p>
     * 输出文件格式：{@code [12字节 IV] [GCM加密数据]}
     *
     * @param inputFile  输入文件（明文）
     * @param outputFile 输出文件（密文）
     * @param key        AES-256 密钥（32 字节）
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
     * @param key           AES-256 密钥（32 字节）
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
     * 先写入 12 字节随机 IV，再写入 GCM 加密数据。
     *
     * @param in  输入流（明文）
     * @param out 输出流（密文）
     * @param key AES-256 密钥（32 字节）
     * @throws IOException 流读写异常
     */
    public static void encryptStream(InputStream in, OutputStream out, byte[] key) throws IOException {
        try {
            validateKey(key);

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

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
            throw new IOException("AES-256-GCM encryption failed", e);
        }
    }

    /**
     * 解密流。
     * <p>
     * 先读取 12 字节 IV，再解密后续 GCM 加密数据。
     *
     * @param in  输入流（密文）
     * @param out 输出流（明文）
     * @param key AES-256 密钥（32 字节）
     * @throws IOException 流读写异常
     */
    public static void decryptStream(InputStream in, OutputStream out, byte[] key) throws IOException {
        try {
            validateKey(key);

            // 读取 IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            int totalRead = 0;
            while (totalRead < GCM_IV_LENGTH) {
                int read = in.read(iv, totalRead, GCM_IV_LENGTH - totalRead);
                if (read == -1) {
                    throw new IOException("Unexpected end of stream while reading IV");
                }
                totalRead += read;
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

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
            throw new IOException("AES-256-GCM decryption failed", e);
        }
    }

    /**
     * 校验密钥长度。
     *
     * @param key AES 密钥
     */
    private static void validateKey(byte[] key) {
        if (key == null || key.length != 32) {
            throw new IllegalArgumentException("AES-256 key must be 32 bytes (256 bits), got: " + (key == null ? "null" : key.length));
        }
    }
}

