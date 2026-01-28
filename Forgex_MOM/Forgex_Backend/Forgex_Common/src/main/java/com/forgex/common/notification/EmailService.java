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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

/**
 * 邮件发送服务
 * <p>提供邮件发送功能，支持简单文本邮件和HTML模板邮件。</p>
 * 
 * <p>配置示例（application.yml）：</p>
 * <pre>
 * spring:
 *   mail:
 *     host: smtp.qq.com
 *     port: 587
 *     username: your-email@qq.com
 *     password: your-auth-code
 *     properties:
 *       mail:
 *         smtp:
 *           auth: true
 *           starttls:
 *             enable: true
 *             required: true
 * </pre>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.enabled:false}")
    private boolean enabled;

    /**
     * 发送简单文本邮件
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容（纯文本）
     * @return true表示发送成功，false表示发送失败
     */
    public boolean sendSimpleEmail(String to, String subject, String content) {
        if (!enabled) {
            log.warn("邮件服务未启用，跳过发送");
            return false;
        }

        if (mailSender == null) {
            log.error("JavaMailSender未配置，无法发送邮件");
            return false;
        }

        if (!StringUtils.hasText(to) || !StringUtils.hasText(subject) || !StringUtils.hasText(content)) {
            log.error("邮件参数不完整：to={}, subject={}, content={}", to, subject, content);
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("简单邮件发送成功：to={}, subject={}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("简单邮件发送失败：to={}, subject={}, error={}", to, subject, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送HTML邮件
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param htmlContent HTML格式的邮件内容
     * @return true表示发送成功，false表示发送失败
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!enabled) {
            log.warn("邮件服务未启用，跳过发送");
            return false;
        }

        if (mailSender == null) {
            log.error("JavaMailSender未配置，无法发送邮件");
            return false;
        }

        if (!StringUtils.hasText(to) || !StringUtils.hasText(subject) || !StringUtils.hasText(htmlContent)) {
            log.error("邮件参数不完整：to={}, subject={}, htmlContent={}", to, subject, htmlContent);
            return false;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true表示HTML格式
            
            mailSender.send(mimeMessage);
            log.info("HTML邮件发送成功：to={}, subject={}", to, subject);
            return true;
        } catch (MessagingException e) {
            log.error("HTML邮件发送失败：to={}, subject={}, error={}", to, subject, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送模板邮件（支持占位符替换）
     * 
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param template HTML模板内容（支持${变量名}占位符）
     * @param variables 变量Map（key为变量名，value为替换值）
     * @return true表示发送成功，false表示发送失败
     */
    public boolean sendTemplateEmail(String to, String subject, String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template)) {
            log.error("邮件模板为空");
            return false;
        }

        // 替换模板中的占位符
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
     * 批量发送邮件
     * 
     * @param toList 收件人邮箱列表
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 成功发送的数量
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
     * 检查邮件服务是否可用
     * 
     * @return true表示可用，false表示不可用
     */
    public boolean isAvailable() {
        return enabled && mailSender != null && StringUtils.hasText(fromEmail);
    }
}

