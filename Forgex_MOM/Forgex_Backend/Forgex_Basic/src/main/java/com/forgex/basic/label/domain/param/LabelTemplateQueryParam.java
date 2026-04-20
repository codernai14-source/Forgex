package com.forgex.basic.label.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标签模板查询参数
 * <p>
 * 用于标签模板列表分页查询的参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标签模板查询参数")
public class LabelTemplateQueryParam extends BaseGetParam {

    /**
     * 模板编码（模糊查询）
     */
    @Schema(description = "模板编码")
    private String templateCode;

    /**
     * 模板名称（模糊查询）
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 模板类型
     */
    @Schema(description = "模板类型")
    private String templateType;

    /**
     * 是否默认模板
     */
    @Schema(description = "是否默认模板")
    private Boolean isDefault;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 工厂 ID
     */
    @Schema(description = "工厂 ID")
    private Long factoryId;
}
