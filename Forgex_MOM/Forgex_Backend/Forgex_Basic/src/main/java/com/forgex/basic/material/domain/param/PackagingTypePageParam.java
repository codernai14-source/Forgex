package com.forgex.basic.material.domain.param;

import lombok.Data;

/**
 * 包装规格分页查询参数。
 * <p>
 * 封装包装规格列表的分页与筛选条件，字段名与 FxDynamicTable 查询字段保持一致。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@Data
public class PackagingTypePageParam {

    /**
     * 页码，从 1 开始。
     */
    private Integer pageNum = 1;

    /**
     * 每页条数。
     */
    private Integer pageSize = 10;

    /**
     * 包装规格编码。
     */
    private String packagingCode;

    /**
     * 包装规格名称。
     */
    private String packagingName;

    /**
     * 包装规格类型，取值来自字典 packaging_spec_type。
     */
    private String packagingSpecType;

    /**
     * 状态：0-禁用，1-启用。
     */
    private Integer status;
}
