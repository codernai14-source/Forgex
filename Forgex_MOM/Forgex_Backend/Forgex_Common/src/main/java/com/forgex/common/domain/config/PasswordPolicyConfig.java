package com.forgex.common.domain.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 密码策略配置。
 * <p>
 * 控制密码存储算法与强度校验规则，初始化与登录时读取生效。
 * 字段：
 * - {@code store} 存储算法（bcrypt/argon2/scrypt/pbkdf2/...）；
 * - {@code minLength} 最小长度；
 * - {@code requireNumbers} 是否必须包含数字；
 * - {@code requireUppercase} 是否必须包含大写字母；
 * - {@code requireLowercase} 是否必须包含小写字母；
 * - {@code requireSymbols} 是否必须包含符号。
 */
@Setter
@Getter
public class PasswordPolicyConfig {
    /** 存储算法 */
    private String store;
    /** 最小长度 */
    private Integer minLength;
    /** 是否要求包含数字 */
    private Boolean requireNumbers;
    /** 是否要求包含大写字母 */
    private Boolean requireUppercase;
    /** 是否要求包含小写字母 */
    private Boolean requireLowercase;
    /** 是否要求包含符号 */
    private Boolean requireSymbols;

}
