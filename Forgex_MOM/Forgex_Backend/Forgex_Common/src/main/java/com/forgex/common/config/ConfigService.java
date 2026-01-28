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
package com.forgex.common.config;

/**
 * 系统配置读取服务（通用）
 * 作用：提供统一的系统配置读取能力，支持按键从表 `sys_config` 读取不同类型的配置；
 * 支持将单键 JSON 的配置值一次性反序列化成实体对象，便于在系统内以强类型方式使用。
 */
public interface ConfigService {
    /**
     * 获取布尔配置
     * 逻辑：按键查询配置表 -> 若为空返回默认值 -> 将文本 'true'/'1' 解析为 true，其它为 false
     * @param key 配置键
     * @param def 默认值
     * @return 解析后的布尔值
     */
    boolean getBoolean(String key, boolean def);

    /**
     * 获取字符串配置
     * 逻辑：按键查询配置表 -> 若为空返回默认值 -> 返回文本值
     * @param key 配置键
     * @param def 默认值
     * @return 字符串值
     */
    String getString(String key, String def);

    /**
     * 获取整数配置
     * 逻辑：按键查询配置表 -> 若为空返回默认值 -> 尝试将文本解析为整数，失败则返回默认值
     * @param key 配置键
     * @param def 默认值
     * @return 整数值
     */
    int getInt(String key, int def);

    /**
     * 获取 JSON 配置并反序列化为实体对象
     * 逻辑：按键查询配置表 -> 若为空返回默认对象 -> 使用 JSON 反序列化为指定类型；解析失败返回默认对象
     * @param key 配置键（value 为 JSON）
     * @param type 目标实体类型
     * @param def 默认对象
     * @return 解析后的实体对象
     * @see cn.hutool.json.JSONUtil#toBean(String, Class)
     */
    <T> T getJson(String key, Class<T> type, T def);

    /**
     * 获取全局 JSON 配置并反序列化为实体对象
     * 逻辑：按键查询配置表（tenant_id = 0）-> 若为空返回默认对象 -> 使用 JSON 反序列化为指定类型；解析失败返回默认对象
     * @param key 配置键（value 为 JSON）
     * @param type 目标实体类型
     * @param def 默认对象
     * @return 解析后的全局配置实体对象
     * @see cn.hutool.json.JSONUtil#toBean(String, Class)
     */
    <T> T getGlobalJson(String key, Class<T> type, T def);

    void setJson(String key, Object value);
}
