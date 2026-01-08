package com.forgex.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.SysUserStyleConfig;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

/**
 * 用户页面样式配置表 Mapper。
 *
 * <p>数据源：指向 {@code forgex_common} 库。</p>
 *
 * @author Forgex
 * @version 1.0.0
 */
@Mapper
@DS("common")
public interface SysUserStyleConfigMapper extends BaseMapper<SysUserStyleConfig> {
}

