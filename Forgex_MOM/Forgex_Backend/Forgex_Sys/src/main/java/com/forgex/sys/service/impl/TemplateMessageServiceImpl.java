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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板消息发送服务实现类。
 * <p>
 * 负责根据模板编码查询模板、解析接收人、填充占位符、落库站内消息并触发 SSE 推送。
 * 当前仅处理站内消息平台，同时支持同步调用和 MQ 消费两种发送入口。
 * </p>
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
     * 占位符匹配规则，格式为 ${变量名}。
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * 站内消息平台标识。
     */
    private static final String PLATFORM_INTERNAL = "INTERNAL";

    /**
     * 默认消息类型。
     */
    private static final String DEFAULT_MESSAGE_TYPE = "NOTICE";

    /**
     * 模板主表 Mapper。
     */
    private final SysMessageTemplateMapper templateMapper;

    /**
     * 模板内容 Mapper。
     */
    private final SysMessageTemplateContentMapper contentMapper;

    /**
     * 模板接收人 Mapper。
     */
    private final SysMessageTemplateReceiverMapper receiverMapper;

    /**
     * 消息 Mapper。
     */
    private final SysMessageMapper messageMapper;

    /**
     * SSE 推送服务。
     */
    private final SseEmitterService sseEmitterService;

    /**
     * JSON 序列化工具。
     */
    private final ObjectMapper objectMapper;

    /**
     * 使用模板发送消息，并显式指定接收人列表。
     *
     * @param templateCode 模板编码
     * @param receiverUserIds 接收人用户 ID 列表
     * @param dataMap 占位符数据
     * @param bizType 业务类型
     * @return 成功发送的消息数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sendByTemplate(String templateCode, List<Long> receiverUserIds, Map<String, Object> dataMap, String bizType) {
        if (!StringUtils.hasText(templateCode)) {
            throw new IllegalArgumentException("Invalid request parameter");
        }
        if (receiverUserIds == null || receiverUserIds.isEmpty()) {
            throw new IllegalArgumentException("Receiver list cannot be empty");
        }

        SysMessageTemplate template = findTemplateByCode(templateCode);
        if (template == null) {
            log.warn("模板不存在: templateCode={}", templateCode);
            return 0;
        }
        if (!Boolean.TRUE.equals(template.getStatus())) {
            log.warn("模板已禁用: templateCode={}", templateCode);
            return 0;
        }

        List<SysMessageTemplateContent> contents = findTemplateContents(template.getId());
        if (contents.isEmpty()) {
            log.warn("模板内容配置为空: templateCode={}", templateCode);
            return 0;
        }

        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            log.warn("Current tenantId is null, cannot send message");
            return 0;
        }

        Set<Long> distinctUserIds = new LinkedHashSet<>(receiverUserIds);
        int sentCount = 0;
        for (Long receiverUserId : distinctUserIds) {
            if (receiverUserId == null) {
                continue;
            }

            for (SysMessageTemplateContent content : contents) {
                if (!PLATFORM_INTERNAL.equals(content.getPlatform())) {
                    continue;
                }

                String title = fillPlaceholders(content.getContentTitle(), content.getContentTitleI18nJson(), dataMap);
                String body = fillPlaceholders(content.getContentBody(), content.getContentBodyI18nJson(), dataMap);
                SysMessage message = createMessage(tenantId, receiverUserId, template, content, title, body, bizType);

                messageMapper.insert(message);
                pushMessage(tenantId, receiverUserId, message);
                sentCount++;
            }
        }

        log.info("模板消息发送完成: templateCode={}, receiverCount={}, sentCount={}",
                templateCode, distinctUserIds.size(), sentCount);
        return sentCount;
    }

    /**
     * 使用模板发送消息，并根据模板配置自动解析接收人。
     *
     * @param templateCode 模板编码
     * @param dataMap 占位符数据
     * @return 成功发送的消息数量
     */
    @Override
    public int sendByTemplate(String templateCode, Map<String, Object> dataMap) {
        if (!StringUtils.hasText(templateCode)) {
            throw new IllegalArgumentException("Invalid request parameter");
        }

        SysMessageTemplate template = findTemplateByCode(templateCode);
        if (template == null) {
            log.warn("模板不存在: templateCode={}", templateCode);
            return 0;
        }

        List<SysMessageTemplateReceiver> receivers = findTemplateReceivers(template.getId());
        if (receivers.isEmpty()) {
            log.warn("模板接收人配置为空: templateCode={}", templateCode);
            return 0;
        }

        List<Long> receiverUserIds = resolveReceiverUserIds(receivers);
        if (receiverUserIds.isEmpty()) {
            log.warn("解析后的接收人列表为空: templateCode={}", templateCode);
            return 0;
        }

        return sendByTemplate(templateCode, receiverUserIds, dataMap, null);
    }

    /**
     * 使用模板发送消息给单个用户。
     *
     * @param templateCode 模板编码
     * @param receiverUserId 接收人用户 ID
     * @param dataMap 占位符数据
     * @param bizType 业务类型
     * @return 成功发送后的消息 ID
     */
    @Override
    public Long sendToUser(String templateCode, Long receiverUserId, Map<String, Object> dataMap, String bizType) {
        if (!StringUtils.hasText(templateCode)) {
            throw new IllegalArgumentException("Invalid request parameter");
        }
        if (receiverUserId == null) {
            throw new IllegalArgumentException("Invalid request parameter");
        }

        SysMessageTemplate template = findTemplateByCode(templateCode);
        if (template == null || !Boolean.TRUE.equals(template.getStatus())) {
            log.warn("模板不存在或已禁用: templateCode={}", templateCode);
            return null;
        }

        List<SysMessageTemplateContent> contents = findTemplateContents(template.getId());
        if (contents.isEmpty()) {
            log.warn("模板内容配置为空: templateCode={}", templateCode);
            return null;
        }

        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            log.warn("Current tenantId is null, cannot send message");
            return null;
        }

        SysMessageTemplateContent internalContent = contents.stream()
                .filter(content -> PLATFORM_INTERNAL.equals(content.getPlatform()))
                .findFirst()
                .orElse(null);
        if (internalContent == null) {
            log.warn("模板未配置站内消息内容: templateCode={}", templateCode);
            return null;
        }

        String title = fillPlaceholders(internalContent.getContentTitle(), internalContent.getContentTitleI18nJson(), dataMap);
        String body = fillPlaceholders(internalContent.getContentBody(), internalContent.getContentBodyI18nJson(), dataMap);
        SysMessage message = createMessage(tenantId, receiverUserId, template, internalContent, title, body, bizType);

        messageMapper.insert(message);
        pushMessage(tenantId, receiverUserId, message);

        log.info("模板消息发送成功: templateCode={}, receiverUserId={}, messageId={}",
                templateCode, receiverUserId, message.getId());
        return message.getId();
    }

    /**
     * 检查模板是否存在且已启用。
     *
     * @param templateCode 模板编码
     * @return 是否可用
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
     * @return 模板实体，不存在时返回 null
     */
    private SysMessageTemplate findTemplateByCode(String templateCode) {
        return templateMapper.selectOne(new LambdaQueryWrapper<SysMessageTemplate>()
                .eq(SysMessageTemplate::getTemplateCode, templateCode)
                .eq(SysMessageTemplate::getDeleted, false)
                .last("LIMIT 1"));
    }

    /**
     * 查询模板内容配置。
     *
     * @param templateId 模板主键
     * @return 内容配置列表
     */
    private List<SysMessageTemplateContent> findTemplateContents(Long templateId) {
        return contentMapper.selectList(new LambdaQueryWrapper<SysMessageTemplateContent>()
                .eq(SysMessageTemplateContent::getTemplateId, templateId)
                .eq(SysMessageTemplateContent::getDeleted, false));
    }

    /**
     * 查询模板接收人配置。
     *
     * @param templateId 模板主键
     * @return 接收人配置列表
     */
    private List<SysMessageTemplateReceiver> findTemplateReceivers(Long templateId) {
        return receiverMapper.selectList(new LambdaQueryWrapper<SysMessageTemplateReceiver>()
                .eq(SysMessageTemplateReceiver::getTemplateId, templateId)
                .eq(SysMessageTemplateReceiver::getDeleted, false));
    }

    /**
     * 解析模板接收人配置，转换为用户 ID 列表。
     * <p>
     * 当前仅真正支持 {@code USER} 类型。
     * {@code ROLE}、{@code DEPT}、{@code POSITION} 仍保留待实现提示。
     * {@code CUSTOM} 表示由调用方在运行时动态传入，这里不读取模板中的 receiverIds。
     * </p>
     *
     * @param receivers 接收人配置列表
     * @return 解析出的用户 ID 列表
     */
    private List<Long> resolveReceiverUserIds(List<SysMessageTemplateReceiver> receivers) {
        Set<Long> userIds = new LinkedHashSet<>();

        for (SysMessageTemplateReceiver receiver : receivers) {
            String receiverType = receiver.getReceiverType();
            List<Long> ids = parseReceiverIds(receiver.getReceiverIds());

            if ("USER".equals(receiverType)) {
                userIds.addAll(ids);
            } else if ("ROLE".equals(receiverType)) {
                log.warn("ROLE 类型接收人解析暂未实现: receiverIds={}", ids);
            } else if ("DEPT".equals(receiverType)) {
                log.warn("DEPT 类型接收人解析暂未实现: receiverIds={}", ids);
            } else if ("POSITION".equals(receiverType)) {
                log.warn("POSITION 类型接收人解析暂未实现: receiverIds={}", ids);
            } else if ("CUSTOM".equals(receiverType)) {
                log.debug("CUSTOM 类型接收人由调用方动态传入，跳过模板 receiverIds: templateReceiverId={}", receiver.getId());
            }
        }

        return new ArrayList<>(userIds);
    }

    /**
     * 解析接收人 ID 的 JSON 字符串。
     *
     * @param receiverIdsJson JSON 字符串，例如 {@code [1,2,3]}
     * @return 用户 ID 列表
     */
    private List<Long> parseReceiverIds(String receiverIdsJson) {
        if (!StringUtils.hasText(receiverIdsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(receiverIdsJson, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            log.error("解析接收人 ID 列表失败: {}", receiverIdsJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 填充模板中的占位符。
     * <p>
     * 优先使用模板字段本身的内容；当模板字段为空且存在多语言 JSON 时，
     * 再从多语言配置中选取回退内容。
     * </p>
     *
     * @param template 模板字符串
     * @param i18nJson 多语言 JSON
     * @param dataMap 占位符数据
     * @return 替换后的内容
     */
    private String fillPlaceholders(String template, String i18nJson, Map<String, Object> dataMap) {
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
     * 从多语言 JSON 中选择一个可用内容。
     * <p>
     * 优先返回 {@code zh-CN}，其次返回 {@code en-US}，
     * 最后返回第一个非空值。
     * </p>
     *
     * @param i18nJson 多语言 JSON
     * @return 解析出的文本
     */
    private String resolveI18nContent(String i18nJson) {
        if (!StringUtils.hasText(i18nJson)) {
            return null;
        }
        try {
            Map<String, String> i18nMap = objectMapper.readValue(i18nJson, new TypeReference<Map<String, String>>() {});
            if (i18nMap.containsKey("zh-CN") && StringUtils.hasText(i18nMap.get("zh-CN"))) {
                return i18nMap.get("zh-CN");
            }
            if (i18nMap.containsKey("en-US") && StringUtils.hasText(i18nMap.get("en-US"))) {
                return i18nMap.get("en-US");
            }
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
     * @param tenantId 租户 ID
     * @param receiverUserId 接收人用户 ID
     * @param template 模板实体
     * @param content 模板内容实体
     * @param title 消息标题
     * @param body 消息正文
     * @param bizType 业务类型
     * @return 待保存的消息实体
     */
    private SysMessage createMessage(Long tenantId, Long receiverUserId, SysMessageTemplate template,
                                     SysMessageTemplateContent content, String title, String body, String bizType) {
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
     * <p>
     * 保留当前逻辑：有发送人用户 ID 时返回英文占位名称，
     * 否则回退为系统发送。
     * </p>
     *
     * @param senderUserId 发送人用户 ID
     * @return 发送人名称
     */
    private String resolveSenderName(Long senderUserId) {
        if (senderUserId != null) {
            return "User(" + senderUserId + ")";
        }
        return "System";
    }

    /**
     * 解析默认业务类型。
     * <p>
     * 当调用方未显式传入 {@code bizType} 时，优先取模板配置的业务类型；
     * 若模板未配置，则回退为模板编码。
     * </p>
     *
     * @param template 模板实体
     * @return 默认业务类型
     */
    private String resolveBizType(SysMessageTemplate template) {
        if (template == null) {
            return null;
        }
        if (StringUtils.hasText(template.getBizType())) {
            return template.getBizType().trim();
        }
        return template.getTemplateCode();
    }

    /**
     * 通过 SSE 推送消息。
     *
     * @param tenantId 租户 ID
     * @param receiverUserId 接收人用户 ID
     * @param message 消息实体
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
     * 将消息实体转换为前端推送使用的 VO。
     *
     * @param msg 消息实体
     * @param receiverTenantId 接收租户 ID
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

    /**
     * 处理来自 MQ 的模板消息发送请求。
     * <p>
     * 该方法会在消费时恢复租户与用户上下文，然后按照模板消息的标准流程完成
     * 模板校验、接收人解析、消息入库与 SSE 推送。
     * </p>
     *
     * @param request MQ 模板消息请求
     */
    @Transactional(rollbackFor = Exception.class)
    public void processTemplateMessageFromMq(com.forgex.common.mq.message.TemplateMessageRequest request) {
        if (request == null || !StringUtils.hasText(request.getTemplateCode())) {
            log.warn("MQ 模板消息请求为空或模板编码为空: request={}", request);
            return;
        }
        if (request.getTenantId() == null) {
            log.warn("MQ 模板消息缺少租户ID: templateCode={}", request.getTemplateCode());
            return;
        }

        Long oldTenantId = TenantContext.get();
        Long oldUserId = UserContext.get();
        try {
            TenantContext.set(request.getTenantId());
            UserContext.set(request.getSenderUserId());

            SysMessageTemplate template = findTemplateByCode(request.getTemplateCode());
            if (template == null) {
                log.warn("模板不存在: templateCode={}", request.getTemplateCode());
                return;
            }
            if (!Boolean.TRUE.equals(template.getStatus())) {
                log.warn("模板已禁用: templateCode={}", request.getTemplateCode());
                return;
            }

            List<SysMessageTemplateContent> contents = findTemplateContents(template.getId());
            if (contents.isEmpty()) {
                log.warn("模板内容配置为空: templateCode={}", request.getTemplateCode());
                return;
            }

            List<Long> receiverUserIds = resolveReceivers(template, request.getReceiverUserIds());
            if (receiverUserIds.isEmpty()) {
                log.warn("MQ 模板消息解析出的接收人为空: templateCode={}", request.getTemplateCode());
                return;
            }

            int sentCount = 0;
            for (SysMessageTemplateContent content : contents) {
                if (!PLATFORM_INTERNAL.equals(content.getPlatform())) {
                    continue;
                }

                String title = fillPlaceholders(content.getContentTitle(), content.getContentTitleI18nJson(), request.getDataMap());
                String body = fillPlaceholders(content.getContentBody(), content.getContentBodyI18nJson(), request.getDataMap());

                for (Long receiverUserId : receiverUserIds) {
                    if (receiverUserId == null) {
                        continue;
                    }

                    SysMessage message = createMessage(request.getTenantId(), receiverUserId, template, content,
                            title, body, request.getBizType());
                    if (StringUtils.hasText(request.getSenderName())) {
                        message.setSenderName(request.getSenderName());
                    }

                    messageMapper.insert(message);
                    pushMessage(request.getTenantId(), receiverUserId, message);
                    sentCount++;
                }
            }

            log.info("MQ 模板消息处理完成: templateCode={}, tenantId={}, receiverCount={}, sentCount={}",
                    request.getTemplateCode(), request.getTenantId(), receiverUserIds.size(), sentCount);
        } catch (Exception e) {
            log.error("MQ 模板消息处理失败: templateCode={}, tenantId={}",
                    request.getTemplateCode(), request.getTenantId(), e);
            throw e;
        } finally {
            if (oldTenantId != null) {
                TenantContext.set(oldTenantId);
            } else {
                TenantContext.clear();
            }
            if (oldUserId != null) {
                UserContext.set(oldUserId);
            } else {
                UserContext.clear();
            }
        }
    }

    /**
     * 解析 MQ 场景下的接收人列表。
     * <p>
     * 解析顺序如下：
     * </p>
     * <p>
     * 1. 如果模板中存在 {@code CUSTOM} 类型接收人，且请求里传入了接收人列表，则优先使用请求值。
     * </p>
     * <p>
     * 2. 否则尝试使用模板中已配置好的接收人。
     * </p>
     * <p>
     * 3. 如果模板配置未解析出结果，但请求中仍传入了接收人列表，则将请求值作为兜底。
     * </p>
     *
     * @param template 模板实体
     * @param requestReceivers 请求中携带的接收人列表
     * @return 最终可用的接收人用户 ID 列表
     */
    private List<Long> resolveReceivers(SysMessageTemplate template, List<Long> requestReceivers) {
        List<SysMessageTemplateReceiver> receivers = findTemplateReceivers(template.getId());
        boolean hasCustomReceiver = receivers.stream()
                .anyMatch(receiver -> "CUSTOM".equalsIgnoreCase(receiver.getReceiverType()));

        if (hasCustomReceiver && requestReceivers != null && !requestReceivers.isEmpty()) {
            Set<Long> distinctUserIds = new LinkedHashSet<>();
            for (Long requestReceiver : requestReceivers) {
                if (requestReceiver != null) {
                    distinctUserIds.add(requestReceiver);
                }
            }
            return new ArrayList<>(distinctUserIds);
        }

        List<Long> configuredReceivers = resolveReceiverUserIds(receivers);
        if (!configuredReceivers.isEmpty()) {
            return configuredReceivers;
        }

        if (requestReceivers != null && !requestReceivers.isEmpty()) {
            Set<Long> distinctUserIds = new LinkedHashSet<>();
            for (Long requestReceiver : requestReceivers) {
                if (requestReceiver != null) {
                    distinctUserIds.add(requestReceiver);
                }
            }
            return new ArrayList<>(distinctUserIds);
        }

        return Collections.emptyList();
    }
}
