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
 * 传输加密配置
 * <p>
 * 描述前端到后端的入参加密方案及密钥材料，用于保护敏感数据在传输过程中的安全。
 * 当前支持 SM2 国密算法。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 */
@Setter
@Getter
public class CryptoTransportConfig {
    /**
     * 算法名称
     * <p>加密算法名称，如 SM2（国密椭圆曲线公钥密码算法）。</p>
     */
    private String algorithm;
    
    /**
     * 公钥 Base64
     * <p>加密使用的公钥，采用 Base64 编码。</p>
     */
    private String publicKey;
    
    /**
     * 私钥 Base64
     * <p>解密使用的私钥，采用 Base64 编码，需妥善保管。</p>
     */
    private String privateKey;
    
    /**
     * 密文格式
     * <p>加密后密文的编码格式，如 BCD 码或 Hex（十六进制）。</p>
     */
    private String cipher;
}
