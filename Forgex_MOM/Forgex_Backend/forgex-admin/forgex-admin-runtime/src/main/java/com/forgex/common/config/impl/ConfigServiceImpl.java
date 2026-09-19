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
package com.forgex.common.config.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.config.ConfigService;
import com.forgex.common.domain.entity.SysConfig;
import com.forgex.common.mapper.SysConfigMapper;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.dynamic.datasource.annotation.DS;


/**
 * 系统配置服务实现
 * 作用：统一从配置库读取系统配置（值为JSON或文本），并提供便捷方法。
 * 数据源：读取自 `forgex_common` 库，因此通过 @DS("common") 指定动态数据源。
 */
@Service
@DS("common")
public class ConfigServiceImpl implements ConfigService {
    /** 配置表 Mapper，负责 sys_config 基础CRUD */
    @Autowired
    private SysConfigMapper mapper;

    /**
     * 获取布尔配置
     * 逻辑：读取配置值，按 'true'/'1' 判断为 true，其它为 false
     * @param key 配置键
     * @param def 默认值
     * @return 布尔结果
     * @see com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
     */
    @Override
    public boolean getBoolean(String key, boolean def) {
        try { DynamicDataSourceContextHolder.push("common");
            SysConfig cfg = mapper.getByKey(key);
            if (cfg == null || cfg.getConfigValue() == null) return def;
            String v = cfg.getConfigValue().trim();
            return "true".equalsIgnoreCase(v) || "1".equals(v);
        } finally { DynamicDataSourceContextHolder.poll(); }
    }

    /**
     * 获取字符串配置
     * 逻辑：读取文本值，空值返回默认
     * @param key 配置键
     * @param def 默认值
     * @return 字符串
     */
    @Override
    public String getString(String key, String def) {
        try { DynamicDataSourceContextHolder.push("common");
            SysConfig cfg = mapper.getByKey(key);
            if (cfg == null || cfg.getConfigValue() == null) return def;
            return cfg.getConfigValue();
        } finally { DynamicDataSourceContextHolder.poll(); }
    }

    /**
     * 获取整数配置
     * 逻辑：尝试解析为整数，失败返回默认
     * @param key 配置键
     * @param def 默认值
     * @return 整数值
     */
    @Override
    public int getInt(String key, int def) {
        try { DynamicDataSourceContextHolder.push("common");
            SysConfig cfg = mapper.getByKey(key);
            if (cfg == null || cfg.getConfigValue() == null) return def;
            try {
                return Integer.parseInt(cfg.getConfigValue().trim());
            } catch (Exception e) {
                return def;
            }
        } finally { DynamicDataSourceContextHolder.poll(); }
    }

    /**
     * 获取 JSON 配置并反序列化为实体对象
     * 逻辑：读取文本 -> 解析为目标类型；异常时返回默认对象
     * @param key 配置键
     * @param type 目标类型
     * @param def 默认对象
     * @return 解析后的实体
     * @see cn.hutool.json.JSONUtil#toBean(String, Class)
     */
    @Override
    public <T> T getJson(String key, Class<T> type, T def) {
        try { DynamicDataSourceContextHolder.push("common");
            SysConfig cfg = mapper.getByKey(key);
            if (cfg == null || cfg.getConfigValue() == null) return def;
            try {
                return JSONUtil.toBean(cfg.getConfigValue(), type);
            } catch (Exception e) {
                return def;
            }
        } finally { DynamicDataSourceContextHolder.poll(); }
    }

    /**
     * 设置 JSON 配置（当前租户）
     * 逻辑：将对象序列化为 JSON 字符串，保存到当前租户的配置表中
     * @param key 配置键
     * @param value 配置对象
     */
    @Override
    public void setJson(String key, Object value) {
        try { DynamicDataSourceContextHolder.push("common");
            String json = JSONUtil.toJsonStr(value);
            SysConfig cfg = mapper.getByKey(key);
            if (cfg == null) {
                cfg = new SysConfig();
                cfg.setConfigKey(key);
                cfg.setConfigValue(json);
                mapper.insert(cfg);
            } else {
                cfg.setConfigValue(json);
                mapper.updateById(cfg);
            }
        } finally { DynamicDataSourceContextHolder.poll(); }
    }

    /**
     * 获取全局 JSON 配置并反序列化为实体对象
     * 逻辑：读取全局配置（tenant_id = 0）文本 -> 解析为目标类型；异常时返回默认对象
     * @param key 配置键
     * @param type 目标类型
     * @param def 默认对象
     * @return 解析后的实体
     * @see cn.hutool.json.JSONUtil#toBean(String, Class)
     * @see com.forgex.common.mapper.SysConfigMapper#getGlobalByKey
     */
    @Override
    public <T> T getGlobalJson(String key, Class<T> type, T def) {
        try { DynamicDataSourceContextHolder.push("common");
            SysConfig cfg = mapper.getGlobalByKey(key);
            if (cfg == null || cfg.getConfigValue() == null) return def;
            try {
                return JSONUtil.toBean(cfg.getConfigValue(), type);
            } catch (Exception e) {
                return def;
            }
        } finally { DynamicDataSourceContextHolder.poll(); }
    }

    /**
     * 设置全局 JSON 配置（tenant_id = 0）
     * 逻辑：将对象序列化为 JSON 字符串，保存到全局配置表中（tenant_id = 0）
     * @param key 配置键
     * @param value 配置对象
     */
    @Override
    public void setGlobalJson(String key, Object value) {
        try { DynamicDataSourceContextHolder.push("common");
            // 序列化配置值为JSON
            String json = JSONUtil.toJsonStr(value);
            // 优先查询全局配置
            SysConfig cfg = mapper.getGlobalByKey(key);
            if (cfg == null) {
                // 若全局配置不存在，则查询任意租户下是否已存在该键
                cfg = mapper.getAnyByKey(key);
            }
            if (cfg == null) {
                // 不存在则新增全局配置
                cfg = new SysConfig();
                cfg.setConfigKey(key);
                cfg.setConfigValue(json);
                cfg.setTenantId(0L);
                mapper.insert(cfg);
            } else {
                // 存在则更新为全局配置内容
                cfg.setConfigValue(json);
                cfg.setTenantId(0L);
                mapper.updateById(cfg);
            }
        } finally { DynamicDataSourceContextHolder.poll(); }
    }
}
