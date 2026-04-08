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
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.service.TemplateMessageService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.sys.domain.entity.SysMessage;
import com.forgex.sys.domain.entity.SysMessageTemplate;
import com.forgex.sys.domain.entity.SysMessageTemplateContent;
import com.forgex.sys.domain.entity.SysMessageTemplateReceiver;
import com.forgex.sys.domain.vo.SysMessageVO;
import com.forgex.sys.mapper.SysMessageMapper;
import com.forgex.sys.mapper.SysMessageTemplateContentMapper;
import com.forgex.sys.mapper.SysMessageTemplateMapper;
import com.forgex.sys.mapper.SysMessageTemplateReceiverMapper;
import com.forgex.sys.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板消息发送服务实现类。
 * <p>
 * 提供基于消息模板的通用消息发送功能，核心流程：
 * <ol>
 *   <li>根据 templateCode 查询模板主表</li>
 *   <li>查询模板内容配置（支持多平台）</li>
 *   <li>查询或解析接收人列表</li>
 *   <li>填充占位符</li>
 *   <li>保存消息并触发 SSE 推送</li>
 * </ol>
 * </p>
 * <p><strong>占位符格式：</strong> ${变量名}，例如 ${userName}、${taskName}</p>
 * <p><strong>多语言支持：</strong> 根据 Accept-Language 请求头或 LangContext 选择对应语言的内容</p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-06
 * @see TemplateMessageService
 * @see SysMessageTemplate
 * @see SysMessageTemplateContent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateMessageServiceImpl implements TemplateMessageService {

    /**
     * 占位符正则表达式：${变量名}
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * 站内消息平台标识
     */
    private static final String PLATFORM_INTERNAL = "INTERNAL";

    /**
     * 默认消息类型
     */
    private static final String DEFAULT_MESSAGE_TYPE = "NOTICE";

    /**
     * 模板主表 Mapper
     */
    private final SysMessageTemplateMapper templateMapper;

    /**
     * 模板内容 Mapper
     */
    private final SysMessageTemplateContentMapper contentMapper;

    /**
     * 模板接收人 Mapper
     */
    private final SysMessageTemplateReceiverMapper receiverMapper;

    /**
     * 消息 Mapper
     */
    private final SysMessageMapper messageMapper;

    /**
     * SSE 推送服务
     */
    private final SseEmitterService sseEmitterService;

    /**
     * JSON 序列化工具
     */
    private final ObjectMapper objectMapper;

    /**
     * 使用模板发送消息（显式指定接收人）。
     * <p>
     * 适用于审批通知等需要动态指定接收人的场景。
     * </p>
     *
     * @param templateCode    模板编码，不能为空
     * @param receiverUserIds 接收人用户ID列表，不能为空
     * @param dataMap         占位符数据Map，可为空
     * @param bizType         业务类型，可为空
     * @return 发送成功的消息数量，失败返回0
     * @throws IllegalArgumentException 当 templateCode 或 receiverUserIds 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sendByTemplate(String templateCode, List<Long> receiverUserIds, Map<String, Object> dataMap, String bizType) {
        // 参数校验
        if (!StringUtils.hasText(templateCode)) {
            throw new IllegalArgumentException("模板编码不能为空");
        }
        if (receiverUserIds == null || receiverUserIds.isEmpty()) {
            throw new IllegalArgumentException("接收人列表不能为空");
        }

        // 查询模板
        SysMessageTemplate template = findTemplateByCode(templateCode);
        if (template == null) {
            log.warn("模板不存在: templateCode={}", templateCode);
            return 0;
        }
        if (!Boolean.TRUE.equals(template.getStatus())) {
            log.warn("模板已禁用: templateCode={}", templateCode);
            return 0;
        }

        // 查询模板内容配置
        List<SysMessageTemplateContent> contents = findTemplateContents(template.getId());
        if (contents.isEmpty()) {
            log.warn("模板内容配置为空: templateCode={}", templateCode);
            return 0;
        }

        // 获取当前租户ID
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            log.warn("当前租户ID为空，无法发送消息");
            return 0;
        }

        // 去重接收人
        Set<Long> distinctUserIds = new LinkedHashSet<>(receiverUserIds);

        // 发送消息
        int sentCount = 0;
        for (Long receiverUserId : distinctUserIds) {
            if (receiverUserId == null) {
                continue;
            }

            for (SysMessageTemplateContent content : contents) {
                // 仅处理站内消息
                if (!PLATFORM_INTERNAL.equals(content.getPlatform())) {
                    continue;
                }

                // 填充占位符
                String title = fillPlaceholders(content.getContentTitle(), content.getContentTitleI18nJson(), dataMap);
                String body = fillPlaceholders(content.getContentBody(), content.getContentBodyI18nJson(), dataMap);

                // 创建消息
                SysMessage message = createMessage(tenantId, receiverUserId, template, content, title, body, bizType);

                // 保存消息
                messageMapper.insert(message);

                // SSE 推送
                pushMessage(tenantId, receiverUserId, message);

                sentCount++;
            }
        }

        log.info("模板消息发送完成: templateCode={}, receiverCount={}, sentCount={}",
                templateCode, distinctUserIds.size(), sentCount);
        return sentCount;
    }

    /**
     * 使用模板发送消息（根据模板配置自动解析接收人）。
     * <p>
     * 适用于定时通知等接收人固定的场景。
     * </p>
     *
     * @param templateCode 模板编码，不能为空
     * @param dataMap      占位符数据Map，可为空
     * @return 发送成功的消息数量，失败返回0
     */
    @Override
    public int sendByTemplate(String templateCode, Map<String, Object> dataMap) {
        // 参数校验
        if (!StringUtils.hasText(templateCode)) {
            throw new IllegalArgumentException("模板编码不能为空");
        }

        // 查询模板
        SysMessageTemplate template = findTemplateByCode(templateCode);
        if (template == null) {
            log.warn("模板不存在: templateCode={}", templateCode);
            return 0;
        }

        // 查询接收人配置
        List<SysMessageTemplateReceiver> receivers = findTemplateReceivers(template.getId());
        if (receivers.isEmpty()) {
            log.warn("模板接收人配置为空: templateCode={}", templateCode);
            return 0;
        }

        // 解析接收人ID列表
        List<Long> receiverUserIds = resolveReceiverUserIds(receivers);
        if (receiverUserIds.isEmpty()) {
            log.warn("解析后的接收人列表为空: templateCode={}", templateCode);
            return 0;
        }

        // 调用显式指定接收人的方法
        return sendByTemplate(templateCode, receiverUserIds, dataMap, null);
    }

    /**
     * 使用模板发送消息给单个用户。
     *
     * @param templateCode   模板编码，不能为空
     * @param receiverUserId 接收人用户ID，不能为空
     * @param dataMap        占位符数据Map，可为空
     * @param bizType        业务类型，可为空
     * @return 发送成功返回消息ID，失败返回null
     */
    @Override
    public Long sendToUser(String templateCode, Long receiverUserId, Map<String, Object> dataMap, String bizType) {
        // 参数校验
        if (!StringUtils.hasText(templateCode)) {
            throw new IllegalArgumentException("模板编码不能为空");
        }
        if (receiverUserId == null) {
            throw new IllegalArgumentException("接收人ID不能为空");
        }

        // 查询模板
        SysMessageTemplate template = findTemplateByCode(templateCode);
        if (template == null || !Boolean.TRUE.equals(template.getStatus())) {
            log.warn("模板不存在或已禁用: templateCode={}", templateCode);
            return null;
        }

        // 查询模板内容配置
        List<SysMessageTemplateContent> contents = findTemplateContents(template.getId());
        if (contents.isEmpty()) {
            log.warn("模板内容配置为空: templateCode={}", templateCode);
            return null;
        }

        // 获取当前租户ID
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            log.warn("当前租户ID为空，无法发送消息");
            return null;
        }

        // 查找站内消息内容
        SysMessageTemplateContent internalContent = contents.stream()
                .filter(c -> PLATFORM_INTERNAL.equals(c.getPlatform()))
                .findFirst()
                .orElse(null);

        if (internalContent == null) {
            log.warn("模板未配置站内消息内容: templateCode={}", templateCode);
            return null;
        }

        // 填充占位符
        String title = fillPlaceholders(internalContent.getContentTitle(), internalContent.getContentTitleI18nJson(), dataMap);
        String body = fillPlaceholders(internalContent.getContentBody(), internalContent.getContentBodyI18nJson(), dataMap);

        // 创建消息
        SysMessage message = createMessage(tenantId, receiverUserId, template, internalContent, title, body, bizType);

        // 保存消息
        messageMapper.insert(message);

        // SSE 推送
        pushMessage(tenantId, receiverUserId, message);

        log.info("模板消息发送成功: templateCode={}, receiverUserId={}, messageId={}",
                templateCode, receiverUserId, message.getId());

        return message.getId();
    }

    /**
     * 检查模板是否存在且启用。
     *
     * @param templateCode 模板编码
     * @return true表示模板存在且启用，false表示不存在或已禁用
     */
    @Override
    public boolean isTemplateAvailable(String templateCode) {
        if (!StringUtils.hasText(templateCode)) {
            return false;
        }
        SysMessageTemplate template = findTemplateByCode(templateCode);
        return template != null && Boolean.TRUE.equals(template.getStatus());
    }

    /**
     * 根据模板编码查询模板主表。
     *
     * @param templateCode 模板编码
     * @return 模板实体，不存在返回null
     */
    private SysMessageTemplate findTemplateByCode(String templateCode) {
        return templateMapper.selectOne(new LambdaQueryWrapper<SysMessageTemplate>()
                .eq(SysMessageTemplate::getTemplateCode, templateCode)
                .eq(SysMessageTemplate::getDeleted, false)
                .last("LIMIT 1"));
    }

    /**
     * 查询模板内容配置列表。
     *
     * @param templateId 模板主表ID
     * @return 内容配置列表
     */
    private List<SysMessageTemplateContent> findTemplateContents(Long templateId) {
        return contentMapper.selectList(new LambdaQueryWrapper<SysMessageTemplateContent>()
                .eq(SysMessageTemplateContent::getTemplateId, templateId)
                .eq(SysMessageTemplateContent::getDeleted, false));
    }

    /**
     * 查询模板接收人配置列表。
     *
     * @param templateId 模板主表ID
     * @return 接收人配置列表
     */
    private List<SysMessageTemplateReceiver> findTemplateReceivers(Long templateId) {
        return receiverMapper.selectList(new LambdaQueryWrapper<SysMessageTemplateReceiver>()
                .eq(SysMessageTemplateReceiver::getTemplateId, templateId)
                .eq(SysMessageTemplateReceiver::getDeleted, false));
    }

    /**
     * 解析接收人用户ID列表。
     * <p>
     * 根据 receiverType 解析：
     * <ul>
     *   <li>USER: 直接使用 receiverIds</li>
     *   <li>ROLE: 查询角色下的用户</li>
     *   <li>DEPT: 查询部门下的用户</li>
     *   <li>POSITION: 查询职位下的用户</li>
     * </ul>
     * </p>
     * <p>
     * TODO: 当前仅支持 USER 类型，其他类型需要注入相关 Mapper
     * </p>
     *
     * @param receivers 接收人配置列表
     * @return 用户ID列表
     */
    private List<Long> resolveReceiverUserIds(List<SysMessageTemplateReceiver> receivers) {
        Set<Long> userIds = new LinkedHashSet<>();

        for (SysMessageTemplateReceiver receiver : receivers) {
            String receiverType = receiver.getReceiverType();
            List<Long> ids = parseReceiverIds(receiver.getReceiverIds());

            if ("USER".equals(receiverType)) {
                // 指定人类型，直接添加
                userIds.addAll(ids);
            } else if ("ROLE".equals(receiverType)) {
                // TODO: 根据角色ID查询用户ID列表
                // 需要注入 SysUserRoleMapper
                log.warn("ROLE 类型接收人解析暂未实现: receiverIds={}", ids);
            } else if ("DEPT".equals(receiverType)) {
                // TODO: 根据部门ID查询用户ID列表
                // 需要注入 SysUserMapper
                log.warn("DEPT 类型接收人解析暂未实现: receiverIds={}", ids);
            } else if ("POSITION".equals(receiverType)) {
                // TODO: 根据职位ID查询用户ID列表
                // 需要注入 SysUserMapper
                log.warn("POSITION 类型接收人解析暂未实现: receiverIds={}", ids);
            }
        }

        return new ArrayList<>(userIds);
    }

    /**
     * 解析接收人ID JSON字符串。
     *
     * @param receiverIdsJson JSON字符串，例如 "[1, 2, 3]"
     * @return ID列表
     */
    private List<Long> parseReceiverIds(String receiverIdsJson) {
        if (!StringUtils.hasText(receiverIdsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(receiverIdsJson, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.error("解析接收人ID列表失败: {}", receiverIdsJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 填充占位符。
     * <p>
     * 支持 ${变量名} 格式的占位符，从 dataMap 中获取值进行替换。
     * </p>
     *
     * @param template     模板字符串
     * @param i18nJson     多语言 JSON
     * @param dataMap      占位符数据
     * @return 填充后的字符串
     */
    private String fillPlaceholders(String template, String i18nJson, Map<String, Object> dataMap) {
        // 优先使用模板字符串，如果为空则从多语言 JSON 解析
        String content = template;
        if (!StringUtils.hasText(content) && StringUtils.hasText(i18nJson)) {
            content = resolveI18nContent(i18nJson);
        }

        if (!StringUtils.hasText(content)) {
            return "";
        }

        if (dataMap == null || dataMap.isEmpty()) {
            return content;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = dataMap.get(key);
            String replacement = value != null ? Matcher.quoteReplacement(String.valueOf(value)) : "";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 从多语言 JSON 中解析内容。
     * <p>
     * 优先使用 zh-CN，其次使用第一个非空值。
     * </p>
     *
     * @param i18nJson 多语言 JSON
     * @return 解析后的内容
     */
    private String resolveI18nContent(String i18nJson) {
        if (!StringUtils.hasText(i18nJson)) {
            return null;
        }
        try {
            Map<String, String> i18nMap = objectMapper.readValue(i18nJson, new TypeReference<Map<String, String>>() {});
            // 优先使用中文
            if (i18nMap.containsKey("zh-CN") && StringUtils.hasText(i18nMap.get("zh-CN"))) {
                return i18nMap.get("zh-CN");
            }
            // 其次使用英文
            if (i18nMap.containsKey("en-US") && StringUtils.hasText(i18nMap.get("en-US"))) {
                return i18nMap.get("en-US");
            }
            // 最后使用第一个非空值
            for (String value : i18nMap.values()) {
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        } catch (Exception e) {
            log.warn("解析多语言 JSON 失败: {}", i18nJson, e);
        }
        return null;
    }

    /**
     * 创建消息实体。
     *
     * @param tenantId      租户ID
     * @param receiverUserId 接收人ID
     * @param template      模板
     * @param content       内容配置
     * @param title         消息标题
     * @param body          消息内容
     * @param bizType       业务类型
     * @return 消息实体
     */
    private SysMessage createMessage(Long tenantId, Long receiverUserId,
                                      SysMessageTemplate template,
                                      SysMessageTemplateContent content,
                                      String title, String body, String bizType) {
        Long senderUserId = UserContext.get();

        SysMessage message = new SysMessage();
        message.setTenantId(tenantId);
        message.setSenderTenantId(tenantId);
        message.setSenderUserId(senderUserId);
        message.setSenderName(resolveSenderName(senderUserId));
        message.setReceiverUserId(receiverUserId);
        message.setScope("INTERNAL");
        message.setTemplateCode(template.getTemplateCode());
        message.setMessageType(template.getMessageType() != null ? template.getMessageType() : DEFAULT_MESSAGE_TYPE);
        message.setPlatform(content.getPlatform());
        message.setTitle(title);
        message.setContent(body);
        message.setLinkUrl(content.getLinkUrl());
        message.setBizType(StringUtils.hasText(bizType) ? bizType : resolveBizType(template));
        message.setStatus(0);
        message.setDeleted(false);

        return message;
    }

    /**
     * 解析发送人名称。
     *
     * @param senderUserId 发送人用户ID
     * @return 发送人名称
     */
    private String resolveSenderName(Long senderUserId) {
        if (senderUserId != null) {
            return "用户(" + senderUserId + ")";
        }
        return "系统";
    }

    /**
     * 解析业务类型。
     * <p>
     * 优先使用方法参数传入的 bizType，否则使用模板名称作为默认业务类型。
     * </p>
     *
     * @param template 模板
     * @return 业务类型
     */
    private String resolveBizType(SysMessageTemplate template) {
        if (template == null) {
            return null;
        }
        // 使用模板编码作为业务类型
        return template.getTemplateCode();
    }

    /**
     * SSE 推送消息。
     *
     * @param tenantId      租户ID
     * @param receiverUserId 接收人ID
     * @param message       消息实体
     */
    private void pushMessage(Long tenantId, Long receiverUserId, SysMessage message) {
        try {
            SysMessageVO vo = toVO(message, tenantId);
            sseEmitterService.sendToUser(tenantId, receiverUserId, "message", vo);
        } catch (Exception e) {
            log.error("SSE 推送消息失败: tenantId={}, receiverUserId={}", tenantId, receiverUserId, e);
        }
    }

    /**
     * 将消息实体转换为 VO。
     *
     * @param msg               消息实体
     * @param receiverTenantId  接收租户ID
     * @return 消息 VO
     */
    private SysMessageVO toVO(SysMessage msg, Long receiverTenantId) {
        SysMessageVO vo = new SysMessageVO();
        vo.setId(msg.getId());
        vo.setSenderTenantId(msg.getSenderTenantId());
        vo.setSenderUserId(msg.getSenderUserId());
        vo.setReceiverTenantId(receiverTenantId);
        vo.setReceiverUserId(msg.getReceiverUserId());
        vo.setScope(msg.getScope());
        vo.setMessageType(msg.getMessageType());
        vo.setType(msg.getMessageType());
        vo.setPlatform(msg.getPlatform());
        vo.setSenderName(msg.getSenderName());
        vo.setTitle(msg.getTitle());
        vo.setContent(msg.getContent());
        vo.setLinkUrl(msg.getLinkUrl());
        vo.setBizType(msg.getBizType());
        vo.setStatus(msg.getStatus());
        vo.setCreateTime(msg.getCreateTime());
        vo.setReadTime(msg.getReadTime());
        return vo;
    }
}