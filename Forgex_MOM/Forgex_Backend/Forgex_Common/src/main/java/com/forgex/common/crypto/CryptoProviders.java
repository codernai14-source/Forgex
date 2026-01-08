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
package com.forgex.common.crypto;

import com.forgex.common.config.ConfigService;
import com.forgex.common.domain.config.CryptoTransportConfig;

/**
 * 密码处理 Provider 注册中心。
 * <p>
 * 负责根据策略键（例如：bcrypt、argon2、scrypt、pbkdf2、sm2、sm4）解析并返回具体 Provider 实现。
 * 部分算法需要从配置库读取参数（如 SM2 密钥对、SM4 密钥）。
 * <p>
 * 使用方式：调用 {@link #resolve(String, com.forgex.common.config.ConfigService)} 获取 Provider 后，
 * 使用其 {@code hash/encrypt/verify} 方法完成密码存储与校验。
 * <p>
 * 可扩展性：新增算法时在此增加分支并实现对应 Provider 类。
 */
public final class CryptoProviders {
    /**
     * 根据策略键解析 Provider。
     * @param store 策略键，忽略大小写；为空时默认 bcrypt
     * @param cfg   配置服务，用于读取算法相关参数
     * @return 匹配的密码处理 Provider
     */
    public static CryptoPasswordProvider resolve(String store, ConfigService cfg) {
        String v = store == null ? "bcrypt" : store.toLowerCase();
        switch (v) {
            case "sm2":
                CryptoTransportConfig c = cfg.getJson("security.crypto.transport", CryptoTransportConfig.class, null);
                return new Sm2PasswordProvider(c);
            case "sm4":
                return new Sm4PasswordProvider(cfg);
            case "argon2":
                return new Argon2PasswordProvider();
            case "scrypt":
                return new SCryptPasswordProvider();
            case "pbkdf2":
                return new Pbkdf2PasswordProvider();
            case "bcrypt":
            default:
                return new BCryptPasswordProvider();
        }
    }
}
