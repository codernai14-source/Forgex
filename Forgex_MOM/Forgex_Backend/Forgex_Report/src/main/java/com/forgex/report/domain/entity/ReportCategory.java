package com.forgex.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表分类实体类
 * <p>
 * 对应数据库表：fx_report_category
 * 用于对报表模板进行分类管理
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
@TableName("fx_report_category")
public class ReportCategory extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类编码
     */
    private String code;
    
    /**
     * 父分类 ID（0 表示根分类）
     */
    private Long parentId;
    
    /**
     * 排序号（数字越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 分类状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 备注说明
     */
    private String remark;
}
