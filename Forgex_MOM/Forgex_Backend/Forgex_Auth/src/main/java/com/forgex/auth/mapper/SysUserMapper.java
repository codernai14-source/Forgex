package com.forgex.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.auth.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 用户表 Mapper
 * 提供对 sys_user 表的基础CRUD能力
 */
@Mapper
@DS("admin")
public interface SysUserMapper extends BaseMapper<SysUser> {
}
