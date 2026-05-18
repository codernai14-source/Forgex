package com.forgex.basic.workcalendar.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 工作日历日期主数据。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_work_calendar_day")
public class BasicWorkCalendarDay extends BaseEntity {

    @TableField("calendar_date")
    private LocalDate calendarDate;

    @TableField("year_value")
    private Integer yearValue;

    @TableField("month_value")
    private Integer monthValue;

    @TableField("day_value")
    private Integer dayValue;

    @TableField("date_type")
    private Integer dateType;

    @TableField("holiday_name")
    private String holidayName;

    @TableField("custom_week")
    private String customWeek;

    @TableField("public_week")
    private String publicWeek;

    @TableField("is_holiday_synced")
    private Boolean holidaySynced;

    @TableField("holiday_source_year")
    private Integer holidaySourceYear;

    @TableField("remark")
    private String remark;
}
