package com.forgex.basic.process.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序主数据实体。
 * <p>
 * 对应数据库表：basic_process
 * 用于存储工序基础信息、所属工段/产线/车间快照，工序类型、报工方式、质检触发点等核心字段。
 * 所属工段、产线、车间的编码和名称字段在保存时按快照写入，避免上级数据变更影响历史工序。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_process")
public class BasicProcess extends BaseEntity {

    /** 工序编码（租户内唯一）。 */
    @TableField("process_code")
    private String processCode;

    /** 工序名称。 */
    @TableField("process_name")
    private String processName;

    /** 所属工段 ID。 */
    @TableField("work_section_id")
    private Long workSectionId;

    /** 所属工段编码（创建时按快照写入）。 */
    @TableField("work_section_code")
    private String workSectionCode;

    /** 所属工段名称（创建时按快照写入）。 */
    @TableField("work_section_name")
    private String workSectionName;

    /** 所属产线 ID（冗余快照）。 */
    @TableField("production_line_id")
    private Long productionLineId;

    /** 所属产线编码（冗余快照）。 */
    @TableField("production_line_code")
    private String productionLineCode;

    /** 所属产线名称（冗余快照）。 */
    @TableField("production_line_name")
    private String productionLineName;

    /** 所属车间 ID（冗余快照）。 */
    @TableField("workshop_id")
    private Long workshopId;

    /** 所属车间编码（冗余快照）。 */
    @TableField("workshop_code")
    private String workshopCode;

    /** 所属车间名称（冗余快照）。 */
    @TableField("workshop_name")
    private String workshopName;

    /**
     * 工序类型（字典：process_type）。
     * 取值示例：PREPARATION=准备工序、PROCESSING=加工工序、ASSEMBLY=装配工序、INSPECTION=检验工序、PACKAGING=包装工序。
     */
    @TableField("process_type")
    private String processType;

    /**
     * 报工方式（字典：report_type）。
     * 取值示例：BY_QUANTITY=按件报工、BY_HOUR=按工时报工、BY_PROCESS=按工序报工、BY_BATCH=按批次报工。
     */
    @TableField("report_type")
    private String reportType;

    /**
     * 质检触发点（字典：qc_trigger_point）。
     * 取值示例：NONE=无触发、START=工序开始、END=工序结束、FIRST_PIECE=首件检验、PATROL=巡检。
     */
    @TableField("qc_trigger_point")
    private String qcTriggerPoint;

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
