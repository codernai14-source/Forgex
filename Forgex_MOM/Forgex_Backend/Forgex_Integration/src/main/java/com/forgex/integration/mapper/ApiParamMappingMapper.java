package com.forgex.integration.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.forgex.integration.domain.entity.ApiParamMapping;
import org.apache.ibatis.annotations.Mapper;

/**
 * 接口参数映射 Mapper 接口
 * <p>
 * 负责接口参数映射关系的数据访问
 * 使用 MyBatis-Plus LambdaQueryWrapper 进行查询，无需 XML 映射
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Mapper
public interface ApiParamMappingMapper extends MPJBaseMapper<ApiParamMapping> {

}
