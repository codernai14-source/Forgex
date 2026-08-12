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

/**
 * 通用密码处理接口。
 * <p>
 * 定义密码在两类场景的统一操作：
 * 1) 可逆加密：用于需要恢复明文的场景（encrypt/decrypt/verify）。
 * 2) 不可逆哈希：用于密码存储与校验（hash/verify）。
 * <p>
 * 使用方式：通过 {@link com.forgex.common.crypto.CryptoProviders#resolve(String, com.forgex.common.config.ConfigService)}
 * 获取具体算法的 Provider，然后调用对应方法。外部仅关注 {@code verify} 的结果来判定登录是否通过。
 * <p>
 * 可扩展性：新增算法时实现本接口并在 {@code CryptoProviders} 中注册策略键。
 */
public interface CryptoPasswordProvider {
    /** 算法名称（策略键），例如：bcrypt、argon2、sm2、sm4、pbkdf2、scrypt。 */
    String name();
    /** 是否支持可逆加密（若 true，encrypt/decrypt 可用）。 */
    boolean supportsEncrypt();
    /** 是否支持不可逆哈希（若 true，hash/verify 可用）。 */
    boolean supportsHash();
    /** 执行可逆加密，返回密文。若算法不可逆，调用将抛出异常。 */
    String encrypt(String plain);
    /** 执行可逆解密，返回明文。若算法不可逆，调用将抛出异常。 */
    String decrypt(String cipher);
    /** 执行不可逆哈希，返回派生值。若算法可逆，调用将抛出异常。 */
    String hash(String plain);
    /** 校验明文与存储值是否匹配；可逆算法通过解密比对，不可逆算法通过重新派生常数时间比对。 */
    boolean verify(String plain, String stored);
}
