package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 标签打印执行参数
 * <p>
 * 用于执行打印和预览的参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "标签打印执行参数")
public class LabelPrintExecuteParam {

    /**
     * 模板 ID（可选，如果不传则根据绑定关系智能匹配）
     */
    @Schema(description = "模板 ID")
    private Long templateId;

    /**
     * 模板类型（当 templateId 为空时必填）
     */
    @Schema(description = "模板类型")
    private String templateType;

    /**
     * 工厂 ID
     */
    @NotNull(message = "工厂 ID 不能为空")
    @Schema(description = "工厂 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long factoryId;

    /**
     * 打印数据（包含物料ID、供应商ID、客户ID等业务数据）
     */
    @NotNull(message = "打印数据不能为空")
    @Schema(description = "打印数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> printData;

    /**
     * 打印张数
     */
    @NotNull(message = "打印张数不能为空")
    @Min(value = 1, message = "打印张数必须大于 0")
    @Schema(description = "打印张数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer printCount;

    /**
     * 打印机名称（可选）
     */
    @Schema(description = "打印机名称")
    private String printerName;

    /**
     * 打印机 IP（可选）
     */
    @Schema(description = "打印机 IP")
    private String printerIp;
}
