package com.forgex.basic.workshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.workshop.domain.entity.BasicWorkshop;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车间主数据 Mapper。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Mapper
public interface BasicWorkshopMapper extends BaseMapper<BasicWorkshop> {
}
