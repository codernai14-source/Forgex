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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.i18n.LangContext;
import com.forgex.sys.domain.dto.DictDTO;
import com.forgex.sys.domain.entity.SysDict;
import com.forgex.sys.domain.entity.SysModule;
import com.forgex.sys.domain.param.DictPageParam;
import com.forgex.sys.domain.vo.DictItemVO;
import com.forgex.sys.domain.vo.DictTreeVO;
import com.forgex.sys.domain.vo.TagStyleVO;
import com.forgex.sys.mapper.SysDictMapper;
import com.forgex.sys.mapper.SysModuleMapper;
import com.forgex.sys.service.IDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Autowired
    private SysModuleMapper moduleMapper;

    @Override
    public List<DictTreeVO> getDictTree(Long tenantId) {
        return buildTree(listTenantDictVos(tenantId), 0L);
    }

    @Override
    public IPage<DictTreeVO> pageDictTree(Long tenantId, DictPageParam pageParam) {
        DictPageParam query = pageParam == null ? new DictPageParam() : pageParam;
        List<DictTreeVO> roots = getDictTree(tenantId).stream()
                .filter(root -> matchRootNode(root, query))
                .collect(Collectors.toList());
        long safePageNum = query.getPageNum() == null || query.getPageNum() <= 0 ? 1 : query.getPageNum();
        long safePageSize = query.getPageSize() == null || query.getPageSize() <= 0 ? 20 : query.getPageSize();

        Page<DictTreeVO> page = new Page<>(safePageNum, safePageSize);
        page.setTotal(roots.size());

        long fromIndex = Math.max((safePageNum - 1) * safePageSize, 0);
        if (fromIndex >= roots.size()) {
            page.setRecords(new ArrayList<>());
            return page;
        }

        long toIndex = Math.min(fromIndex + safePageSize, roots.size());
        page.setRecords(new ArrayList<>(roots.subList((int) fromIndex, (int) toIndex)));
        return page;
    }

    @Override
    public List<DictItemVO> getDictItemsByCode(String dictCode, Long tenantId) {
        String lang = LangContext.get();
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

        SysDict dictType = dictMapper.selectOne(new LambdaQueryWrapper<SysDict>()
                .eq(tenantId != null, SysDict::getTenantId, tenantId)
                .eq(SysDict::getDeleted, false)
                .eq(SysDict::getDictCode, dictCode)
                .eq(SysDict::getParentId, 0L)
                .last("limit 1"));
        if (dictType == null) {
            return new ArrayList<>();
        }

        List<SysDict> dictItems = dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(tenantId != null, SysDict::getTenantId, tenantId)
                .eq(SysDict::getDeleted, false)
                .eq(SysDict::getParentId, dictType.getId())
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getOrderNum)
                .orderByAsc(SysDict::getId));

        List<DictItemVO> items = new ArrayList<>();
        for (SysDict dict : dictItems) {
            DictItemVO vo = new DictItemVO();
            vo.setLabel(resolveI18nText(dict.getDictValueI18nJson(), dict.getDictName()));
            vo.setValue(dict.getDictValue());
            vo.setTagStyle(parseTagStyle(dict.getTagStyleJson()));
            items.add(vo);
        }

        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(items), 24, TimeUnit.HOURS);
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
                .eq(tenantId != null, SysDict::getTenantId, tenantId)
                .eq(SysDict::getDeleted, false)
                .eq(SysDict::getNodePath, nodePath)
                .last("limit 1"));
        if (node == null) {
            return new ArrayList<>();
        }

        List<SysDict> dictItems = dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(tenantId != null, SysDict::getTenantId, tenantId)
                .eq(SysDict::getDeleted, false)
                .eq(SysDict::getParentId, node.getId())
                .eq(SysDict::getStatus, 1)
                .orderByAsc(SysDict::getOrderNum)
                .orderByAsc(SysDict::getId));

        List<DictItemVO> items = new ArrayList<>();
        for (SysDict dict : dictItems) {
            DictItemVO vo = new DictItemVO();
            vo.setLabel(resolveI18nText(dict.getDictValueI18nJson(), dict.getDictName()));
            vo.setValue(dict.getDictValue());
            vo.setTagStyle(parseTagStyle(dict.getTagStyleJson()));
            items.add(vo);
        }

        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(items), 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入字典缓存失败：{}", cacheKey, e);
        }
        return items;
    }

    @Override
    public void addDict(DictDTO dictDTO) {
        Long tenantId = dictDTO.getTenantId();
        SysDict dict = new SysDict();
        BeanUtils.copyProperties(dictDTO, dict);
        dict.setTenantId(tenantId);
        dict.setDeleted(false);

        Long parentId = normalizeParentId(dict.getParentId());
        dict.setParentId(parentId);

        if (!StringUtils.hasText(dict.getDictCode())) {
            if (StringUtils.hasText(dict.getDictValue())) {
                dict.setDictCode(dict.getDictValue());
            } else if (StringUtils.hasText(dict.getDictName())) {
                dict.setDictCode(dict.getDictName());
            }
        }

        SysDict parent = null;
        if (parentId > 0) {
            parent = requireTenantDict(parentId, tenantId);
        }
        dict.setModuleId(parent == null ? dictDTO.getModuleId() : parent.getModuleId());

        dict.setNodePath(parent == null ? dict.getDictCode() : parent.getNodePath() + "/" + dict.getDictCode());
        dict.setLevel(parent == null ? 1 : ((parent.getLevel() == null ? 1 : parent.getLevel()) + 1));
        dict.setChildrenCount(0);
        dict.setCreateTime(LocalDateTime.now());
        dict.setUpdateTime(LocalDateTime.now());

        dictMapper.insert(dict);

        if (parent != null) {
            dictMapper.update(null, new LambdaUpdateWrapper<SysDict>()
                    .eq(SysDict::getId, parent.getId())
                    .eq(SysDict::getTenantId, tenantId)
                    .eq(SysDict::getDeleted, false)
                    .setSql("children_count = children_count + 1")
                    .set(SysDict::getUpdateTime, LocalDateTime.now()));
        }

        clearDictCache(null, tenantId);
    }

    @Override
    public void updateDict(DictDTO dictDTO) {
        if (dictDTO.getId() == null) {
            throw new RuntimeException("字典ID不能为空");
        }

        Long tenantId = dictDTO.getTenantId();
        SysDict old = requireTenantDict(dictDTO.getId(), tenantId);

        Long newParentId = dictDTO.getParentId() == null ? normalizeParentId(old.getParentId()) : normalizeParentId(dictDTO.getParentId());
        if (Objects.equals(dictDTO.getId(), newParentId)) {
            throw new RuntimeException("不能将父节点设置为自己");
        }

        SysDict newParent = null;
        if (newParentId > 0) {
            newParent = requireTenantDict(newParentId, old.getTenantId());
        }

        SysDict dict = new SysDict();
        BeanUtils.copyProperties(dictDTO, dict);
        dict.setId(old.getId());
        dict.setTenantId(old.getTenantId());
        dict.setParentId(newParentId);
        dict.setModuleId(newParent == null ? dictDTO.getModuleId() : newParent.getModuleId());
        dict.setUpdateTime(LocalDateTime.now());

        if (!StringUtils.hasText(dict.getDictCode())) {
            dict.setDictCode(old.getDictCode());
        }

        String newPath = newParent == null ? dict.getDictCode() : newParent.getNodePath() + "/" + dict.getDictCode();
        Integer newLevel = newParent == null ? 1 : ((newParent.getLevel() == null ? 1 : newParent.getLevel()) + 1);

        boolean pathChanged = !Objects.equals(old.getNodePath(), newPath);
        boolean parentChanged = !Objects.equals(normalizeParentId(old.getParentId()), newParentId);
        boolean moduleChanged = !Objects.equals(old.getModuleId(), dict.getModuleId());

        dict.setNodePath(newPath);
        dict.setLevel(newLevel);
        dictMapper.updateById(dict);

        if ((pathChanged || moduleChanged) && StringUtils.hasText(old.getNodePath())) {
            List<SysDict> descendants = dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                    .eq(SysDict::getTenantId, old.getTenantId())
                    .eq(SysDict::getDeleted, false)
                    .likeRight(SysDict::getNodePath, old.getNodePath() + "/")
                    .orderByAsc(SysDict::getLevel));
            for (SysDict descendant : descendants) {
                SysDict update = new SysDict();
                update.setId(descendant.getId());
                update.setNodePath(newPath + descendant.getNodePath().substring(old.getNodePath().length()));
                if (descendant.getLevel() != null && old.getLevel() != null) {
                    update.setLevel(descendant.getLevel() - old.getLevel() + newLevel);
                }
                update.setModuleId(dict.getModuleId());
                update.setUpdateTime(LocalDateTime.now());
                dictMapper.updateById(update);
            }
        }

        if (parentChanged) {
            Long oldParentId = normalizeParentId(old.getParentId());
            if (oldParentId > 0) {
                dictMapper.update(null, new LambdaUpdateWrapper<SysDict>()
                        .eq(SysDict::getId, oldParentId)
                        .eq(SysDict::getTenantId, old.getTenantId())
                        .eq(SysDict::getDeleted, false)
                        .setSql("children_count = GREATEST(children_count - 1, 0)")
                        .set(SysDict::getUpdateTime, LocalDateTime.now()));
            }
            if (newParentId > 0) {
                dictMapper.update(null, new LambdaUpdateWrapper<SysDict>()
                        .eq(SysDict::getId, newParentId)
                        .eq(SysDict::getTenantId, old.getTenantId())
                        .eq(SysDict::getDeleted, false)
                        .setSql("children_count = children_count + 1")
                        .set(SysDict::getUpdateTime, LocalDateTime.now()));
            }
        }

        clearDictCache(null, old.getTenantId());
    }

    @Override
    public void deleteDict(Long id, Long tenantId) {
        if (id == null) {
            throw new RuntimeException("字典ID不能为空");
        }

        SysDict old = requireTenantDict(id, tenantId);

        Long childCount = dictMapper.selectCount(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getTenantId, old.getTenantId())
                .eq(SysDict::getDeleted, false)
                .eq(SysDict::getParentId, id));
        if (childCount > 0) {
            throw new RuntimeException("该字典下存在子节点，无法删除");
        }

        int rows = dictMapper.update(null, new LambdaUpdateWrapper<SysDict>()
                .eq(SysDict::getId, id)
                .eq(SysDict::getTenantId, old.getTenantId())
                .eq(SysDict::getDeleted, false)
                .set(SysDict::getDeleted, true)
                .set(SysDict::getUpdateTime, LocalDateTime.now()));
        if (rows <= 0) {
            throw new RuntimeException("字典不存在");
        }

        Long parentId = normalizeParentId(old.getParentId());
        if (parentId > 0) {
            dictMapper.update(null, new LambdaUpdateWrapper<SysDict>()
                    .eq(SysDict::getId, parentId)
                    .eq(SysDict::getTenantId, old.getTenantId())
                    .eq(SysDict::getDeleted, false)
                    .setSql("children_count = GREATEST(children_count - 1, 0)")
                    .set(SysDict::getUpdateTime, LocalDateTime.now()));
        }

        clearDictCache(null, old.getTenantId());
    }

    @Override
    public void clearDictCache(String dictCode, Long tenantId) {
        try {
            String prefix = "dict:" + tenantId + ":";
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("清除字典缓存失败，tenantId={}", tenantId, e);
        }
    }

    private List<DictTreeVO> listTenantDictVos(Long tenantId) {
        List<SysDict> dicts = dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(tenantId != null, SysDict::getTenantId, tenantId)
                .eq(SysDict::getDeleted, false)
                .orderByAsc(SysDict::getParentId)
                .orderByAsc(SysDict::getOrderNum)
                .orderByAsc(SysDict::getId));
        Map<Long, String> moduleNameMap = loadModuleNameMap(dicts);

        List<DictTreeVO> result = new ArrayList<>();
        for (SysDict dict : dicts) {
            DictTreeVO vo = new DictTreeVO();
            BeanUtils.copyProperties(dict, vo);
            vo.setModuleName(moduleNameMap.get(dict.getModuleId()));
            vo.setChildren(new ArrayList<>());
            result.add(vo);
        }
        return result;
    }

    private Map<Long, String> loadModuleNameMap(List<SysDict> dicts) {
        if (dicts == null || dicts.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> moduleIds = dicts.stream()
                .map(SysDict::getModuleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (moduleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysModule> modules = moduleMapper.selectList(new LambdaQueryWrapper<SysModule>()
                .in(SysModule::getId, moduleIds)
                .eq(SysModule::getDeleted, false));
        Map<Long, String> result = new HashMap<>();
        for (SysModule module : modules) {
            result.put(module.getId(), module.getName());
        }
        return result;
    }

    private SysDict requireTenantDict(Long id, Long tenantId) {
        SysDict dict = dictMapper.selectOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getId, id)
                .eq(tenantId != null, SysDict::getTenantId, tenantId)
                .eq(SysDict::getDeleted, false)
                .last("limit 1"));
        if (dict == null) {
            throw new RuntimeException("字典不存在");
        }
        return dict;
    }

    private List<DictTreeVO> buildTree(List<DictTreeVO> allNodes, Long parentId) {
        List<DictTreeVO> result = new ArrayList<>();
        for (DictTreeVO node : allNodes) {
            if (Objects.equals(normalizeParentId(node.getParentId()), normalizeParentId(parentId))) {
                node.setChildren(buildTree(allNodes, node.getId()));
                result.add(node);
            }
        }
        return result;
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private boolean matchRootNode(DictTreeVO root, DictPageParam query) {
        if (root == null) {
            return false;
        }
        if (StringUtils.hasText(query.getDictCode())
                && !containsIgnoreCase(root.getDictCode(), query.getDictCode())) {
            return false;
        }
        if (StringUtils.hasText(query.getDictName())
                && !containsIgnoreCase(root.getDictName(), query.getDictName())) {
            return false;
        }
        return query.getModuleId() == null || Objects.equals(root.getModuleId(), query.getModuleId());
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return StringUtils.hasText(source)
                && source.toLowerCase().contains(keyword.trim().toLowerCase());
    }

    private String resolveI18nText(String i18nJson, String fallback) {
        if (!StringUtils.hasText(i18nJson)) {
            return fallback;
        }
        try {
            JsonNode node = objectMapper.readTree(i18nJson);
            String lang = LangContext.get();
            String value = getText(node, lang);
            if (StringUtils.hasText(value)) {
                return value;
            }
            if (StringUtils.hasText(lang)) {
                int index = lang.indexOf('-');
                if (index > 0) {
                    value = getText(node, lang.substring(0, index));
                    if (StringUtils.hasText(value)) {
                        return value;
                    }
                }
            }
            value = getText(node, "zh-CN");
            if (StringUtils.hasText(value)) {
                return value;
            }
            value = getText(node, "zh");
            if (StringUtils.hasText(value)) {
                return value;
            }
            if (node.isObject()) {
                java.util.Iterator<java.util.Map.Entry<String, JsonNode>> iterator = node.fields();
                if (iterator.hasNext()) {
                    JsonNode first = iterator.next().getValue();
                    if (first != null && first.isTextual() && StringUtils.hasText(first.asText())) {
                        return first.asText();
                    }
                }
            }
        } catch (Exception ignored) {
            return fallback;
        }
        return fallback;
    }

    private String getText(JsonNode node, String key) {
        if (node == null || !node.isObject() || !StringUtils.hasText(key)) {
            return null;
        }
        JsonNode value = node.get(key);
        if (value != null && value.isTextual() && StringUtils.hasText(value.asText())) {
            return value.asText();
        }
        return null;
    }

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
