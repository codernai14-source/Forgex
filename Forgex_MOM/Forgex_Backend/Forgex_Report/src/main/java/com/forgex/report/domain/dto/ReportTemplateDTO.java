package com.forgex.report.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表模板数据传输对象
 * <p>
 * 用于报表模板的数据传输和展示
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see com.forgex.report.domain.entity.ReportTemplate
 */
@Data
@Schema(description = "报表模板 DTO")
public class ReportTemplateDTO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;
    
    /**
     * 报表名称
     */
    @Schema(description = "报表名称")
    private String name;
    
    /**
     * 报表编码
     */
    @Schema(description = "报表编码")
    private String code;
    
    /**
     * 报表引擎类型（UREPORT/JIMU）
     */
    @Schema(description = "报表引擎类型")
    private String engineType;
    
    /**
     * 分类 ID
     */
    @Schema(description = "分类 ID")
    private Long categoryId;
    
    /**
     * 分类名称（关联查询字段，非数据库字段）
     */
    @Schema(description = "分类名称")
    private String categoryName;
    
    /**
     * 数据源 ID
     */
    @Schema(description = "数据源 ID")
    private Long datasourceId;
    
    /**
     * 数据源名称（关联查询字段，非数据库字段）
     */
    @Schema(description = "数据源名称")
    private String datasourceName;
    
    /**
     * 报表模板内容（XML 或 JSON 格式）
     */
    @Schema(description = "报表模板内容")
    private String content;
    
    /**
     * 报表模板文件路径
     */
    @Schema(description = "报表模板文件路径")
    private String templatePath;
    
    /**
     * 报表状态：0-禁用，1-启用
     */
    @Schema(description = "报表状态")
    private Integer status;
    
    /**
     * 备注说明
     */
    @Schema(description = "备注说明")
    private String remark;
    
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
