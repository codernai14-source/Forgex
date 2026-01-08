package com.forgex.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 管理库用户表 Mapper
 */
@Mapper
@DS("admin")
public interface SysUserMapper extends BaseMapper<SysUser> {
}
