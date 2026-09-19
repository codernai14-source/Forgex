package com.forgex.common.crypto;

/**
 * 字段加密 SM4 密钥提供者。
 * <p>
 * 由业务模块（通常是 Sys + KMS）注册实现；未注册时 {@link Sm4PasswordProvider} 回退读取配置库。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see Sm4PasswordProvider
 */
public interface FieldEncryptKeyProvider {

    /**
     * 解析当前生效的 SM4 密钥十六进制串。
     *
     * @return 16 字节密钥的 hex；无法提供时返回 null
     */
    String resolveSm4KeyHex();
}
