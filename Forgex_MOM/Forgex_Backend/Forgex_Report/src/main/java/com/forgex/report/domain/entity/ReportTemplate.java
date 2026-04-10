package com.forgex.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import com.forgex.report.enums.ReportEngineType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表模板实体类
 * <p>
 * 对应数据库表：fx_report_template
 * 支持 UReport2 和 JimuReport 双引擎
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see BaseEntity
 * @see ReportEngineType
 * @see ReportCategory
 * @see ReportDatasource
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_report_template")
public class ReportTemplate extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 报表名称
     */
    private String name;
    
    /**
     * 报表编码（唯一标识）
     */
    private String code;
    
    /**
     * 报表引擎类型
     * @see com.forgex.report.enums.ReportEngineType#UREPORT
     * @see com.forgex.report.enums.ReportEngineType#JIMU
     */
    private String engineType;
    
    /**
     * 分类 ID
     * @see ReportCategory#id
     */
    private Long categoryId;
    
    /**
     * 数据源 ID
     * @see ReportDatasource#id
     */
    private Long datasourceId;
    
    /**
     * 报表模板内容（XML 或 JSON 格式）
     */
    private String content;
    
    /**
     * 报表模板文件路径（用于 UReport2）
     */
    private String templatePath;
    
    /**
     * 报表状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 备注说明
     */
    private String remark;
}
