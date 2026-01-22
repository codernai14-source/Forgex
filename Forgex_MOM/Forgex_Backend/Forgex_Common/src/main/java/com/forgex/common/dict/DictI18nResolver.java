package com.forgex.common.dict;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.domain.entity.dict.SysDictNode;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.mapper.dict.SysDictNodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 字典国际化解析器
 * <p>
 * 负责根据当前语言环境解析字典值的多语言文本，支持Redis缓存以提高性能。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>根据字典节点路径和当前语言环境，获取字典子节点的标签映射</li>
 *   <li>支持多语言回退机制（优先使用当前语言，回退到中文，最后使用第一个可用语言）</li>
 *   <li>使用Redis缓存字典数据，缓存时长24小时</li>
 *   <li>支持从字典节点的国际化JSON字段解析多语言文本</li>
 * </ul>
 * <p><strong>语言回退顺序：</strong></p>
 * <ol>
 *   <li>当前语言（如"en-US"、"ja-JP"）</li>
 *   <li>当前语言的主语言部分（如"en"、"ja"）</li>
 *   <li>简体中文（"zh-CN"）</li>
 *   <li>中文（"zh"）</li>
 *   <li>第一个可用的语言值</li>
 * </ol>
 * <p><strong>缓存策略：</strong></p>
 * <ul>
 *   <li>缓存键格式：{@code dicti18n:{tenantId}:{lang}:{nodePath}}</li>
 *   <li>缓存时长：24小时</li>
 *   <li>缓存内容：字典值到国际化文本的映射</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DictI18n
 * @see SysDictNode
 */
@Service
@RequiredArgsConstructor
public class DictI18nResolver {
    /**
     * 字典节点Mapper
     */
    private final SysDictNodeMapper dictNodeMapper;

    /**
     * Redis字符串模板
     */
    private final StringRedisTemplate redis;

    /**
     * JSON序列化工具
     */
    private final ObjectMapper objectMapper;

    /**
     * 获取字典子节点的标签映射
     * <p>
     * 根据租户ID、字典节点路径和当前语言环境，
     * 获取字典子节点的字典值到国际化文本的映射。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>检查参数有效性（租户ID和节点路径不能为空）</li>
     *   <li>尝试从Redis缓存获取数据</li>
     *   <li>缓存未命中时，从数据库查询字典节点</li>
     *   <li>查询字典节点的所有启用状态的子节点</li>
     *   <li>解析每个子节点的国际化文本</li>
     *   <li>构建字典值到文本的映射</li>
     *   <li>将结果写入Redis缓存（24小时）</li>
     * </ol>
     * 
     * @param tenantId 租户ID
     * @param nodePath 字典节点路径
     * @return 字典值到国际化文本的映射，参数无效或节点不存在时返回空Map
     */
    public Map<String, String> getChildLabelMap(Long tenantId, String nodePath) {
        // 参数校验：租户ID和节点路径不能为空
        if (tenantId == null || !StringUtils.hasText(nodePath)) {
            return Collections.emptyMap();
        }

        // 获取当前语言环境
        String lang = LangContext.get();
        
        // 构建缓存键
        String cacheKey = "dicti18n:" + tenantId + ":" + lang + ":" + nodePath;
        
        try {
            // 尝试从Redis缓存获取
            String raw = redis.opsForValue().get(cacheKey);
            if (StringUtils.hasText(raw)) {
                return objectMapper.readValue(raw, new TypeReference<Map<String, String>>() {});
            }
        } catch (Exception ignored) {}

        // 缓存未命中，从数据库查询字典节点
        SysDictNode node = dictNodeMapper.selectOne(new LambdaQueryWrapper<SysDictNode>()
                .eq(SysDictNode::getNodePath, nodePath)
                .eq(SysDictNode::getTenantId, tenantId)
                .last("limit 1"));
        
        // 节点不存在，返回空Map
        if (node == null) {
            return Collections.emptyMap();
        }

        // 查询字典节点的所有启用状态的子节点
        List<SysDictNode> children = dictNodeMapper.selectList(new LambdaQueryWrapper<SysDictNode>()
                .eq(SysDictNode::getParentId, node.getId())
                .eq(SysDictNode::getStatus, 1)
                .orderByAsc(SysDictNode::getOrderNum));
        
        // 构建字典值到国际化文本的映射
        Map<String, String> map = new LinkedHashMap<>();
        for (SysDictNode c : children) {
            // 跳过字典值为空的节点
            if (!StringUtils.hasText(c.getDictValue())) {
                continue;
            }
            // 解析国际化文本并添加到映射
            map.put(c.getDictValue(), resolveI18nText(c.getDictValueI18nJson(), c.getDictName()));
        }
        
        try {
            // 将结果写入Redis缓存（24小时）
            String json = objectMapper.writeValueAsString(map);
            redis.opsForValue().set(cacheKey, json, 24, TimeUnit.HOURS);
        } catch (Exception ignored) {}
        
        return map;
    }

    /**
     * 获取字典子节点的标签映射（包含标签样式）
     * <p>
     * 根据租户ID、字典节点路径和当前语言环境，
     * 获取字典子节点的字典值到字典项（包含标签和样式）的映射。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>检查参数有效性（租户ID和节点路径不能为空）</li>
     *   <li>尝试从Redis缓存获取数据</li>
     *   <li>缓存未命中时，从数据库查询字典节点</li>
     *   <li>查询字典节点的所有启用状态的子节点</li>
     *   <li>解析每个子节点的国际化文本和标签样式</li>
     *   <li>构建字典值到字典项的映射</li>
     *   <li>将结果写入Redis缓存（24小时）</li>
     * </ol>
     * 
     * @param tenantId 租户ID
     * @param nodePath 字典节点路径
     * @return 字典值到字典项的映射，参数无效或节点不存在时返回空Map
     */
    public Map<String, DictItem> getChildItemMap(Long tenantId, String nodePath) {
        // 参数校验：租户ID和节点路径不能为空
        if (tenantId == null || !StringUtils.hasText(nodePath)) {
            return Collections.emptyMap();
        }

        // 获取当前语言环境
        String lang = LangContext.get();
        
        // 构建缓存键
        String cacheKey = "dictitem:" + tenantId + ":" + lang + ":" + nodePath;
        
        try {
            // 尝试从Redis缓存获取
            String raw = redis.opsForValue().get(cacheKey);
            if (StringUtils.hasText(raw)) {
                return objectMapper.readValue(raw, new TypeReference<Map<String, DictItem>>() {});
            }
        } catch (Exception ignored) {}

        // 缓存未命中，从数据库查询字典节点
        SysDictNode node = dictNodeMapper.selectOne(new LambdaQueryWrapper<SysDictNode>()
                .eq(SysDictNode::getNodePath, nodePath)
                .eq(SysDictNode::getTenantId, tenantId)
                .last("limit 1"));
        
        // 节点不存在，返回空Map
        if (node == null) {
            return Collections.emptyMap();
        }

        // 查询字典节点的所有启用状态的子节点
        List<SysDictNode> children = dictNodeMapper.selectList(new LambdaQueryWrapper<SysDictNode>()
                .eq(SysDictNode::getParentId, node.getId())
                .eq(SysDictNode::getStatus, 1)
                .orderByAsc(SysDictNode::getOrderNum));
        
        // 构建字典值到字典项的映射
        Map<String, DictItem> map = new LinkedHashMap<>();
        for (SysDictNode c : children) {
            // 跳过字典值为空的节点
            if (!StringUtils.hasText(c.getDictValue())) {
                continue;
            }
            // 解析国际化文本和标签样式
            String label = resolveI18nText(c.getDictValueI18nJson(), c.getDictName());
            DictItem.TagStyle tagStyle = parseTagStyle(c.getTagStyleJson());
            // 添加到映射
            map.put(c.getDictValue(), new DictItem(c.getDictValue(), label, tagStyle));
        }
        
        try {
            // 将结果写入Redis缓存（24小时）
            String json = objectMapper.writeValueAsString(map);
            redis.opsForValue().set(cacheKey, json, 24, TimeUnit.HOURS);
        } catch (Exception ignored) {}
        
        return map;
    }

    /**
     * 解析标签样式JSON
     * <p>
     * 从JSON字符串中解析标签样式信息。
     * </p>
     * 
     * @param tagStyleJson 标签样式JSON字符串
     * @return 标签样式对象，解析失败时返回null
     */
    private DictItem.TagStyle parseTagStyle(String tagStyleJson) {
        if (!StringUtils.hasText(tagStyleJson)) {
            return null;
        }
        
        try {
            JsonNode node = objectMapper.readTree(tagStyleJson);
            DictItem.TagStyle tagStyle = new DictItem.TagStyle();
            
            if (node.has("color")) {
                tagStyle.setColor(node.get("color").asText());
            }
            if (node.has("borderColor")) {
                tagStyle.setBorderColor(node.get("borderColor").asText());
            }
            if (node.has("backgroundColor")) {
                tagStyle.setBackgroundColor(node.get("backgroundColor").asText());
            }
            
            return tagStyle;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析国际化文本
     * <p>
     * 根据当前语言环境，从国际化JSON中解析对应的文本。
     * 支持语言回退机制，确保总能返回可用的文本。
     * </p>
     * <p><strong>语言回退顺序：</strong></p>
     * <ol>
     *   <li>当前语言（如"en-US"、"ja-JP"）</li>
     *   <li>当前语言的主语言部分（如"en"、"ja"）</li>
     *   <li>简体中文（"zh-CN"）</li>
     *   <li>中文（"zh"）</li>
     *   <li>第一个可用的语言值</li>
     * </ol>
     * 
     * @param i18nJson 国际化JSON字符串
     * @param fallback 回退文本，当所有语言都不可用时使用
     * @return 解析后的国际化文本，解析失败时返回回退文本
     */
    private String resolveI18nText(String i18nJson, String fallback) {
        // 国际化JSON为空，直接返回回退文本
        if (!StringUtils.hasText(i18nJson)) {
            return fallback;
        }
        
        try {
            // 解析JSON为树结构
            JsonNode node = objectMapper.readTree(i18nJson);
            
            // 获取当前语言环境
            String lang = LangContext.get();
            
            // 尝试获取当前语言的文本
            String val = getText(node, lang);
            if (StringUtils.hasText(val)) {
                return val;
            }
            
            // 尝试获取主语言的文本（如en-US -> en）
            if (StringUtils.hasText(lang)) {
                int idx = lang.indexOf('-');
                if (idx > 0) {
                    val = getText(node, lang.substring(0, idx));
                    if (StringUtils.hasText(val)) {
                        return val;
                    }
                }
            }
            
            // 回退到简体中文
            val = getText(node, "zh-CN");
            if (StringUtils.hasText(val)) {
                return val;
            }
            
            // 回退到中文
            val = getText(node, "zh");
            if (StringUtils.hasText(val)) {
                return val;
            }
            
            // 使用第一个可用的语言值
            if (node.isObject()) {
                java.util.Iterator<Map.Entry<String, JsonNode>> it = node.fields();
                if (it.hasNext()) {
                    JsonNode first = it.next().getValue();
                    if (first != null && first.isTextual() && StringUtils.hasText(first.asText())) {
                        return first.asText();
                    }
                }
            }
            
            // 所有尝试都失败，返回回退文本
            return fallback;
        } catch (Exception e) {
            // 解析异常，返回回退文本
            return fallback;
        }
    }

    /**
     * 从JSON节点中获取指定语言的文本
     * <p>
     * 根据语言键从JSON对象中获取对应的文本值。
     * </p>
     * 
     * @param node JSON节点对象
     * @param key 语言键（如"en-US"、"zh-CN"）
     * @return 对应语言的文本值，不存在或格式错误时返回null
     */
    private String getText(JsonNode node, String key) {
        // 参数校验
        if (node == null || !node.isObject() || !StringUtils.hasText(key)) {
            return null;
        }
        
        // 获取指定键的值
        JsonNode v = node.get(key);
        if (v != null && v.isTextual() && StringUtils.hasText(v.asText())) {
            return v.asText();
        }
        
        return null;
    }
}
