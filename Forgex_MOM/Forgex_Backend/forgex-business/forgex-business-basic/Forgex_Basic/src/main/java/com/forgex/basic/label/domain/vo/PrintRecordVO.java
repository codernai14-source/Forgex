package com.forgex.basic.label.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签打印记录视图对象
 * <p>
 * 用于前端展示标签打印记录信息
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@Schema(description = "标签打印记录 VO")
public class PrintRecordVO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;

    /**
     * 打印流水号
     */
    @Schema(description = "打印流水号")
    private String printNo;

    /**
     * 模板 ID
     */
    @Schema(description = "模板 ID")
    private Long templateId;

    /**
     * 模板编码
     */
    @Schema(description = "模板编码")
    private String templateCode;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 模板版本
     */
    @Schema(description = "模板版本")
    private Integer templateVersion;

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
     * 打印类型：NORMAL=正常打印，REPRINT=补打
     */
    @Schema(description = "打印类型")
    private String printType;

    /**
     * 打印类型名称
     */
    @Schema(description = "打印类型名称")
    private String printTypeName;

    /**
     * 条码号/序列号
     */
    @Schema(description = "条码号")
    private String barcodeNo;

    /**
     * 工程卡号
     */
    @Schema(description = "工程卡号")
    private String engineeringCardNo;

    /**
     * LOT 号
     */
    @Schema(description = "LOT 号")
    private String lotNo;

    /**
     * 批次号
     */
    @Schema(description = "批次号")
    private String batchNo;

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
     * 打印张数
     */
    @Schema(description = "打印张数")
    private Integer printCount;

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
     * 操作人 ID
     */
    @Schema(description = "操作人 ID")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @Schema(description = "操作人姓名")
    private String operatorName;

    /**
     * 打印时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "打印时间")
    private LocalDateTime printTime;

    /**
     * 租户 ID
     */
    @Schema(description = "租户 ID")
    private Long tenantId;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}

