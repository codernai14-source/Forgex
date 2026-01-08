package com.forgex.common.crypto;

import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.asymmetric.KeyType;
import com.forgex.common.domain.config.CryptoTransportConfig;
import java.nio.charset.StandardCharsets;
import cn.hutool.core.util.HexUtil;

/**
 * SM2 密码 Provider（可逆）。
 * <p>
 * 使用配置库中的密钥对进行加/解密，适用于传输加密或需恢复明文的场景；
 * 不推荐作为密码存储方案。验证逻辑通过解密后比对明文。
 */
public class Sm2PasswordProvider implements CryptoPasswordProvider {
    private final CryptoTransportConfig cfg;
    public Sm2PasswordProvider(CryptoTransportConfig cfg) { this.cfg = cfg; }
    /** 算法名称：sm2 */
    @Override public String name() { return "sm2"; }
    /** 支持可逆加密 */
    @Override public boolean supportsEncrypt() { return true; }
    /** 不支持不可逆哈希 */
    @Override public boolean supportsHash() { return false; }
    /** 使用公钥加密为十六进制密文 */
    @Override public String encrypt(String plain) {
        SM2 sm2 = new SM2(null, cfg == null ? null : cfg.getPublicKey());
        byte[] enc = sm2.encrypt(plain.getBytes(StandardCharsets.UTF_8));
        return HexUtil.encodeHexStr(enc);
    }
    /** 使用私钥解密十六进制密文为明文 */
    @Override public String decrypt(String cipher) {
        SM2 sm2 = new SM2(cfg == null ? null : cfg.getPrivateKey(), cfg == null ? null : cfg.getPublicKey());
        byte[] dec = sm2.decrypt(HexUtil.decodeHex(cipher), KeyType.PrivateKey);
        return new String(dec, StandardCharsets.UTF_8);
    }
    /** 可逆算法不支持哈希 */
    @Override public String hash(String plain) { throw new UnsupportedOperationException("sm2 is reversible"); }
    /** 解密存储值并与明文比对 */
    @Override public boolean verify(String plain, String stored) {
        try { String raw = decrypt(stored); return java.util.Objects.equals(plain, raw); } catch (Exception e) { return false; }
    }
}
