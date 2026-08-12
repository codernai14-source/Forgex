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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.dynamic.datasource.annotation.DS;

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
    default SysConfig getByKey(String key) {
        // 构建查询条件
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        // 限定列，避免无效列导致SQL错误
        wrapper.select(SysConfig::getId, SysConfig::getConfigKey, SysConfig::getConfigValue);
        // 仅查询未删除的数据
        wrapper.eq(SysConfig::getDeleted, 0);
        // 按配置键查询
        wrapper.eq(SysConfig::getConfigKey, key);
        // 查询单条记录
        return this.selectOne(wrapper);
    }
    
    /**
     * 按键读取全局配置（tenant_id = 0）
     *
     * @param key 配置键
     * @return 全局配置对象
     */
    @DS("common")
    default SysConfig getGlobalByKey(String key) {
        // 构建查询条件
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        // 限定列，避免无效列导致SQL错误
        wrapper.select(SysConfig::getId, SysConfig::getConfigKey, SysConfig::getConfigValue, SysConfig::getTenantId);
        // 仅查询未删除的数据
        wrapper.eq(SysConfig::getDeleted, 0);
        // 限定全局租户
        wrapper.eq(SysConfig::getTenantId, 0L);
        // 按配置键查询
        wrapper.eq(SysConfig::getConfigKey, key);
        // 查询单条记录
        return this.selectOne(wrapper);
    }
    
    /**
     * 按键读取配置（不限租户，用于检查配置键是否已存在）
     *
     * @param key 配置键
     * @return 配置对象
     */
    @DS("common")
    default SysConfig getAnyByKey(String key) {
        // 构建查询条件
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        // 限定列，避免无效列导致SQL错误
        wrapper.select(SysConfig::getId, SysConfig::getTenantId, SysConfig::getConfigKey, SysConfig::getConfigValue);
        // 仅查询未删除的数据
        wrapper.eq(SysConfig::getDeleted, 0);
        // 按配置键查询
        wrapper.eq(SysConfig::getConfigKey, key);
        // 取第一条
        wrapper.last("limit 1");
        // 查询单条记录
        return this.selectOne(wrapper);
    }
}
