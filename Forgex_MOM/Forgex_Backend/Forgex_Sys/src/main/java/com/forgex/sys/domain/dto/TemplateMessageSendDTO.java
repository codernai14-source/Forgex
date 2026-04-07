package com.forgex.sys.domain.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 模板消息发送DTO。
 * <p>
 * 封装基于模板发送消息所需的参数。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-06
 */
@Data
public class TemplateMessageSendDTO {

    /**
     * 模板编码（必填）。
     * <p>
     * 对应 sys_message_template.template_code
     * </p>
     */
    private String templateCode;

    /**
     * 接收人用户ID列表（必填）。
     * <p>
     * 显式指定接收人，适用于审批通知等场景。
     * </p>
     */
    private List<Long> receiverUserIds;

    /**
     * 单个接收人用户ID（可选）。
     * <p>
     * 当仅需发送给单个用户时可使用此字段，与 receiverUserIds 二选一。
     * </p>
     */
    private Long receiverUserId;

    /**
     * 占位符数据Map（可选）。
     * <p>
     * 用于填充模板中的占位符，例如 {"userName": "张三", "taskName": "请假申请"}
     * </p>
     */
    private Map<String, Object> dataMap;

    /**
     * 业务类型（可选）。
     * <p>
     * 优先级高于模板配置的业务类型。
     * </p>
     */
    private String bizType;
}