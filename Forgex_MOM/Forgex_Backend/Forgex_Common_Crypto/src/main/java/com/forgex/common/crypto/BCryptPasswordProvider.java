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

import cn.hutool.crypto.digest.BCrypt;

/**
 * BCrypt 密码 Provider。
 * <p>
 * 提供不可逆哈希与校验，适合密码存储。使用 Hutool 的 BCrypt 实现。
 * 使用：通过 {@link com.forgex.common.crypto.CryptoProviders#resolve(String, com.forgex.common.config.ConfigService)} 获取实例后，
 * 调用 {@link #hash(String)} 存储、{@link #verify(String, String)} 校验。
 */
public class BCryptPasswordProvider implements CryptoPasswordProvider {
    /** 算法名称：bcrypt */
    @Override public String name() { return "bcrypt"; }
    /** 不支持可逆加密 */
    @Override public boolean supportsEncrypt() { return false; }
    /** 支持不可逆哈希 */
    @Override public boolean supportsHash() { return true; }
    /** 不可逆算法不支持加密 */
    @Override public String encrypt(String plain) { throw new UnsupportedOperationException("bcrypt not reversible"); }
    /** 不可逆算法不支持解密 */
    @Override public String decrypt(String cipher) { throw new UnsupportedOperationException("bcrypt not reversible"); }
    /** 生成 BCrypt 哈希 */
    @Override public String hash(String plain) { return BCrypt.hashpw(plain); }
    /** 校验明文与存储的 BCrypt 哈希是否匹配 */
    @Override public boolean verify(String plain, String stored) { return BCrypt.checkpw(plain, stored); }
}
