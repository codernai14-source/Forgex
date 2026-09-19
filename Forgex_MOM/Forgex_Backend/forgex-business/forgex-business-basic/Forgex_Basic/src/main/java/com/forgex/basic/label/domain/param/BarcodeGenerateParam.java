package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 条码生成参数
 * <p>
 * 用于生成新条码的请求参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "条码生成参数")
public class BarcodeGenerateParam {

    /**
     * 模板类型（必填）
     */
    @NotBlank(message = "模板类型不能为空")
    @Schema(description = "模板类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateType;

    /**
     * 物料 ID（可选）
     */
    @Schema(description = "物料 ID")
    private Long materialId;

    /**
     * LOT 号（可选）
     */
    @Schema(description = "LOT 号")
    private String lotNo;

    /**
     * 工程卡号（可选）
     */
    @Schema(description = "工程卡号")
    private String engineeringCardNo;

    /**
     * 业务场景（必填）
     * INCOMING=来料，PRODUCTION=生产，OUTBOUND=出库，OTHER=其他
     */
    @NotBlank(message = "业务场景不能为空")
    @Schema(description = "业务场景: INCOMING=来料, PRODUCTION=生产, OUTBOUND=出库, OTHER=其他", requiredMode = Schema.RequiredMode.REQUIRED)
    private String businessScene;

    /**
     * 工厂 ID（可选）
     */
    @Schema(description = "工厂 ID")
    private Long factoryId;

    /**
     * 批次号（可选）
     */
    @Schema(description = "批次号")
    private String batchNo;

    /**
     * 备注（可选）
     */
    @Schema(description = "备注")
    private String remark;
}

