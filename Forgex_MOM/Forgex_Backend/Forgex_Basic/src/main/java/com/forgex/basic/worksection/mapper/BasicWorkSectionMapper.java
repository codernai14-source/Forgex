package com.forgex.basic.worksection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.worksection.domain.entity.BasicWorkSection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工段主数据 Mapper。
 * <p>
 * 继承 MyBatis-Plus {@link BaseMapper} 提供工段实体的基础 CRUD 能力。
 * 复杂的多表关联查询可在对应的 XML 中按需扩展。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BaseMapper
 * @see BasicWorkSection
 */
@Mapper
public interface BasicWorkSectionMapper extends BaseMapper<BasicWorkSection> {
}
