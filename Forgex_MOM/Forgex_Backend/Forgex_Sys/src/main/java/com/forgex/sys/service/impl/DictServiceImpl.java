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
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.forgex.common.i18n.LangContext;
import com.forgex.sys.domain.dto.DictDTO;
import com.forgex.sys.domain.entity.SysDict;
import com.forgex.sys.domain.vo.DictItemVO;
import com.forgex.sys.domain.vo.DictTreeVO;
import com.forgex.sys.domain.vo.TagStyleVO;
import com.forgex.sys.mapper.SysDictMapper;
import com.forgex.sys.service.IDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据字典服务实现
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 * @version 1.1.0
 */
@Slf4j
@Service
public class DictServiceImpl implements IDictService {
    
    @Autowired
    private SysDictMapper dictMapper;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 获取字典树
     * <p>
     * 查询指定租户下的所有字典数据，并构建成树形结构。
     * 只返回未删除的字典数据，按排序号升序排列。
     * </p>
     * 
     * @param tenantId 租户ID
     * @return 字典树列表
     */
    @Override
    public List<DictTreeVO> getDictTree(Long tenantId) {
        // 查询所有字典（未删除）
        List<SysDict> allDicts = dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDeleted, false)
                .orderByAsc(SysDict::getOrderNum));
        
        // 转换为 VO
        List<DictTreeVO> allVos = allDicts.stream().map(dict -> {
            DictTreeVO vo = new DictTreeVO();
            BeanUtils.copyProperties(dict, vo);
            return vo;
        }).collect(Collectors.toList());
        
        // 构建树形结构
        return buildTree(allVos, 0L);
    }

    /**
     * 根据字典编码获取字典项列表（带缓存）
     * <p>
     * 先从 Redis 缓存读取，如果缓存不存在则从数据库查询并写入缓存。
     * 缓存过期时间为 24 小时。
     * 只返回启用状态的字典项，按排序号升序排列。
     * </p>
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     * @return 字典项列表
     */
    @Override
    public List<DictItemVO> getDictItemsByCode(String dictCode, Long tenantId) {
        String lang = LangContext.get();
        // 先从缓存读取
        String cacheKey = String.format("dict:%d:%s:code:%s", tenantId, lang, dictCode);
        try {
            String cacheValue = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cacheValue)) {
                log.debug("从缓存读取字典：{}", cacheKey);
                return objectMapper.readValue(cacheValue, new TypeReference<List<DictItemVO>>() {});
            }
        } catch (Exception e) {
            log.warn("读取字典缓存失败：{}", cacheKey, e);
        }
        
        // 缓存不存在，从数据库查询
        // 1. 查询字典类型
        SysDict dictType = dictMapper.selectOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictCode, dictCode)
                .eq(SysDict::getParentId, 0L)
                .eq(SysDict::getDeleted, false)
                .last("limit 1"));
        
        if (dictType == null) {
            log.warn("字典类型不存在：{}", dictCode);
            return new ArrayList<>();
        }
        
        // 2. 查询字典项
        List<SysDict> dictItems = dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getParentId, dictType.getId())
                .eq(SysDict::getDeleted, false)
                .eq(SysDict::getStatus, 1) // 只查询启用的
                .orderByAsc(SysDict::getOrderNum));
        
        // 3. 转换为 VO
        List<DictItemVO> items = dictItems.stream().map(dict -> {
            DictItemVO vo = new DictItemVO();
            vo.setLabel(resolveI18nText(dict.getDictValueI18nJson(), dict.getDictName()));
            vo.setValue(dict.getDictValue());
            vo.setTagStyle(parseTagStyle(dict.getTagStyleJson()));
            return vo;
        }).collect(Collectors.toList());
        
        // 4. 写入缓存
        try {
            String jsonValue = objectMapper.writeValueAsString(items);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, 24, TimeUnit.HOURS);
            log.debug("写入字典缓存：{}", cacheKey);
        } catch (Exception e) {
            log.warn("写入字典缓存失败：{}", cacheKey, e);
        }
        
        return items;
    }

    @Override
    public List<DictItemVO> getDictItemsByPath(String nodePath, Long tenantId) {
        String lang = LangContext.get();
        String cacheKey = String.format("dict:%d:%s:path:%s", tenantId, lang, nodePath);
        try {
            String cacheValue = redisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cacheValue)) {
                return objectMapper.readValue(cacheValue, new TypeReference<List<DictItemVO>>() {});
            }
        } catch (Exception e) {
            log.warn("读取字典缓存失败：{}", cacheKey, e);
        }

        SysDict node = dictMapper.selectOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDeleted, false)
                .eq(SysDict::getNodePath, nodePath)
                .last("limit 1"));
        if (node == null) {
            return new ArrayList<>();
        }
        List<SysDict> dictItems = dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getParentId, node.getId())
                .eq(SysDict::getDeleted, false)
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getOrderNum));
        List<DictItemVO> items = dictItems.stream().map(dict -> {
            DictItemVO vo = new DictItemVO();
            vo.setLabel(resolveI18nText(dict.getDictValueI18nJson(), dict.getDictName()));
            vo.setValue(dict.getDictValue());
            vo.setTagStyle(parseTagStyle(dict.getTagStyleJson()));
            return vo;
        }).collect(Collectors.toList());

        try {
            String jsonValue = objectMapper.writeValueAsString(items);
            redisTemplate.opsForValue().set(cacheKey, jsonValue, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入字典缓存失败：{}", cacheKey, e);
        }
        return items;
    }
    
    /**
     * 新增字典
     * <p>
     * 创建新的字典类型或字典项。
     * 新增后会自动清除相关缓存。
     * </p>
     * 
     * @param dictDTO 字典数据传输对象
     */
    @Override
    public void addDict(DictDTO dictDTO) {
        Long tenantId = dictDTO.getTenantId();
        SysDict dict = new SysDict();
        BeanUtils.copyProperties(dictDTO, dict);
        dict.setCreateTime(LocalDateTime.now());
        dict.setUpdateTime(LocalDateTime.now());
        dict.setDeleted(false);

        if (!StringUtils.hasText(dict.getDictCode())) {
            if (StringUtils.hasText(dict.getDictValue())) {
                dict.setDictCode(dict.getDictValue());
            } else if (StringUtils.hasText(dict.getDictName())) {
                dict.setDictCode(dict.getDictName());
            }
        }

        SysDict parent = null;
        if (dict.getParentId() != null && dict.getParentId() > 0) {
            parent = dictMapper.selectById(dict.getParentId());
            if (parent == null || Boolean.TRUE.equals(parent.getDeleted())) {
                throw new RuntimeException("父节点不存在");
            }
        }
        String nodePath = parent == null ? dict.getDictCode() : parent.getNodePath() + "/" + dict.getDictCode();
        dict.setNodePath(nodePath);
        dict.setLevel(parent == null ? 1 : (parent.getLevel() == null ? 2 : parent.getLevel() + 1));
        dict.setChildrenCount(0);
        
        dictMapper.insert(dict);

        if (parent != null) {
            dictMapper.update(null, new LambdaUpdateWrapper<SysDict>()
                    .eq(SysDict::getId, parent.getId())
                    .setSql("children_count = children_count + 1")
                    .set(SysDict::getUpdateTime, LocalDateTime.now()));
        }

        clearDictCache(null, tenantId);
        
    }
    
    /**
     * 更新字典
     * <p>
     * 更新字典类型或字典项的信息。
     * 更新后会自动清除相关缓存。
     * </p>
     * 
     * @param dictDTO 字典数据传输对象
     * @throws RuntimeException 如果字典ID为空
     */
    @Override
    public void updateDict(DictDTO dictDTO) {
        if (dictDTO.getId() == null) {
            throw new RuntimeException("字典ID不能为空");
        }

        SysDict old = dictMapper.selectById(dictDTO.getId());
        if (old == null || Boolean.TRUE.equals(old.getDeleted())) {
            throw new RuntimeException("字典不存在");
        }

        SysDict dict = new SysDict();
        BeanUtils.copyProperties(dictDTO, dict);
        dict.setUpdateTime(LocalDateTime.now());

        if (!StringUtils.hasText(dict.getDictCode())) {
            dict.setDictCode(old.getDictCode());
        }
        if (dict.getParentId() == null) {
            dict.setParentId(old.getParentId());
        }

        SysDict newParent = null;
        if (dict.getParentId() != null && dict.getParentId() > 0) {
            newParent = dictMapper.selectById(dict.getParentId());
            if (newParent == null || Boolean.TRUE.equals(newParent.getDeleted())) {
                throw new RuntimeException("父节点不存在");
            }
        }

        String newPath = newParent == null ? dict.getDictCode() : newParent.getNodePath() + "/" + dict.getDictCode();
        Integer newLevel = newParent == null ? 1 : (newParent.getLevel() == null ? 2 : newParent.getLevel() + 1);

        boolean pathChanged = old.getNodePath() != null && !old.getNodePath().equals(newPath);
        boolean parentChanged = old.getParentId() != null && !old.getParentId().equals(dict.getParentId());

        dict.setNodePath(newPath);
        dict.setLevel(newLevel);

        dictMapper.updateById(dict);

        if (pathChanged) {
            List<SysDict> descendants = dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                    .eq(SysDict::getTenantId, old.getTenantId())
                    .eq(SysDict::getDeleted, false)
                    .likeRight(SysDict::getNodePath, old.getNodePath() + "/"));
            for (SysDict d : descendants) {
                String suffix = d.getNodePath().substring(old.getNodePath().length());
                SysDict u = new SysDict();
                u.setId(d.getId());
                u.setNodePath(newPath + suffix);
                if (d.getLevel() != null && old.getLevel() != null) {
                    u.setLevel(d.getLevel() - old.getLevel() + newLevel);
                }
                u.setUpdateTime(LocalDateTime.now());
                dictMapper.updateById(u);
            }
        }

        if (parentChanged) {
            if (old.getParentId() != null && old.getParentId() > 0) {
                dictMapper.update(null, new LambdaUpdateWrapper<SysDict>()
                        .eq(SysDict::getId, old.getParentId())
                        .setSql("children_count = GREATEST(children_count - 1, 0)")
                        .set(SysDict::getUpdateTime, LocalDateTime.now()));
            }
            if (dict.getParentId() != null && dict.getParentId() > 0) {
                dictMapper.update(null, new LambdaUpdateWrapper<SysDict>()
                        .eq(SysDict::getId, dict.getParentId())
                        .setSql("children_count = children_count + 1")
                        .set(SysDict::getUpdateTime, LocalDateTime.now()));
            }
        }

        clearDictCache(null, old.getTenantId());
    }
    
    /**
     * 删除字典（逻辑删除）
     * <p>
     * 执行逻辑删除操作，不会物理删除数据。
     * 删除前会检查是否存在子节点，如果存在则不允许删除。
     * 删除后会自动清除相关缓存。
     * </p>
     * 
     * @param id 字典ID
     * @throws RuntimeException 如果字典ID为空或存在子节点
     */
    @Override
    public void deleteDict(Long id) {
        if (id == null) {
            throw new RuntimeException("字典ID不能为空");
        }
        
        SysDict old = dictMapper.selectById(id);
        if (old == null || Boolean.TRUE.equals(old.getDeleted())) {
            throw new RuntimeException("字典不存在");
        }

        Long childCount = dictMapper.selectCount(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getParentId, id)
                .eq(SysDict::getDeleted, false));
        
        if (childCount > 0) {
            throw new RuntimeException("该字典下存在子节点，无法删除");
        }
        
        // 逻辑删除
        SysDict dict = new SysDict();
        dict.setId(id);
        dict.setDeleted(true);
        dict.setUpdateTime(LocalDateTime.now());
        
        dictMapper.updateById(dict);

        if (old.getParentId() != null && old.getParentId() > 0) {
            dictMapper.update(null, new LambdaUpdateWrapper<SysDict>()
                    .eq(SysDict::getId, old.getParentId())
                    .setSql("children_count = GREATEST(children_count - 1, 0)")
                    .set(SysDict::getUpdateTime, LocalDateTime.now()));
        }

        clearDictCache(null, old.getTenantId());
    }
    
    /**
     * 清除字典缓存
     */
    @Override
    public void clearDictCache(String dictCode, Long tenantId) {
        try {
            String prefix = "dict:" + tenantId + ":";
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("清除字典缓存失败：tenantId={}", tenantId, e);
        }
    }
    
    /**
     * 构建树形结构
     */
    private List<DictTreeVO> buildTree(List<DictTreeVO> allNodes, Long parentId) {
        List<DictTreeVO> result = new ArrayList<>();
        
        for (DictTreeVO node : allNodes) {
            if (parentId.equals(node.getParentId())) {
                // 递归查找子节点
                List<DictTreeVO> children = buildTree(allNodes, node.getId());
                node.setChildren(children);
                result.add(node);
            }
        }
        
        return result;
    }

    private String resolveI18nText(String i18nJson, String fallback) {
        if (!StringUtils.hasText(i18nJson)) {
            return fallback;
        }
        try {
            JsonNode node = objectMapper.readTree(i18nJson);
            String lang = LangContext.get();
            String v = getText(node, lang);
            if (StringUtils.hasText(v)) {
                return v;
            }
            if (StringUtils.hasText(lang)) {
                int idx = lang.indexOf('-');
                if (idx > 0) {
                    v = getText(node, lang.substring(0, idx));
                    if (StringUtils.hasText(v)) {
                        return v;
                    }
                }
            }
            v = getText(node, "zh-CN");
            if (StringUtils.hasText(v)) {
                return v;
            }
            v = getText(node, "zh");
            if (StringUtils.hasText(v)) {
                return v;
            }
            if (node.isObject()) {
                java.util.Iterator<java.util.Map.Entry<String, JsonNode>> it = node.fields();
                if (it.hasNext()) {
                    JsonNode first = it.next().getValue();
                    if (first != null && first.isTextual() && StringUtils.hasText(first.asText())) {
                        return first.asText();
                    }
                }
            }
            return fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private String getText(JsonNode node, String key) {
        if (node == null || !node.isObject() || !StringUtils.hasText(key)) {
            return null;
        }
        JsonNode v = node.get(key);
        if (v != null && v.isTextual() && StringUtils.hasText(v.asText())) {
            return v.asText();
        }
        return null;
    }

    /**
     * 解析标签样式配置JSON
     * <p>
     * 将JSON格式的标签样式配置解析为TagStyleVO对象
     * 如果JSON为空或解析失败，返回null
     * </p>
     * 
     * @param tagStyleJson 标签样式配置JSON，格式：{"color":"success","icon":"CheckCircleOutlined"}
     * @return TagStyleVO对象，解析失败返回null
     * @see TagStyleVO
     */
    private TagStyleVO parseTagStyle(String tagStyleJson) {
        if (!StringUtils.hasText(tagStyleJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(tagStyleJson, TagStyleVO.class);
        } catch (Exception e) {
            log.warn("解析标签样式配置失败：{}", tagStyleJson, e);
            return null;
        }
    }
}
