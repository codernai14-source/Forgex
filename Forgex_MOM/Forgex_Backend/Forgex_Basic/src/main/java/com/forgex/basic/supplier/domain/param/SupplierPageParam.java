package com.forgex.basic.supplier.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商分页查询参数类
 * <p>
 * 用于接收前端分页搜索供应商时的条件参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "供应商分页查询参数")
public class SupplierPageParam extends BaseGetParam {

    /**
     * 供应商编码（模糊查询）
     */
    @Schema(description = "供应商编码")
    private String supplierCode;

    /**
     * 供应商名称（模糊查询）
     */
    @Schema(description = "供应商名称")
    private String supplierName;

    /**
     * 供应商类型
     */
    @Schema(description = "供应商类型")
    private String supplierType;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态")
    private Integer status;
}
