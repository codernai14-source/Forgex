package com.forgex.basic.shift.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班次主数据实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_shift")
public class BasicShift extends BaseEntity {

    /** 班次名称。 */
    @TableField("shift_name")
    private String shiftName;

    /** 班次编码。 */
    @TableField("shift_code")
    private String shiftCode;

    /** 是否启用。 */
    @TableField("status")
    private Boolean status;

    /** 备注。 */
    @TableField("remark")
    private String remark;
}
