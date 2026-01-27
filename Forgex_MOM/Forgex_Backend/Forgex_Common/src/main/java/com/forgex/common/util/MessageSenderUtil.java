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
package com.forgex.common.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.util.mq.MqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息发送工具类
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
@RequiredArgsConstructor
public class MessageSenderUtil {
    
    private final ObjectMapper objectMapper;
    private final MqSender mqSender;
    
    // 占位符正则表达式：${变量名}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * 消息发送参数
     */
    public static class MessageSendParam {
        /** 模板编码 */
        private String templateCode;
        
        /** 数据Map（用于填充占位符，可为空） */
        private Map<String, Object> dataMap;
        
        /** 发送人名称（可为空，默认为当前用户或系统） */
        private String senderName;
        
        /** 业务类型（可为空） */
        private String bizType;
        
        public MessageSendParam(String templateCode) {
            this.templateCode = templateCode;
        }
        
        public MessageSendParam(String templateCode, Map<String, Object> dataMap) {
            this.templateCode = templateCode;
            this.dataMap = dataMap;
        }
        
        public MessageSendParam(String templateCode, Map<String, Object> dataMap, String senderName) {
            this.templateCode = templateCode;
            this.dataMap = dataMap;
            this.senderName = senderName;
        }
        
        public MessageSendParam(String templateCode, Map<String, Object> dataMap, String senderName, String bizType) {
            this.templateCode = templateCode;
            this.dataMap = dataMap;
            this.senderName = senderName;
            this.bizType = bizType;
        }
        
        // Getters and Setters
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
        public Map<String, Object> getDataMap() { return dataMap; }
        public void setDataMap(Map<String, Object> dataMap) { this.dataMap = dataMap; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public String getBizType() { return bizType; }
        public void setBizType(String bizType) { this.bizType = bizType; }
    }
    
    /**
     * 发送消息（同步）
     * 
     * @param param 消息发送参数
     * @return 发送成功的消息数量
     */
    public int sendMessage(MessageSendParam param) {
        try {
            return doSendMessage(param);
        } catch (Exception e) {
            log.error("发送消息失败: templateCode={}, error={}", param.getTemplateCode(), e.getMessage(), e);
            throw new BusinessException("发送消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送消息（异步，通过MQ）
     * 
     * @param param 消息发送参数
     */
    public void sendMessageAsync(MessageSendParam param) {
        try {
            // 将参数序列化为JSON
            String json = objectMapper.writeValueAsString(param);
            // 发送到MQ
            boolean success = mqSender.send("MESSAGE_SEND_TOPIC", "ASYNC_SEND", json);
            if (!success) {
                log.warn("消息发送到MQ失败，将同步发送: templateCode={}", param.getTemplateCode());
                sendMessage(param);
            }
        } catch (Exception e) {
            log.error("异步发送消息失败，将同步发送: templateCode={}, error={}", param.getTemplateCode(), e.getMessage());
            sendMessage(param);
        }
    }
    
    /**
     * 执行消息发送
     */
    private int doSendMessage(MessageSendParam param) {
        // 注意：这里需要注入相关的Mapper和Service，为了避免循环依赖，
        // 实际实现时应该通过ApplicationContext动态获取Bean
        // 这里提供核心逻辑框架
        
        log.info("开始发送消息: templateCode={}", param.getTemplateCode());
        
        // 1. 根据模板编码查询模板主表
        // SysMessageTemplate template = templateMapper.selectOne(...)
        // if (template == null || !template.getStatus()) {
        //     throw new BusinessException("消息模板不存在或已禁用");
        // }
        
        // 2. 查询接收人配置
        // List<SysMessageTemplateReceiver> receivers = receiverMapper.selectList(...)
        
        // 3. 解析接收人列表
        // Set<Long> receiverUserIds = parseReceiverUserIds(receivers);
        
        // 4. 查询模板内容配置
        // List<SysMessageTemplateContent> contents = contentMapper.selectList(...)
        
        // 5. 准备发送人信息
        String senderName = param.getSenderName();
        if (!StringUtils.hasText(senderName)) {
            Long userId = UserContext.get();
            if (userId != null) {
                // 查询用户信息
                // SysUser user = userMapper.selectById(userId);
                // senderName = user.getUsername() + "(" + user.getAccount() + ")";
                senderName = "当前用户(admin)"; // 临时占位
            } else {
                senderName = "系统(admin)";
            }
        }
        
        // 6. 遍历接收人和平台，发送消息
        int sentCount = 0;
        // for (Long receiverUserId : receiverUserIds) {
        //     for (SysMessageTemplateContent content : contents) {
        //         // 填充占位符
        //         String title = fillPlaceholder(content.getContentTitle(), param.getDataMap());
        //         String body = fillPlaceholder(content.getContentBody(), param.getDataMap());
        //         
        //         // 创建消息记录
        //         SysMessage message = new SysMessage();
        //         message.setTenantId(TenantContext.get());
        //         message.setSenderName(senderName);
        //         message.setReceiverUserId(receiverUserId);
        //         message.setTemplateCode(param.getTemplateCode());
        //         message.setMessageType(template.getMessageType());
        //         message.setPlatform(content.getPlatform());
        //         message.setTitle(title);
        //         message.setContent(body);
        //         message.setLinkUrl(content.getLinkUrl());
        //         message.setBizType(param.getBizType());
        //         message.setStatus(0); // 未读
        //         
        //         // 保存消息记录
        //         messageMapper.insert(message);
        //         sentCount++;
        //         
        //         // 如果是站内消息，通过SSE推送
        //         if ("INTERNAL".equals(content.getPlatform())) {
        //             sseEmitterService.sendToUser(receiverUserId, message);
        //         }
        //     }
        // }
        
        log.info("消息发送完成: templateCode={}, sentCount={}", param.getTemplateCode(), sentCount);
        return sentCount;
    }
    
    /**
     * 解析接收人用户ID列表
     * 
     * @param receivers 接收人配置列表
     * @return 用户ID集合
     */
    private Set<Long> parseReceiverUserIds(List<?> receivers) {
        Set<Long> userIds = new HashSet<>();
        
        // 遍历接收人配置
        // for (SysMessageTemplateReceiver receiver : receivers) {
        //     String receiverType = receiver.getReceiverType();
        //     List<Long> receiverIds = parseReceiverIds(receiver.getReceiverIds());
        //     
        //     switch (receiverType) {
        //         case "USER": // 指定人
        //             userIds.addAll(receiverIds);
        //             break;
        //         case "ROLE": // 角色
        //             // 查询角色下的用户
        //             List<SysUserRole> userRoles = userRoleMapper.selectList(
        //                 new LambdaQueryWrapper<SysUserRole>()
        //                     .in(SysUserRole::getRoleId, receiverIds)
        //             );
        //             userIds.addAll(userRoles.stream()
        //                 .map(SysUserRole::getUserId)
        //                 .collect(Collectors.toSet()));
        //             break;
        //         case "DEPT": // 部门
        //             // 查询部门下的用户
        //             List<SysUser> deptUsers = userMapper.selectList(
        //                 new LambdaQueryWrapper<SysUser>()
        //                     .in(SysUser::getDepartmentId, receiverIds)
        //             );
        //             userIds.addAll(deptUsers.stream()
        //                 .map(SysUser::getId)
        //                 .collect(Collectors.toSet()));
        //             break;
        //         case "POSITION": // 职位
        //             // 查询职位下的用户
        //             List<SysUser> posUsers = userMapper.selectList(
        //                 new LambdaQueryWrapper<SysUser>()
        //                     .in(SysUser::getPositionId, receiverIds)
        //             );
        //             userIds.addAll(posUsers.stream()
        //                 .map(SysUser::getId)
        //                 .collect(Collectors.toSet()));
        //             break;
        //     }
        // }
        
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
     * 
     * @param template 模板字符串（包含${变量名}格式的占位符）
     * @param dataMap 数据Map
     * @return 填充后的字符串
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
}

