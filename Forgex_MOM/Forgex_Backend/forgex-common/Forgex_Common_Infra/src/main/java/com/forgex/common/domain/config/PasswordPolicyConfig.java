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
package com.forgex.common.domain.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 密码策略配置
 * <p>
 * 控制密码存储算法与强度校验规则，初始化与登录时读取生效。
 * 该配置用于定义系统密码的安全策略，包括加密算法和复杂度要求。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 */
@Setter
@Getter
public class PasswordPolicyConfig {
    /** 是否启用密码有效期策略。 */
    private Boolean expireEnabled = false;

    /** 密码有效天数，启用后生效。 */
    private Integer expireDays = 90;

    /** 到期前预警天数。 */
    private Integer expireWarnDays = 7;
    /**
     * 存储算法
     * <p>密码加密存储算法，支持 bcrypt、argon2、scrypt、pbkdf2 等。</p>
     */
    private String store;
    
    /**
     * 默认密码
     * <p>系统初始化或重置密码时使用的默认密码。</p>
     */
    private String defaultPassword;
    
    /**
     * 最小长度
     * <p>密码的最小长度要求。</p>
     */
    private Integer minLength;
    
    /**
     * 是否要求包含数字
     * <p>密码是否必须包含数字字符。</p>
     */
    private Boolean requireNumbers;
    
    /**
     * 是否要求包含大写字母
     * <p>密码是否必须包含大写英文字母。</p>
     */
    private Boolean requireUppercase;
    
    /**
     * 是否要求包含小写字母
     * <p>密码是否必须包含小写英文字母。</p>
     */
    private Boolean requireLowercase;
    
    /**
     * 是否要求包含符号
     * <p>密码是否必须包含特殊符号（如!@#$% 等）。</p>
     */
    private Boolean requireSymbols;

    /**
     * 密码最大有效期（天）。
     * <p>
     * 0 表示不过期。等保建议 90 天。登录强制改密需合规等级 L2/L3。
     * </p>
     */
    private Integer maxAgeDays;

    /**
     * 禁止复用的最近密码数量。
     * <p>
     * 定期改密时不允许与当前密码及最近 N 次历史密码相同。0 表示不限制。等保建议不少于 5 次。
     * </p>
     */
    private Integer historyCount;

    /**
     * 是否要求首次登录修改初始密码。
     * <p>
     * 管理员重置或新用户使用默认密码后，下次登录强制改密。需合规等级 L2/L3。
     * </p>
     */
    private Boolean forceChangeOnFirstLogin;

    /**
     * 最低密码强度等级（1-4，对应启用字符类别数量）。
     */
    private Integer minStrengthLevel;
}
