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

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 短信发送服务（阿里云）
 * <p>提供短信发送功能，支持验证码短信和通知短信。</p>
 * 
 * <p>配置示例（application.yml）：</p>
 * <pre>
 * aliyun:
 *   sms:
 *     enabled: true
 *     access-key-id: your-access-key-id
 *     access-key-secret: your-access-key-secret
 *     sign-name: 您的签名
 *     template-code: SMS_123456789
 * </pre>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class SmsService {

    @Value("${aliyun.sms.enabled:false}")
    private boolean enabled;

    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name:}")
    private String signName;

    @Value("${aliyun.sms.endpoint:dysmsapi.aliyuncs.com}")
    private String endpoint;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建阿里云短信客户端
     * 
     * @return 短信客户端实例
     * @throws Exception 创建失败
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(endpoint);
        return new Client(config);
    }

    /**
     * 发送短信
     * 
     * @param phoneNumber 手机号码
     * @param templateCode 短信模板CODE
     * @param templateParam 模板参数（JSON格式）
     * @return true表示发送成功，false表示发送失败
     */
    public boolean sendSms(String phoneNumber, String templateCode, Map<String, Object> templateParam) {
        if (!enabled) {
            log.warn("短信服务未启用，跳过发送");
            return false;
        }

        if (!StringUtils.hasText(phoneNumber) || !StringUtils.hasText(templateCode)) {
            log.error("短信参数不完整：phoneNumber={}, templateCode={}", phoneNumber, templateCode);
            return false;
        }

        try {
            Client client = createClient();
            
            // 构建请求
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(signName)
                    .setTemplateCode(templateCode);
            
            // 设置模板参数
            if (templateParam != null && !templateParam.isEmpty()) {
                String paramJson = objectMapper.writeValueAsString(templateParam);
                request.setTemplateParam(paramJson);
            }
            
            // 发送短信
            SendSmsResponse response = client.sendSms(request);
            
            // 检查结果
            if ("OK".equals(response.getBody().getCode())) {
                log.info("短信发送成功：phoneNumber={}, templateCode={}", phoneNumber, templateCode);
                return true;
            } else {
                log.error("短信发送失败：phoneNumber={}, templateCode={}, code={}, message={}", 
                        phoneNumber, templateCode, response.getBody().getCode(), response.getBody().getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("短信发送异常：phoneNumber={}, templateCode={}, error={}", 
                    phoneNumber, templateCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送验证码短信
     * 
     * @param phoneNumber 手机号码
     * @param code 验证码
     * @param templateCode 短信模板CODE（可选，为空则使用默认配置）
     * @return true表示发送成功，false表示发送失败
     */
    public boolean sendVerifyCode(String phoneNumber, String code, String templateCode) {
        Map<String, Object> param = Map.of("code", code);
        
        // 如果未指定模板，使用配置的默认模板
        String template = StringUtils.hasText(templateCode) ? templateCode : 
                System.getProperty("aliyun.sms.template-code", "");
        
        if (!StringUtils.hasText(template)) {
            log.error("短信模板CODE未配置");
            return false;
        }
        
        return sendSms(phoneNumber, template, param);
    }

    /**
     * 发送验证码短信（使用默认模板）
     * 
     * @param phoneNumber 手机号码
     * @param code 验证码
     * @return true表示发送成功，false表示发送失败
     */
    public boolean sendVerifyCode(String phoneNumber, String code) {
        return sendVerifyCode(phoneNumber, code, null);
    }

    /**
     * 批量发送短信
     * 
     * @param phoneNumbers 手机号码列表（逗号分隔）
     * @param templateCode 短信模板CODE
     * @param templateParam 模板参数
     * @return true表示发送成功，false表示发送失败
     */
    public boolean sendBatchSms(String phoneNumbers, String templateCode, Map<String, Object> templateParam) {
        if (!enabled) {
            log.warn("短信服务未启用，跳过发送");
            return false;
        }

        if (!StringUtils.hasText(phoneNumbers) || !StringUtils.hasText(templateCode)) {
            log.error("短信参数不完整：phoneNumbers={}, templateCode={}", phoneNumbers, templateCode);
            return false;
        }

        try {
            Client client = createClient();
            
            // 构建请求
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumbers)
                    .setSignName(signName)
                    .setTemplateCode(templateCode);
            
            // 设置模板参数
            if (templateParam != null && !templateParam.isEmpty()) {
                String paramJson = objectMapper.writeValueAsString(templateParam);
                request.setTemplateParam(paramJson);
            }
            
            // 发送短信
            SendSmsResponse response = client.sendSms(request);
            
            // 检查结果
            if ("OK".equals(response.getBody().getCode())) {
                log.info("批量短信发送成功：phoneNumbers={}, templateCode={}", phoneNumbers, templateCode);
                return true;
            } else {
                log.error("批量短信发送失败：phoneNumbers={}, templateCode={}, code={}, message={}", 
                        phoneNumbers, templateCode, response.getBody().getCode(), response.getBody().getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("批量短信发送异常：phoneNumbers={}, templateCode={}, error={}", 
                    phoneNumbers, templateCode, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 检查短信服务是否可用
     * 
     * @return true表示可用，false表示不可用
     */
    public boolean isAvailable() {
        return enabled && StringUtils.hasText(accessKeyId) 
                && StringUtils.hasText(accessKeySecret) 
                && StringUtils.hasText(signName);
    }
}

