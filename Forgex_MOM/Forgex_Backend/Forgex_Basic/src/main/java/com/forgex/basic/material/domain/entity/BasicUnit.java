package com.forgex.basic.material.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计量单位主数据实体。
 * <p>
 * 对应 basic_unit，按计量单位类型维护单位编码、名称和备注。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_unit")
public class BasicUnit extends BaseEntity {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 计量单位类型 ID
     */
    @TableField("unit_type_id")
    private Long unitTypeId;

    /**
     * 计量单位编码
     */
    @TableField("unit_code")
    private String unitCode;

    /**
     * 计量单位名称
     */
    @TableField("unit_name")
    private String unitName;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
