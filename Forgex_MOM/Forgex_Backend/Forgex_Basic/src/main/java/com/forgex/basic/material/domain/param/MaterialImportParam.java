package com.forgex.basic.material.domain.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 物料导入请求参数类
 * <p>
 * 用于接收前端导入物料时的策略配置参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Data
@Schema(description = "物料导入参数")
public class MaterialImportParam {

    /**
     * 是否覆盖已有数据
     * true: 如果物料编码已存在，则更新数据
     * false: 如果物料编码已存在，则跳过该行
     */
    @Schema(description = "是否覆盖已有数据", defaultValue = "false")
    private Boolean isCovered = false;

    /**
     * 导入批次号（可选，用于日志追踪或业务关联）
     */
    @Schema(description = "导入批次号")
    private String batchNo;
}

