package com.forgex.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置表 Mapper
 */
@Mapper
@DS("common")
public interface SysConfigMapper extends BaseMapper<SysConfig> {
    /**
     * 按键读取配置（限定列，避免无效列导致SQL错误）
     */
    @DS("common")
    @Select("SELECT id, config_key, config_value FROM sys_config WHERE deleted = 0 AND config_key = #{key}")
    SysConfig getByKey(@Param("key") String key);
}
