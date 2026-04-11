package com.forgex.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表数据源实体类
 * <p>
 * 对应数据库表：fx_report_datasource
 * 用于配置报表使用的数据源连接信息
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see BaseEntity
 * @see com.forgex.report.domain.entity.ReportTemplate
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_report_datasource")
public class ReportDatasource extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 数据源名称
     */
    private String name;
    
    /**
     * 数据源编码（唯一标识）
     */
    private String code;
    
    /**
     * 数据源类型（mysql/oracle/sqlserver/postgresql/jdbc 等）
     */
    private String type;
    
    /**
     * JDBC 驱动类名
     */
    private String driverClass;
    
    /**
     * JDBC 连接 URL
     */
    private String url;
    
    /**
     * 数据库用户名
     */
    private String username;
    
    /**
     * 数据库密码（加密存储）
     */
    private String password;
    
    /**
     * 数据源状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 备注说明
     */
    private String remark;
}
