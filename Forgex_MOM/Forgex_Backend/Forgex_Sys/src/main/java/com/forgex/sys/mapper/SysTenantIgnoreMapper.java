package com.forgex.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysTenantIgnore;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

@Mapper
@DS("admin")
public interface SysTenantIgnoreMapper extends BaseMapper<SysTenantIgnore> {
}
