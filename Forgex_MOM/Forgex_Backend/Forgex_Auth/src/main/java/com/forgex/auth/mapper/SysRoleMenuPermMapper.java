package com.forgex.auth.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.auth.domain.entity.SysRoleMenuPerm;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色菜单授权 Mapper（仅用于权限计算）。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Mapper
@DS("admin")
public interface SysRoleMenuPermMapper extends BaseMapper<SysRoleMenuPerm> {
}

