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
}
