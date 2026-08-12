package com.forgex.common.license;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * License 配置属性
 * <p>
 * 用于承载授权文件路径、公钥、机器码覆盖等运行时配置。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "forgex.license")
public class LicenseProperties {

    /**
     * 是否启用授权校验。
     */
    private boolean enabled = true;

    /**
     * Ed25519 公钥，要求为 Base64 编码的 X509 公钥。
     */
    private String publicKey = "";

    /**
     * Ed25519 公钥文件名称。
     */
    private String publicKeyFileName = "public-key.base64";

    /**
     * 授权文件名称。
     */
    private String fileName = "license.lic";

    /**
     * 授权请求信息文件名称。
     */
    private String requestInfoFileName = "request-info.json";

    /**
     * 授权历史文件名称。
     */
    private String historyFileName = "activation-history.json";

    /**
     * 机器码覆盖值，便于安装器或测试环境注入。
     */
    private String overrideMachineCode = "";

    /**
     * 客户码前缀。
     */
    private String customerCodePrefix = "FXC";

    /**
     * 即将到期阈值（天）。
     */
    private int warningDays = 30;

    /**
     * 调度器是否启用。
     */
    private boolean schedulerEnabled = false;

    /**
     * 调度刷新间隔，单位毫秒。
     */
    private long refreshIntervalMs = 15 * 60 * 1000L;
}
