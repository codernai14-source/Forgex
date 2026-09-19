package com.forgex.basic.process.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.process.domain.entity.BasicProcess;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工序主数据 Mapper。
 * <p>
 * 继承 MyBatis-Plus {@link BaseMapper} 提供工序实体的基础 CRUD 能力。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 * @see BaseMapper
 * @see BasicProcess
 */
@Mapper
public interface BasicProcessMapper extends BaseMapper<BasicProcess> {
}
