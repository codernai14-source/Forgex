package com.forgex.basic.label.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签绑定数据传输对象
 * <p>
 * 用于标签模板绑定关系的数据传输和展示
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "标签绑定 DTO")
public class LabelBindingDTO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 模板 ID
     */
    @Schema(description = "模板 ID")
    private Long templateId;

    /**
     * 模板名称（关联查询字段）
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 绑定类型：MATERIAL=按物料，SUPPLIER=按供应商，CUSTOMER=按客户
     */
    @Schema(description = "绑定类型")
    private String bindingType;

    /**
     * 绑定值（物料编码/供应商编码/客户编码）
     */
    @Schema(description = "绑定值")
    private String bindingValue;

    /**
     * 绑定名称（物料名称/供应商名称/客户名称，关联查询字段）
     */
    @Schema(description = "绑定名称")
    private String bindingName;

    /**
     * 优先级
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 工厂 ID
     */
    @Schema(description = "工厂 ID")
    private Long factoryId;

    /**
     * 租户 ID
     */
    @Schema(description = "租户 ID")
    private Long tenantId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
