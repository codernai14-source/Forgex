/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.common.crypto;

import org.bouncycastle.crypto.generators.SCrypt;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * scrypt 密码 Provider（不可逆）。
 * <p>
 * 基于 BouncyCastle 的 SCrypt 实现，提供不可逆哈希与校验；默认参数 N=16384, r=8, p=1。
 * 输出格式：{@code scrypt$N=<N>,r=<r>,p=<p>$saltBase64$hashBase64}。
 */
public class SCryptPasswordProvider implements CryptoPasswordProvider {
    private static final int N = 16384;
    private static final int r = 8;
    private static final int p = 1;
    private static final int SALT_LEN = 16;
    private static final int OUT_LEN = 32;

    /** 算法名称：scrypt */
    @Override public String name() { return "scrypt"; }
    /** 不支持可逆加密 */
    @Override public boolean supportsEncrypt() { return false; }
    /** 支持不可逆哈希 */
    @Override public boolean supportsHash() { return true; }
    /** 不可逆算法不支持加密 */
    @Override public String encrypt(String plain) { throw new UnsupportedOperationException("scrypt not reversible"); }
    /** 不可逆算法不支持解密 */
    @Override public String decrypt(String cipher) { throw new UnsupportedOperationException("scrypt not reversible"); }

    /** 生成 scrypt 哈希（随机盐+参数 N/r/p） */
    @Override public String hash(String plain) {
        byte[] salt = new byte[SALT_LEN]; new SecureRandom().nextBytes(salt);
        byte[] out = SCrypt.generate(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8), salt, N, r, p, OUT_LEN);
        return "scrypt$N=" + N + ",r=" + r + ",p=" + p + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(out);
    }

    /** 校验明文与存储 scrypt 哈希是否匹配 */
    @Override public boolean verify(String plain, String stored) {
        try {
            if (stored == null || !stored.startsWith("scrypt$")) return false;
            String[] parts = stored.split("\\$");
            String params = parts[1];
            String[] kvs = params.split(",");
            int n = Integer.parseInt(kvs[0].substring(2));
            int rr = Integer.parseInt(kvs[1].substring(2));
            int pp = Integer.parseInt(kvs[2].substring(2));
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] hash = Base64.getDecoder().decode(parts[3]);
            byte[] out = SCrypt.generate(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8), salt, n, rr, pp, hash.length);
            if (out.length != hash.length) return false;
            int d = 0; for (int i = 0; i < out.length; i++) d |= out[i] ^ hash[i];
            return d == 0;
        } catch (Exception e) { return false; }
    }
}
