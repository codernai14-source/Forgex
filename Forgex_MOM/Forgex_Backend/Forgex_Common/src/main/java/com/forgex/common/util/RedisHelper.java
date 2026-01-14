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
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class RedisHelper {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 写入字符串值。
     *
     * @param key   key
     * @param value value
     * @param ttl   过期时间（可为空，表示不过期）
     */
    public void setString(String key, String value, Duration ttl) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        if (ttl == null) {
            redisTemplate.opsForValue().set(key, value);
            return;
        }
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 获取字符串值。
     *
     * @param key key
     * @return value
     */
    public String getString(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 写入对象（JSON 序列化）。
     *
     * @param key   key
     * @param value 对象
     * @param ttl   过期时间（可为空）
     */
    public void setJson(String key, Object value, Duration ttl) {
        if (!StringUtils.hasText(key) || value == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            setString(key, json, ttl);
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取对象（JSON 反序列化）。
     *
     * @param key   key
     * @param clazz 类型
     * @param <T>   泛型
     * @return 对象（失败返回 null）
     */
    public <T> T getJson(String key, Class<T> clazz) {
        String json = getString(key);
        if (!StringUtils.hasText(json) || clazz == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 删除 key。
     *
     * @param key key
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        return redisTemplate.delete(key);
    }

    /**
     * 判断 key 是否存在。
     *
     * @param key key
     * @return 是否存在
     */
    public Boolean exists(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        return redisTemplate.hasKey(key);
    }
}

