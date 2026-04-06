package com.forgex.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.service.TemplateMessageService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysMessageReadDTO;
import com.forgex.sys.domain.dto.SysMessageSendDTO;
import com.forgex.sys.domain.dto.TemplateMessageSendDTO;
import com.forgex.sys.domain.param.SysMessageParam;
import com.forgex.sys.domain.vo.SysMessageVO;
import com.forgex.sys.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 系统消息控制器
 * <p>提供系统消息的发送、查询和标记已读功能。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/message")
@RequiredArgsConstructor
public class SysMessageController {

    /**
     * 系统消息服务
     */
    private final SysMessageService messageService;

    /**
     * 模板消息服务
     */
    private final TemplateMessageService templateMessageService;

    /**
     * 发送系统消息
     * <p>向指定用户发送系统消息。</p>
     * 
     * @param dto 消息发送参数
     * @return 消息ID，失败返回错误信息
     */
    @PostMapping("/send")
    public R<Long> send(@RequestBody SysMessageSendDTO dto) {
        // 检查登录状态
        if (TenantContext.get() == null || UserContext.get() == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        
        // 发送消息
        Long id = messageService.send(dto);
        return id == null ? R.fail(CommonPrompt.OPERATION_FAILED) : R.ok(id);
    }

    /**
     * 查询未读消息列表
     * <p>查询当前用户的未读消息列表。</p>
     * 
     * @param limit 最大返回数量
     * @return 未读消息列表，未登录返回错误信息
     */
    @GetMapping("/unread")
    public R<List<SysMessageVO>> unread(@RequestParam(value = "limit", required = false) Integer limit) {
        // 检查登录状态
        if (TenantContext.get() == null || UserContext.get() == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        
        return R.ok(messageService.listUnread(limit));
    }

    /**
     * 获取未读消息数量
     * <p>获取当前用户的未读消息数量。</p>
     * 
     * @return 未读消息数量
     */
    @PostMapping("/unread-count")
    public R<Long> unreadCount() {
        // 检查登录状态
        if (TenantContext.get() == null || UserContext.get() == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        
        return R.ok(messageService.getUnreadCount());
    }

    /**
     * 标记消息已读
     * <p>将指定消息标记为已读状态。</p>
     * 
     * @param dto 消息已读参数
     * @return 标记结果，未登录返回错误信息
     */
    @PostMapping("/read")
    public R<Boolean> read(@RequestBody SysMessageReadDTO dto) {
        // 检查登录状态
        if (TenantContext.get() == null || UserContext.get() == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        
        // 获取消息ID
        Long id = dto == null ? null : dto.getId();
        return R.ok(messageService.markRead(id));
    }

    /**
     * 标记所有消息已读
     * <p>将当前用户的所有未读消息标记为已读状态。</p>
     * 
     * @return 标记结果
     */
    @PostMapping("/read-all")
    public R<Boolean> readAll() {
        // 检查登录状态
        if (TenantContext.get() == null || UserContext.get() == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        
        return R.ok(messageService.markAllRead());
    }

    /**
     * 分页查询消息列表
     * <p>分页查询当前用户的消息列表。</p>
     * 
     * @param param 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public R<Page<SysMessageVO>> page(@RequestBody SysMessageParam param) {
        // 检查登录状态
        if (TenantContext.get() == null || UserContext.get() == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }
        
        return R.ok(messageService.page(param));
    }

    /**
     * 使用模板发送消息。
     * <p>
     * 根据模板编码和占位符数据发送消息，支持批量接收人。
     * 适用于审批通知、系统提醒等场景。
     * </p>
     *
     * @param dto 模板消息发送参数
     * @return 发送成功的消息数量
     */
    @PostMapping("/send-by-template")
    public R<Integer> sendByTemplate(@RequestBody TemplateMessageSendDTO dto) {
        // 检查登录状态
        if (TenantContext.get() == null || UserContext.get() == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }

        // 参数校验
        if (dto == null || !StringUtils.hasText(dto.getTemplateCode())) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        // 解析接收人列表
        List<Long> receiverUserIds = dto.getReceiverUserIds();
        if (receiverUserIds == null || receiverUserIds.isEmpty()) {
            // 尝试使用单个接收人ID
            if (dto.getReceiverUserId() != null) {
                receiverUserIds = Collections.singletonList(dto.getReceiverUserId());
            } else {
                return R.fail(CommonPrompt.PARAM_EMPTY);
            }
        }

        // 发送消息
        int count = templateMessageService.sendByTemplate(
                dto.getTemplateCode(),
                receiverUserIds,
                dto.getDataMap(),
                dto.getBizType()
        );

        return R.ok(count);
    }

    /**
     * 使用模板发送消息给单个用户。
     * <p>
     * 简化的单用户发送接口，返回消息ID。
     * </p>
     *
     * @param dto 模板消息发送参数
     * @return 消息ID，失败返回null
     */
    @PostMapping("/send-to-user")
    public R<Long> sendToUser(@RequestBody TemplateMessageSendDTO dto) {
        // 检查登录状态
        if (TenantContext.get() == null || UserContext.get() == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }

        // 参数校验
        if (dto == null || !StringUtils.hasText(dto.getTemplateCode())) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        Long receiverUserId = dto.getReceiverUserId();
        if (receiverUserId == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        // 发送消息
        Long messageId = templateMessageService.sendToUser(
                dto.getTemplateCode(),
                receiverUserId,
                dto.getDataMap(),
                dto.getBizType()
        );

        return messageId != null ? R.ok(messageId) : R.fail(CommonPrompt.OPERATION_FAILED);
    }
}
