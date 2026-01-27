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
package com.forgex.common.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.util.mq.MqSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 消息发送服务
 * <p>
 * 提供基于消息模板的通用消息发送功能，支持：
 * 1. 根据模板编码自动查找模板配置
 * 2. 根据接收人配置自动解析接收人列表
 * 3. 支持占位符替换
 * 4. 支持多平台发送（站内、企业微信、短信、邮箱）
 * 5. 支持MQ异步发送
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class MessageSenderService {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MqSender mqSender;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    // 占位符正则表达式：${变量名}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * 发送消息（同步）
     * 
     * @param templateCode 模板编码
     * @return 发送成功的消息数量
     */
    public int sendMessage(String templateCode) {
        return sendMessage(templateCode, null, null, null);
    }
    
    /**
     * 发送消息（同步）
     * 
     * @param templateCode 模板编码
     * @param dataMap 数据Map（用于填充占位符）
     * @return 发送成功的消息数量
     */
    public int sendMessage(String templateCode, Map<String, Object> dataMap) {
        return sendMessage(templateCode, dataMap, null, null);
    }
    
    /**
     * 发送消息（同步）
     * 
     * @param templateCode 模板编码
     * @param dataMap 数据Map（用于填充占位符）
     * @param senderName 发送人名称（可为空，默认为当前用户或系统）
     * @param bizType 业务类型（可为空）
     * @return 发送成功的消息数量
     */
    public int sendMessage(String templateCode, Map<String, Object> dataMap, String senderName, String bizType) {
        try {
            return doSendMessage(templateCode, dataMap, senderName, bizType);
        } catch (Exception e) {
            log.error("发送消息失败: templateCode={}, error={}", templateCode, e.getMessage(), e);
            throw new BusinessException("发送消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送消息（异步，通过MQ）
     * 
     * @param templateCode 模板编码
     */
    public void sendMessageAsync(String templateCode) {
        sendMessageAsync(templateCode, null, null, null);
    }
    
    /**
     * 发送消息（异步，通过MQ）
     * 
     * @param templateCode 模板编码
     * @param dataMap 数据Map（用于填充占位符）
     */
    public void sendMessageAsync(String templateCode, Map<String, Object> dataMap) {
        sendMessageAsync(templateCode, dataMap, null, null);
    }
    
    /**
     * 发送消息（异步，通过MQ）
     * 
     * @param templateCode 模板编码
     * @param dataMap 数据Map（用于填充占位符）
     * @param senderName 发送人名称（可为空，默认为当前用户或系统）
     * @param bizType 业务类型（可为空）
     */
    public void sendMessageAsync(String templateCode, Map<String, Object> dataMap, String senderName, String bizType) {
        try {
            // 构建参数Map
            Map<String, Object> param = new HashMap<>();
            param.put("templateCode", templateCode);
            param.put("dataMap", dataMap);
            param.put("senderName", senderName);
            param.put("bizType", bizType);
            param.put("tenantId", TenantContext.get());
            param.put("userId", UserContext.get());
            
            // 将参数序列化为JSON
            String json = objectMapper.writeValueAsString(param);
            
            // 发送到MQ
            boolean success = mqSender.send("MESSAGE_SEND_TOPIC", "ASYNC_SEND", json);
            if (!success) {
                log.warn("消息发送到MQ失败，将同步发送: templateCode={}", templateCode);
                sendMessage(templateCode, dataMap, senderName, bizType);
            }
        } catch (Exception e) {
            log.error("异步发送消息失败，将同步发送: templateCode={}, error={}", templateCode, e.getMessage());
            sendMessage(templateCode, dataMap, senderName, bizType);
        }
    }
    
    /**
     * 执行消息发送
     */
    private int doSendMessage(String templateCode, Map<String, Object> dataMap, String senderName, String bizType) {
        log.info("开始发送消息: templateCode={}", templateCode);
        
        // 动态获取所需的Mapper和Service（避免循环依赖）
        Object templateMapper = getBean("sysMessageTemplateMapper");
        Object receiverMapper = getBean("sysMessageTemplateReceiverMapper");
        Object contentMapper = getBean("sysMessageTemplateContentMapper");
        Object messageMapper = getBean("sysMessageMapper");
        Object userMapper = getBean("sysUserMapper");
        Object userRoleMapper = getBean("sysUserRoleMapper");
        Object sseEmitterService = getBean("sseEmitterService");
        
        try {
            // 1. 根据模板编码查询模板主表
            LambdaQueryWrapper<?> templateWrapper = new LambdaQueryWrapper<>();
            templateWrapper.eq(true, "template_code", templateCode);
            templateWrapper.eq(true, "status", true);
            
            Object template = invokeMethod(templateMapper, "selectOne", templateWrapper);
            if (template == null) {
                throw new BusinessException("消息模板不存在或已禁用: " + templateCode);
            }
            
            Long templateId = (Long) getField(template, "id");
            String messageType = (String) getField(template, "messageType");
            
            // 2. 查询接收人配置
            LambdaQueryWrapper<?> receiverWrapper = new LambdaQueryWrapper<>();
            receiverWrapper.eq(true, "template_id", templateId);
            List<?> receivers = (List<?>) invokeMethod(receiverMapper, "selectList", receiverWrapper);
            
            // 3. 解析接收人列表
            Set<Long> receiverUserIds = parseReceiverUserIds(receivers, userMapper, userRoleMapper);
            if (receiverUserIds.isEmpty()) {
                log.warn("消息模板没有配置接收人: templateCode={}", templateCode);
                return 0;
            }
            
            // 4. 查询模板内容配置
            LambdaQueryWrapper<?> contentWrapper = new LambdaQueryWrapper<>();
            contentWrapper.eq(true, "template_id", templateId);
            List<?> contents = (List<?>) invokeMethod(contentMapper, "selectList", contentWrapper);
            
            if (contents == null || contents.isEmpty()) {
                log.warn("消息模板没有配置内容: templateCode={}", templateCode);
                return 0;
            }
            
            // 5. 准备发送人信息
            if (!StringUtils.hasText(senderName)) {
                Long userId = UserContext.get();
                if (userId != null) {
                    Object user = invokeMethod(userMapper, "selectById", userId);
                    if (user != null) {
                        String username = (String) getField(user, "username");
                        String account = (String) getField(user, "account");
                        senderName = username + "(" + account + ")";
                    } else {
                        senderName = "系统(admin)";
                    }
                } else {
                    senderName = "系统(admin)";
                }
            }
            
            // 6. 遍历接收人和平台，发送消息
            int sentCount = 0;
            Long tenantId = TenantContext.get();
            
            for (Long receiverUserId : receiverUserIds) {
                for (Object content : contents) {
                    String platform = (String) getField(content, "platform");
                    String contentTitle = (String) getField(content, "contentTitle");
                    String contentBody = (String) getField(content, "contentBody");
                    String linkUrl = (String) getField(content, "linkUrl");
                    
                    // 填充占位符
                    String title = fillPlaceholder(contentTitle, dataMap);
                    String body = fillPlaceholder(contentBody, dataMap);
                    
                    // 创建消息记录
                    Object message = createMessageEntity(
                        tenantId, senderName, receiverUserId, templateCode, 
                        messageType, platform, title, body, linkUrl, bizType
                    );
                    
                    // 保存消息记录
                    invokeMethod(messageMapper, "insert", message);
                    sentCount++;
                    
                    // 根据平台类型发送消息
                    if ("INTERNAL".equals(platform) && sseEmitterService != null) {
                        // 站内消息：通过SSE推送
                        try {
                            invokeMethod(sseEmitterService, "sendToUser", receiverUserId, message);
                        } catch (Exception e) {
                            log.warn("SSE推送失败: receiverUserId={}, error={}", receiverUserId, e.getMessage());
                        }
                    } else if ("EMAIL".equals(platform)) {
                        // 邮件：通过EmailService发送
                        sendEmail(receiverUserId, title, body, userMapper);
                    } else if ("SMS".equals(platform)) {
                        // 短信：通过SmsService发送
                        sendSms(receiverUserId, body, userMapper);
                    } else if ("WECHAT".equals(platform)) {
                        // 企业微信：预留接口
                        log.info("企业微信消息发送功能待实现: receiverUserId={}", receiverUserId);
                    }
                }
            }
            
            log.info("消息发送完成: templateCode={}, sentCount={}", templateCode, sentCount);
            return sentCount;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送消息异常: templateCode={}", templateCode, e);
            throw new BusinessException("发送消息异常: " + e.getMessage());
        }
    }
    
    /**
     * 发送邮件
     */
    private void sendEmail(Long receiverUserId, String subject, String content, Object userMapper) {
        try {
            // 获取EmailService
            Object emailService = getBean("emailService");
            if (emailService == null) {
                log.warn("EmailService未配置，跳过邮件发送");
                return;
            }
            
            // 查询用户邮箱
            Object user = invokeMethod(userMapper, "selectById", receiverUserId);
            if (user == null) {
                log.warn("用户不存在，无法发送邮件: userId={}", receiverUserId);
                return;
            }
            
            String email = (String) getField(user, "email");
            if (!StringUtils.hasText(email)) {
                log.warn("用户未配置邮箱，无法发送邮件: userId={}", receiverUserId);
                return;
            }
            
            // 发送HTML邮件
            invokeMethod(emailService, "sendHtmlEmail", email, subject, content);
            log.info("邮件发送成功: userId={}, email={}, subject={}", receiverUserId, email, subject);
        } catch (Exception e) {
            log.error("邮件发送失败: userId={}, error={}", receiverUserId, e.getMessage(), e);
        }
    }
    
    /**
     * 发送短信
     */
    private void sendSms(Long receiverUserId, String content, Object userMapper) {
        try {
            // 获取SmsService
            Object smsService = getBean("smsService");
            if (smsService == null) {
                log.warn("SmsService未配置，跳过短信发送");
                return;
            }
            
            // 查询用户手机号
            Object user = invokeMethod(userMapper, "selectById", receiverUserId);
            if (user == null) {
                log.warn("用户不存在，无法发送短信: userId={}", receiverUserId);
                return;
            }
            
            String phone = (String) getField(user, "phone");
            if (!StringUtils.hasText(phone)) {
                log.warn("用户未配置手机号，无法发送短信: userId={}", receiverUserId);
                return;
            }
            
            // 注意：短信需要使用模板CODE，这里简化处理
            // 实际使用时应该在消息模板中配置短信模板CODE
            log.info("短信发送（需配置模板）: userId={}, phone={}, content={}", receiverUserId, phone, content);
        } catch (Exception e) {
            log.error("短信发送失败: userId={}, error={}", receiverUserId, e.getMessage(), e);
        }
    }
    
    /**
     * 解析接收人用户ID列表
     */
    private Set<Long> parseReceiverUserIds(List<?> receivers, Object userMapper, Object userRoleMapper) {
        Set<Long> userIds = new HashSet<>();
        
        try {
            for (Object receiver : receivers) {
                String receiverType = (String) getField(receiver, "receiverType");
                String receiverIdsJson = (String) getField(receiver, "receiverIds");
                List<Long> receiverIds = parseReceiverIds(receiverIdsJson);
                
                switch (receiverType) {
                    case "USER": // 指定人
                        userIds.addAll(receiverIds);
                        break;
                    case "ROLE": // 角色
                        // 查询角色下的用户
                        LambdaQueryWrapper<?> roleWrapper = new LambdaQueryWrapper<>();
                        roleWrapper.in(true, "role_id", receiverIds);
                        List<?> userRoles = (List<?>) invokeMethod(userRoleMapper, "selectList", roleWrapper);
                        for (Object userRole : userRoles) {
                            Long userId = (Long) getField(userRole, "userId");
                            if (userId != null) {
                                userIds.add(userId);
                            }
                        }
                        break;
                    case "DEPT": // 部门
                        // 查询部门下的用户
                        LambdaQueryWrapper<?> deptWrapper = new LambdaQueryWrapper<>();
                        deptWrapper.in(true, "department_id", receiverIds);
                        List<?> deptUsers = (List<?>) invokeMethod(userMapper, "selectList", deptWrapper);
                        for (Object user : deptUsers) {
                            Long userId = (Long) getField(user, "id");
                            if (userId != null) {
                                userIds.add(userId);
                            }
                        }
                        break;
                    case "POSITION": // 职位
                        // 查询职位下的用户
                        LambdaQueryWrapper<?> posWrapper = new LambdaQueryWrapper<>();
                        posWrapper.in(true, "position_id", receiverIds);
                        List<?> posUsers = (List<?>) invokeMethod(userMapper, "selectList", posWrapper);
                        for (Object user : posUsers) {
                            Long userId = (Long) getField(user, "id");
                            if (userId != null) {
                                userIds.add(userId);
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            log.error("解析接收人列表失败", e);
        }
        
        return userIds;
    }
    
    /**
     * 解析接收人ID列表（JSON字符串转List）
     */
    private List<Long> parseReceiverIds(String receiverIdsJson) {
        try {
            return objectMapper.readValue(receiverIdsJson, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.error("解析接收人ID列表失败: {}", receiverIdsJson, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 填充占位符
     */
    private String fillPlaceholder(String template, Map<String, Object> dataMap) {
        if (!StringUtils.hasText(template)) {
            return template;
        }
        
        if (dataMap == null || dataMap.isEmpty()) {
            return template;
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = dataMap.get(key);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 创建消息实体
     */
    private Object createMessageEntity(Long tenantId, String senderName, Long receiverUserId,
                                      String templateCode, String messageType, String platform,
                                      String title, String content, String linkUrl, String bizType) {
        try {
            Class<?> clazz = Class.forName("com.forgex.sys.domain.entity.SysMessage");
            Object message = clazz.getDeclaredConstructor().newInstance();
            
            setField(message, "tenantId", tenantId);
            setField(message, "senderName", senderName);
            setField(message, "receiverUserId", receiverUserId);
            setField(message, "templateCode", templateCode);
            setField(message, "messageType", messageType);
            setField(message, "platform", platform);
            setField(message, "title", title);
            setField(message, "content", content);
            setField(message, "linkUrl", linkUrl);
            setField(message, "bizType", bizType);
            setField(message, "status", 0); // 未读
            
            return message;
        } catch (Exception e) {
            throw new RuntimeException("创建消息实体失败", e);
        }
    }
    
    /**
     * 获取Bean
     */
    private Object getBean(String beanName) {
        try {
            return applicationContext.getBean(beanName);
        } catch (Exception e) {
            log.warn("获取Bean失败: {}", beanName);
            return null;
        }
    }
    
    /**
     * 反射调用方法
     */
    private Object invokeMethod(Object obj, String methodName, Object... args) throws Exception {
        if (obj == null) {
            return null;
        }
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        return obj.getClass().getMethod(methodName, paramTypes).invoke(obj, args);
    }
    
    /**
     * 反射获取字段值
     */
    private Object getField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
    
    /**
     * 反射设置字段值
     */
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}

