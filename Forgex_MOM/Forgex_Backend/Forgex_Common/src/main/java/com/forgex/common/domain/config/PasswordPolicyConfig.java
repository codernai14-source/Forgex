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
    /** 榛樿瀵嗙爜 */
    private String defaultPassword;
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
