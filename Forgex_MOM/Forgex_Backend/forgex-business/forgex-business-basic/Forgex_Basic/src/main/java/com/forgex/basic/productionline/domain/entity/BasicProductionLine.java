package com.forgex.basic.productionline.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产线主数据实体。
 * <p>
 * 对应数据库表：basic_production_line
 * 用于存储产线基础信息、所属车间快照、产线类型、负责人信息等核心字段。
 * 车间编码 / 车间名称 / 负责人工号 / 负责人姓名 字段在保存时按快照写入，避免后续车间或人员信息变更影响历史产线数据。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_production_line")
public class BasicProductionLine extends BaseEntity {

    /** 产线编码（租户内唯一）。 */
    @TableField("production_line_code")
    private String productionLineCode;

    /** 产线名称。 */
    @TableField("production_line_name")
    private String productionLineName;

    /** 所属车间 ID。 */
    @TableField("workshop_id")
    private Long workshopId;

    /** 所属车间编码（创建时按快照写入）。 */
    @TableField("workshop_code")
    private String workshopCode;

    /** 所属车间名称（创建时按快照写入）。 */
    @TableField("workshop_name")
    private String workshopName;

    /**
     * 产线类型（字典：prod_line_type）。
     * 取值示例：ASSEMBLY=装配产线、WELDING=焊接产线、MACHINING=机加工产线、PAINTING=喷涂产线、PACKAGING=包装产线。
     */
    @TableField("production_line_type")
    private String productionLineType;

    /** 负责人 ID（员工主键）。 */
    @TableField("manager_employee_id")
    private Long managerEmployeeId;

    /** 负责人工号（创建时按快照写入）。 */
    @TableField("manager_employee_no")
    private String managerEmployeeNo;

    /** 负责人姓名（创建时按快照写入）。 */
    @TableField("manager_employee_name")
    private String managerEmployeeName;

    /** 排序号。 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 是否启用：0=禁用，1=启用。 */
    @TableField("status")
    private Integer status;

    /** 备注。 */
    @TableField("remark")
    private String remark;
}
