package com.forgex.basic.workshop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车间主数据实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_workshop")
public class BasicWorkshop extends BaseEntity {

    /** 车间编码。 */
    @TableField("workshop_code")
    private String workshopCode;

    /** 车间名称。 */
    @TableField("workshop_name")
    private String workshopName;

    /** 所属工厂 ID。 */
    @TableField("factory_id")
    private Long factoryId;

    /** 是否启用。 */
    @TableField("status")
    private Boolean status;

    /** 备注。 */
    @TableField("remark")
    private String remark;
}
