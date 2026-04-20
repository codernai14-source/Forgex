package com.forgex.report.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表数据源查询参数类
 * <p>
 * 用于封装报表数据源查询条件
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see BaseGetParam
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报表数据源查询参数")
public class ReportDatasourceParam extends BaseGetParam {

    /**
     * 数据源名称（模糊查询）
     */
    @Schema(description = "数据源名称")
    private String name;
    
    /**
     * 数据源编码（精确查询）
     */
    @Schema(description = "数据源编码")
    private String code;
    
    /**
     * 数据源类型（mysql/oracle/sqlserver/postgresql/jdbc 等）
     */
    @Schema(description = "数据源类型")
    private String type;
    
    /**
     * 数据源状态：0-禁用，1-启用
     */
    @Schema(description = "数据源状态")
    private Integer status;
}
