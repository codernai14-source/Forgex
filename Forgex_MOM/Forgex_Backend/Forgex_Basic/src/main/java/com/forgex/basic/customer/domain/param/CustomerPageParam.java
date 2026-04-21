package com.forgex.basic.customer.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户分页查询参数类
 * <p>
 * 用于接收前端分页搜索客户时的条件参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户分页查询参数")
public class CustomerPageParam extends BaseGetParam {

    /**
     * 客户编码（模糊查询）
     */
    @Schema(description = "客户编码")
    private String customerCode;

    /**
     * 客户名称（模糊查询）
     */
    @Schema(description = "客户名称")
    private String customerName;

    /**
     * 客户类型
     * DOMESTIC=国内，OVERSEAS=海外
     */
    @Schema(description = "客户类型")
    private String customerType;

    /**
     * 状态：0-禁用，1-启用
     */
    @Schema(description = "状态")
    private Integer status;
}
