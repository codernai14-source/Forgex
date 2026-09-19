package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysHomepageComponentConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页组件配置 Mapper。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Mapper
@DS("admin")
public interface SysHomepageComponentConfigMapper extends BaseMapper<SysHomepageComponentConfig> {
}
