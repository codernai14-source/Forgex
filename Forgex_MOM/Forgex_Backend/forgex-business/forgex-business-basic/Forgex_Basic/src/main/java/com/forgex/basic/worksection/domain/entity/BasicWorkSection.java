package com.forgex.basic.worksection.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工段主数据实体。
 * <p>
 * 对应数据库表：basic_work_section
 * 用于存储工段基础信息、所属车间与产线快照等核心字段。
 * 车间编码 / 车间名称 / 产线编码 / 产线名称 字段在保存时按快照写入，避免后续车间或产线信息变更影响历史工段数据。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_work_section")
public class BasicWorkSection extends BaseEntity {

    /** 工段编码（租户内唯一）。 */
    @TableField("work_section_code")
    private String workSectionCode;

    /** 工段名称。 */
    @TableField("work_section_name")
    private String workSectionName;

    /** 所属车间 ID。 */
    @TableField("workshop_id")
    private Long workshopId;

    /** 所属车间编码（创建时按快照写入）。 */
    @TableField("workshop_code")
    private String workshopCode;

    /** 所属车间名称（创建时按快照写入）。 */
    @TableField("workshop_name")
    private String workshopName;

    /** 所属产线 ID（可为空，工段可不归属产线）。 */
    @TableField("production_line_id")
    private Long productionLineId;

    /** 所属产线编码（创建时按快照写入，可为空）。 */
    @TableField("production_line_code")
    private String productionLineCode;

    /** 所属产线名称（创建时按快照写入，可为空）。 */
    @TableField("production_line_name")
    private String productionLineName;

    /** 顺序号。 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 是否启用：0=禁用，1=启用。 */
    @TableField("status")
    private Integer status;

    /** 备注。 */
    @TableField("remark")
    private String remark;
}
