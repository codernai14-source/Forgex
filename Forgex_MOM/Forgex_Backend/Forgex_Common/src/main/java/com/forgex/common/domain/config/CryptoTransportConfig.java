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
