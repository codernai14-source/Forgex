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
import cn.hutool.crypto.SmUtil;
import org.bouncycastle.crypto.digests.SM3Digest;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * SM3 哈希摘要工具类（国密标准 GM/T 0004-2012）。
 * <p>
 * 提供 SM3 国密哈希摘要计算功能，用于数据完整性校验和防篡改。
 * SM3 输出 256 位（32 字节）哈希值，安全强度等同于 SHA-256。
 * <p>
 * 功能列表：
 * <ul>
 *   <li>字符串 SM3 哈希</li>
 *   <li>字节数组 SM3 哈希</li>
 *   <li>文件 SM3 哈希（流式处理，支持大文件）</li>
 *   <li>哈希值验证</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 字符串哈希
 * String hash = SM3Utils.digestHex("Hello World");
 *
 * // 文件哈希
 * String fileHash = SM3Utils.digestHex(new File("document.pdf"));
 *
 * // 验证完整性
 * boolean valid = SM3Utils.verify("Hello World", hash);
 * boolean fileValid = SM3Utils.verifyFile(new File("document.pdf"), fileHash);
 * </pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public final class SM3Utils {

    private static final int BUFFER_SIZE = 8192;

    private SM3Utils() {
        // 工具类不允许实例化
    }

    /**
     * 计算字符串的 SM3 哈希值。
     *
     * @param data 原始字符串
     * @return SM3 哈希值（64 字符十六进制字符串）
     */
    public static String digestHex(String data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        return SmUtil.sm3(data);
    }

    /**
     * 计算字节数组的 SM3 哈希值。
     *
     * @param data 原始字节数组
     * @return SM3 哈希值（64 字符十六进制字符串）
     */
    public static String digestHex(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        SM3Digest digest = new SM3Digest();
        digest.update(data, 0, data.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return HexUtil.encodeHexStr(hash);
    }

    /**
     * 计算文件的 SM3 哈希值（流式处理，支持大文件）。
     * <p>
     * 使用 BouncyCastle 的 SM3Digest 进行增量计算，避免将整个文件加载到内存。
     *
     * @param file 目标文件
     * @return SM3 哈希值（64 字符十六进制字符串）
     * @throws IOException 文件读取异常
     */
    public static String digestHex(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("file must not be null and must exist");
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return digestHex(fis);
        }
    }

    /**
     * 计算输入流的 SM3 哈希值（流式处理）。
     * <p>
     * 使用 BouncyCastle 的 SM3Digest 进行增量计算。
     *
     * @param inputStream 输入流
     * @return SM3 哈希值（64 字符十六进制字符串）
     * @throws IOException 流读取异常
     */
    public static String digestHex(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream must not be null");
        }
        SM3Digest digest = new SM3Digest();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return HexUtil.encodeHexStr(hash);
    }

    /**
     * 验证字符串的 SM3 哈希值是否匹配。
     *
     * @param data         原始字符串
     * @param expectedHash 期望的 SM3 哈希值（十六进制）
     * @return 是否匹配
     */
    public static boolean verify(String data, String expectedHash) {
        if (data == null || expectedHash == null) {
            return false;
        }
        String actualHash = digestHex(data);
        return actualHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * 验证字节数组的 SM3 哈希值是否匹配。
     *
     * @param data         原始字节数组
     * @param expectedHash 期望的 SM3 哈希值（十六进制）
     * @return 是否匹配
     */
    public static boolean verify(byte[] data, String expectedHash) {
        if (data == null || expectedHash == null) {
            return false;
        }
        String actualHash = digestHex(data);
        return actualHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * 验证文件的 SM3 哈希值是否匹配。
     *
     * @param file         目标文件
     * @param expectedHash 期望的 SM3 哈希值（十六进制）
     * @return 是否匹配
     * @throws IOException 文件读取异常
     */
    public static boolean verifyFile(File file, String expectedHash) throws IOException {
        if (file == null || expectedHash == null) {
            return false;
        }
        String actualHash = digestHex(file);
        return actualHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * 验证输入流的 SM3 哈希值是否匹配。
     *
     * @param inputStream  输入流
     * @param expectedHash 期望的 SM3 哈希值（十六进制）
     * @return 是否匹配
     * @throws IOException 流读取异常
     */
    public static boolean verifyStream(InputStream inputStream, String expectedHash) throws IOException {
        if (inputStream == null || expectedHash == null) {
            return false;
        }
        String actualHash = digestHex(inputStream);
        return actualHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * 计算字符串的 SM3 HMAC 哈希值。
     * <p>
     * 使用密钥进行 HMAC-SM3 计算，适用于消息认证。
     *
     * @param data 原始字符串
     * @param key  密钥
     * @return HMAC-SM3 哈希值（十六进制字符串）
     */
    public static String hmacHex(String data, byte[] key) {
        if (data == null || key == null) {
            throw new IllegalArgumentException("data and key must not be null");
        }
        return cn.hutool.crypto.SmUtil.hmacSm3(key).digestHex(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证 HMAC-SM3 哈希值是否匹配。
     *
     * @param data         原始字符串
     * @param key          密钥
     * @param expectedHmac 期望的 HMAC 哈希值（十六进制）
     * @return 是否匹配
     */
    public static boolean verifyHmac(String data, byte[] key, String expectedHmac) {
        if (data == null || key == null || expectedHmac == null) {
            return false;
        }
        String actualHmac = hmacHex(data, key);
        return actualHmac.equalsIgnoreCase(expectedHmac);
    }
}


