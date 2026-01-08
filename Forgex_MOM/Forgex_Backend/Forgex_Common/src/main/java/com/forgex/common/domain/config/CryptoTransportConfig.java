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
 * 传输加密配置。
 * <p>
 * 描述前端到后端的入参加密方案及密钥材料（当前支持 SM2）。
 * 字段：
 * - {@code algorithm} 算法名称（如 SM2）；
 * - {@code publicKey} 公钥（Base64）；
 * - {@code privateKey} 私钥（Base64）；
 * - {@code cipher} 密文格式（BCD/Hex 等）。
 */
@Setter
@Getter
public class CryptoTransportConfig {
    /** 算法名称 */
    private String algorithm;
    /** 公钥 Base64 */
    private String publicKey;
    /** 私钥 Base64 */
    private String privateKey;
    /** 密文格式（BCD/Hex） */
    private String cipher;

}
