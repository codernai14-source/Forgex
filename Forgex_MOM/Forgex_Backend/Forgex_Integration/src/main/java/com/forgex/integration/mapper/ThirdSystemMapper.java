package com.forgex.integration.mapper;

import com.github.yulichang.base.MPJBaseMapper;
import com.forgex.integration.domain.entity.ThirdSystem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 第三方系统 Mapper 接口
 * <p>
 * 负责第三方系统信息的数据访问，支持多租户和逻辑删除
 * 使用 MyBatis-Plus 的 LambdaQueryWrapper 进行查询，无需 XML 映射
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Mapper
public interface ThirdSystemMapper extends MPJBaseMapper<ThirdSystem> {

}
