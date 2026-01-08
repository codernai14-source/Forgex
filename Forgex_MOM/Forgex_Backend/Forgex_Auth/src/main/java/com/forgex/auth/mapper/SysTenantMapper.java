package com.forgex.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.auth.domain.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 租户表 Mapper
 * 提供对 sys_tenant 表的基础CRUD能力
 */
@Mapper
@DS("admin")
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}

