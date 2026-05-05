package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口出站目标实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_api_outbound_target")
public class ApiOutboundTarget extends BaseEntity {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接口配置 ID。
     */
    private Long apiConfigId;

    /**
     * 第三方系统 ID。
     */
    private Long thirdSystemId;

    /**
     * 目标编码。
     */
    private String targetCode;

    /**
     * 目标名称。
     */
    private String targetName;

    /**
     * 目标url。
     */
    private String targetUrl;

    /**
     * HTTP 请求方法。
     */
    private String httpMethod;

    /**
     * 内容类型。
     */
    private String contentType;

    /**
     * 调用模式。
     */
    private String invokeMode;

    /**
     * 超时时间（毫秒）。
     */
    private Integer timeoutMs;

    /**
     * 重试次数。
     */
    private Integer retryCount;

    /**
     * 重试间隔（毫秒）。
     */
    private Integer retryIntervalMs;

    /**
     * 排序号。
     */
    private Integer orderNum;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}
