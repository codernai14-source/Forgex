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
 * 提供系统消息的发送、查询和标记已读功能。
 * 支持内部消息（同一租户）和外部消息（不同租户）。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>发送系统消息，支持内部和外部消息</li>
 *   <li>通过SSE实时推送消息给接收用户</li>
 *   <li>查询未读消息列表</li>
 *   <li>标记消息为已读状态</li>
 *   <li>跨租户消息权限校验</li>
 * </ul>
 * <p><strong>消息类型：</strong></p>
 * <ul>
 *   <li>{@code INTERNAL} - 内部消息，发送者和接收者在同一租户</li>
 *   <li>{@code EXTERNAL} - 外部消息，发送者和接收者在不同租户（需要白名单授权）</li>
 * </ul>
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
     * 站内人工发送消息时，表字段 {@code message_type} 非空且无库默认值时的默认类型：通知。
     *
     * @see com.forgex.sys.domain.entity.SysMessage#getMessageType()
     */
    private static final String DEFAULT_MESSAGE_TYPE = "NOTICE";

    /**
     * 站内消息默认渠道：站内（与 {@code scope=INTERNAL} 语义一致，均为站内场景）。
     *
     * @see com.forgex.sys.domain.entity.SysMessage#getPlatform()
     */
    private static final String DEFAULT_PLATFORM = "INTERNAL";

    /**
     * 消息Mapper
     */
    private final SysMessageMapper messageMapper;

    /**
     * SSE推送服务
     */
    private final SseEmitterService sseEmitterService;
    
    /**
     * 租户消息白名单Mapper
     */
    private final SysTenantMessageWhitelistMapper whitelistMapper;

    /**
     * 发送系统消息
     * <p>
     * 创建系统消息并通过SSE实时推送给接收用户。
     * 支持内部消息（同一租户）和外部消息（不同租户）。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>参数校验：DTO、租户ID、用户ID、接收用户ID、标题不能为空</li>
     *   <li>确定接收租户ID：如果未指定，使用发送租户ID</li>
     *   <li>确定消息范围：如果未指定，根据租户关系自动判断</li>
     *   <li>如果是内部消息，强制使用发送租户ID作为接收租户ID</li>
     *   <li>构建消息实体，设置所有字段</li>
     *   <li>切换租户上下文，插入消息到数据库</li>
     *   <li>恢复原始租户上下文</li>
     *   <li>构建消息VO，通过SSE推送给接收用户</li>
     *   <li>返回消息ID</li>
     * </ol>
     * 
     * @param dto 消息发送DTO
     * @return 消息ID，参数无效或发送失败返回null
     */
    @Override
    public Long send(SysMessageSendDTO dto) {
        // 参数校验：DTO不能为空
        if (dto == null) {
            return null;
        }
        
        // 获取发送者租户ID
        Long senderTenantId = TenantContext.get();
        
        // 获取发送者用户ID
        Long senderUserId = UserContext.get();
        
        // 发送者信息为空，直接返回
        if (senderTenantId == null || senderUserId == null) {
            return null;
        }
        
        // 接收用户ID为空，直接返回
        if (dto.getReceiverUserId() == null) {
            return null;
        }
        
        // 标题为空，直接返回
        if (!StringUtils.hasText(dto.getTitle())) {
            return null;
        }

        // 获取接收租户ID
        Long receiverTenantId = dto.getReceiverTenantId();
        
        // 接收租户ID未指定，使用发送租户ID
        if (receiverTenantId == null) {
            receiverTenantId = senderTenantId;
        }

        // 确定消息范围
        String scope = StringUtils.hasText(dto.getScope()) ? dto.getScope().trim().toUpperCase() : null;
        
        // 消息范围未指定，根据租户关系自动判断
        if (!StringUtils.hasText(scope)) {
            scope = receiverTenantId.equals(senderTenantId) ? "INTERNAL" : "EXTERNAL";
        }
        
        // 如果是内部消息，强制使用发送租户ID作为接收租户ID
        if ("INTERNAL".equals(scope)) {
            receiverTenantId = senderTenantId;
        } else if ("EXTERNAL".equals(scope)) {
            // 外部消息需要校验跨租户权限
            if (!receiverTenantId.equals(senderTenantId)) {
                // 检查是否有跨租户消息发送权限
                if (!checkCrossTenantPermission(senderTenantId, receiverTenantId)) {
                    log.warn("跨租户消息发送被拒绝: senderTenantId={}, receiverTenantId={}", 
                            senderTenantId, receiverTenantId);
                    throw new BusinessException("无权向该租户发送消息，请联系管理员配置租户消息白名单");
                }
            }
        }

        // 构建消息实体
        SysMessage msg = new SysMessage();
        msg.setTenantId(receiverTenantId);
        msg.setSenderTenantId(senderTenantId);
        msg.setSenderUserId(senderUserId);
        // sender_name 表字段非空且无默认值，插入时必须赋值（见实体 SysMessage#senderName 说明）
        msg.setSenderName(resolveSenderName(senderUserId));
        msg.setReceiverUserId(dto.getReceiverUserId());
        msg.setScope(scope);
        // message_type、platform 表字段非空且无默认值，须显式写入；未传时使用站内通知默认值
        msg.setMessageType(resolveMessageType(dto.getMessageType()));
        msg.setPlatform(resolvePlatform(dto.getPlatform()));
        msg.setTitle(dto.getTitle());
        msg.setContent(dto.getContent());
        msg.setLinkUrl(dto.getLinkUrl());
        msg.setBizType(dto.getBizType());
        msg.setStatus(0);
        msg.setDeleted(false);

        // 保存原始租户ID
        Long originTenantId = TenantContext.get();
        
        try {
            // 切换租户上下文为接收租户
            TenantContext.set(receiverTenantId);
            
            // 插入消息到数据库
            messageMapper.insert(msg);
        } finally {
            // 恢复原始租户上下文
            TenantContext.set(originTenantId);
        }

        // 构建消息VO
        SysMessageVO push = toVO(msg, receiverTenantId);
        
        // 通过SSE推送给接收用户
        sseEmitterService.sendToUser(receiverTenantId, msg.getReceiverUserId(), "message", push);

        // 返回消息ID
        return msg.getId();
    }

    /**
     * 查询未读消息列表
     * <p>
     * 查询当前用户的未读消息，按创建时间倒序排列。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>获取当前租户ID和用户ID</li>
     *   <li>参数无效时返回空列表</li>
     *   <li>限制查询数量：默认20，最大200</li>
     *   <li>查询未读消息（status=0），按创建时间倒序</li>
     *   <li>将消息实体转换为VO</li>
     *   <li>返回VO列表</li>
     * </ol>
     * 
     * @param limit 最大返回数量，null或<=0时使用默认值20
     * @return 未读消息列表
     */
    @Override
    public List<SysMessageVO> listUnread(Integer limit) {
        // 获取当前租户ID
        Long tenantId = TenantContext.get();
        
        // 获取当前用户ID
        Long userId = UserContext.get();
        
        // 参数无效，返回空列表
        if (tenantId == null || userId == null) {
            return List.of();
        }
        
        // 限制查询数量：默认20，最大200
        int l = limit == null || limit <= 0 ? 20 : Math.min(limit, 200);
        
        // 查询未读消息（status=0），按创建时间倒序
        List<SysMessage> list = messageMapper.selectList(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverUserId, userId)
                .eq(SysMessage::getStatus, 0)
                .orderByDesc(SysMessage::getCreateTime)
                .last("limit " + l));
        
        // 查询结果为空，返回空列表
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        
        // 将消息实体转换为VO
        List<SysMessageVO> out = new ArrayList<>(list.size());
        for (SysMessage m : list) {
            out.add(toVO(m, tenantId));
        }
        
        // 返回VO列表
        return out;
    }

    /**
     * 获取未读消息数量
     * 
     * @return 未读消息数量
     */
    @Override
    public Long getUnreadCount() {
        // 获取当前租户ID
        Long tenantId = TenantContext.get();
        
        // 获取当前用户ID
        Long userId = UserContext.get();
        
        // 参数无效，返回0
        if (tenantId == null || userId == null) {
            return 0L;
        }
        
        // 查询未读消息数量
        return messageMapper.selectCount(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverUserId, userId)
                .eq(SysMessage::getStatus, 0));
    }

    /**
     * 标记消息已读
     * <p>
     * 将指定消息标记为已读状态，并记录阅读时间。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>参数校验：消息ID不能为空</li>
     *   <li>获取当前租户ID和用户ID</li>
     *   <li>参数无效时返回false</li>
     *   <li>更新消息状态为已读（status=1）</li>
     *   <li>设置阅读时间为当前时间</li>
     *   <li>返回更新结果（影响行数>0表示成功）</li>
     * </ol>
     * 
     * @param id 消息ID
     * @return true表示标记成功，false表示标记失败
     */
    @Override
    public boolean markRead(Long id) {
        // 参数校验：消息ID不能为空
        if (id == null) {
            return false;
        }
        
        // 获取当前租户ID
        Long tenantId = TenantContext.get();
        
        // 获取当前用户ID
        Long userId = UserContext.get();
        
        // 参数无效，返回false
        if (tenantId == null || userId == null) {
            return false;
        }
        
        // 更新消息状态为已读（status=1），设置阅读时间
        int rows = messageMapper.update(
                null,
                new LambdaUpdateWrapper<SysMessage>()
                        .eq(SysMessage::getId, id)
                        .eq(SysMessage::getReceiverUserId, userId)
                        .eq(SysMessage::getStatus, 0)
                        .set(SysMessage::getStatus, 1)
                        .set(SysMessage::getReadTime, LocalDateTime.now())
        );
        
        // 返回更新结果（影响行数>0表示成功）
        return rows > 0;
    }
    
    /**
     * 标记所有消息已读
     * 
     * @return true表示标记成功，false表示标记失败
     */
    @Override
    public boolean markAllRead() {
        // 获取当前租户ID
        Long tenantId = TenantContext.get();
        
        // 获取当前用户ID
        Long userId = UserContext.get();
        
        // 参数无效，返回false
        if (tenantId == null || userId == null) {
            return false;
        }
        
        // 更新所有未读消息为已读
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
        // 获取当前租户ID
        Long tenantId = TenantContext.get();
        
        // 获取当前用户ID
        Long userId = UserContext.get();
        
        // 参数无效，返回空分页
        if (tenantId == null || userId == null) {
            return new Page<>(param.getPageNum(), param.getPageSize(), 0);
        }
        
        // 构建查询条件
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getReceiverUserId, userId)
               .eq(StringUtils.hasText(param.getMessageType()), 
                   SysMessage::getMessageType, param.getMessageType())
               .eq(StringUtils.hasText(param.getPlatform()), 
                   SysMessage::getPlatform, param.getPlatform())
               .eq(param.getStatus() != null, 
                   SysMessage::getStatus, param.getStatus())
               .like(StringUtils.hasText(param.getTitle()), 
                     SysMessage::getTitle, param.getTitle())
               .orderByDesc(SysMessage::getCreateTime);
        
        // 分页查询
        Page<SysMessage> page = new Page<>(param.getPageNum(), param.getPageSize());
        Page<SysMessage> result = messageMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<SysMessageVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SysMessageVO> voList = result.getRecords().stream()
                .map(m -> toVO(m, tenantId))
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    /**
     * 将消息实体转换为VO
     * <p>
     * 复制消息实体的所有字段到VO对象。
     * </p>
     * 
     * @param msg 消息实体
     * @param receiverTenantId 接收租户ID
     * @return 消息VO
     */
    /**
     * 解析发送人展示名称，供写入 {@link SysMessage#setSenderName(String)}。
     * <p>
     * 与 {@link com.forgex.sys.domain.entity.SysMessage#getSenderName()} 约定一致：
     * 优先使用当前登录账号（Session {@code LOGIN_ACCOUNT}，见 {@link CurrentUserUtils#getAccount()}）；
     * 若不可用则使用 {@code 用户({userId})} 兜底；极端情况下回退为 {@code 系统}。
     * </p>
     *
     * @param senderUserId 发送方用户 ID，用于账号缺失时的展示文本
     * @return 非空字符串，可直接写入数据库 {@code sender_name} 列
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
     * 解析消息类型，写入 {@link SysMessage#setMessageType(String)}。
     *
     * @param raw 调用方传入值，允许为空
     * @return 大写非空串，默认 {@link #DEFAULT_MESSAGE_TYPE}
     */
    private String resolveMessageType(String raw) {
        if (StringUtils.hasText(raw)) {
            return raw.trim().toUpperCase();
        }
        return DEFAULT_MESSAGE_TYPE;
    }

    /**
     * 解析消息渠道，写入 {@link SysMessage#setPlatform(String)}。
     *
     * @param raw 调用方传入值，允许为空
     * @return 大写非空串，默认 {@link #DEFAULT_PLATFORM}
     */
    private String resolvePlatform(String raw) {
        if (StringUtils.hasText(raw)) {
            return raw.trim().toUpperCase();
        }
        return DEFAULT_PLATFORM;
    }

    private SysMessageVO toVO(SysMessage msg, Long receiverTenantId) {
        // 创建VO对象
        SysMessageVO vo = new SysMessageVO();
        
        // 复制所有字段
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
        
        // 返回VO对象
        return vo;
    }
    
    /**
     * 检查跨租户消息发送权限
     * <p>
     * 通过查询租户消息白名单表，判断发送方租户是否有权限向接收方租户发送消息。
     * </p>
     * <p><strong>权限规则：</strong></p>
     * <ul>
     *   <li>同一租户内部消息：无需校验，直接允许</li>
     *   <li>跨租户消息：必须在白名单中且状态为启用</li>
     *   <li>超级管理员租户（ID=1）：默认拥有向所有租户发送消息的权限</li>
     * </ul>
     * 
     * @param senderTenantId 发送方租户ID
     * @param receiverTenantId 接收方租户ID
     * @return true表示有权限，false表示无权限
     */
    private boolean checkCrossTenantPermission(Long senderTenantId, Long receiverTenantId) {
        // 参数校验
        if (senderTenantId == null || receiverTenantId == null) {
            return false;
        }
        
        // 同一租户，直接允许
        if (senderTenantId.equals(receiverTenantId)) {
            return true;
        }
        
        // 超级管理员租户（ID=1）默认拥有所有权限
        if (senderTenantId == 1L) {
            return true;
        }
        
        try {
            // 查询白名单配置
            Long count = whitelistMapper.selectCount(new LambdaQueryWrapper<SysTenantMessageWhitelist>()
                    .eq(SysTenantMessageWhitelist::getSenderTenantId, senderTenantId)
                    .eq(SysTenantMessageWhitelist::getReceiverTenantId, receiverTenantId)
                    .eq(SysTenantMessageWhitelist::getEnabled, true)
                    .eq(SysTenantMessageWhitelist::getDeleted, false));
            
            // 存在启用的白名单记录，则允许发送
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("检查跨租户消息权限失败: senderTenantId={}, receiverTenantId={}", 
                    senderTenantId, receiverTenantId, e);
            // 异常情况下，为了安全起见，拒绝发送
            return false;
        }
    }
}

