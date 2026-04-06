package com.forgex.workflow.service;

import com.forgex.common.web.R;
import com.forgex.workflow.client.SysMessageClient;
import com.forgex.workflow.client.dto.SysMessageSendRequest;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskNodeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 工作流实时通知服务。
 * <p>
 * 统一将审批流关键节点的消息发送到系统消息中心，
 * 由系统服务完成消息落库与 SSE 实时推送，避免工作流模块再依赖
 * 模板接收人推断审批人，保证“待我审批”和站内通知使用同一条链路。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowNotificationService {

    private static final String INTERNAL_SCOPE = "INTERNAL";
    private static final String INTERNAL_PLATFORM = "INTERNAL";
    private static final String MESSAGE_TYPE_NOTICE = "NOTICE";
    private static final String MESSAGE_TYPE_WARNING = "WARNING";

    private static final String BIZ_TYPE_PENDING = "WF_PENDING";
    private static final String BIZ_TYPE_APPROVED = "WF_APPROVED";
    private static final String BIZ_TYPE_REJECTED = "WF_REJECTED";
    private static final String BIZ_TYPE_FINISHED = "WF_FINISHED";

    private static final String APPROVAL_PENDING_LINK = "/workspace/approval/my/pending";
    private static final String APPROVAL_INITIATED_LINK = "/workspace/approval/my/initiated";

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysMessageClient sysMessageClient;

    /**
     * 通知待审批人。
     *
     * @param execution   审批执行记录
     * @param node        当前激活的审批节点
     * @param approverIds 待处理人 ID 列表
     */
    public void notifyPendingApprovers(WfTaskExecution execution, WfTaskNodeConfig node, Long[] approverIds) {
        if (execution == null || node == null || approverIds == null || approverIds.length == 0) {
            return;
        }

        String title = "【审批待办】" + defaultTaskName(execution);
        String content = new StringBuilder()
                .append("发起人：").append(defaultText(execution.getInitiatorName(), "系统"))
                .append("\n当前节点：").append(defaultText(node.getNodeName(), "审批节点"))
                .append("\n发起时间：").append(formatTime(execution.getStartTime()))
                .toString();

        sendToUsers(execution.getTenantId(), Arrays.asList(approverIds), title, content,
                APPROVAL_PENDING_LINK, BIZ_TYPE_PENDING, MESSAGE_TYPE_NOTICE);
    }

    /**
     * 通知发起人审批节点已通过。
     *
     * @param execution    审批执行记录
     * @param currentNode  当前审批节点名称
     * @param approverName 处理人名称
     * @param comment      审批意见
     */
    public void notifyApproved(WfTaskExecution execution, String currentNode, String approverName, String comment) {
        if (execution == null || execution.getInitiatorId() == null) {
            return;
        }

        StringBuilder content = new StringBuilder()
                .append("审批名称：").append(defaultTaskName(execution))
                .append("\n审批节点：").append(defaultText(currentNode, "审批节点"))
                .append("\n处理人：").append(defaultText(approverName, "系统"))
                .append("\n处理结果：已通过");
        if (StringUtils.hasText(comment)) {
            content.append("\n审批意见：").append(comment.trim());
        }

        sendToUsers(execution.getTenantId(), Set.of(execution.getInitiatorId()),
                "【审批通过】" + defaultTaskName(execution), content.toString(),
                APPROVAL_INITIATED_LINK, BIZ_TYPE_APPROVED, MESSAGE_TYPE_NOTICE);
    }

    /**
     * 通知发起人审批被驳回。
     *
     * @param execution    审批执行记录
     * @param currentNode  当前审批节点名称
     * @param approverName 处理人名称
     * @param comment      驳回意见
     * @param rejectType   驳回类型
     */
    public void notifyRejected(WfTaskExecution execution,
                               String currentNode,
                               String approverName,
                               String comment,
                               Integer rejectType) {
        if (execution == null || execution.getInitiatorId() == null) {
            return;
        }

        String rejectTypeText = Objects.equals(rejectType, 2) ? "退回上一步" : "驳回结束";
        StringBuilder content = new StringBuilder()
                .append("审批名称：").append(defaultTaskName(execution))
                .append("\n审批节点：").append(defaultText(currentNode, "审批节点"))
                .append("\n处理人：").append(defaultText(approverName, "系统"))
                .append("\n处理结果：").append(rejectTypeText);
        if (StringUtils.hasText(comment)) {
            content.append("\n驳回原因：").append(comment.trim());
        }

        sendToUsers(execution.getTenantId(), Set.of(execution.getInitiatorId()),
                "【审批驳回】" + defaultTaskName(execution), content.toString(),
                APPROVAL_INITIATED_LINK, BIZ_TYPE_REJECTED, MESSAGE_TYPE_WARNING);
    }

    /**
     * 通知发起人审批已完成。
     *
     * @param execution 审批执行记录
     * @param status    最终状态
     */
    public void notifyFinished(WfTaskExecution execution, Integer status) {
        if (execution == null || execution.getInitiatorId() == null || !Objects.equals(status, 2)) {
            return;
        }

        String content = new StringBuilder()
                .append("审批名称：").append(defaultTaskName(execution))
                .append("\n发起人：").append(defaultText(execution.getInitiatorName(), "系统"))
                .append("\n完成时间：").append(formatTime(execution.getEndTime()))
                .append("\n处理结果：审批完成")
                .toString();

        sendToUsers(execution.getTenantId(), Set.of(execution.getInitiatorId()),
                "【审批完成】" + defaultTaskName(execution), content,
                APPROVAL_INITIATED_LINK, BIZ_TYPE_FINISHED, MESSAGE_TYPE_NOTICE);
    }

    /**
     * 向指定用户集合发送站内消息。
     *
     * @param tenantId    租户 ID
     * @param userIds     用户 ID 集合
     * @param title       消息标题
     * @param content     消息内容
     * @param linkUrl     跳转链接
     * @param bizType     业务类型
     * @param messageType 消息类型
     */
    private void sendToUsers(Long tenantId,
                             Collection<Long> userIds,
                             String title,
                             String content,
                             String linkUrl,
                             String bizType,
                             String messageType) {
        if (tenantId == null || userIds == null || userIds.isEmpty()) {
            return;
        }

        Set<Long> distinctUserIds = new LinkedHashSet<>();
        for (Long userId : userIds) {
            if (userId != null) {
                distinctUserIds.add(userId);
            }
        }
        if (distinctUserIds.isEmpty()) {
            return;
        }

        for (Long userId : distinctUserIds) {
            SysMessageSendRequest request = new SysMessageSendRequest();
            request.setReceiverTenantId(tenantId);
            request.setReceiverUserId(userId);
            request.setScope(INTERNAL_SCOPE);
            request.setPlatform(INTERNAL_PLATFORM);
            request.setMessageType(messageType);
            request.setTitle(title);
            request.setContent(content);
            request.setLinkUrl(linkUrl);
            request.setBizType(bizType);
            sendSafely(request);
        }
    }

    /**
     * 调用系统消息服务发送消息。
     *
     * @param request 消息请求参数
     */
    private void sendSafely(SysMessageSendRequest request) {
        try {
            R<Long> result = sysMessageClient.send(request);
            if (result == null || result.getCode() != 200) {
                log.warn("工作流通知发送失败，receiverUserId={}, code={}, message={}",
                        request.getReceiverUserId(),
                        result == null ? null : result.getCode(),
                        result == null ? null : result.getMessage());
            }
        } catch (Exception ex) {
            log.warn("工作流通知发送异常，receiverUserId={}, title={}",
                    request.getReceiverUserId(), request.getTitle(), ex);
        }
    }

    /**
     * 获取默认审批名称。
     *
     * @param execution 审批执行记录
     * @return 审批名称
     */
    private String defaultTaskName(WfTaskExecution execution) {
        return defaultText(execution == null ? null : execution.getTaskName(), "审批任务");
    }

    /**
     * 格式化时间文本。
     *
     * @param time 时间
     * @return 格式化后的文本
     */
    private String formatTime(LocalDateTime time) {
        return time == null ? "-" : DATETIME_FORMATTER.format(time);
    }

    /**
     * 返回默认文本。
     *
     * @param value        原始值
     * @param defaultValue 默认值
     * @return 兜底后的文本
     */
    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }
}
