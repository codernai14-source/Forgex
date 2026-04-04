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
package com.forgex.common.service.table.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.domain.dto.table.FxTableColumnDTO;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.common.domain.dto.table.FxTableQueryFieldDTO;
import com.forgex.common.domain.dto.table.FxUserTableConfigDTO;
import com.forgex.common.domain.entity.table.FxTableColumnConfig;
import com.forgex.common.domain.entity.table.FxTableConfig;
import com.forgex.common.domain.entity.table.FxUserTableConfig;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.mapper.table.FxTableColumnConfigMapper;
import com.forgex.common.mapper.table.FxTableConfigMapper;
import com.forgex.common.mapper.table.FxUserTableConfigMapper;
import com.forgex.common.service.table.FxUserTableConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 用户级别表格配置服务实现类
 * <p>
 * 提供用户级别表格配置的管理功能实现，支持用户个性化配置表格。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>获取用户级别表格配置</li>
 *   <li>保存用户级别表格配置</li>
 *   <li>删除用户级别表格配置</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxUserTableConfigService
 * @see FxUserTableConfigDTO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FxUserTableConfigServiceImpl implements FxUserTableConfigService {
    
    /**
     * 用户级别表格配置 Mapper
     */
    private final FxUserTableConfigMapper userTableConfigMapper;
    
    /**
     * 表格配置 Mapper
     */
    private final FxTableConfigMapper tableConfigMapper;
    
    /**
     * 表格列配置 Mapper
     */
    private final FxTableColumnConfigMapper tableColumnConfigMapper;
    
    @Override
    public FxUserTableConfigDTO getUserTableConfig(String tableCode, Long tenantId, Long userId) {
        // 参数校验
        if (!StringUtils.hasText(tableCode) || tenantId == null || userId == null) {
            return null;
        }
        
        // 查询用户级别配置
        LambdaQueryWrapper<FxUserTableConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxUserTableConfig::getTableCode, tableCode)
               .eq(FxUserTableConfig::getTenantId, tenantId)
               .eq(FxUserTableConfig::getUserId, userId)
               .eq(FxUserTableConfig::getDeleted, 0)
               .last("limit 1");
        
        FxUserTableConfig userConfig = userTableConfigMapper.selectOne(wrapper);
        if (userConfig == null) {
            return null;
        }
        
        // 构建 DTO
        FxUserTableConfigDTO dto = new FxUserTableConfigDTO();
        dto.setTableCode(userConfig.getTableCode());
        dto.setUserId(userConfig.getUserId());
        dto.setTenantId(userConfig.getTenantId());
        dto.setPageSize(userConfig.getPageSize());
        dto.setSortConfig(userConfig.getSortConfig());
        dto.setVersion(userConfig.getVersion());
        dto.setCreateBy(userConfig.getCreateBy());
        dto.setCreateTime(userConfig.getCreateTime());
        dto.setUpdateBy(userConfig.getUpdateBy());
        dto.setUpdateTime(userConfig.getUpdateTime());
        
        // 从租户或公共配置加载列配置和查询字段
        FxTableConfigDTO baseConfig = loadBaseTableConfig(tableCode, tenantId);
        if (baseConfig != null) {
            dto.setColumns(baseConfig.getColumns());
            dto.setQueryFields(baseConfig.getQueryFields());
        }
        
        log.debug("获取用户级别表格配置成功，tableCode: {}, userId: {}, tenantId: {}", 
                tableCode, userId, tenantId);
        
        return dto;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @DS("common")
    public Long saveUserTableConfig(FxUserTableConfigDTO dto, Long userId) {
        // 参数校验
        if (!StringUtils.hasText(dto.getTableCode()) || dto.getTenantId() == null || userId == null) {
            throw new IllegalArgumentException("表编码、租户 ID 和用户 ID 不能为空");
        }
        
        // 检查是否已存在配置
        LambdaQueryWrapper<FxUserTableConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxUserTableConfig::getTableCode, dto.getTableCode())
               .eq(FxUserTableConfig::getTenantId, dto.getTenantId())
               .eq(FxUserTableConfig::getUserId, userId)
               .eq(FxUserTableConfig::getDeleted, 0)
               .last("limit 1");
        
        FxUserTableConfig existingConfig = userTableConfigMapper.selectOne(wrapper);
        
        FxUserTableConfig config;
        if (existingConfig != null) {
            // 更新现有配置
            config = existingConfig;
            config.setPageSize(dto.getPageSize());
            config.setSortConfig(dto.getSortConfig());
            config.setColumnConfig(dto.getColumns() != null ? JSONUtil.toJsonStr(dto.getColumns()) : null);
            config.setQueryConfig(dto.getQueryFields() != null ? JSONUtil.toJsonStr(dto.getQueryFields()) : null);
            config.setVersion(config.getVersion() + 1);
            
            userTableConfigMapper.updateById(config);
            log.info("更新用户级别表格配置成功，tableCode: {}, userId: {}, configId: {}", 
                    dto.getTableCode(), userId, config.getId());
        } else {
            // 创建新配置
            config = new FxUserTableConfig();
            config.setTableCode(dto.getTableCode());
            config.setTenantId(dto.getTenantId());
            config.setUserId(userId);
            config.setPageSize(dto.getPageSize() != null ? dto.getPageSize() : 20);
            config.setSortConfig(dto.getSortConfig());
            config.setColumnConfig(dto.getColumns() != null ? JSONUtil.toJsonStr(dto.getColumns()) : null);
            config.setQueryConfig(dto.getQueryFields() != null ? JSONUtil.toJsonStr(dto.getQueryFields()) : null);
            config.setVersion(1);
            
            userTableConfigMapper.insert(config);
            log.info("创建用户级别表格配置成功，tableCode: {}, userId: {}, configId: {}", 
                    dto.getTableCode(), userId, config.getId());
        }
        
        return config.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUserTableConfig(String tableCode, Long tenantId, Long userId) {
        // 参数校验
        if (!StringUtils.hasText(tableCode) || tenantId == null || userId == null) {
            return false;
        }
        
        // 查询配置
        LambdaQueryWrapper<FxUserTableConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxUserTableConfig::getTableCode, tableCode)
               .eq(FxUserTableConfig::getTenantId, tenantId)
               .eq(FxUserTableConfig::getUserId, userId)
               .eq(FxUserTableConfig::getDeleted, 0)
               .last("limit 1");
        
        FxUserTableConfig config = userTableConfigMapper.selectOne(wrapper);
        if (config == null) {
            log.warn("删除用户级别表格配置失败，配置不存在，tableCode: {}, userId: {}, tenantId: {}", 
                    tableCode, userId, tenantId);
            return false;
        }
        
        // 逻辑删除
        config.setDeleted(true);
        userTableConfigMapper.updateById(config);
        
        log.info("删除用户级别表格配置成功，tableCode: {}, userId: {}, configId: {}", 
                tableCode, userId, config.getId());
        
        return true;
    }
    
    /**
     * 加载基础表格配置（租户或公共配置）
     * <p>
     * 用于获取列配置和查询字段配置。
     * </p>
     *
     * @param tableCode 表编码
     * @param tenantId 租户 ID
     * @return 表格配置 DTO
     */
    private FxTableConfigDTO loadBaseTableConfig(String tableCode, Long tenantId) {
        // 先查询租户配置
        FxTableConfig config = queryTableConfig(tableCode, tenantId);
        
        // 如果租户配置不存在，查询公共配置
        if (config == null) {
            config = queryTableConfig(tableCode, 0L);
        }
        
        if (config == null || Boolean.FALSE.equals(config.getEnabled())) {
            return null;
        }
        
        // 查询列配置
        List<FxTableColumnConfig> cols = tableColumnConfigMapper.selectList(
            new LambdaQueryWrapper<FxTableColumnConfig>()
                .eq(FxTableColumnConfig::getTableCode, tableCode)
                .eq(FxTableColumnConfig::getTenantId, config.getTenantId())
                .eq(FxTableColumnConfig::getDeleted, 0)
        );
        
        // 构建 DTO
        FxTableConfigDTO dto = new FxTableConfigDTO();
        dto.setTableCode(config.getTableCode());
        dto.setTableName(config.getTableCode()); // 简化处理，不使用国际化
        dto.setTableType(config.getTableType());
        dto.setRowKey(config.getRowKey() != null ? config.getRowKey() : "id");
        dto.setDefaultPageSize(config.getDefaultPageSize() != null ? config.getDefaultPageSize() : 20);
        dto.setDefaultSortJson(config.getDefaultSortJson());
        dto.setVersion(config.getVersion());
        dto.setEnabled(config.getEnabled());
        
        // 构建列配置列表
        List<FxTableColumnDTO> columnDtos = new ArrayList<>();
        List<FxTableQueryFieldDTO> queryFields = new ArrayList<>();
        
        cols.sort(Comparator.comparing(c -> c.getOrderNum() == null ? 0 : c.getOrderNum()));
        
        for (FxTableColumnConfig c : cols) {
            if (Boolean.FALSE.equals(c.getEnabled())) {
                continue;
            }
            
            FxTableColumnDTO cd = new FxTableColumnDTO();
            cd.setField(c.getField());
            cd.setTitle(c.getField()); // 简化处理
            cd.setAlign(c.getAlign());
            cd.setWidth(c.getWidth());
            cd.setFixed(c.getFixed());
            cd.setEllipsis(c.getEllipsis());
            cd.setSortable(c.getSortable());
            cd.setSorterField(c.getSorterField());
            cd.setQueryable(c.getQueryable());
            cd.setQueryType(c.getQueryType());
            cd.setQueryOperator(c.getQueryOperator());
            cd.setDictCode(c.getDictCode());
            cd.setDictField(c.getField() + "Text");
            cd.setRenderType(c.getRenderType());
            cd.setPermKey(c.getPermKey());
            columnDtos.add(cd);
            
            // 构建查询字段
            if (Boolean.TRUE.equals(c.getQueryable())) {
                FxTableQueryFieldDTO q = new FxTableQueryFieldDTO();
                q.setField(c.getField());
                q.setLabel(c.getField());
                q.setQueryType(c.getQueryType() != null ? c.getQueryType() : "input");
                q.setQueryOperator(c.getQueryOperator() != null ? c.getQueryOperator() : "like");
                q.setDictCode(c.getDictCode());
                queryFields.add(q);
            }
        }
        
        dto.setColumns(columnDtos);
        dto.setQueryFields(queryFields);
        
        return dto;
    }
    
    /**
     * 查询表格配置
     *
     * @param tableCode 表编码
     * @param tenantId 租户 ID
     * @return 表格配置实体
     */
    private FxTableConfig queryTableConfig(String tableCode, Long tenantId) {
        return tableConfigMapper.selectOne(
            new LambdaQueryWrapper<FxTableConfig>()
                .eq(FxTableConfig::getTableCode, tableCode)
                .eq(FxTableConfig::getTenantId, tenantId)
                .eq(FxTableConfig::getDeleted, 0)
                .orderByAsc(FxTableConfig::getId)
                .last("limit 1")
        );
    }
}
