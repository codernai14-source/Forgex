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
package com.forgex.common.notification;

import com.forgex.common.config.ConfigService;
import com.forgex.common.domain.config.EmailConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

/**
 * 邮件发送服务。
 * <p>优先读取系统配置中的邮件配置；若未配置，再回退到 spring.mail 配置。</p>
 */
@Slf4j
@Service
public class EmailService {

    private static final String KEY_MAIL_SETTINGS = "mail.settings";

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private ConfigService configService;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.enabled:false}")
    private boolean enabled;

    /**
     * 发送简单文本邮件。
     */
    public boolean sendSimpleEmail(String to, String subject, String content) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(subject) || !StringUtils.hasText(content)) {
            log.error("邮件参数不完整：to={}, subject={}, content={}", to, subject, content);
            return false;
        }

        MailClient client = resolveMailClient();
        if (!client.isAvailable()) {
            log.warn("邮件服务未配置，跳过发送");
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(client.getFromAddress());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            client.getSender().send(message);
            log.info("简单邮件发送成功：to={}, subject={}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("简单邮件发送失败：to={}, subject={}, error={}", to, subject, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送 HTML 邮件。
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(subject) || !StringUtils.hasText(htmlContent)) {
            log.error("邮件参数不完整：to={}, subject={}, htmlContent={}", to, subject, htmlContent);
            return false;
        }

        MailClient client = resolveMailClient();
        if (!client.isAvailable()) {
            log.warn("邮件服务未配置，跳过发送");
            return false;
        }

        try {
            MimeMessage mimeMessage = client.getSender().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(client.getFromAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            client.getSender().send(mimeMessage);
            log.info("HTML 邮件发送成功：to={}, subject={}", to, subject);
            return true;
        } catch (MessagingException e) {
            log.error("HTML 邮件发送失败：to={}, subject={}, error={}", to, subject, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送模板邮件。
     */
    public boolean sendTemplateEmail(String to, String subject, String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template)) {
            log.error("邮件模板为空");
            return false;
        }

        String htmlContent = template;
        if (variables != null && !variables.isEmpty()) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                htmlContent = htmlContent.replace(placeholder, value);
            }
        }

        return sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 批量发送邮件。
     */
    public int sendBatchEmail(String[] toList, String subject, String content) {
        if (toList == null || toList.length == 0) {
            log.warn("收件人列表为空");
            return 0;
        }

        int successCount = 0;
        for (String to : toList) {
            if (sendSimpleEmail(to, subject, content)) {
                successCount++;
            }
        }
        log.info("批量邮件发送完成：总数={}, 成功={}", toList.length, successCount);
        return successCount;
    }

    /**
     * 检查邮件服务是否可用。
     */
    public boolean isAvailable() {
        return resolveMailClient().isAvailable();
    }

    private MailClient resolveMailClient() {
        EmailConfig config = resolveConfig();
        if (config != null && isDynamicConfigAvailable(config)) {
            return new MailClient(buildSender(config), config.getSenderAccount());
        }
        return new MailClient(mailSender, fromEmail, enabled);
    }

    private EmailConfig resolveConfig() {
        if (configService == null) {
            return null;
        }
        return configService.getGlobalJson(KEY_MAIL_SETTINGS, EmailConfig.class, EmailConfig.defaults());
    }

    private boolean isDynamicConfigAvailable(EmailConfig config) {
        if (config == null) {
            return false;
        }
        if (!StringUtils.hasText(config.getSenderAccount())
                || !StringUtils.hasText(config.getSmtpHost())
                || config.getSmtpPort() == null) {
            return false;
        }
        return !Boolean.TRUE.equals(config.getAuthEnabled()) || StringUtils.hasText(config.getSenderPassword());
    }

    private JavaMailSender buildSender(EmailConfig config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getSmtpHost());
        sender.setPort(config.getSmtpPort());
        sender.setUsername(config.getSenderAccount());
        sender.setPassword(config.getSenderPassword());
        sender.setDefaultEncoding("UTF-8");

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", Boolean.TRUE.equals(config.getAuthEnabled()));
        properties.put("mail.smtp.ssl.enable", Boolean.TRUE.equals(config.getSslEnabled()));
        properties.put("mail.smtp.starttls.enable", Boolean.TRUE.equals(config.getStarttlsEnabled()));
        return sender;
    }

    private static final class MailClient {
        private final JavaMailSender sender;
        private final String fromAddress;
        private final boolean available;

        private MailClient(JavaMailSender sender, String fromAddress) {
            this(sender, fromAddress, sender != null && StringUtils.hasText(fromAddress));
        }

        private MailClient(JavaMailSender sender, String fromAddress, boolean enabled) {
            this.sender = sender;
            this.fromAddress = fromAddress;
            this.available = enabled && sender != null && StringUtils.hasText(fromAddress);
        }

        private JavaMailSender getSender() {
            return sender;
        }

        private String getFromAddress() {
            return fromAddress;
        }

        private boolean isAvailable() {
            return available;
        }
    }
}
