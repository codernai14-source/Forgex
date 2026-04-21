package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 标签打印参数
 * <p>
 * 用于发起标签打印的请求参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "标签打印参数")
public class LabelPrintParam {

    /**
     * 工厂 ID
     */
    @NotNull(message = "工厂 ID 不能为空")
    @Schema(description = "工厂 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long factoryId;

    /**
     * 模板类型
     */
    @NotBlank(message = "模板类型不能为空")
    @Schema(description = "模板类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateType;

    /**
     * 打印数据（键值对，用于替换模板占位符）
     */
    @NotNull(message = "打印数据不能为空")
    @Schema(description = "打印数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> printData;

    /**
     * 打印张数（默认为 1）
     */
    @Schema(description = "打印张数")
    private Integer printCount;

    /**
     * 物料 ID
     */
    @Schema(description = "物料 ID")
    private Long materialId;

    /**
     * 供应商 ID
     */
    @Schema(description = "供应商 ID")
    private Long supplierId;

    /**
     * 客户 ID
     */
    @Schema(description = "客户 ID")
    private Long customerId;
}
