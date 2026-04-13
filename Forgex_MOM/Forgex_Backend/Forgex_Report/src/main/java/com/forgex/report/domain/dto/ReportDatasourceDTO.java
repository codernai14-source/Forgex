package com.forgex.report.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报表数据源数据传输对象
 * <p>
 * 用于报表数据源的数据传输和展示
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see com.forgex.report.domain.entity.ReportDatasource
 */
@Data
@Schema(description = "报表数据源 DTO")
public class ReportDatasourceDTO {

    /**
     * 主键 ID
     */
    @Schema(description = "主键 ID")
    private Long id;
    
    /**
     * 数据源名称
     */
    @Schema(description = "数据源名称")
    private String name;
    
    /**
     * 数据源编码
     */
    @Schema(description = "数据源编码")
    private String code;
    
    /**
     * 数据源类型（mysql/oracle/sqlserver/postgresql/jdbc 等）
     */
    @Schema(description = "数据源类型")
    private String type;
    
    /**
     * JDBC 驱动类名
     */
    @Schema(description = "JDBC 驱动类名")
    private String driverClass;
    
    /**
     * JDBC 连接 URL
     */
    @Schema(description = "JDBC 连接 URL")
    private String url;
    
    /**
     * 数据库用户名
     */
    @Schema(description = "数据库用户名")
    private String username;
    
    /**
     * 数据库密码（不返回前端）
     */
    @Schema(description = "数据库密码", accessMode = io.swagger.v3.oas.annotations.media.Schema.AccessMode.WRITE_ONLY)
    private String password;
    
    /**
     * 数据源状态：0-禁用，1-启用
     */
    @Schema(description = "数据源状态")
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
