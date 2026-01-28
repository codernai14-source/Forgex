package com.forgex.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Redis 通用工具类。
 * <p>
 * 约定：统一使用字符串 Key，值可为字符串或 JSON 序列化对象。\n
 * 提供基础能力：get/set/del/ttl/exists。\n
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #setString(String, String, Duration)} - 写入字符串值，支持过期时间</li>
 *   <li>{@link #getString(String)} - 获取字符串值</li>
 *   <li>{@link #setJson(String, Object, Duration)} - 写入对象（JSON序列化）</li>
 *   <li>{@link #getJson(String, Class)} - 获取对象（JSON反序列化）</li>
 *   <li>{@link #delete(String)} - 删除key</li>
 *   <li>{@link #exists(String)} - 判断key是否存在</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 写入字符串
 * redisHelper.setString("user:123", "张三", Duration.ofHours(2));
 * 
 * // 获取字符串
 * String name = redisHelper.getString("user:123");
 * 
 * // 写入对象
 * User user = new User();
 * user.setName("李四");
 * redisHelper.setJson("user:456", user, Duration.ofDays(1));
 * 
 * // 获取对象
 * User cachedUser = redisHelper.getJson("user:456", User.class);
 * 
 * // 删除key
 * redisHelper.delete("user:123");
 * 
 * // 判断key是否存在
 * boolean exists = redisHelper.exists("user:123");
 * }</pre>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see org.springframework.data.redis.core.StringRedisTemplate
 * @see com.fasterxml.jackson.databind.ObjectMapper
 */
@Component
@RequiredArgsConstructor
public class RedisHelper {

    /**
     * StringRedisTemplate实例
     * <p>
     * 用于Redis的字符串操作
     * </p>
     */
    private final StringRedisTemplate redisTemplate;
    
    /**
     * ObjectMapper实例
     * <p>
     * 用于JSON序列化和反序列化
     * </p>
     */
    private final ObjectMapper objectMapper;

    /**
     * 写入字符串值。
     * <p>
     * 支持设置过期时间，若不设置过期时间则永久存储。
     * </p>
     * <p>使用说明：</p>
     * <ul>
     *   <li>key为空时直接返回，不执行任何操作</li>
     *   <li>ttl为空时使用永久存储</li>
     *   <li>ttl不为空时设置指定过期时间</li>
     * </ul>
     *
     * @param key   Redis键，不能为空
     * @param value 要存储的字符串值
     * @param ttl   过期时间，为null表示不过期
     */
    public void setString(String key, String value, Duration ttl) {
        // key为空时不执行操作
        if (!StringUtils.hasText(key)) {
            return;
        }
        // 不设置过期时间，永久存储
        if (ttl == null) {
            redisTemplate.opsForValue().set(key, value);
            return;
        }
        // 设置过期时间
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 获取字符串值。
     * <p>
     * 从Redis中获取指定key的字符串值。
     * </p>
     * <p>使用说明：</p>
     * <ul>
     *   <li>key为空时返回null</li>
     *   <li>key不存在时返回null</li>
     * </ul>
     *
     * @param key Redis键
     * @return 字符串值，key不存在或为空时返回null
     */
    public String getString(String key) {
        // key为空时直接返回null
        if (!StringUtils.hasText(key)) {
            return null;
        }
        // 从Redis获取值
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 写入对象（JSON 序列化）。
     * <p>
     * 将对象序列化为JSON字符串后存入Redis。
     * </p>
     * <p>使用说明：</p>
     * <ul>
     *   <li>key为空或value为空时不执行操作</li>
     *   <li>序列化失败时静默失败，不抛出异常</li>
     * </ul>
     *
     * @param key   Redis键
     * @param value 要存储的对象
     * @param ttl   过期时间，为null表示不过期
     */
    public void setJson(String key, Object value, Duration ttl) {
        // 参数校验：key或value为空时不执行操作
        if (!StringUtils.hasText(key) || value == null) {
            return;
        }
        try {
            // 将对象序列化为JSON字符串
            String json = objectMapper.writeValueAsString(value);
            // 调用setString方法存储，支持过期时间
            setString(key, json, ttl);
        } catch (Exception ignored) {
            // 序列化失败时静默失败，不抛出异常
        }
    }

    /**
     * 获取对象（JSON 反序列化）。
     * <p>
     * 从Redis获取JSON字符串并反序列化为指定类型的对象。
     * </p>
     * <p>使用说明：</p>
     * <ul>
     *   <li>key不存在时返回null</li>
     *   <li>clazz为空时返回null</li>
     *   <li>反序列化失败时返回null</li>
     * </ul>
     *
     * @param key   Redis键
     * @param clazz 目标类型
     * @param <T>   泛型类型
     * @return 反序列化后的对象，失败时返回null
     */
    public <T> T getJson(String key, Class<T> clazz) {
        // 从Redis获取JSON字符串
        String json = getString(key);
        // 参数校验：JSON为空或类型为空时返回null
        if (!StringUtils.hasText(json) || clazz == null) {
            return null;
        }
        try {
            // 将JSON字符串反序列化为指定类型对象
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            // 反序列化失败时返回null
            return null;
        }
    }

    /**
     * 删除 key。
     * <p>
     * 从Redis中删除指定的key。
     * </p>
     * <p>使用说明：</p>
     * <ul>
     *   <li>key为空时返回false</li>
     *   <li>key不存在时返回false</li>
     *   <li>删除成功时返回true</li>
     * </ul>
     *
     * @param key Redis键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        // key为空时返回false
        if (!StringUtils.hasText(key)) {
            return false;
        }
        // 删除key并返回结果
        return redisTemplate.delete(key);
    }

    /**
     * 判断 key 是否存在。
     * <p>
     * 检查Redis中是否存在指定的key。
     * </p>
     * <p>使用说明：</p>
     * <ul>
     *   <li>key为空时返回false</li>
     *   <li>key存在时返回true</li>
     *   <li>key不存在时返回false</li>
     * </ul>
     *
     * @param key Redis键
     * @return 是否存在
     */
    public Boolean exists(String key) {
        // key为空时返回false
        if (!StringUtils.hasText(key)) {
            return false;
        }
        // 检查key是否存在
        return redisTemplate.hasKey(key);
    }
}

