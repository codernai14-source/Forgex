/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgex.sys.service;

/**
 * 密钥管理服务接口（KMS）。
 * <p>
 * 提供密钥生命周期管理功能，包括：
 * <ul>
 *   <li>密钥生成与存储（加密后存储到数据库）</li>
 *   <li>密钥检索（解密后返回明文密钥）</li>
 *   <li>密钥轮换（生成新版本，旧版本标记为 ROTATED）</li>
 *   <li>密钥禁用/删除</li>
 *   <li>所有操作记录审计日志</li>
 * </ul>
 * <p>
 * 支持的密钥类型：AES（128/256位）、SM4（128位）、RSA（2048/4096位）、SM2
 * <p>
 * 密钥存储安全：所有密钥使用主密钥（Master Key）进行 AES-256-GCM 加密后再持久化。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface KmsService {

    /**
     * 生成并存储一个新密钥。
     *
     * @param alias       密钥别名（业务唯一标识）
     * @param keyType     密钥类型：AES / SM4 / RSA / SM2
     * @param keySize     密钥长度（位），如 128、256、2048、4096
     * @param description 密钥描述（可选）
     * @return 密钥 ID
     */
    Long generateKey(String alias, String keyType, int keySize, String description);

    /**
     * 获取当前激活的密钥明文。
     *
     * @param alias 密钥别名
     * @return 密钥明文（Base64 编码），如无激活密钥返回 null
     */
    String getActiveKey(String alias);

    /**
     * 获取指定版本的密钥明文。
     *
     * @param alias   密钥别名
     * @param version 密钥版本
     * @return 密钥明文（Base64 编码），如无对应密钥返回 null
     */
    String getKey(String alias, int version);

    /**
     * 轮换密钥。
     * <p>
     * 生成同类型同长度的新密钥，版本号 +1，旧密钥状态改为 ROTATED。
     *
     * @param alias 密钥别名
     * @return 新密钥 ID
     */
    Long rotateKey(String alias);

    /**
     * 禁用密钥。
     *
     * @param alias 密钥别名
     * @return 是否成功
     */
    boolean disableKey(String alias);

    /**
     * 获取密钥当前最大版本号。
     *
     * @param alias 密钥别名
     * @return 最大版本号，如无记录返回 0
     */
    int getMaxVersion(String alias);
}

