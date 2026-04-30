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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import com.forgex.common.domain.dto.table.FxTableColumnDTO;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.common.domain.dto.table.FxTableQueryFieldDTO;
import com.forgex.common.domain.entity.table.FxTableColumnConfig;
import com.forgex.common.domain.entity.table.FxTableConfig;
import com.forgex.common.domain.entity.table.FxUserTableConfig;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.mapper.table.FxTableColumnConfigMapper;
import com.forgex.common.mapper.table.FxTableConfigMapper;
import com.forgex.common.mapper.table.FxUserTableConfigMapper;
import com.forgex.common.service.table.FxTableConfigService;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 表格配置服务实现类
 * <p>
 * 提供表格配置的查询和分页功能实现，支持三级配置管理：
 * 用户级别 > 租户级别 > 公共配置
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>根据表编码、租户 ID 和用户 ID 获取表格配置（三级查询）</li>
 *   <li>支持表格配置的分页查询</li>
 *   <li>支持根据查询条件过滤表格配置</li>
 *   <li>支持国际化文本解析</li>
 * </ul>
 * <p><strong>查询优先级：</strong></p>
 * <ol>
 *   <li>用户级别配置（如果 userId 不为空）</li>
 *   <li>租户级别配置</li>
 *   <li>公共配置（tenant_id=0）</li>
 * </ol>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableConfigService
 * @see FxUserTableConfigMapper
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FxTableConfigServiceImpl implements FxTableConfigService {
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
     * 表格配置 Mapper
     */
    private final FxTableConfigMapper tableConfigMapper;

    /**
     * 表格列配置 Mapper
     */
    private final FxTableColumnConfigMapper tableColumnConfigMapper;
    
    /**
     * 用户级别表格配置 Mapper
     */
    private final FxUserTableConfigMapper userTableConfigMapper;

    /**
     * 获取表格配置
     * <p>
     * 根据表编码、租户 ID 和用户 ID 获取表格配置，
     * 包括表格列配置和查询字段配置。
     * </p>
     * <p><strong>查询优先级：</strong></p>
     * <ol>
     *   <li>用户级别配置（如果 userId 不为空）</li>
     *   <li>租户级别配置</li>
     *   <li>公共配置（tenant_id=0）</li>
     * </ol>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>参数校验：表编码和租户 ID 不能为空</li>
     *   <li>如果 userId 不为空，先查询用户级别配置</li>
     *   <li>如果用户配置不存在，查询租户级别配置</li>
     *   <li>如果租户配置不存在，查询公共配置（tenant_id=0）</li>
     *   <li>检查表格是否启用</li>
     *   <li>查询表格的所有列配置</li>
     *   <li>过滤启用的列并按排序字段排序</li>
     *   <li>解析列标题的国际化文本</li>
     *   <li>构建查询字段列表</li>
     *   <li>组装表格配置 DTO 并返回</li>
     * </ol>
     * 
     * @param tableCode 表编码，不能为空
     * @param tenantId 租户 ID，不能为空
     * @param userId 用户 ID，如果为空则跳过用户级别配置
     * @return 表格配置 DTO，参数无效或表格不存在或未启用时返回 null
     */
    @Override
    public FxTableConfigDTO getTableConfig(String tableCode, Long tenantId, Long userId) {
        // 参数校验：表编码和租户 ID 不能为空
        if (!StringUtils.hasText(tableCode) || tenantId == null) {
            return null;
        }

        // 1. 先查询用户级别配置（如果 userId 不为空）
        if (userId != null) {
            FxTableConfigDTO userConfig = getUserTableConfig(tableCode, tenantId, userId);
            if (userConfig != null) {
                log.debug("使用用户级别表格配置，tableCode: {}, userId: {}, tenantId: {}", 
                        tableCode, userId, tenantId);
                return userConfig;
            }
        }
        
        // 2. 查询租户级别配置
        FxTableConfigDTO tenantConfig = getTenantTableConfig(tableCode, tenantId);
        if (tenantConfig != null) {
            log.debug("使用租户级别表格配置，tableCode: {}, tenantId: {}", tableCode, tenantId);
            return tenantConfig;
        }
        
        // 3. 查询公共配置（tenant_id=0）
        FxTableConfigDTO publicConfig = getTenantTableConfig(tableCode, 0L);
        if (publicConfig != null) {
            log.debug("使用公共表格配置，tableCode: {}", tableCode);
            return publicConfig;
        }
        
        // 所有配置都不存在，返回 null
        log.debug("未找到表格配置，tableCode: {}, tenantId: {}, userId: {}", tableCode, tenantId, userId);
        return null;
    }
    
    /**
     * 查询用户级别表格配置
     * <p>
     * 从用户级别配置表中查询用户的个性化配置。
     * </p>
     *
     * @param tableCode 表编码
     * @param tenantId 租户 ID
     * @param userId 用户 ID
     * @return 用户级别表格配置 DTO，不存在时返回 null
     */
    private FxTableConfigDTO getUserTableConfig(String tableCode, Long tenantId, Long userId) {
        // 查询用户级别配置
        FxUserTableConfig userConfig = userTableConfigMapper.selectOne(
            new LambdaQueryWrapper<FxUserTableConfig>()
                .eq(FxUserTableConfig::getTableCode, tableCode)
                .eq(FxUserTableConfig::getTenantId, tenantId)
                .eq(FxUserTableConfig::getUserId, userId)
                .eq(FxUserTableConfig::getDeleted, 0)
                .last("limit 1")
        );
        
        if (userConfig == null) {
            return null;
        }
        
        // 从租户或公共配置加载基础配置（列配置和查询字段）
        FxTableConfigDTO baseConfig = getTenantTableConfig(tableCode, tenantId);
        if (baseConfig == null) {
            baseConfig = getTenantTableConfig(tableCode, 0L);
        }
        
        if (baseConfig == null) {
            return null;
        }
        
        // 构建用户配置 DTO
        FxTableConfigDTO dto = new FxTableConfigDTO();
        dto.setTableCode(userConfig.getTableCode());
        dto.setTableName(baseConfig.getTableName());
        dto.setTableType(baseConfig.getTableType());
        dto.setRowKey(baseConfig.getRowKey());
        dto.setColumns(filterVisibleColumns(mergeUserColumns(baseConfig.getColumns(), userConfig.getColumnConfig())));
        dto.setQueryFields(mergeUserQueryFields(tableCode, baseConfig.getQueryFields(), userConfig.getQueryConfig()));
        dto.setDefaultPageSize(userConfig.getPageSize() != null ? userConfig.getPageSize() : baseConfig.getDefaultPageSize());
        dto.setDefaultSortJson(userConfig.getSortConfig() != null ? userConfig.getSortConfig() : baseConfig.getDefaultSortJson());
        dto.setVersion(userConfig.getVersion());
        
        return dto;
    }
    
    /**
     * 查询租户级别表格配置
     * <p>
     * 从租户配置表中查询租户的配置。
     * </p>
     *
     * @param tableCode 表编码
     * @param tenantId 租户 ID
     * @return 租户级别表格配置 DTO，不存在时返回 null
     */
    private FxTableConfigDTO getTenantTableConfig(String tableCode, Long tenantId) {
        // 查询表编码和租户 ID 匹配的表格配置
        FxTableConfig cfg = tableConfigMapper.selectOne(new LambdaQueryWrapper<FxTableConfig>()
                .eq(FxTableConfig::getTableCode, tableCode)
                .eq(FxTableConfig::getTenantId, tenantId)  // 添加租户过滤
                .eq(FxTableConfig::getDeleted, 0)
                .orderByAsc(FxTableConfig::getId)
                .last("limit 1"));
        
        // 检查表格是否启用
        if (cfg == null || Boolean.FALSE.equals(cfg.getEnabled())) {
            return null;
        }

        // 查询表格的所有列配置
        List<FxTableColumnConfig> cols = tableColumnConfigMapper.selectList(new LambdaQueryWrapper<FxTableColumnConfig>()
                .eq(FxTableColumnConfig::getTableCode, tableCode)
                .eq(FxTableColumnConfig::getTenantId, tenantId)
                .eq(FxTableColumnConfig::getDeleted, 0));
        
        // 过滤启用的列并按排序字段排序
        cols.sort(Comparator.comparing(c -> c.getOrderNum() == null ? 0 : c.getOrderNum()));
        
        // 构建列 DTO 列表
        List<FxTableColumnDTO> columnDtos = new ArrayList<>();
        List<FxTableQueryFieldDTO> queryFields = new ArrayList<>();
        
        // 遍历列配置，构建 DTO
        for (FxTableColumnConfig c : cols) {
            // 跳过未启用的列
            if (Boolean.FALSE.equals(c.getEnabled())) {
                continue;
            }
            
            // 构建列 DTO
            FxTableColumnDTO cd = new FxTableColumnDTO();
            cd.setField(c.getField());
            cd.setTitle(resolveI18nText(c.getTitleI18nJson(), c.getField()));
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
            cd.setDictField(StringUtils.hasText(c.getField()) ? c.getField() + "Text" : null);
            cd.setRenderType(c.getRenderType());
            cd.setPermKey(c.getPermKey());
            columnDtos.add(cd);

            // 构建查询字段 DTO
            if (Boolean.TRUE.equals(c.getQueryable())) {
                FxTableQueryFieldDTO q = new FxTableQueryFieldDTO();
                q.setField(c.getField());
                q.setLabel(resolveI18nText(c.getTitleI18nJson(), c.getField()));
                q.setQueryType(StringUtils.hasText(c.getQueryType()) ? c.getQueryType() : "input");
                q.setQueryOperator(StringUtils.hasText(c.getQueryOperator()) ? c.getQueryOperator() : "like");
                q.setDictCode(c.getDictCode());
                queryFields.add(q);
            }
        }

        // 构建表格配置 DTO
        FxTableConfigDTO dto = new FxTableConfigDTO();
        dto.setTableCode(cfg.getTableCode());
        dto.setTableName(resolveI18nText(cfg.getTableNameI18nJson(), cfg.getTableCode()));
        dto.setTableType(cfg.getTableType());
        dto.setRowKey(StringUtils.hasText(cfg.getRowKey()) ? cfg.getRowKey() : "id");
        dto.setDefaultPageSize(cfg.getDefaultPageSize() == null ? 20 : cfg.getDefaultPageSize());
        dto.setDefaultSortJson(cfg.getDefaultSortJson());
        normalizeBuiltinColumns(tableCode, columnDtos);
        dto.setColumns(columnDtos);
        dto.setQueryFields(normalizeBuiltinQueryFields(tableCode, queryFields));
        dto.setVersion(cfg.getVersion());
        dto.setEnabled(cfg.getEnabled());
        dto.setCreateBy(cfg.getCreateBy());
        dto.setCreateTime(cfg.getCreateTime());
        dto.setUpdateBy(cfg.getUpdateBy());
        dto.setUpdateTime(cfg.getUpdateTime());
        return dto;
    }

    /**
     * 分页查询表格配置
     * <p>
     * 根据分页参数和查询条件分页查询表格配置列表。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>根据查询条件执行分页查询</li>
     *   <li>遍历查询结果，构建表格配置 DTO 列表</li>
     *   <li>解析表格名称的国际化文本</li>
     *   <li>组装分页结果并返回</li>
     * </ol>
     * 
     * @param page 分页参数
     * @param wrapper 查询条件包装器
     * @param tenantId 租户 ID，预留参数，当前版本未使用
     * @param userId 用户 ID，预留参数，当前版本未使用
     * @return 表格配置 DTO 分页结果
     */
    @Override
    public IPage<FxTableConfigDTO> page(Page<FxTableConfig> page, LambdaQueryWrapper<FxTableConfig> wrapper, Long tenantId, Long userId) {
        // 根据查询条件执行分页查询
        Page<FxTableConfig> result = tableConfigMapper.selectPage(page, wrapper);
        List<FxTableConfigDTO> dtoList = new ArrayList<>();
        
        // 遍历查询结果，构建表格配置 DTO 列表
        for (FxTableConfig config : result.getRecords()) {
            FxTableConfigDTO dto = new FxTableConfigDTO();
            dto.setId(config.getId());
            dto.setTableCode(config.getTableCode());
            dto.setTableName(resolveI18nText(config.getTableNameI18nJson(), config.getTableCode()));
            dto.setTableType(config.getTableType());
            dto.setRowKey(StringUtils.hasText(config.getRowKey()) ? config.getRowKey() : "id");
            dto.setDefaultPageSize(config.getDefaultPageSize() == null ? 20 : config.getDefaultPageSize());
            dto.setDefaultSortJson(config.getDefaultSortJson());
            dto.setVersion(config.getVersion());
            dto.setEnabled(config.getEnabled());
            dto.setCreateBy(config.getCreateBy());
            dto.setCreateTime(config.getCreateTime());
            dto.setUpdateBy(config.getUpdateBy());
            dto.setUpdateTime(config.getUpdateTime());
            dtoList.add(dto);
        }
        
        // 组装分页结果
        Page<FxTableConfigDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    /**
     * 解析国际化文本
     * <p>
     * 根据当前语言环境，从国际化 JSON 中解析对应的文本。
     * 支持语言回退机制，确保总能返回可用的文本。
     * </p>
     * <p><strong>语言回退顺序：</strong></p>
     * <ol>
     *   <li>当前语言（如"en-US"、"ja-JP"）</li>
     *   <li>当前语言的主语言部分（如"en"、"ja"）</li>
     *   <li>第一个可用的语言值</li>
     * </ol>
     * 
     * @param i18nJson 国际化 JSON 字符串
     * @param fallback 回退文本，当所有语言都不可用时使用
     * @return 解析后的国际化文本，解析失败时返回回退文本
     */
    private String resolveI18nText(String i18nJson, String fallback) {
        // 国际化 JSON 为空，直接返回回退文本
        if (!StringUtils.hasText(i18nJson)) {
            return fallback;
        }
        
        JSONObject obj;
        try {
            // 解析 JSON 为对象
            obj = JSONUtil.parseObj(i18nJson);
        } catch (Exception e) {
            return fallback;
        }
        
        // 获取当前语言环境
        String lang = LangContext.get();
        
        // 尝试获取当前语言的文本
        if (StringUtils.hasText(lang) && obj.containsKey(lang)) {
            String v = obj.getStr(lang);
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        
        // 尝试获取主语言的文本（如 en-US -> en）
        if (StringUtils.hasText(lang)) {
            int idx = lang.indexOf('-');
            if (idx > 0) {
                String prefix = lang.substring(0, idx);
                if (obj.containsKey(prefix)) {
                    String v = obj.getStr(prefix);
                    if (StringUtils.hasText(v)) {
                        return v;
                    }
                }
            }
        }
        
        // 尝试使用第一个可用的语言值
        try {
            for (String key : obj.keySet()) {
                String v = obj.getStr(key);
                if (StringUtils.hasText(v)) {
                    return v;
                }
            }
        } catch (Exception ignored) {}
        
        // 所有尝试都失败，返回回退文本
        return fallback;
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

    private List<FxTableColumnDTO> filterVisibleColumns(List<FxTableColumnDTO> columns) {
        List<FxTableColumnDTO> visibleColumns = new ArrayList<>();
        for (FxTableColumnDTO column : columns) {
            if (!Boolean.FALSE.equals(column.getVisible())) {
                visibleColumns.add(column);
            }
        }
        return visibleColumns;
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
            queryField.setQueryType("multiSelect");
            queryField.setQueryOperator("in");
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
}
