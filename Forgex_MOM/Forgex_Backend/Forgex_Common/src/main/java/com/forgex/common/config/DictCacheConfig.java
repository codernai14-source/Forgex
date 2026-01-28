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

import com.forgex.common.dict.DictCacheInvalidationListener;
import com.forgex.common.dict.DictItem;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 字典缓存配置类
 * <p>
 * 配置字典数据的本地缓存（Caffeine）和Redis消息监听，实现三级缓存架构。
 * </p>
 * <p><strong>缓存架构：</strong></p>
 * <ul>
 *   <li>L1: 本地缓存（Caffeine）- 30秒过期，最多缓存10000个字典</li>
 *   <li>L2: Redis缓存 - 24小时过期</li>
 *   <li>L3: 数据库</li>
 * </ul>
 * <p><strong>缓存失效机制：</strong></p>
 * <ul>
 *   <li>字典更新时发布Redis消息</li>
 *   <li>所有节点监听消息并清除本地缓存</li>
 *   <li>确保多节点缓存一致性</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.dict.DictI18nResolver
 * @see com.forgex.common.dict.DictCacheInvalidationListener
 */
@Configuration
public class DictCacheConfig {
    
    /**
     * 字典标签本地缓存
     * <p>
     * 缓存字典值到国际化标签的映射关系。
     * </p>
     * <p><strong>缓存配置：</strong></p>
     * <ul>
     *   <li>缓存键格式: {@code tenantId:lang:nodePath}</li>
     *   <li>缓存值类型: {@code Map<String, String>} (字典值 -> 标签)</li>
     *   <li>最大容量: 10000个字典</li>
     *   <li>过期时间: 写入后30秒</li>
     *   <li>统计信息: 启用（用于监控缓存命中率）</li>
     * </ul>
     *
     * @return Caffeine缓存实例
     */
    @Bean("dictLabelCache")
    public Cache<String, Map<String, String>> dictLabelCache() {
        return Caffeine.newBuilder()
                // 最多缓存10000个字典
                .maximumSize(10000)
                // 写入后30秒过期，避免数据过期
                .expireAfterWrite(30, TimeUnit.SECONDS)
                // 记录统计信息（命中率、加载时间等）
                .recordStats()
                .build();
    }
    
    /**
     * 字典项本地缓存（包含样式）
     * <p>
     * 缓存字典值到字典项（包含标签和样式）的映射关系。
     * </p>
     * <p><strong>缓存配置：</strong></p>
     * <ul>
     *   <li>缓存键格式: {@code tenantId:lang:nodePath}</li>
     *   <li>缓存值类型: {@code Map<String, DictItem>} (字典值 -> 字典项)</li>
     *   <li>最大容量: 10000个字典</li>
     *   <li>过期时间: 写入后30秒</li>
     *   <li>统计信息: 启用</li>
     * </ul>
     *
     * @return Caffeine缓存实例
     */
    @Bean("dictItemCache")
    public Cache<String, Map<String, DictItem>> dictItemCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .recordStats()
                .build();
    }
    
    /**
     * Redis消息监听容器
     * <p>
     * 监听字典缓存失效消息，实现多节点缓存同步。
     * </p>
     * <p><strong>监听配置：</strong></p>
     * <ul>
     *   <li>监听主题: {@code dict:cache:invalidate}</li>
     *   <li>消息格式: {@code tenantId:nodePath}</li>
     *   <li>处理逻辑: 清除本地缓存和Redis缓存</li>
     * </ul>
     *
     * @param connectionFactory Redis连接工厂
     * @param listener 缓存失效监听器
     * @return Redis消息监听容器
     * @see DictCacheInvalidationListener
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            DictCacheInvalidationListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 监听字典缓存失效消息
        container.addMessageListener(listener, new PatternTopic("dict:cache:invalidate"));
        return container;
    }
}

