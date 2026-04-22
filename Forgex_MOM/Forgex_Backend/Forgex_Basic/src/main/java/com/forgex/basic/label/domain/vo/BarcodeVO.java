package com.forgex.basic.label.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 条码记录视图对象
 * <p>
 * 用于前端展示条码记录信息
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "条码记录 VO")
public class BarcodeVO {

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
     * 模板类型名称
     */
    @Schema(description = "模板类型名称")
    private String templateTypeName;

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
     * 供应商名称
     */
    @Schema(description = "供应商名称")
    private String supplierName;

    /**
     * 客户编码
     */
    @Schema(description = "客户编码")
    private String customerCode;

    /**
     * 客户名称
     */
    @Schema(description = "客户名称")
    private String customerName;

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
     * 业务场景名称
     */
    @Schema(description = "业务场景名称")
    private String businessSceneName;

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
     * 状态名称
     */
    @Schema(description = "状态名称")
    private String statusName;

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
     * 工厂名称
     */
    @Schema(description = "工厂名称")
    private String factoryName;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
