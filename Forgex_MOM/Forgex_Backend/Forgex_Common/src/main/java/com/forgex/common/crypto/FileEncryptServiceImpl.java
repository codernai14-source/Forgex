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
import com.forgex.common.config.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.SecureRandom;

/**
 * 文件加密服务实现。
 * <p>
 * 提供基于 AES-256-GCM 和 SM4-CBC 的文件加密/解密功能，以及 SM3 哈希完整性校验。
 * <p>
 * 密钥管理：
 * <ul>
 *   <li>AES-256 密钥：从配置库 {@code security.crypto.aes} 读取，若不存在则自动生成（32 字节）</li>
 *   <li>SM4 密钥：从配置库 {@code security.crypto.sm4} 读取，若不存在则自动生成（16 字节）</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileEncryptServiceImpl implements FileEncryptService {

    private static final int AES_KEY_SIZE = 32; // 256 bits
    private static final int SM4_KEY_SIZE = 16; // 128 bits

    private final ConfigService configService;

    @Override
    public void encryptFile(File inputFile, File outputFile, String algorithm) throws IOException {
        log.info("加密文件: {} -> {}, 算法: {}", inputFile.getName(), outputFile.getName(), algorithm);
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            encryptStream(fis, fos, algorithm);
        }
        log.info("文件加密完成: {}", outputFile.getName());
    }

    @Override
    public void decryptFile(File encryptedFile, File outputFile, String algorithm) throws IOException {
        log.info("解密文件: {} -> {}, 算法: {}", encryptedFile.getName(), outputFile.getName(), algorithm);
        try (FileInputStream fis = new FileInputStream(encryptedFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            decryptStream(fis, fos, algorithm);
        }
        log.info("文件解密完成: {}", outputFile.getName());
    }

    @Override
    public void encryptStream(InputStream in, OutputStream out, String algorithm) throws IOException {
        String algo = normalizeAlgorithm(algorithm);
        switch (algo) {
            case ALGORITHM_AES:
                AESFileEncryptor.encryptStream(in, out, resolveAESKey());
                break;
            case ALGORITHM_SM4:
                SM4FileEncryptor.encryptStream(in, out, resolveSM4Key());
                break;
            default:
                throw new IllegalArgumentException("Unsupported file encryption algorithm: " + algorithm);
        }
    }

    @Override
    public void decryptStream(InputStream in, OutputStream out, String algorithm) throws IOException {
        String algo = normalizeAlgorithm(algorithm);
        switch (algo) {
            case ALGORITHM_AES:
                AESFileEncryptor.decryptStream(in, out, resolveAESKey());
                break;
            case ALGORITHM_SM4:
                SM4FileEncryptor.decryptStream(in, out, resolveSM4Key());
                break;
            default:
                throw new IllegalArgumentException("Unsupported file encryption algorithm: " + algorithm);
        }
    }

    @Override
    public String computeDigest(File file) throws IOException {
        return SM3Utils.digestHex(file);
    }

    @Override
    public String computeDigest(InputStream inputStream) throws IOException {
        return SM3Utils.digestHex(inputStream);
    }

    @Override
    public boolean verifyDigest(File file, String expectedHash) throws IOException {
        return SM3Utils.verifyFile(file, expectedHash);
    }

    // ======================== 密钥管理 ========================

    /**
     * 获取 AES-256 密钥（从配置库读取，不存在则自动生成）。
     *
     * @return AES-256 密钥（32 字节）
     */
    private byte[] resolveAESKey() {
        java.util.Map m = configService.getJson("security.crypto.aes", java.util.Map.class, null);
        String keyHex = m == null ? null : (String) m.get("keyHex");
        if (keyHex == null || keyHex.isEmpty()) {
            log.info("AES-256 密钥不存在，自动生成...");
            byte[] key = new byte[AES_KEY_SIZE];
            new SecureRandom().nextBytes(key);
            keyHex = HexUtil.encodeHexStr(key);
            java.util.Map<String, String> n = new java.util.HashMap<>();
            n.put("keyHex", keyHex);
            configService.setJson("security.crypto.aes", n);
            log.info("AES-256 密钥已生成并保存到配置库");
        }
        return HexUtil.decodeHex(keyHex);
    }

    /**
     * 获取 SM4 密钥（从配置库读取，不存在则自动生成）。
     *
     * @return SM4 密钥（16 字节）
     */
    private byte[] resolveSM4Key() {
        java.util.Map m = configService.getJson("security.crypto.sm4", java.util.Map.class, null);
        String keyHex = m == null ? null : (String) m.get("keyHex");
        if (keyHex == null || keyHex.isEmpty()) {
            log.info("SM4 密钥不存在，自动生成...");
            byte[] key = new byte[SM4_KEY_SIZE];
            new SecureRandom().nextBytes(key);
            keyHex = HexUtil.encodeHexStr(key);
            java.util.Map<String, String> n = new java.util.HashMap<>();
            n.put("keyHex", keyHex);
            configService.setJson("security.crypto.sm4", n);
            log.info("SM4 密钥已生成并保存到配置库");
        }
        return HexUtil.decodeHex(keyHex);
    }

    /**
     * 标准化算法名称。
     */
    private static String normalizeAlgorithm(String algorithm) {
        if (algorithm == null) {
            return ALGORITHM_AES; // 默认使用 AES-256-GCM
        }
        return algorithm.trim().toLowerCase();
    }
}

