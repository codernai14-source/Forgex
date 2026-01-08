package com.forgex.common.crypto;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.symmetric.SM4;
import com.forgex.common.config.ConfigService;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * SM4 密码 Provider（可逆）。
 * <p>
 * 使用配置库中的对称密钥进行加/解密，初始化或首次使用时自动生成并保存密钥。
 * 不推荐作为密码存储方案；适合需要恢复明文的业务数据加密。
 */
public class Sm4PasswordProvider implements CryptoPasswordProvider {
    private final ConfigService cfg;
    public Sm4PasswordProvider(ConfigService cfg) { this.cfg = cfg; }
    /** 算法名称：sm4 */
    @Override public String name() { return "sm4"; }
    /** 支持可逆加密 */
    @Override public boolean supportsEncrypt() { return true; }
    /** 不支持不可逆哈希 */
    @Override public boolean supportsHash() { return false; }

    /**
     * 确保存在 SM4 密钥；若不存在则生成并写入配置库。
     * @return 十六进制编码的密钥
     */
    private String ensureKeyHex() {
        java.util.Map m = cfg.getJson("security.crypto.sm4", java.util.Map.class, null);
        String keyHex = m == null ? null : (String) m.get("keyHex");
        if (keyHex == null || keyHex.length() == 0) {
            byte[] k = new byte[16]; new SecureRandom().nextBytes(k);
            keyHex = HexUtil.encodeHexStr(k);
            java.util.Map<String,String> n = new java.util.HashMap<>();
            n.put("keyHex", keyHex);
            cfg.setJson("security.crypto.sm4", n);
        }
        return keyHex;
    }

    /** 获取 SM4 算法实例（读取配置密钥）。 */
    private SM4 sm4() { String keyHex = ensureKeyHex(); return new SM4(HexUtil.decodeHex(keyHex)); }

    /** 使用 SM4 对称加密并返回十六进制密文 */
    @Override public String encrypt(String plain) {
        return sm4().encryptHex(plain, StandardCharsets.UTF_8);
    }
    /** 解密十六进制密文为明文 */
    @Override public String decrypt(String cipher) {
        byte[] dec = sm4().decrypt(cipher);
        return new String(dec, StandardCharsets.UTF_8);
    }
    /** 可逆算法不支持哈希 */
    @Override public String hash(String plain) { throw new UnsupportedOperationException("sm4 is reversible"); }
    /** 解密存储值并与明文比对 */
    @Override public boolean verify(String plain, String stored) {
        try { String raw = decrypt(stored); return java.util.Objects.equals(plain, raw); } catch (Exception e) { return false; }
    }
}
