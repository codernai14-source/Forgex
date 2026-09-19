package com.forgex.basic.label.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "条码查询参数")
public class BarcodeQueryParam {

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页大小")
    private Integer pageSize = 10;

    @Schema(description = "条码号")
    private String barcodeNo;

    @Schema(description = "模板类型")
    private String templateType;

    @Schema(description = "物料 ID")
    private Long materialId;

    @Schema(description = "物料编码")
    private String materialCode;

    @Schema(description = "供应商编码")
    private String supplierCode;

    @Schema(description = "客户编码")
    private String customerCode;

    @Schema(description = "LOT 号")
    private String lotNo;

    @Schema(description = "工程卡号")
    private String engineeringCardNo;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "业务场景: INCOMING=来料, PRODUCTION=生产, OUTBOUND=出库, OTHER=其他")
    private String businessScene;

    @Schema(description = "工厂 ID")
    private Long factoryId;

    @Schema(description = "状态: 0-已作废, 1-已生成")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
