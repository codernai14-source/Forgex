package com.forgex.basic.shift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.shift.domain.entity.BasicShift;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班次主数据 Mapper。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Mapper
public interface BasicShiftMapper extends BaseMapper<BasicShift> {
}
