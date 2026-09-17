package com.forgex.common.crypto;

/**
 * 字段加密密钥提供者注册表。
 * <p>
 * {@link Sm4PasswordProvider} 通过静态查找避免 Common_Crypto 反向依赖 Sys。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public final class FieldEncryptKeyProviders {

    private static volatile FieldEncryptKeyProvider current;

    private FieldEncryptKeyProviders() {
    }

    /**
     * 注册当前进程的密钥提供者。
     *
     * @param provider 提供者，允许为 null 以清空
     */
    public static void register(FieldEncryptKeyProvider provider) {
        current = provider;
    }

    /**
     * 获取当前提供者。
     *
     * @return 提供者，未注册时为 null
     */
    public static FieldEncryptKeyProvider current() {
        return current;
    }
}
