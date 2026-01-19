package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.sys.domain.dto.SysMessageSendDTO;
import com.forgex.sys.domain.entity.SysMessage;
import com.forgex.sys.domain.vo.SysMessageVO;
import com.forgex.sys.mapper.SysMessageMapper;
import com.forgex.sys.service.SseEmitterService;
import com.forgex.sys.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
 * </ul>
 * <p><strong>消息类型：</strong></p>
 * <ul>
 *   <li>{@code INTERNAL} - 内部消息，发送者和接收者在同一租户</li>
 *   <li>{@code EXTERNAL} - 外部消息，发送者和接收者在不同租户</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see SysMessageService
 * @see SysMessage
 * @see SysMessageVO
 */
@Service
@RequiredArgsConstructor
public class SysMessageServiceImpl implements SysMessageService {
    /**
     * 消息Mapper
     */
    private final SysMessageMapper messageMapper;

    /**
     * SSE推送服务
     */
    private final SseEmitterService sseEmitterService;

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
        }

        // 构建消息实体
        SysMessage msg = new SysMessage();
        msg.setTenantId(receiverTenantId);
        msg.setSenderTenantId(senderTenantId);
        msg.setSenderUserId(senderUserId);
        msg.setReceiverUserId(dto.getReceiverUserId());
        msg.setScope(scope);
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
     * 将消息实体转换为VO
     * <p>
     * 复制消息实体的所有字段到VO对象。
     * </p>
     * 
     * @param msg 消息实体
     * @param receiverTenantId 接收租户ID
     * @return 消息VO
     */
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
}

