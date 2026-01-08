package com.forgex.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 管理库租户表 Mapper
 */
@Mapper
@DS("admin")
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}
