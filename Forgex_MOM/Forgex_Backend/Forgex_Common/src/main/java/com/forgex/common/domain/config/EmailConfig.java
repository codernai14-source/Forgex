package com.forgex.common.domain.config;

import lombok.Data;

/**
 * 邮件发送配置。
 */
@Data
public class EmailConfig {

    /** 配置类型：local/aliyun/qq */
    private String providerType;

    /** 发件邮箱账号 */
    private String senderAccount;

    /** 发件邮箱密码或授权码 */
    private String senderPassword;

    /** SMTP 主机 */
    private String smtpHost;

    /** SMTP 端口 */
    private Integer smtpPort;

    /** 是否启用账号密码认证 */
    private Boolean authEnabled;

    /** 是否启用 SSL */
    private Boolean sslEnabled;

    /** 是否启用 STARTTLS */
    private Boolean starttlsEnabled;

    public static EmailConfig defaults() {
        EmailConfig config = new EmailConfig();
        config.setProviderType("local");
        config.setSenderAccount("");
        config.setSenderPassword("");
        config.setSmtpHost("");
        config.setSmtpPort(465);
        config.setAuthEnabled(Boolean.TRUE);
        config.setSslEnabled(Boolean.TRUE);
        config.setStarttlsEnabled(Boolean.TRUE);
        return config;
    }
}
