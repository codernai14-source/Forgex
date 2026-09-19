package com.forgex.auth.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.auth.domain.entity.SysSocialLogin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社交登录Mapper接口
 * <p>提供社交登录数据的数据库访问操作。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
@DS("admin")
public interface SysSocialLoginMapper extends BaseMapper<SysSocialLogin> {
}

