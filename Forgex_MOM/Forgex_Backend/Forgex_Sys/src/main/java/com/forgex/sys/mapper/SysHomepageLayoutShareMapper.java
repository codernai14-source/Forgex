package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysHomepageLayoutShare;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页布局分享码 Mapper。
 */
@Mapper
@DS("admin")
public interface SysHomepageLayoutShareMapper extends BaseMapper<SysHomepageLayoutShare> {
}
