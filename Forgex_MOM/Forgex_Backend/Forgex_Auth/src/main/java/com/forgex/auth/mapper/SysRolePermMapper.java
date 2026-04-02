package com.forgex.auth.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.auth.domain.entity.SysRolePerm;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("admin")
public interface SysRolePermMapper extends BaseMapper<SysRolePerm> {
}
