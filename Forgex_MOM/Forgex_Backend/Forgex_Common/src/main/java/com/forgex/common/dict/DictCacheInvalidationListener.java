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
package com.forgex.common.dict;

import com.github.benmanes.caffeine.cache.Cache;
import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import com.forgex.common.service.i18n.I18nLanguageTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 字典缓存失效监听器
 * <p>
 * 监听Redis发布的字典缓存失效消息，清除本地缓存，确保多节点缓存一致性。
 * </p>
 * <p><strong>工作原理：</strong></p>
 * <ol>
 *   <li>字典数据更新时，发布Redis消息到 {@code dict:cache:invalidate} 主题</li>
 *   <li>所有应用节点监听该主题</li>
 *   <li>收到消息后，清除本地Caffeine缓存</li>
 *   <li>下次查询时重新从Redis或数据库加载</li>
 * </ol>
 * <p><strong>消息格式：</strong></p>
 * <pre>
 * tenantId:nodePath
 * 例如: 1:user.status
 * </pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.config.DictCacheConfig
 * @see com.forgex.common.dict.DictI18nResolver
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DictCacheInvalidationListener implements MessageListener {

    /**
     * 字典标签本地缓存
     */
    private final Cache<String, Map<String, String>> dictLabelCache;

    /**
     * 字典项本地缓存（包含样式）
     */
    private final Cache<String, Map<String, DictItem>> dictItemCache;

    private final I18nLanguageTypeService languageTypeService;

    /**
     * 处理缓存失效消息
     * <p>
     * 收到消息后，清除指定字典的所有语言版本的本地缓存。
     * </p>
     * <p><strong>处理流程：</strong></p>
     * <ol>
     *   <li>解析消息内容，提取租户ID和字典节点路径</li>
     *   <li>遍历所有支持的语言（zh-CN、en-US、ja-JP）</li>
     *   <li>构建缓存键并清除本地缓存</li>
     *   <li>记录日志</li>
     * </ol>
     *
     * @param message Redis消息对象
     * @param pattern 订阅的主题模式
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 解析消息内容
            String msg = new String(message.getBody());
            log.info("收到字典缓存失效消息: {}", msg);

            // 消息格式: tenantId:nodePath
            String[] parts = msg.split(":", 2);
            if (parts.length >= 2) {
                Long tenantId = Long.parseLong(parts[0]);
                String nodePath = parts[1];

                // 清除所有语言的本地缓存
                for (String lang : resolveEnabledLanguageCodes()) {
                    String cacheKey = tenantId + ":" + lang + ":" + nodePath;
                    dictLabelCache.invalidate(cacheKey);
                    dictItemCache.invalidate(cacheKey);
                }

                log.info("本地缓存已清除: tenantId={}, nodePath={}", tenantId, nodePath);
            } else {
                log.warn("缓存失效消息格式错误: {}", msg);
            }
        } catch (Exception e) {
            log.error("处理缓存失效消息失败", e);
        }
    }
    private List<String> resolveEnabledLanguageCodes() {
        LinkedHashSet<String> languageCodes = new LinkedHashSet<>();
        try {
            List<FxI18nLanguageType> enabledLanguages = languageTypeService.listEnabled();
            if (enabledLanguages != null) {
                enabledLanguages.stream()
                        .map(FxI18nLanguageType::getLangCode)
                        .filter(StringUtils::hasText)
                        .forEach(languageCodes::add);
            }
        } catch (Exception ex) {
            log.warn("加载启用语言列表失败，使用默认语言兜底", ex);
        }
        languageCodes.add("zh-CN");
        languageCodes.add("en-US");
        return new ArrayList<>(languageCodes);
    }
}

