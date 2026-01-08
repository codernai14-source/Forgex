package com.forgex.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysUserProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户附属信息 Mapper
 */
@Mapper
public interface SysUserProfileMapper extends BaseMapper<SysUserProfile> {
}
