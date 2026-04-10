package com.forgex.report.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表分类数据传输对象
 * <p>
 * 用于报表分类的数据传输和展示
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see com.forgex.report.domain.entity.ReportCategory
 */
@Data
@Schema(description = "报表分类 DTO")
public class ReportCategoryDTO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;
    
    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String name;
    
    /**
     * 分类编码
     */
    @Schema(description = "分类编码")
    private String code;
    
    /**
     * 父分类 ID
     */
    @Schema(description = "父分类 ID")
    private Long parentId;
    
    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer sortOrder;
    
    /**
     * 备注说明
     */
    @Schema(description = "备注说明")
    private String remark;
    
    /**
     * 分类状态：0-禁用，1-启用
     */
    @Schema(description = "分类状态")
    private Integer status;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
