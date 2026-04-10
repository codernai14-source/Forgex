package com.forgex.report.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表模板查询参数类
 * <p>
 * 用于封装报表模板查询条件
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see BaseGetParam
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报表模板查询参数")
public class ReportTemplateParam extends BaseGetParam {

    /**
     * 报表名称（模糊查询）
     */
    @Schema(description = "报表名称")
    private String name;
    
    /**
     * 报表编码（精确查询）
     */
    @Schema(description = "报表编码")
    private String code;
    
    /**
     * 报表引擎类型（UREPORT/JIMU）
     * @see com.forgex.report.enums.ReportEngineType
     */
    @Schema(description = "报表引擎类型")
    private String engineType;
    
    /**
     * 分类 ID
     */
    @Schema(description = "分类 ID")
    private Long categoryId;
    
    /**
     * 报表状态：0-禁用，1-启用
     */
    @Schema(description = "报表状态")
    private Integer status;
}
