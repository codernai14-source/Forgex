package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.sys.domain.dto.SysMessageSendDTO;
import com.forgex.sys.domain.entity.SysMessage;
import com.forgex.sys.domain.entity.SysTenantMessageWhitelist;
import com.forgex.sys.domain.param.SysMessageParam;
import com.forgex.sys.domain.vo.SysMessageVO;
import com.forgex.sys.mapper.SysMessageMapper;
import com.forgex.sys.mapper.SysTenantMessageWhitelistMapper;
import com.forgex.sys.service.SseEmitterService;
import com.forgex.sys.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统消息服务实现类
 * <p>
 * 提供系统消息的发送、查询和标记已读功能，
 * 支持同租户站内消息和跨租户消息。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see SysMessageService
 * @see SysMessage
 * @see SysMessageVO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl implements SysMessageService {

    /**
     * 站内人工发送消息时的默认消息类型。
     *
     * @see com.forgex.sys.domain.entity.SysMessage#getMessageType()
     */
    private static final String DEFAULT_MESSAGE_TYPE = "NOTICE";
    private static final String CATEGORY_SYSTEM = "SYSTEM";
    private static final String CATEGORY_MESSAGE = "MESSAGE";

    /**
     * 站内消息默认渠道。
     *
     * @see com.forgex.sys.domain.entity.SysMessage#getPlatform()
     */
    private static final String DEFAULT_PLATFORM = "INTERNAL";

    private final SysMessageMapper messageMapper;
    private final SseEmitterService sseEmitterService;
    private final SysTenantMessageWhitelistMapper whitelistMapper;

    /**
     * 发送系统消息
     *
     * @param dto 消息发送参数
     * @return 消息ID，参数非法时返回 `null`
     */
    @Override
    public Long send(SysMessageSendDTO dto) {
        if (dto == null) {
            return null;
        }

        Long senderTenantId = TenantContext.get();
        Long senderUserId = UserContext.get();
        if (senderTenantId == null || senderUserId == null) {
            return null;
        }
        if (dto.getReceiverUserId() == null) {
            return null;
        }
        if (!StringUtils.hasText(dto.getTitle())) {
            return null;
        }

        Long receiverTenantId = dto.getReceiverTenantId();
        if (receiverTenantId == null) {
            receiverTenantId = senderTenantId;
        }

        String scope = StringUtils.hasText(dto.getScope()) ? dto.getScope().trim().toUpperCase() : null;
        if (!StringUtils.hasText(scope)) {
            scope = receiverTenantId.equals(senderTenantId) ? "INTERNAL" : "EXTERNAL";
        }

        if ("INTERNAL".equals(scope)) {
            receiverTenantId = senderTenantId;
        } else if ("EXTERNAL".equals(scope) && !receiverTenantId.equals(senderTenantId)) {
            if (!checkCrossTenantPermission(senderTenantId, receiverTenantId)) {
                log.warn("跨租户消息发送被拒绝: senderTenantId={}, receiverTenantId={}",
                        senderTenantId, receiverTenantId);
                throw new BusinessException("无权向该租户发送消息，请联系管理员配置租户消息白名单");
            }
        }

        SysMessage msg = new SysMessage();
        msg.setTenantId(receiverTenantId);
        msg.setSenderTenantId(senderTenantId);
        msg.setSenderUserId(senderUserId);
        // sender_name 字段非空，需要在插入前补齐发送人名称。
        msg.setSenderName(resolveSenderName(senderUserId));
        msg.setReceiverUserId(dto.getReceiverUserId());
        msg.setScope(scope);
        // message_type、platform 字段没有默认值时，使用系统默认值兜底。
        msg.setMessageType(resolveMessageType(dto.getMessageType()));
        msg.setCategory(resolveCategory(dto.getCategory(), CATEGORY_MESSAGE));
        msg.setPlatform(resolvePlatform(dto.getPlatform()));
        msg.setTitle(dto.getTitle());
        msg.setContent(dto.getContent());
        msg.setLinkUrl(dto.getLinkUrl());
        msg.setBizType(dto.getBizType());
        msg.setStatus(0);
        msg.setDeleted(false);

        Long originTenantId = TenantContext.get();
        try {
            TenantContext.set(receiverTenantId);
            messageMapper.insert(msg);
        } finally {
            TenantContext.set(originTenantId);
        }

        SysMessageVO push = toVO(msg, receiverTenantId);
        sseEmitterService.sendToUser(receiverTenantId, msg.getReceiverUserId(), "message", push);
        return msg.getId();
    }

    /**
     * 查询未读消息列表
     *
     * @param limit 最大返回数量
     * @return 未读消息列表
     */
    @Override
    public List<SysMessageVO> listUnread(Integer limit) {
        return listUnread(limit, null);
    }

    @Override
    public List<SysMessageVO> listUnread(Integer limit, String category) {
        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();
        if (tenantId == null || userId == null) {
            return List.of();
        }

        int l = limit == null || limit <= 0 ? 20 : Math.min(limit, 200);
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverUserId, userId)
                .eq(SysMessage::getStatus, 0)
                .eq(StringUtils.hasText(category), SysMessage::getCategory, normalizeCategory(category))
                .orderByDesc(SysMessage::getCreateTime)
                .last("limit " + l);
        List<SysMessage> list = messageMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        List<SysMessageVO> out = new ArrayList<>(list.size());
        for (SysMessage m : list) {
            out.add(toVO(m, tenantId));
        }
        return out;
    }

    /**
     * 获取未读消息数量
     *
     * @return 未读消息数量
     */
    @Override
    public Long getUnreadCount() {
        return getUnreadCount(null);
    }

    @Override
    public Long getUnreadCount(String category) {
        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();
        if (tenantId == null || userId == null) {
            return 0L;
        }

        return messageMapper.selectCount(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverUserId, userId)
                .eq(StringUtils.hasText(category), SysMessage::getCategory, normalizeCategory(category))
                .eq(SysMessage::getStatus, 0));
    }

    /**
     * 标记消息已读
     *
     * @param id 消息ID
     * @return 是否标记成功
     */
    @Override
    public boolean markRead(Long id) {
        if (id == null) {
            return false;
        }

        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();
        if (tenantId == null || userId == null) {
            return false;
        }

        int rows = messageMapper.update(
                null,
                new LambdaUpdateWrapper<SysMessage>()
                        .eq(SysMessage::getId, id)
                        .eq(SysMessage::getReceiverUserId, userId)
                        .eq(SysMessage::getStatus, 0)
                        .set(SysMessage::getStatus, 1)
                        .set(SysMessage::getReadTime, LocalDateTime.now())
        );
        return rows > 0;
    }

    /**
     * 标记所有消息已读
     *
     * @return 是否标记成功
     */
    @Override
    public boolean markAllRead() {
        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();
        if (tenantId == null || userId == null) {
            return false;
        }

        messageMapper.update(
                null,
                new LambdaUpdateWrapper<SysMessage>()
                        .eq(SysMessage::getReceiverUserId, userId)
                        .eq(SysMessage::getStatus, 0)
                        .set(SysMessage::getStatus, 1)
                        .set(SysMessage::getReadTime, LocalDateTime.now())
        );
        return true;
    }

    /**
     * 分页查询消息列表
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @Override
    public Page<SysMessageVO> page(SysMessageParam param) {
        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();
        if (tenantId == null || userId == null) {
            return new Page<>(param.getPageNum(), param.getPageSize(), 0);
        }

        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getReceiverUserId, userId)
                .eq(StringUtils.hasText(param.getMessageType()),
                        SysMessage::getMessageType, param.getMessageType())
                .eq(StringUtils.hasText(param.getPlatform()),
                        SysMessage::getPlatform, param.getPlatform())
                .eq(StringUtils.hasText(param.getCategory()),
                        SysMessage::getCategory, normalizeCategory(param.getCategory()))
                .eq(param.getStatus() != null,
                        SysMessage::getStatus, param.getStatus())
                .like(StringUtils.hasText(param.getTitle()),
                        SysMessage::getTitle, param.getTitle())
                .orderByDesc(SysMessage::getCreateTime);

        Page<SysMessage> page = new Page<>(param.getPageNum(), param.getPageSize());
        Page<SysMessage> result = messageMapper.selectPage(page, wrapper);

        Page<SysMessageVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SysMessageVO> voList = result.getRecords().stream()
                .map(m -> toVO(m, tenantId))
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 解析发送人展示名称，供写入 {@link SysMessage#setSenderName(String)}。
     *
     * @param senderUserId 发送方用户ID
     * @return 发送人名称
     */
    private String resolveSenderName(Long senderUserId) {
        String account = CurrentUserUtils.getAccount();
        if (StringUtils.hasText(account)) {
            return account.trim();
        }
        if (senderUserId != null) {
            return "用户(" + senderUserId + ")";
        }
        return "系统";
    }

    /**
     * 解析消息类型。
     *
     * @param raw 调用方传入值
     * @return 大写非空字符串
     */
    private String resolveMessageType(String raw) {
        if (StringUtils.hasText(raw)) {
            return raw.trim().toUpperCase();
        }
        return DEFAULT_MESSAGE_TYPE;
    }

    /**
     * 解析消息渠道。
     *
     * @param raw 调用方传入值
     * @return 大写非空字符串
     */
    private String resolvePlatform(String raw) {
        if (StringUtils.hasText(raw)) {
            return raw.trim().toUpperCase();
        }
        return DEFAULT_PLATFORM;
    }

    private String resolveCategory(String raw, String defaultCategory) {
        if (StringUtils.hasText(raw)) {
            return normalizeCategory(raw);
        }
        return defaultCategory;
    }

    private String normalizeCategory(String raw) {
        String value = StringUtils.hasText(raw) ? raw.trim().toUpperCase() : "";
        if (CATEGORY_SYSTEM.equals(value)) {
            return CATEGORY_SYSTEM;
        }
        return CATEGORY_MESSAGE;
    }

    /**
     * 将消息实体转换为VO。
     *
     * @param msg 消息实体
     * @param receiverTenantId 接收租户ID
     * @return 消息VO
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
        vo.setCategory(msg.getCategory());
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
     * 检查跨租户消息发送权限。
     *
     * @param senderTenantId 发送方租户ID
     * @param receiverTenantId 接收方租户ID
     * @return 是否允许发送
     */
    private boolean checkCrossTenantPermission(Long senderTenantId, Long receiverTenantId) {
        if (senderTenantId == null || receiverTenantId == null) {
            return false;
        }
        if (senderTenantId.equals(receiverTenantId)) {
            return true;
        }
        // 超级管理员租户使用 tenantId=0。
        if (senderTenantId == 0L) {
            return true;
        }

        try {
            Long count = whitelistMapper.selectCount(new LambdaQueryWrapper<SysTenantMessageWhitelist>()
                    .eq(SysTenantMessageWhitelist::getSenderTenantId, senderTenantId)
                    .eq(SysTenantMessageWhitelist::getReceiverTenantId, receiverTenantId)
                    .eq(SysTenantMessageWhitelist::getEnabled, true)
                    .eq(SysTenantMessageWhitelist::getDeleted, false));
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查跨租户消息权限失败: senderTenantId={}, receiverTenantId={}",
                    senderTenantId, receiverTenantId, e);
            return false;
        }
    }
}
