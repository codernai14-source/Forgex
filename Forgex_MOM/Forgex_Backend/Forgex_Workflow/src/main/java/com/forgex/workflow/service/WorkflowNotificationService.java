package com.forgex.workflow.service;

import com.forgex.common.web.R;
import com.forgex.workflow.client.SysMessageClient;
import com.forgex.workflow.client.dto.SysMessageSendRequest;
import com.forgex.workflow.client.dto.TemplateMessageSendRequest;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.domain.entity.WfTaskNodeConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 工作流实时通知服务。
 * <p>
 * 统一将审批流关键节点的消息发送到系统消息中心，
 * 由系统服务完成消息落库与 SSE 实时推送，避免工作流模块再依赖
 * 模板接收人推断审批人，保证"待我审批"和站内通知使用同一条链路。
 * </p>
 * <p>
 * <strong>消息发送模式：</strong>
 * </p>
 * <ul>
 *   <li>模板模式（推荐）：根据模板编码发送消息，支持占位符替换和多语言</li>
 *   <li>直发模式（后备）：直接发送硬编码消息内容</li>
 * </ul>
 * <p>
 * <strong>模板编码约定：</strong>
 * </p>
 * <ul>
 *   <li>WF_PENDING：审批待办通知</li>
 *   <li>WF_APPROVED：审批通过通知</li>
 *   <li>WF_REJECTED：审批驳回通知</li>
 *   <li>WF_FINISHED：审批完成通知</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.1.0
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

    /**
     * 审批待办通知模板编码。
     */
    private static final String TEMPLATE_PENDING = "WF_PENDING";

    /**
     * 审批通过通知模板编码。
     */
    private static final String TEMPLATE_APPROVED = "WF_APPROVED";

    /**
     * 审批驳回通知模板编码。
     */
    private static final String TEMPLATE_REJECTED = "WF_REJECTED";

    /**
     * 审批完成通知模板编码。
     */
    private static final String TEMPLATE_FINISHED = "WF_FINISHED";

    private static final String APPROVAL_PENDING_LINK = "/workspace/approval/my/pending";
    private static final String APPROVAL_INITIATED_LINK = "/workspace/approval/my/initiated";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String HEADER_LEGACY_TENANT_ID = "Tenant-Id";
    private static final String LOGIN_MESSAGE_NOT_LOGIN = "\u672A\u767B\u5F55";
    private static final String LOGIN_MESSAGE_EXPIRED = "\u767B\u5F55\u8FC7\u671F";

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysMessageClient sysMessageClient;

    private enum TemplateSendResult {
        SUCCESS,
        FAILED,
        AUTH_MISSING,
        AUTH_FAILED
    }

    /**
     * 通知待审批人。
     * <p>
     * 优先使用模板消息发送，如果模板不存在则降级为直发模式。
     * </p>
     *
     * @param execution   审批执行记录
     * @param node        当前激活的审批节点
     * @param approverIds 待处理人 ID 列表
     */
    public void notifyPendingApprovers(WfTaskExecution execution, WfTaskNodeConfig node, Long[] approverIds) {
        if (execution == null || node == null || approverIds == null || approverIds.length == 0) {
            return;
        }

        List<Long> receiverIds = Arrays.asList(approverIds);

        // 构建模板占位符数据
        Map<String, Object> dataMap = buildPendingDataMap(execution, node);

        // 尝试使用模板发送
        TemplateSendResult templateResult = trySendByTemplate(
                TEMPLATE_PENDING, receiverIds, dataMap, BIZ_TYPE_PENDING, execution.getId());
        if (templateResult == TemplateSendResult.SUCCESS) {
            return;
        }
        if (templateResult == TemplateSendResult.AUTH_MISSING || templateResult == TemplateSendResult.AUTH_FAILED) {
            return;
        }

        // 模板发送失败，降级为直发模式
        if (templateResult == TemplateSendResult.FAILED) {
            log.info("模板消息发送失败，降级为直发模式: templateCode={}", TEMPLATE_PENDING);
            String title = "【审批待办】" + defaultTaskName(execution);
            String content = buildPendingContent(execution, node);
            sendToUsersDirectly(execution.getTenantId(), receiverIds, title, content,
                    APPROVAL_PENDING_LINK, BIZ_TYPE_PENDING, MESSAGE_TYPE_NOTICE, execution.getId(), TEMPLATE_PENDING);
        }
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

        List<Long> receiverIds = List.of(execution.getInitiatorId());

        // 构建模板占位符数据
        Map<String, Object> dataMap = buildApprovedDataMap(execution, currentNode, approverName, comment);

        // 尝试使用模板发送
        TemplateSendResult templateResult = trySendByTemplate(
                TEMPLATE_APPROVED, receiverIds, dataMap, BIZ_TYPE_APPROVED, execution.getId());
        if (templateResult == TemplateSendResult.SUCCESS) {
            return;
        }
        if (templateResult == TemplateSendResult.AUTH_MISSING || templateResult == TemplateSendResult.AUTH_FAILED) {
            return;
        }

        // 模板发送失败，降级为直发模式
        if (templateResult == TemplateSendResult.FAILED) {
            log.info("模板消息发送失败，降级为直发模式: templateCode={}", TEMPLATE_APPROVED);
            String title = "【审批通过】" + defaultTaskName(execution);
            String content = buildApprovedContent(execution, currentNode, approverName, comment);
            sendToUsersDirectly(execution.getTenantId(), receiverIds, title, content,
                    APPROVAL_INITIATED_LINK, BIZ_TYPE_APPROVED, MESSAGE_TYPE_NOTICE, execution.getId(), TEMPLATE_APPROVED);
        }
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

        List<Long> receiverIds = List.of(execution.getInitiatorId());

        // 构建模板占位符数据
        Map<String, Object> dataMap = buildRejectedDataMap(execution, currentNode, approverName, comment, rejectType);

        // 尝试使用模板发送
        TemplateSendResult templateResult = trySendByTemplate(
                TEMPLATE_REJECTED, receiverIds, dataMap, BIZ_TYPE_REJECTED, execution.getId());
        if (templateResult == TemplateSendResult.SUCCESS) {
            return;
        }
        if (templateResult == TemplateSendResult.AUTH_MISSING || templateResult == TemplateSendResult.AUTH_FAILED) {
            return;
        }

        // 模板发送失败，降级为直发模式
        if (templateResult == TemplateSendResult.FAILED) {
            log.info("模板消息发送失败，降级为直发模式: templateCode={}", TEMPLATE_REJECTED);
            String title = "【审批驳回】" + defaultTaskName(execution);
            String content = buildRejectedContent(execution, currentNode, approverName, comment, rejectType);
            sendToUsersDirectly(execution.getTenantId(), receiverIds, title, content,
                    APPROVAL_INITIATED_LINK, BIZ_TYPE_REJECTED, MESSAGE_TYPE_WARNING, execution.getId(), TEMPLATE_REJECTED);
        }
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

        List<Long> receiverIds = List.of(execution.getInitiatorId());

        // 构建模板占位符数据
        Map<String, Object> dataMap = buildFinishedDataMap(execution);

        // 尝试使用模板发送
        TemplateSendResult templateResult = trySendByTemplate(
                TEMPLATE_FINISHED, receiverIds, dataMap, BIZ_TYPE_FINISHED, execution.getId());
        if (templateResult == TemplateSendResult.SUCCESS) {
            return;
        }
        if (templateResult == TemplateSendResult.AUTH_MISSING || templateResult == TemplateSendResult.AUTH_FAILED) {
            return;
        }

        // 模板发送失败，降级为直发模式
        if (templateResult == TemplateSendResult.FAILED) {
            log.info("模板消息发送失败，降级为直发模式: templateCode={}", TEMPLATE_FINISHED);
            String title = "【审批完成】" + defaultTaskName(execution);
            String content = buildFinishedContent(execution);
            sendToUsersDirectly(execution.getTenantId(), receiverIds, title, content,
                    APPROVAL_INITIATED_LINK, BIZ_TYPE_FINISHED, MESSAGE_TYPE_NOTICE, execution.getId(), TEMPLATE_FINISHED);
        }
    }

    /**
     * 尝试使用模板发送消息。
     *
     * @param templateCode 模板编码
     * @param receiverIds  接收人ID列表
     * @param dataMap      占位符数据
     * @param bizType      业务类型
     * @return 模板发送结果
     */
    private TemplateSendResult trySendByTemplate(String templateCode,
                                                 List<Long> receiverIds,
                                                 Map<String, Object> dataMap,
                                                 String bizType,
                                                 Long executionId) {
        if (!hasAuthContext(executionId, templateCode, null)) {
            return TemplateSendResult.AUTH_MISSING;
        }

        try {
            TemplateMessageSendRequest request = new TemplateMessageSendRequest();
            request.setTemplateCode(templateCode);
            request.setReceiverUserIds(receiverIds);
            request.setDataMap(dataMap);
            request.setBizType(bizType);

            R<Integer> result = sysMessageClient.sendByTemplate(request);
            if (result != null && result.getCode() == 200 && result.getData() != null && result.getData() > 0) {
                log.info("模板消息发送成功: templateCode={}, receiverCount={}, executionId={}",
                        templateCode, result.getData(), executionId);
                return TemplateSendResult.SUCCESS;
            }

            if (isAuthFailure(result == null ? null : result.getMessage())) {
                log.warn("模板消息发送鉴权失败，停止降级发送: templateCode={}, executionId={}, receiverUserId={}, code={}, message={}",
                        templateCode,
                        executionId,
                        null,
                        result == null ? null : result.getCode(),
                        result == null ? null : result.getMessage());
                return TemplateSendResult.AUTH_FAILED;
            }

            log.warn("模板消息发送失败: templateCode={}, executionId={}, code={}, message={}",
                    templateCode,
                    executionId,
                    result == null ? null : result.getCode(),
                    result == null ? null : result.getMessage());
            return TemplateSendResult.FAILED;
        } catch (Exception ex) {
            if (isAuthFailure(ex.getMessage())) {
                log.warn("模板消息发送异常且鉴权失败，停止降级发送: templateCode={}, executionId={}, receiverUserId={}",
                        templateCode, executionId, null, ex);
                return TemplateSendResult.AUTH_FAILED;
            }
            log.warn("模板消息发送异常: templateCode={}, executionId={}", templateCode, executionId, ex);
            return TemplateSendResult.FAILED;
        }
    }

    /**
     * 向指定用户集合发送站内消息（直发模式）。
     *
     * @param tenantId    租户 ID
     * @param userIds     用户 ID 集合
     * @param title       消息标题
     * @param content     消息内容
     * @param linkUrl     跳转链接
     * @param bizType     业务类型
     * @param messageType 消息类型
     * @param executionId 审批执行ID
     * @param templateCode 模板编码
     */
    private void sendToUsersDirectly(Long tenantId,
                                     Collection<Long> userIds,
                                     String title,
                                     String content,
                                     String linkUrl,
                                     String bizType,
                                     String messageType,
                                     Long executionId,
                                     String templateCode) {
        if (tenantId == null || userIds == null || userIds.isEmpty()) {
            return;
        }
        if (!hasAuthContext(executionId, templateCode, null)) {
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
            sendSafely(request, executionId, templateCode);
        }
    }

    /**
     * 调用系统消息服务发送消息。
     *
     * @param request 消息请求参数
     */
    private void sendSafely(SysMessageSendRequest request, Long executionId, String templateCode) {
        try {
            R<Long> result = sysMessageClient.send(request);
            if (result == null || result.getCode() != 200) {
                if (isAuthFailure(result == null ? null : result.getMessage())) {
                    log.warn("工作流通知发送鉴权失败，已跳过: executionId={}, templateCode={}, receiverUserId={}, code={}, message={}",
                            executionId,
                            templateCode,
                            request.getReceiverUserId(),
                            result == null ? null : result.getCode(),
                            result == null ? null : result.getMessage());
                    return;
                }
                log.warn("工作流通知发送失败，executionId={}, templateCode={}, receiverUserId={}, code={}, message={}",
                        executionId,
                        templateCode,
                        request.getReceiverUserId(),
                        result == null ? null : result.getCode(),
                        result == null ? null : result.getMessage());
            }
        } catch (Exception ex) {
            if (isAuthFailure(ex.getMessage())) {
                log.warn("工作流通知发送异常且鉴权失败，已跳过: executionId={}, templateCode={}, receiverUserId={}, title={}",
                        executionId, templateCode, request.getReceiverUserId(), request.getTitle(), ex);
                return;
            }
            log.warn("工作流通知发送异常，executionId={}, templateCode={}, receiverUserId={}, title={}",
                    executionId, templateCode, request.getReceiverUserId(), request.getTitle(), ex);
        }
    }

    private boolean hasAuthContext(Long executionId, String templateCode, Long receiverUserId) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            List<String> missingHeaders = List.of("RequestContext", HEADER_USER_ID, HEADER_TENANT_ID);
            log.warn("工作流通知鉴权上下文缺失，跳过发送: executionId={}, templateCode={}, receiverUserId={}, missingHeaders={}",
                    executionId, templateCode, receiverUserId, missingHeaders);
            return false;
        }

        HttpServletRequest request = attributes.getRequest();
        List<String> missingHeaders = new ArrayList<>();
        if (!StringUtils.hasText(request.getHeader(HEADER_USER_ID))) {
            missingHeaders.add(HEADER_USER_ID);
        }

        String tenantId = request.getHeader(HEADER_TENANT_ID);
        String legacyTenantId = request.getHeader(HEADER_LEGACY_TENANT_ID);
        if (!StringUtils.hasText(tenantId) && !StringUtils.hasText(legacyTenantId)) {
            missingHeaders.add(HEADER_TENANT_ID);
        }

        if (!missingHeaders.isEmpty()) {
            log.warn("工作流通知鉴权上下文缺失，跳过发送: executionId={}, templateCode={}, receiverUserId={}, missingHeaders={}",
                    executionId, templateCode, receiverUserId, missingHeaders);
            return false;
        }
        return true;
    }

    private boolean isAuthFailure(String message) {
        if (!StringUtils.hasText(message)) {
            return false;
        }
        String normalizedMessage = message.trim();
        return normalizedMessage.contains(LOGIN_MESSAGE_NOT_LOGIN)
                || normalizedMessage.contains(LOGIN_MESSAGE_EXPIRED);
    }

    // ==================== 模板数据构建方法 ====================

    /**
     * 构建审批待办通知的占位符数据。
     *
     * @param execution 审批执行记录
     * @param node      审批节点
     * @return 占位符数据Map
     */
    private Map<String, Object> buildPendingDataMap(WfTaskExecution execution, WfTaskNodeConfig node) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("taskName", defaultTaskName(execution));
        dataMap.put("initiatorName", defaultText(execution.getInitiatorName(), "系统"));
        dataMap.put("nodeName", defaultText(node.getNodeName(), "审批节点"));
        dataMap.put("startTime", formatTime(execution.getStartTime()));
        return dataMap;
    }

    /**
     * 构建审批通过通知的占位符数据。
     *
     * @param execution    审批执行记录
     * @param currentNode  当前节点名称
     * @param approverName 审批人名称
     * @param comment      审批意见
     * @return 占位符数据Map
     */
    private Map<String, Object> buildApprovedDataMap(WfTaskExecution execution, String currentNode,
                                                      String approverName, String comment) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("taskName", defaultTaskName(execution));
        dataMap.put("nodeName", defaultText(currentNode, "审批节点"));
        dataMap.put("approverName", defaultText(approverName, "系统"));
        dataMap.put("result", "已通过");
        dataMap.put("comment", defaultText(comment, ""));
        return dataMap;
    }

    /**
     * 构建审批驳回通知的占位符数据。
     *
     * @param execution    审批执行记录
     * @param currentNode  当前节点名称
     * @param approverName 审批人名称
     * @param comment      驳回意见
     * @param rejectType   驳回类型
     * @return 占位符数据Map
     */
    private Map<String, Object> buildRejectedDataMap(WfTaskExecution execution, String currentNode,
                                                      String approverName, String comment, Integer rejectType) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("taskName", defaultTaskName(execution));
        dataMap.put("nodeName", defaultText(currentNode, "审批节点"));
        dataMap.put("approverName", defaultText(approverName, "系统"));
        dataMap.put("result", Objects.equals(rejectType, 2) ? "退回上一步" : "驳回结束");
        dataMap.put("comment", defaultText(comment, ""));
        return dataMap;
    }

    /**
     * 构建审批完成通知的占位符数据。
     *
     * @param execution 审批执行记录
     * @return 占位符数据Map
     */
    private Map<String, Object> buildFinishedDataMap(WfTaskExecution execution) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("taskName", defaultTaskName(execution));
        dataMap.put("initiatorName", defaultText(execution.getInitiatorName(), "系统"));
        dataMap.put("endTime", formatTime(execution.getEndTime()));
        dataMap.put("result", "审批完成");
        return dataMap;
    }

    // ==================== 直发模式内容构建方法 ====================

    /**
     * 构建审批待办通知内容（直发模式）。
     */
    private String buildPendingContent(WfTaskExecution execution, WfTaskNodeConfig node) {
        return new StringBuilder()
                .append("发起人：").append(defaultText(execution.getInitiatorName(), "系统"))
                .append("\n当前节点：").append(defaultText(node.getNodeName(), "审批节点"))
                .append("\n发起时间：").append(formatTime(execution.getStartTime()))
                .toString();
    }

    /**
     * 构建审批通过通知内容（直发模式）。
     */
    private String buildApprovedContent(WfTaskExecution execution, String currentNode,
                                         String approverName, String comment) {
        StringBuilder content = new StringBuilder()
                .append("审批名称：").append(defaultTaskName(execution))
                .append("\n审批节点：").append(defaultText(currentNode, "审批节点"))
                .append("\n处理人：").append(defaultText(approverName, "系统"))
                .append("\n处理结果：已通过");
        if (StringUtils.hasText(comment)) {
            content.append("\n审批意见：").append(comment.trim());
        }
        return content.toString();
    }

    /**
     * 构建审批驳回通知内容（直发模式）。
     */
    private String buildRejectedContent(WfTaskExecution execution, String currentNode,
                                         String approverName, String comment, Integer rejectType) {
        String rejectTypeText = Objects.equals(rejectType, 2) ? "退回上一步" : "驳回结束";
        StringBuilder content = new StringBuilder()
                .append("审批名称：").append(defaultTaskName(execution))
                .append("\n审批节点：").append(defaultText(currentNode, "审批节点"))
                .append("\n处理人：").append(defaultText(approverName, "系统"))
                .append("\n处理结果：").append(rejectTypeText);
        if (StringUtils.hasText(comment)) {
            content.append("\n驳回原因：").append(comment.trim());
        }
        return content.toString();
    }

    /**
     * 构建审批完成通知内容（直发模式）。
     */
    private String buildFinishedContent(WfTaskExecution execution) {
        return new StringBuilder()
                .append("审批名称：").append(defaultTaskName(execution))
                .append("\n发起人：").append(defaultText(execution.getInitiatorName(), "系统"))
                .append("\n完成时间：").append(formatTime(execution.getEndTime()))
                .append("\n处理结果：审批完成")
                .toString();
    }

    // ==================== 工具方法 ====================

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

