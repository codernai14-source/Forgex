package com.forgex.basic.shift.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 班次时段实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_shift_period")
public class BasicShiftPeriod extends BaseEntity {

    /** 班次 ID。 */
    @TableField("shift_id")
    private Long shiftId;

    /** 时间类型：WORK 工作，REST 休息。 */
    @TableField("time_type")
    private String timeType;

    /** 开始时间。 */
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    @TableField("start_time")
    private LocalTime startTime;

    /** 结束时间。 */
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    @TableField("end_time")
    private LocalTime endTime;

    /** 排序号。 */
    @TableField("sort_order")
    private Integer sortOrder;
}
