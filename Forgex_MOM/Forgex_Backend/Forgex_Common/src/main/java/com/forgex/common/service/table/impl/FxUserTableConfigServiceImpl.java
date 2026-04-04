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
import org.springframework.beans.BeanUtils;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final String USER_TABLE_CODE = "UserTable";
    private static final String POSITION_TABLE_CODE = "PositionTable";
    private static final String DICT_TABLE_CODE = "DictTable";
    private static final String USERNAME_FIELD = "username";
    private static final String ACCOUNT_FIELD = "account";
    private static final String POSITION_ID_FIELD = "positionId";
    private static final String ROLE_ID_FIELD = "roleId";
    private static final String ENTRY_DATE_FIELD = "entryDate";
    private static final String ACTION_FIELD = "action";
    private static final String USERNAME_TITLE_I18N_JSON =
            "{\"zh-CN\":\"\\u7528\\u6237\\u540d\",\"en-US\":\"Username\",\"zh-TW\":\"\\u4f7f\\u7528\\u8005\\u540d\\u7a31\",\"ja-JP\":\"\\u30e6\\u30fc\\u30b6\\u30fc\\u540d\",\"ko-KR\":\"\\uc0ac\\uc6a9\\uc790\\uba85\"}";
    private static final String ACCOUNT_TITLE_I18N_JSON =
            "{\"zh-CN\":\"\\u8d26\\u53f7\",\"en-US\":\"Account\",\"zh-TW\":\"\\u5e33\\u865f\",\"ja-JP\":\"\\u30a2\\u30ab\\u30a6\\u30f3\\u30c8\",\"ko-KR\":\"\\uacc4\\uc815\"}";
    private static final String POSITION_TITLE_I18N_JSON =
            "{\"zh-CN\":\"\\u804c\\u4f4d\",\"en-US\":\"Position\",\"zh-TW\":\"\\u8077\\u4f4d\",\"ja-JP\":\"\\u8077\\u4f4d\",\"ko-KR\":\"\\uc9c1\\ucc45\"}";
    private static final String ROLE_TITLE_I18N_JSON =
            "{\"zh-CN\":\"\\u89d2\\u8272\",\"en-US\":\"Role\",\"zh-TW\":\"\\u89d2\\u8272\",\"ja-JP\":\"\\u30ed\\u30fc\\u30eb\",\"ko-KR\":\"\\uc5ed\\ud560\"}";
    private static final String ENTRY_DATE_TITLE_I18N_JSON =
            "{\"zh-CN\":\"\\u5165\\u804c\\u65f6\\u95f4\",\"en-US\":\"Entry Date\",\"zh-TW\":\"\\u5165\\u8077\\u6642\\u9593\",\"ja-JP\":\"\\u5165\\u793e\\u65e5\",\"ko-KR\":\"\\uc785\\uc0ac\\uc77c\"}";

    
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
            dto.setColumns(mergeUserColumns(baseConfig.getColumns(), userConfig.getColumnConfig()));
            dto.setQueryFields(mergeUserQueryFields(tableCode, baseConfig.getQueryFields(), userConfig.getQueryConfig()));
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
        
        normalizeBuiltinColumns(tableCode, columnDtos);

        dto.setColumns(columnDtos);
        dto.setQueryFields(normalizeBuiltinQueryFields(tableCode, queryFields));
        
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

    private List<FxTableColumnDTO> mergeUserColumns(List<FxTableColumnDTO> baseColumns, String columnConfigJson) {
        List<FxTableColumnDTO> safeBaseColumns = baseColumns == null ? new ArrayList<>() : new ArrayList<>(baseColumns);
        Map<String, FxTableColumnDTO> userColumnsMap = parseUserColumns(columnConfigJson);
        List<FxTableColumnDTO> mergedColumns = new ArrayList<>();

        for (int i = 0; i < safeBaseColumns.size(); i++) {
            FxTableColumnDTO baseCol = safeBaseColumns.get(i);
            FxTableColumnDTO mergedCol = new FxTableColumnDTO();
            BeanUtils.copyProperties(baseCol, mergedCol);

            FxTableColumnDTO userCol = userColumnsMap.get(baseCol.getField());
            mergedCol.setVisible(userCol != null ? userCol.getVisible() : Boolean.TRUE);
            mergedCol.setOrder(userCol != null && userCol.getOrder() != null ? userCol.getOrder() : i);
            mergedColumns.add(mergedCol);
        }

        mergedColumns.sort(Comparator.comparing(col -> col.getOrder() == null ? Integer.MAX_VALUE : col.getOrder()));
        return mergedColumns;
    }

    private Map<String, FxTableColumnDTO> parseUserColumns(String columnConfigJson) {
        Map<String, FxTableColumnDTO> userColumnsMap = new HashMap<>();
        if (!StringUtils.hasText(columnConfigJson)) {
            return userColumnsMap;
        }

        try {
            List<FxTableColumnDTO> userColumns = JSONUtil.toList(JSONUtil.parseArray(columnConfigJson), FxTableColumnDTO.class);
            for (FxTableColumnDTO userColumn : userColumns) {
                if (userColumn != null && StringUtils.hasText(userColumn.getField())) {
                    userColumnsMap.put(userColumn.getField(), userColumn);
                }
            }
        } catch (Exception e) {
            log.warn("解析用户列配置失败，columnConfig: {}", columnConfigJson, e);
        }
        return userColumnsMap;
    }

    private List<FxTableQueryFieldDTO> mergeUserQueryFields(String tableCode, List<FxTableQueryFieldDTO> baseQueryFields, String queryConfigJson) {
        if (!StringUtils.hasText(queryConfigJson)) {
            return normalizeBuiltinQueryFields(tableCode, baseQueryFields);
        }

        try {
            List<FxTableQueryFieldDTO> queryFields = JSONUtil.toList(JSONUtil.parseArray(queryConfigJson), FxTableQueryFieldDTO.class);
            return normalizeBuiltinQueryFields(
                tableCode,
                queryFields == null || queryFields.isEmpty() ? baseQueryFields : queryFields
            );
        } catch (Exception e) {
            log.warn("解析用户查询配置失败，queryConfig: {}", queryConfigJson, e);
            return normalizeBuiltinQueryFields(tableCode, baseQueryFields);
        }
    }

    private List<FxTableQueryFieldDTO> ensureBuiltinUserTableQueryFields(String tableCode, List<FxTableQueryFieldDTO> queryFields) {
        List<FxTableQueryFieldDTO> safeQueryFields =
            queryFields == null ? new ArrayList<>() : new ArrayList<>(queryFields);
        if (!USER_TABLE_CODE.equals(tableCode)) {
            return safeQueryFields;
        }

        Map<String, FxTableQueryFieldDTO> normalizedFields = new LinkedHashMap<>();
        for (FxTableQueryFieldDTO queryField : safeQueryFields) {
            if (queryField == null || !StringUtils.hasText(queryField.getField())) {
                continue;
            }
            String field = queryField.getField();
            if (!isBuiltinUserTableQueryField(field) || normalizedFields.containsKey(field)) {
                continue;
            }
            normalizedFields.put(field, buildBuiltinUserTableQueryField(field, queryField));
        }

        addBuiltinUserTableQueryField(normalizedFields, ACCOUNT_FIELD);
        addBuiltinUserTableQueryField(normalizedFields, USERNAME_FIELD);
        addBuiltinUserTableQueryField(normalizedFields, POSITION_ID_FIELD);
        addBuiltinUserTableQueryField(normalizedFields, ROLE_ID_FIELD);
        addBuiltinUserTableQueryField(normalizedFields, ENTRY_DATE_FIELD);
        return new ArrayList<>(normalizedFields.values());
    }

    private boolean isBuiltinUserTableQueryField(String field) {
        return ACCOUNT_FIELD.equals(field)
            || USERNAME_FIELD.equals(field)
            || POSITION_ID_FIELD.equals(field)
            || ROLE_ID_FIELD.equals(field)
            || ENTRY_DATE_FIELD.equals(field);
    }

    private void addBuiltinUserTableQueryField(Map<String, FxTableQueryFieldDTO> normalizedFields, String field) {
        if (!normalizedFields.containsKey(field)) {
            normalizedFields.put(field, buildBuiltinUserTableQueryField(field, null));
        }
    }

    private FxTableQueryFieldDTO buildBuiltinUserTableQueryField(String field, FxTableQueryFieldDTO source) {
        FxTableQueryFieldDTO queryField = new FxTableQueryFieldDTO();
        if (source != null) {
            BeanUtils.copyProperties(source, queryField);
        }
        queryField.setField(field);

        if (ACCOUNT_FIELD.equals(field)) {
            queryField.setLabel(resolveI18nText(ACCOUNT_TITLE_I18N_JSON, ACCOUNT_FIELD));
            queryField.setQueryType("input");
            queryField.setQueryOperator("like");
            queryField.setDictCode(null);
        } else if (USERNAME_FIELD.equals(field)) {
            queryField.setLabel(resolveI18nText(USERNAME_TITLE_I18N_JSON, USERNAME_FIELD));
            queryField.setQueryType("input");
            queryField.setQueryOperator("like");
            queryField.setDictCode(null);
        } else if (POSITION_ID_FIELD.equals(field)) {
            queryField.setLabel(resolveI18nText(POSITION_TITLE_I18N_JSON, POSITION_ID_FIELD));
            queryField.setQueryType("select");
            queryField.setQueryOperator("eq");
            queryField.setDictCode(POSITION_ID_FIELD);
        } else if (ROLE_ID_FIELD.equals(field)) {
            queryField.setLabel(resolveI18nText(ROLE_TITLE_I18N_JSON, ROLE_ID_FIELD));
            queryField.setQueryType("select");
            queryField.setQueryOperator("eq");
            queryField.setDictCode(ROLE_ID_FIELD);
        } else if (ENTRY_DATE_FIELD.equals(field)) {
            queryField.setLabel(resolveI18nText(ENTRY_DATE_TITLE_I18N_JSON, ENTRY_DATE_FIELD));
            queryField.setQueryType("dateRange");
            queryField.setQueryOperator("between");
            queryField.setDictCode(null);
        }
        return queryField;
    }

    private void ensureBuiltinUserTableColumns(String tableCode, List<FxTableColumnDTO> columns) {
        if (!USER_TABLE_CODE.equals(tableCode) || columns == null) {
            return;
        }

        boolean hasUsernameColumn = columns.stream()
                .anyMatch(column -> USERNAME_FIELD.equals(column.getField()));
        if (hasUsernameColumn) {
            return;
        }

        FxTableColumnDTO usernameColumn = new FxTableColumnDTO();
        usernameColumn.setField(USERNAME_FIELD);
        usernameColumn.setTitle(resolveI18nText(USERNAME_TITLE_I18N_JSON, USERNAME_FIELD));
        usernameColumn.setWidth(140);
        usernameColumn.setEllipsis(false);
        usernameColumn.setSortable(false);
        usernameColumn.setQueryable(false);
        usernameColumn.setVisible(true);

        int insertIndex = 0;
        for (int i = 0; i < columns.size(); i++) {
            if (ACCOUNT_FIELD.equals(columns.get(i).getField())) {
                insertIndex = i + 1;
                break;
            }
        }
        columns.add(insertIndex, usernameColumn);
    }

    private void normalizeBuiltinColumns(String tableCode, List<FxTableColumnDTO> columns) {
        if (columns == null) {
            return;
        }
        if (USER_TABLE_CODE.equals(tableCode)) {
            ensureBuiltinUserTableColumns(tableCode, columns);
            return;
        }
        if (POSITION_TABLE_CODE.equals(tableCode)) {
            normalizePositionTableColumns(columns);
            return;
        }
        if (DICT_TABLE_CODE.equals(tableCode)) {
            ensureDictActionColumn(columns);
        }
    }

    private List<FxTableQueryFieldDTO> normalizeBuiltinQueryFields(String tableCode, List<FxTableQueryFieldDTO> queryFields) {
        if (USER_TABLE_CODE.equals(tableCode)) {
            return ensureBuiltinUserTableQueryFields(tableCode, queryFields);
        }
        if (POSITION_TABLE_CODE.equals(tableCode)) {
            return ensurePositionTableQueryFields(queryFields);
        }
        return queryFields == null ? new ArrayList<>() : new ArrayList<>(queryFields);
    }

    private void normalizePositionTableColumns(List<FxTableColumnDTO> columns) {
        Map<String, FxTableColumnDTO> normalizedMap = new LinkedHashMap<>();
        for (FxTableColumnDTO column : columns) {
            if (column == null || !StringUtils.hasText(column.getField())) {
                continue;
            }
            String normalizedField = normalizePositionField(column.getField());
            if (!StringUtils.hasText(normalizedField) || normalizedMap.containsKey(normalizedField)) {
                continue;
            }
            FxTableColumnDTO normalized = new FxTableColumnDTO();
            BeanUtils.copyProperties(column, normalized);
            normalized.setField(normalizedField);
            applyPositionColumnDefaults(normalized);
            normalizedMap.put(normalizedField, normalized);
        }

        List<FxTableColumnDTO> orderedColumns = new ArrayList<>();
        orderedColumns.add(getOrCreatePositionColumn(normalizedMap, "positionName"));
        orderedColumns.add(getOrCreatePositionColumn(normalizedMap, "positionCode"));
        orderedColumns.add(getOrCreatePositionColumn(normalizedMap, "positionLevel"));
        orderedColumns.add(getOrCreatePositionColumn(normalizedMap, "orderNum"));
        orderedColumns.add(getOrCreatePositionColumn(normalizedMap, "status"));
        orderedColumns.add(getOrCreatePositionColumn(normalizedMap, "remark"));
        orderedColumns.add(getOrCreatePositionColumn(normalizedMap, "createTime"));

        for (Map.Entry<String, FxTableColumnDTO> entry : normalizedMap.entrySet()) {
            if (containsField(orderedColumns, entry.getKey()) || ACTION_FIELD.equals(entry.getKey())) {
                continue;
            }
            orderedColumns.add(entry.getValue());
        }

        orderedColumns.add(getOrCreatePositionActionColumn(normalizedMap));
        columns.clear();
        columns.addAll(orderedColumns);
    }

    private List<FxTableQueryFieldDTO> ensurePositionTableQueryFields(List<FxTableQueryFieldDTO> queryFields) {
        Map<String, FxTableQueryFieldDTO> normalizedMap = new LinkedHashMap<>();
        List<FxTableQueryFieldDTO> safeQueryFields =
                queryFields == null ? new ArrayList<FxTableQueryFieldDTO>() : queryFields;
        for (FxTableQueryFieldDTO queryField : safeQueryFields) {
            if (queryField == null || !StringUtils.hasText(queryField.getField())) {
                continue;
            }
            String normalizedField = normalizePositionField(queryField.getField());
            if (!StringUtils.hasText(normalizedField) || normalizedMap.containsKey(normalizedField)) {
                continue;
            }
            FxTableQueryFieldDTO normalized = new FxTableQueryFieldDTO();
            BeanUtils.copyProperties(queryField, normalized);
            normalized.setField(normalizedField);
            applyPositionQueryDefaults(normalized);
            normalizedMap.put(normalizedField, normalized);
        }

        addPositionQueryField(normalizedMap, "positionName");
        addPositionQueryField(normalizedMap, "positionCode");
        addPositionQueryField(normalizedMap, "status");
        return new ArrayList<>(normalizedMap.values());
    }

    private void ensureDictActionColumn(List<FxTableColumnDTO> columns) {
        boolean hasAction = columns.stream().anyMatch(column -> ACTION_FIELD.equals(column.getField()));
        if (hasAction) {
            return;
        }
        FxTableColumnDTO actionColumn = new FxTableColumnDTO();
        actionColumn.setField(ACTION_FIELD);
        actionColumn.setTitle("操作");
        actionColumn.setAlign("center");
        actionColumn.setWidth(180);
        actionColumn.setFixed("right");
        actionColumn.setQueryable(false);
        actionColumn.setVisible(true);
        columns.add(actionColumn);
    }

    private String normalizePositionField(String field) {
        if ("position_name".equals(field)) {
            return "positionName";
        }
        if ("description".equals(field)) {
            return "positionCode";
        }
        if ("create_by".equals(field)) {
            return "createBy";
        }
        if ("create_time".equals(field)) {
            return "createTime";
        }
        if ("update_by".equals(field)) {
            return "updateBy";
        }
        if ("update_time".equals(field)) {
            return "updateTime";
        }
        return field;
    }

    private FxTableColumnDTO getOrCreatePositionColumn(Map<String, FxTableColumnDTO> normalizedMap, String field) {
        FxTableColumnDTO column = normalizedMap.get(field);
        if (column == null) {
            column = new FxTableColumnDTO();
            column.setField(field);
            applyPositionColumnDefaults(column);
        }
        return column;
    }

    private FxTableColumnDTO getOrCreatePositionActionColumn(Map<String, FxTableColumnDTO> normalizedMap) {
        FxTableColumnDTO column = normalizedMap.get(ACTION_FIELD);
        if (column == null) {
            column = new FxTableColumnDTO();
            column.setField(ACTION_FIELD);
        }
        column.setTitle("操作");
        column.setAlign("center");
        column.setWidth(160);
        column.setFixed("right");
        column.setQueryable(false);
        column.setVisible(true);
        return column;
    }

    private void applyPositionColumnDefaults(FxTableColumnDTO column) {
        String field = column.getField();
        if ("positionName".equals(field)) {
            applyColumnDefaults(column, "职位名称", "left", 180, null);
        } else if ("positionCode".equals(field)) {
            applyColumnDefaults(column, "职位编码", "left", 140, null);
        } else if ("positionLevel".equals(field)) {
            applyColumnDefaults(column, "职位级别", "center", 120, "positionLevel");
        } else if ("orderNum".equals(field)) {
            applyColumnDefaults(column, "排序", "center", 90, null);
        } else if ("status".equals(field)) {
            applyColumnDefaults(column, "状态", "center", 100, "status");
        } else if ("remark".equals(field)) {
            applyColumnDefaults(column, "备注", "left", 220, null);
        } else if ("createTime".equals(field)) {
            applyColumnDefaults(column, "创建时间", "center", 180, null);
        } else if ("createBy".equals(field)) {
            applyColumnDefaults(column, "创建人", "left", 140, null);
        } else if ("updateTime".equals(field)) {
            applyColumnDefaults(column, "更新时间", "center", 180, null);
        } else if ("updateBy".equals(field)) {
            applyColumnDefaults(column, "更新人", "left", 140, null);
        }
    }

    private void applyColumnDefaults(FxTableColumnDTO column, String title, String align, int width, String dictCode) {
        column.setTitle(title);
        if (!StringUtils.hasText(column.getAlign())) {
            column.setAlign(align);
        }
        if (column.getWidth() == null) {
            column.setWidth(width);
        }
        if (dictCode != null) {
            column.setDictCode(dictCode);
        }
        if (column.getQueryable() == null) {
            column.setQueryable(false);
        }
        if (column.getVisible() == null) {
            column.setVisible(true);
        }
    }

    private void applyPositionQueryDefaults(FxTableQueryFieldDTO queryField) {
        String field = queryField.getField();
        if ("positionName".equals(field)) {
            queryField.setLabel("职位名称");
            queryField.setQueryType("input");
            queryField.setQueryOperator("like");
            queryField.setDictCode(null);
        } else if ("positionCode".equals(field)) {
            queryField.setLabel("职位编码");
            queryField.setQueryType("input");
            queryField.setQueryOperator("like");
            queryField.setDictCode(null);
        } else if ("status".equals(field)) {
            queryField.setLabel("状态");
            queryField.setQueryType("select");
            queryField.setQueryOperator("eq");
            queryField.setDictCode("status");
        }
    }

    private void addPositionQueryField(Map<String, FxTableQueryFieldDTO> normalizedMap, String field) {
        if (normalizedMap.containsKey(field)) {
            return;
        }
        FxTableQueryFieldDTO queryField = new FxTableQueryFieldDTO();
        queryField.setField(field);
        applyPositionQueryDefaults(queryField);
        normalizedMap.put(field, queryField);
    }

    private boolean containsField(List<FxTableColumnDTO> columns, String field) {
        return columns.stream().anyMatch(column -> field.equals(column.getField()));
    }

    private String resolveI18nText(String i18nJson, String fallback) {
        if (!StringUtils.hasText(i18nJson)) {
            return fallback;
        }

        try {
            String lang = LangContext.get();
            Map<String, Object> i18nMap = JSONUtil.toBean(i18nJson, Map.class);
            if (StringUtils.hasText(lang)) {
                Object exactValue = i18nMap.get(lang);
                if (exactValue instanceof String) {
                    String exactText = (String) exactValue;
                    if (StringUtils.hasText(exactText)) {
                        return exactText;
                    }
                }

                int splitIndex = lang.indexOf('-');
                if (splitIndex > 0) {
                    String shortLang = lang.substring(0, splitIndex);
                    Object shortValue = i18nMap.get(shortLang);
                    if (shortValue instanceof String) {
                        String shortText = (String) shortValue;
                        if (StringUtils.hasText(shortText)) {
                            return shortText;
                        }
                    }
                }
            }

            for (Object value : i18nMap.values()) {
                if (value instanceof String) {
                    String text = (String) value;
                    if (StringUtils.hasText(text)) {
                        return text;
                    }
                }
            }
        } catch (Exception ignored) {
            return fallback;
        }

        return fallback;
    }
}
