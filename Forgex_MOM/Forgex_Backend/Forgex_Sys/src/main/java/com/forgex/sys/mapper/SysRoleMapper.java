package com.forgex.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 角色表 Mapper
 */
@Mapper
@DS("admin")
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
