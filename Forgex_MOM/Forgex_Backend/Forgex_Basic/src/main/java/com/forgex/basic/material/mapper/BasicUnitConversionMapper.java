package com.forgex.basic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.material.domain.entity.BasicUnitConversion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 计量单位换算关系 Mapper。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Mapper
public interface BasicUnitConversionMapper extends BaseMapper<BasicUnitConversion> {
}
