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
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.DictDTO;
import com.forgex.sys.domain.entity.SysDict;
import com.forgex.sys.domain.vo.DictItemVO;
import com.forgex.sys.domain.vo.DictTreeVO;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 数据字典服务实现
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
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
                .eq(SysDict::getTenantId, tenantId)
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
        // 先从缓存读取
        String cacheKey = String.format("dict:%d:%s", tenantId, dictCode);
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
                .eq(SysDict::getTenantId, tenantId)
                .eq(SysDict::getDeleted, false)
                .last("limit 1"));
        
        if (dictType == null) {
            log.warn("字典类型不存在：{}", dictCode);
            return new ArrayList<>();
        }
        
        // 2. 查询字典项
        List<SysDict> dictItems = dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getParentId, dictType.getId())
                .eq(SysDict::getTenantId, tenantId)
                .eq(SysDict::getDeleted, false)
                .eq(SysDict::getStatus, 1) // 只查询启用的
                .orderByAsc(SysDict::getOrderNum));
        
        // 3. 转换为 VO
        List<DictItemVO> items = dictItems.stream().map(dict -> {
            DictItemVO vo = new DictItemVO();
            vo.setLabel(dict.getDictName());
            vo.setValue(dict.getDictValue());
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
        SysDict dict = new SysDict();
        BeanUtils.copyProperties(dictDTO, dict);
        dict.setCreateTime(LocalDateTime.now());
        dict.setUpdateTime(LocalDateTime.now());
        dict.setDeleted(false);
        
        dictMapper.insert(dict);
        
        // 如果是根节点，清除对应缓存
        if (StringUtils.hasText(dictDTO.getDictCode())) {
            clearDictCache(dictDTO.getDictCode(), dictDTO.getTenantId());
        } else if (dictDTO.getParentId() != null && dictDTO.getParentId() > 0) {
            // 如果是子节点，查询父节点的 dictCode 并清除缓存
            SysDict parent = dictMapper.selectById(dictDTO.getParentId());
            if (parent != null && StringUtils.hasText(parent.getDictCode())) {
                clearDictCache(parent.getDictCode(), dictDTO.getTenantId());
            }
        }
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
        
        SysDict dict = new SysDict();
        BeanUtils.copyProperties(dictDTO, dict);
        dict.setUpdateTime(LocalDateTime.now());
        
        dictMapper.updateById(dict);
        
        // 清除缓存
        SysDict oldDict = dictMapper.selectById(dictDTO.getId());
        if (oldDict != null) {
            if (StringUtils.hasText(oldDict.getDictCode())) {
                clearDictCache(oldDict.getDictCode(), oldDict.getTenantId());
            } else if (oldDict.getParentId() != null && oldDict.getParentId() > 0) {
                SysDict parent = dictMapper.selectById(oldDict.getParentId());
                if (parent != null && StringUtils.hasText(parent.getDictCode())) {
                    clearDictCache(parent.getDictCode(), oldDict.getTenantId());
                }
            }
        }
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
        
        // 检查是否有子节点
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
        
        // 清除缓存
        SysDict oldDict = dictMapper.selectById(id);
        if (oldDict != null) {
            if (StringUtils.hasText(oldDict.getDictCode())) {
                clearDictCache(oldDict.getDictCode(), oldDict.getTenantId());
            } else if (oldDict.getParentId() != null && oldDict.getParentId() > 0) {
                SysDict parent = dictMapper.selectById(oldDict.getParentId());
                if (parent != null && StringUtils.hasText(parent.getDictCode())) {
                    clearDictCache(parent.getDictCode(), oldDict.getTenantId());
                }
            }
        }
    }
    
    /**
     * 清除字典缓存
     */
    @Override
    public void clearDictCache(String dictCode, Long tenantId) {
        String cacheKey = String.format("dict:%d:%s", tenantId, dictCode);
        try {
            redisTemplate.delete(cacheKey);
            log.debug("清除字典缓存：{}", cacheKey);
        } catch (Exception e) {
            log.warn("清除字典缓存失败：{}", cacheKey, e);
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
}
