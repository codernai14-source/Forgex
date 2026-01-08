package com.forgex.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.auth.domain.entity.SysTenantIgnore;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 租户隔离跳过配置表 Mapper
 */
@Mapper
@DS("admin")
public interface SysTenantIgnoreMapper extends BaseMapper<SysTenantIgnore> {
}
