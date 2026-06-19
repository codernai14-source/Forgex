package com.forgex.basic.productionline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.productionline.domain.entity.BasicProductionLine;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产线主数据 Mapper。
 * <p>
 * 继承 MyBatis-Plus {@link BaseMapper} 提供产线实体的基础 CRUD 能力。
 * 复杂的多表关联查询可在对应的 XML 中按需扩展。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BaseMapper
 * @see BasicProductionLine
 */
@Mapper
public interface BasicProductionLineMapper extends BaseMapper<BasicProductionLine> {
}
