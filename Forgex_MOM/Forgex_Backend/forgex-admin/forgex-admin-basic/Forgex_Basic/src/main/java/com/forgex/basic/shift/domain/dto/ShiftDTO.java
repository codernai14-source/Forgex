package com.forgex.basic.shift.domain.dto;

import com.forgex.basic.shift.domain.entity.BasicShift;
import com.forgex.basic.shift.domain.entity.BasicShiftPeriod;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 班次主数据 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftDTO extends BasicShift {

    /** 班次时段列表。 */
    private List<BasicShiftPeriod> periodList = new ArrayList<>();
}
