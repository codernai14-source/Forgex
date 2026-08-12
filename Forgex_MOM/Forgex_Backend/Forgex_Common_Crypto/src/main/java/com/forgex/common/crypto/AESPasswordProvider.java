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

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * AES-256-GCM 密码 Provider（可逆）。
 * <p>
 * 使用配置库中的对称密钥进行 AES-256-GCM 加/解密，初始化或首次使用时自动生成并保存密钥。
 * 输出格式：十六进制编码的 {@code IV(12字节) || 密文 || Tag(16字节)}。
 * <p>
 * 安全级别：银行级/军事级（AES-256），GCM 模式同时提供加密和完整性保护。
 * <p>
 * 不推荐作为密码存储方案；适合需要恢复明文的业务数据加密。
 */
public class AESPasswordProvider implements CryptoPasswordProvider {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 32; // 256 bits
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits

    private final ConfigService cfg;

    public AESPasswordProvider(ConfigService cfg) {
        this.cfg = cfg;
    }

    /** 算法名称：aes */
    @Override
    public String name() {
        return "aes";
    }

    /** 支持可逆加密 */
    @Override
    public boolean supportsEncrypt() {
        return true;
    }

    /** 不支持不可逆哈希 */
    @Override
    public boolean supportsHash() {
        return false;
    }

    /**
     * 确保存在 AES-256 密钥；若不存在则生成并写入配置库。
     *
     * @return 十六进制编码的密钥（64 字符 = 32 字节 = 256 位）
     */
    private String ensureKeyHex() {
        java.util.Map m = cfg.getJson("security.crypto.aes", java.util.Map.class, null);
        String keyHex = m == null ? null : (String) m.get("keyHex");
        if (keyHex == null || keyHex.isEmpty()) {
            byte[] k = new byte[KEY_SIZE];
            new SecureRandom().nextBytes(k);
            keyHex = HexUtil.encodeHexStr(k);
            java.util.Map<String, String> n = new java.util.HashMap<>();
            n.put("keyHex", keyHex);
            cfg.setJson("security.crypto.aes", n);
        }
        return keyHex;
    }

    /**
     * 获取 AES SecretKeySpec 实例。
     */
    private SecretKeySpec getSecretKey() {
        String keyHex = ensureKeyHex();
        byte[] keyBytes = HexUtil.decodeHex(keyHex);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 使用 AES-256-GCM 加密并返回十六进制密文。
     * 输出格式：Hex(IV || Ciphertext || Tag)
     */
    @Override
    public String encrypt(String plain) {
        try {
            SecretKeySpec key = getSecretKey();
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            // IV + 密文（含GCM Tag）
            byte[] result = new byte[GCM_IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, result, 0, GCM_IV_LENGTH);
            System.arraycopy(encrypted, 0, result, GCM_IV_LENGTH, encrypted.length);

            return HexUtil.encodeHexStr(result);
        } catch (Exception e) {
            throw new IllegalStateException("AES-256-GCM encrypt failed", e);
        }
    }

    /**
     * 解密十六进制密文为明文。
     * 输入格式：Hex(IV || Ciphertext || Tag)
     */
    @Override
    public String decrypt(String cipherText) {
        try {
            SecretKeySpec key = getSecretKey();
            byte[] data = HexUtil.decodeHex(cipherText);

            // 分离 IV 和密文
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(data, 0, iv, 0, GCM_IV_LENGTH);
            byte[] encrypted = new byte[data.length - GCM_IV_LENGTH];
            System.arraycopy(data, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("AES-256-GCM decrypt failed", e);
        }
    }

    /** 可逆算法不支持哈希 */
    @Override
    public String hash(String plain) {
        throw new UnsupportedOperationException("aes is reversible");
    }

    /** 解密存储值并与明文比对 */
    @Override
    public boolean verify(String plain, String stored) {
        try {
            String raw = decrypt(stored);
            return Objects.equals(plain, raw);
        } catch (Exception e) {
            return false;
        }
    }
}

