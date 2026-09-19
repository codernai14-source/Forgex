package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 打印记录查询参数
 * <p>
 * 用于分页查询打印记录的请求参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "打印记录查询参数")
public class PrintRecordQueryParam {

    /**
     * 页码（默认 1）
     */
    @Schema(description = "页码")
    private Integer pageNum = 1;

    /**
     * 每页大小（默认 10）
     */
    @Schema(description = "每页大小")
    private Integer pageSize = 10;

    /**
     * 打印流水号（可选，模糊查询）
     */
    @Schema(description = "打印流水号")
    private String printNo;

    /**
     * 模板类型（可选）
     */
    @Schema(description = "模板类型")
    private String templateType;

    /**
     * 条码号（可选，模糊查询）
     */
    @Schema(description = "条码号")
    private String barcodeNo;

    /**
     * 工程卡号（可选，模糊查询）
     */
    @Schema(description = "工程卡号")
    private String engineeringCardNo;

    /**
     * LOT 号（可选，模糊查询）
     */
    @Schema(description = "LOT 号")
    private String lotNo;

    /**
     * 物料编码（可选，模糊查询）
     */
    @Schema(description = "物料编码")
    private String materialCode;

    /**
     * 操作员 ID（可选）
     */
    @Schema(description = "操作员 ID")
    private Long operatorId;

    /**
     * 打印开始时间（可选）
     */
    @Schema(description = "打印开始时间")
    private String printTimeStart;

    /**
     * 打印结束时间（可选）
     */
    @Schema(description = "打印结束时间")
    private String printTimeEnd;

    /**
     * 工厂 ID（可选）
     */
    @Schema(description = "工厂 ID")
    private Long factoryId;
}
