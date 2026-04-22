package com.forgex.sys.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Excel 导出配置分页查询参数。
 * <p>
 * 用于接收导出配置管理页面的分页、表名称、表编码以及启用状态筛选条件。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0
 * @since 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExcelExportConfigPageParam extends BaseGetParam {
    /**
     * 表名称，支持模糊查询。
     */
    private String tableName;

    /**
     * 表编码，支持模糊查询。
     */
    private String tableCode;

    /**
     * 启用状态。
     * <p>
     * true 表示启用，false 表示禁用；为空时不按状态过滤。
     * </p>
     */
    private Boolean enabled;
}
