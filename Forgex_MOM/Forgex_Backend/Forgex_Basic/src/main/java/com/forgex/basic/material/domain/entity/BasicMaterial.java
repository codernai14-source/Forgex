package com.forgex.basic.material.domain.entity;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物料管理主表实体类
 * <p>
 * 对应数据库表：basic_material
 * 用于存储物料基础信息，包含物料编码、名称、类型、分类等核心字段
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_material")
public class BasicMaterial extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物料编码（全局唯一，租户内唯一）
     */
    @TableField("material_code")
    private String materialCode;

    /**
     * 物料名称
     */
    @TableField("material_name")
    private String materialName;

    /**
     * 物料类型
     * RAW_MATERIAL=原材料，FINISHED_GOODS=成品，TOOL=工具，SEMI_FINISHED=半成品，OTHER=其它
     */
    @TableField("material_type")
    private String materialType;

    /**
     * 物料分类（字典编码，支持多级分类）
     */
    @TableField("material_category")
    private String materialCategory;

    /**
     * 规格型号
     */
    @TableField("specification")
    private String specification;

    /**
     * 计量单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 品牌
     */
    @TableField("brand")
    private String brand;

    /**
     * 物料图片 URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 详细描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 审批状态
     * NO_APPROVAL_REQUIRED=无需审批，PENDING=未审批，APPROVED=已审批，REJECTED=已驳回
     */
    @TableField("approval_status")
    private String approvalStatus;

}
