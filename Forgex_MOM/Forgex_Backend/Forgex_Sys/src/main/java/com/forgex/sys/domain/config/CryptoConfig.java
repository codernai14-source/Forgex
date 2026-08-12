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

package com.forgex.sys.domain.config;

import lombok.Data;

/**
 * 加密配置聚合对象。
 * <p>
 * 聚合了系统中各类加密算法的密钥和配置，供 B 端「加密配置」Tab 统一管理。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class CryptoConfig {

    /** SM4 对称加密配置 */
    private Sm4Config sm4 = new Sm4Config();

    /** AES-256 对称加密配置 */
    private AesConfig aes = new AesConfig();

    /** RSA 非对称加密配置 */
    private RsaConfig rsa = new RsaConfig();

    /** KMS 密钥管理配置 */
    private KmsConfig kms = new KmsConfig();

    /** 文件加密配置 */
    private FileEncryptConfig fileEncrypt = new FileEncryptConfig();

    /** 字段加密配置 */
    private FieldEncryptConfig fieldEncrypt = new FieldEncryptConfig();

    /**
     * 返回默认加密配置。
     *
     * @return 默认 CryptoConfig
     */
    public static CryptoConfig defaults() {
        return new CryptoConfig();
    }

    /**
     * SM4 对称加密配置。
     */
    @Data
    public static class Sm4Config {
        /** SM4 密钥（32字符Hex，128位） */
        private String keyHex = "";
    }

    /**
     * AES-256 对称加密配置。
     */
    @Data
    public static class AesConfig {
        /** AES-256 密钥（64字符Hex，256位） */
        private String keyHex = "";
    }

    /**
     * RSA 非对称加密配置。
     */
    @Data
    public static class RsaConfig {
        /** RSA 公钥（Base64） */
        private String publicKey = "";
        /** RSA 私钥（Base64） */
        private String privateKey = "";
        /** 密钥长度（2048/4096） */
        private int keySize = 2048;
    }

    /**
     * KMS 密钥管理配置。
     * <p>
     * 主密钥自 V0.8.0 起改为外部注入（环境变量 / 密钥文件），不再落库。
     * 此处的 {@code masterSource} / {@code masterKeyFile} 仅作为展示与默认值声明，
     * 实际读取逻辑由 {@code KmsServiceImpl} 按环境变量优先级处理。
     */
    @Data
    public static class KmsConfig {
        /**
         * 主密钥来源，默认 {@code env}。
         * <ul>
         *   <li>{@code env}：从环境变量 {@code FORGEX_KMS_MASTER_KEY_HEX} 读取</li>
         *   <li>{@code file}：从 {@code FORGEX_KMS_MASTER_KEY_FILE} 指定的文件读取，
         *       未设则默认 {@code ${FORGEX_LICENSE_DIR}/kms.key}</li>
         * </ul>
         */
        private String masterSource = "env";

        /** 主密钥文件路径（仅 file 来源时使用，默认空表示取 ${FORGEX_LICENSE_DIR}/kms.key） */
        private String masterKeyFile = "";

        /**
         * KMS 主密钥（64字符Hex，256位）。
         * <p>
         * 注意：自 V0.8.0 起该字段仅用于 B 端配置展示，不再作为实际主密钥来源。
         * 实际主密钥从外部环境变量或密钥文件注入。
         */
        private String masterKeyHex = "";

        /** 密钥轮换提醒天数 */
        private int rotateRemindDays = 90;
    }

    /**
     * 文件加密配置。
     */
    @Data
    public static class FileEncryptConfig {
        /** 是否启用文件自动加密 */
        private boolean enabled = false;
        /** 默认加密算法（aes/sm4） */
        private String defaultAlgorithm = "aes";
    }

    /**
     * 字段透明加密配置。
     */
    @Data
    public static class FieldEncryptConfig {
        /** 是否启用字段透明加密 */
        private boolean enabled = true;
        /** 全局默认算法（SM4/AES） */
        private String defaultAlgorithm = "SM4";
    }
}

