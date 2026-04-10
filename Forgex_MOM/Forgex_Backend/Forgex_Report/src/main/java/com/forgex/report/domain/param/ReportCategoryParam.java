package com.forgex.report.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表分类查询参数类
 * <p>
 * 用于封装报表分类查询条件
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see BaseGetParam
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报表分类查询参数")
public class ReportCategoryParam extends BaseGetParam {

    /**
     * 分类名称（模糊查询）
     */
    @Schema(description = "分类名称")
    private String name;
    
    /**
     * 分类编码（精确查询）
     */
    @Schema(description = "分类编码")
    private String code;
    
    /**
     * 父分类 ID
     */
    @Schema(description = "父分类 ID")
    private Long parentId;
    
    /**
     * 分类状态：0-禁用，1-启用
     */
    @Schema(description = "分类状态")
    private Integer status;
}
