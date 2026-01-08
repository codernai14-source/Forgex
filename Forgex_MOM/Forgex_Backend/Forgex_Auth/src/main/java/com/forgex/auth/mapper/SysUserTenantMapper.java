package com.forgex.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.auth.domain.entity.SysUserTenant;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 用户-租户关联表 Mapper
 * 提供对 sys_user_tenant 表的基础CRUD能力
 */
@Mapper
@DS("admin")
public interface SysUserTenantMapper extends BaseMapper<SysUserTenant> {
}
