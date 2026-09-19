package com.forgex.basic.material.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计量单位类型实体。
 * <p>
 * 对应 basic_unit_type，用于维护计量单位类型树结构。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_unit_type")
public class BasicUnitType extends BaseEntity {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 类型编码
     */
    @TableField("unit_type_code")
    private String unitTypeCode;

    /**
     * 类型名称
     */
    @TableField("unit_type_name")
    private String unitTypeName;

    /**
     * 父级 ID，根节点为 0
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 层级路径，格式如 0/父级ID/当前ID
     */
    @TableField("level_path")
    private String levelPath;
}
