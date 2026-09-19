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

    /** 顺序号。 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 所属工厂 ID。 */
    @TableField("factory_id")
    private Long factoryId;

    /**
     * 车间类型（字典：workshop_type）。
     * 取值示例：RAW=原材料车间、PROCESSING=加工车间、ASSEMBLY=装配车间、PACKAGING=包装车间、AUXILIARY=辅助车间。
     */
    @TableField("workshop_type")
    private String workshopType;

    /**
     * 车间负责人 ID（员工主键）。
     */
    @TableField("workshop_manager_id")
    private Long workshopManagerId;

    /**
     * 车间负责人姓名（创建时按快照写入）。
     */
    @TableField("workshop_manager_name")
    private String workshopManagerName;

    /** 是否启用。 */
    @TableField("status")
    private Boolean status;

    /** 备注。 */
    @TableField("remark")
    private String remark;
}
