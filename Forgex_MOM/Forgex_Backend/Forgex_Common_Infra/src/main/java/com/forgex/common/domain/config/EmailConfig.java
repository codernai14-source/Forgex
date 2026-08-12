package com.forgex.common.domain.config;

import lombok.Data;

/**
 * 邮件发送配置
 * <p>
 * 用于配置邮件发送的相关参数，包括 SMTP 服务器、认证信息、加密方式等。
 * 该配置支持多种邮件服务商（本地、阿里云、QQ 邮箱等）。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 */
@Data
public class EmailConfig {
    
    /**
     * 配置类型
     * <p>邮件服务提供商类型，支持 local（本地）、aliyun（阿里云）、qq（QQ 邮箱）等。</p>
     */
    private String providerType;
    
    /**
     * 发件邮箱账号
     * <p>用于发送邮件的邮箱地址。</p>
     */
    private String senderAccount;
    
    /**
     * 发件邮箱密码或授权码
     * <p>邮箱的登录密码或 SMTP 授权码。</p>
     */
    private String senderPassword;
    
    /**
     * SMTP 主机
     * <p>邮件服务器的 SMTP 主机地址。</p>
     */
    private String smtpHost;
    
    /**
     * SMTP 端口
     * <p>邮件服务器的 SMTP 端口号，通常为 25、465 或 587。</p>
     */
    private Integer smtpPort;
    
    /**
     * 是否启用账号密码认证
     * <p>标识是否需要 SMTP 认证，true 表示需要认证。</p>
     */
    private Boolean authEnabled;
    
    /**
     * 是否启用 SSL
     * <p>标识是否启用 SSL 加密连接。</p>
     */
    private Boolean sslEnabled;
    
    /**
     * 是否启用 STARTTLS
     * <p>标识是否启用 STARTTLS 加密方式。</p>
     */
    private Boolean starttlsEnabled;
    
    /**
     * 创建默认的邮件配置
     *
     * @return 默认的邮件配置对象
     */
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
