package com.forgex.common.crypto;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Argon2id 密码 Provider（不可逆）。
 * <p>
 * 基于 BouncyCastle 的 Argon2BytesGenerator 实现，提供不可逆哈希与校验；
 * 默认参数内存 64MB、迭代 3、并行度 1。输出格式：{@code argon2id$m=<memKiB>,t=<iters>,p=<par>$saltBase64$hashBase64}。
 * 可在后续版本外置参数到配置库以支持调优。
 */
public class Argon2PasswordProvider implements CryptoPasswordProvider {
    private static final int MEMORY_KIB = 65536;
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 1;
    private static final int SALT_LEN = 16;
    private static final int OUT_LEN = 32;

    /** 算法名称：argon2 */
    @Override public String name() { return "argon2"; }
    /** 不支持可逆加密 */
    @Override public boolean supportsEncrypt() { return false; }
    /** 支持不可逆哈希 */
    @Override public boolean supportsHash() { return true; }
    /** 不可逆算法不支持加密 */
    @Override public String encrypt(String plain) { throw new UnsupportedOperationException("argon2 not reversible"); }
    /** 不可逆算法不支持解密 */
    @Override public String decrypt(String cipher) { throw new UnsupportedOperationException("argon2 not reversible"); }

    /** 生成 Argon2id 哈希（随机盐+内存/迭代/并行度参数） */
    @Override public String hash(String plain) {
        byte[] salt = new byte[SALT_LEN]; new SecureRandom().nextBytes(salt);
        byte[] out = derive(plain.toCharArray(), salt, MEMORY_KIB, ITERATIONS, PARALLELISM, OUT_LEN);
        return "argon2id$m=" + MEMORY_KIB + ",t=" + ITERATIONS + ",p=" + PARALLELISM + "$" + Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(out);
    }

    /** 校验明文与存储 Argon2id 哈希是否匹配 */
    @Override public boolean verify(String plain, String stored) {
        try {
            if (stored == null || !stored.startsWith("argon2id$")) return false;
            String[] parts = stored.split("\\$");
            String params = parts[1];
            String[] kvs = params.substring(3).split(","); // skip 'm='
            int m = Integer.parseInt(kvs[0]);
            int t = Integer.parseInt(kvs[1].substring(2)); // drop 't='
            int p = Integer.parseInt(kvs[2].substring(2)); // drop 'p='
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] hash = Base64.getDecoder().decode(parts[3]);
            byte[] out = derive(plain.toCharArray(), salt, m, t, p, hash.length);
            if (out.length != hash.length) return false;
            int r = 0; for (int i = 0; i < out.length; i++) r |= out[i] ^ hash[i];
            return r == 0;
        } catch (Exception e) { return false; }
    }

    /**
     * Argon2 派生内部实现。
     * @param password 明文字符数组
     * @param salt 盐值
     * @param mKiB 内存（KiB）
     * @param iterations 迭代次数
     * @param parallelism 并行度
     * @param outLen 输出长度（字节）
     * @return 派生字节数组
     */
    private byte[] derive(char[] password, byte[] salt, int mKiB, int iterations, int parallelism, int outLen) {
        Argon2Parameters.Builder b = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withMemoryAsKB(mKiB)
                .withIterations(iterations)
                .withParallelism(parallelism);
        Argon2Parameters params = b.build();
        Argon2BytesGenerator gen = new Argon2BytesGenerator();
        gen.init(params);
        byte[] out = new byte[outLen];
        gen.generateBytes(password, out);
        return out;
    }
}

