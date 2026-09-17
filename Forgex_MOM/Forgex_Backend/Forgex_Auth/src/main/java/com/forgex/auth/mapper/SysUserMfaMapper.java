package com.forgex.auth.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.auth.domain.entity.SysUserMfa;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 MFA 表 Mapper。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
@DS("admin")
public interface SysUserMfaMapper extends BaseMapper<SysUserMfa> {
}
