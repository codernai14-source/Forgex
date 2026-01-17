/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
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
     * 
     * @param key 配置键
     * @return 配置对象
     */
    @DS("common")
    @Select("SELECT id, config_key, config_value FROM sys_config WHERE deleted = 0 AND config_key = #{key}")
    SysConfig getByKey(@Param("key") String key);
    
    /**
     * 按键读取全局配置（tenant_id = 0）
     * 
     * @param key 配置键
     * @return 全局配置对象
     */
    @DS("common")
    @Select("SELECT id, config_key, config_value FROM sys_config WHERE deleted = 0 AND tenant_id = 0 AND config_key = #{key}")
    SysConfig getGlobalByKey(@Param("key") String key);
}
