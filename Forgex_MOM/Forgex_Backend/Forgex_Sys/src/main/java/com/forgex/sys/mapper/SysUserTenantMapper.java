package com.forgex.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysUserTenant;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 用户-租户绑定 Mapper
 */
@Mapper
@DS("admin")
public interface SysUserTenantMapper extends BaseMapper<SysUserTenant> {
}
