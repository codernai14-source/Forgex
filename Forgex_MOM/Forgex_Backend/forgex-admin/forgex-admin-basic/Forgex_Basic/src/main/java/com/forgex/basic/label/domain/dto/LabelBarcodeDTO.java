package com.forgex.basic.label.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 条码记录数据传输对象
 * <p>
 * 用于条码记录的数据传输和展示
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "条码记录 DTO")
public class LabelBarcodeDTO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 条码号/序列号
     */
    @Schema(description = "条码号")
    private String barcodeNo;

    /**
     * 模板类型
     */
    @Schema(description = "模板类型")
    private String templateType;

    /**
     * 物料 ID
     */
    @Schema(description = "物料 ID")
    private Long materialId;

    /**
     * 物料编码
     */
    @Schema(description = "物料编码")
    private String materialCode;

    /**
     * 物料名称
     */
    @Schema(description = "物料名称")
    private String materialName;

    /**
     * 供应商编码
     */
    @Schema(description = "供应商编码")
    private String supplierCode;

    /**
     * 客户编码
     */
    @Schema(description = "客户编码")
    private String customerCode;

    /**
     * 批次号
     */
    @Schema(description = "批次号")
    private String batchNo;

    /**
     * LOT 号
     */
    @Schema(description = "LOT 号")
    private String lotNo;

    /**
     * 工单号
     */
    @Schema(description = "工单号")
    private String workOrderNo;

    /**
     * 工程卡号
     */
    @Schema(description = "工程卡号")
    private String engineeringCardNo;

    /**
     * 生产线
     */
    @Schema(description = "生产线")
    private String productionLine;

    /**
     * 业务场景：INCOMING=来料，PRODUCTION=生产，OUTBOUND=出库，OTHER=其他
     */
    @Schema(description = "业务场景")
    private String businessScene;

    /**
     * 条码生成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "条码生成时间")
    private LocalDateTime generateTime;

    /**
     * 状态：0-失效，1-有效
     */
    @Schema(description = "状态")
    private Integer status;

    /**
     * 租户 ID
     */
    @Schema(description = "租户 ID")
    private Long tenantId;

    /**
     * 工厂 ID
     */
    @Schema(description = "工厂 ID")
    private Long factoryId;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

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
