package com.forgex.common.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PBKDF2 密码 Provider（HmacSHA256）。
 * <p>
 * 提供不可逆哈希与校验，输出格式：{@code pbkdf2$iterations$saltBase64$hashBase64}。
 * 适合密码存储，参数可在后续扩展为外部配置。
 */
public class Pbkdf2PasswordProvider implements CryptoPasswordProvider {
    private static final int ITERATIONS = 120000;
    private static final int SALT_LEN = 16;
    private static final int KEY_LEN = 256; // bits

    /** 算法名称：pbkdf2 */
    @Override public String name() { return "pbkdf2"; }
    /** 不支持可逆加密 */
    @Override public boolean supportsEncrypt() { return false; }
    /** 支持不可逆哈希 */
    @Override public boolean supportsHash() { return true; }
    /** 不可逆算法不支持加密 */
    @Override public String encrypt(String plain) { throw new UnsupportedOperationException("pbkdf2 not reversible"); }
    /** 不可逆算法不支持解密 */
    @Override public String decrypt(String cipher) { throw new UnsupportedOperationException("pbkdf2 not reversible"); }

    /** 生成 PBKDF2 哈希（随机盐+高迭代） */
    @Override public String hash(String plain) {
        try {
            byte[] salt = new byte[SALT_LEN];
            new SecureRandom().nextBytes(salt);
            byte[] dk = derive(plain.toCharArray(), salt, ITERATIONS, KEY_LEN);
            return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(dk);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** 校验明文与存储 PBKDF2 哈希是否匹配 */
    @Override public boolean verify(String plain, String stored) {
        try {
            if (stored == null || !stored.startsWith("pbkdf2$")) return false;
            String[] parts = stored.split("\\$");
            int it = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] hash = Base64.getDecoder().decode(parts[3]);
            byte[] dk = derive(plain.toCharArray(), salt, it, KEY_LEN);
            if (dk.length != hash.length) return false;
            int r = 0; for (int i = 0; i < dk.length; i++) r |= dk[i] ^ hash[i];
            return r == 0;
        } catch (Exception e) { return false; }
    }

    private byte[] derive(char[] password, byte[] salt, int iterations, int keyLen) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLen);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }
}
