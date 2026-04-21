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
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * RSA 密码 Provider（可逆）。
 * <p>
 * 使用 RSA 非对称加密算法进行加/解密，支持 RSA-2048 和 RSA-4096。
 * 公钥加密、私钥解密，适用于短密文传输加密场景。
 * <p>
 * <strong>注意</strong>：RSA 加密有明文长度限制（RSA-2048 最多加密 214 字节，RSA-4096 最多加密 470 字节），
 * 不适合大数据量加密，建议仅用于密钥交换或短密码加密。大数据量请使用 AES-256 或 SM4。
 * <p>
 * 密钥存储位置：{@code security.crypto.rsa}（ConfigService JSON 配置）
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public class RSAPasswordProvider implements CryptoPasswordProvider {

    private final String publicKeyBase64;
    private final String privateKeyBase64;

    /**
     * 使用 Base64 编码的密钥对创建 RSA Provider。
     *
     * @param publicKeyBase64  公钥 Base64
     * @param privateKeyBase64 私钥 Base64
     */
    public RSAPasswordProvider(String publicKeyBase64, String privateKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
        this.privateKeyBase64 = privateKeyBase64;
    }

    /** 算法名称：rsa */
    @Override
    public String name() {
        return "rsa";
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
     * 使用 RSA 公钥加密并返回十六进制密文。
     */
    @Override
    public String encrypt(String plain) {
        try {
            RSA rsa = new RSA(null, publicKeyBase64);
            byte[] encrypted = rsa.encrypt(plain.getBytes(StandardCharsets.UTF_8), KeyType.PublicKey);
            return HexUtil.encodeHexStr(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("RSA encrypt failed", e);
        }
    }

    /**
     * 使用 RSA 私钥解密十六进制密文为明文。
     */
    @Override
    public String decrypt(String cipherText) {
        try {
            RSA rsa = new RSA(privateKeyBase64, publicKeyBase64);
            byte[] decrypted = rsa.decrypt(HexUtil.decodeHex(cipherText), KeyType.PrivateKey);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("RSA decrypt failed", e);
        }
    }

    /** 可逆算法不支持哈希 */
    @Override
    public String hash(String plain) {
        throw new UnsupportedOperationException("rsa is reversible");
    }

    /** 解密存储值并与明文比对 */
    @Override
    public boolean verify(String plain, String stored) {
        try {
            String raw = decrypt(stored);
            return java.util.Objects.equals(plain, raw);
        } catch (Exception e) {
            return false;
        }
    }

    // ======================== 密钥生成工具 ========================

    /**
     * 生成 RSA 密钥对。
     *
     * @param keySize 密钥长度（推荐 2048 或 4096）
     * @return Base64 编码的密钥对，[0]=公钥, [1]=私钥
     */
    public static String[] generateKeyPair(int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();
            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            return new String[]{publicKey, privateKey};
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA-" + keySize + " key pair", e);
        }
    }

    /**
     * 生成 RSA-2048 密钥对。
     *
     * @return Base64 编码的密钥对，[0]=公钥, [1]=私钥
     */
    public static String[] generateKeyPair2048() {
        return generateKeyPair(2048);
    }

    /**
     * 生成 RSA-4096 密钥对。
     *
     * @return Base64 编码的密钥对，[0]=公钥, [1]=私钥
     */
    public static String[] generateKeyPair4096() {
        return generateKeyPair(4096);
    }
}

