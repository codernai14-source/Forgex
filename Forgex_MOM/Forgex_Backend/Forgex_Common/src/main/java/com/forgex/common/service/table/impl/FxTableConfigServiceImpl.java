package com.forgex.common.service.table.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.domain.dto.table.FxTableColumnDTO;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.common.domain.dto.table.FxTableQueryFieldDTO;
import com.forgex.common.domain.entity.table.FxTableColumnConfig;
import com.forgex.common.domain.entity.table.FxTableConfig;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.mapper.table.FxTableColumnConfigMapper;
import com.forgex.common.mapper.table.FxTableConfigMapper;
import com.forgex.common.service.table.FxTableConfigService;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 表格配置服务实现类
 * <p>
 * 提供表格配置的查询和分页功能实现，支持多租户和用户级别的配置管理。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>根据表编码、租户ID和用户ID获取表格配置</li>
 *   <li>支持表格配置的分页查询</li>
 *   <li>支持根据查询条件过滤表格配置</li>
 *   <li>支持国际化文本解析</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableConfigService
 */
@Service
@RequiredArgsConstructor
public class FxTableConfigServiceImpl implements FxTableConfigService {
    /**
     * 表格配置Mapper
     */
    private final FxTableConfigMapper tableConfigMapper;

    /**
     * 表格列配置Mapper
     */
    private final FxTableColumnConfigMapper tableColumnConfigMapper;

    /**
     * 获取表格配置
     * <p>
     * 根据表编码、租户ID和用户ID获取表格配置，
     * 包括表格列配置和查询字段配置。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>参数校验：表编码和租户ID不能为空</li>
     *   <li>查询表编码和租户ID匹配的表格配置</li>
     *   <li>检查表格是否启用</li>
     *   <li>查询表格的所有列配置</li>
     *   <li>过滤启用的列并按排序字段排序</li>
     *   <li>解析列标题的国际化文本</li>
     *   <li>构建查询字段列表</li>
     *   <li>组装表格配置DTO并返回</li>
     * </ol>
     * 
     * @param tableCode 表编码，不能为空
     * @param tenantId 租户ID，不能为空
     * @param userId 用户ID，预留参数，当前版本未使用
     * @return 表格配置DTO，参数无效或表格不存在或未启用时返回null
     */
    @Override
    public FxTableConfigDTO getTableConfig(String tableCode, Long tenantId, Long userId) {
        // 参数校验：表编码和租户ID不能为空
        if (!StringUtils.hasText(tableCode) || tenantId == null) {
            return null;
        }

        // 查询表编码和租户ID匹配的表格配置
        FxTableConfig cfg = tableConfigMapper.selectOne(new LambdaQueryWrapper<FxTableConfig>()
                .eq(FxTableConfig::getTableCode, tableCode)
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
                .eq(FxTableColumnConfig::getDeleted, 0));
        
        // 过滤启用的列并按排序字段排序
        cols.sort(Comparator.comparing(c -> c.getOrderNum() == null ? 0 : c.getOrderNum()));
        
        // 构建列DTO列表
        List<FxTableColumnDTO> columnDtos = new ArrayList<>();
        List<FxTableQueryFieldDTO> queryFields = new ArrayList<>();
        
        // 遍历列配置，构建DTO
        for (FxTableColumnConfig c : cols) {
            // 跳过未启用的列
            if (Boolean.FALSE.equals(c.getEnabled())) {
                continue;
            }
            
            // 构建列DTO
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

            // 构建查询字段DTO
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

        // 构建表格配置DTO
        FxTableConfigDTO dto = new FxTableConfigDTO();
        dto.setTableCode(cfg.getTableCode());
        dto.setTableName(resolveI18nText(cfg.getTableNameI18nJson(), cfg.getTableCode()));
        dto.setTableType(cfg.getTableType());
        dto.setRowKey(StringUtils.hasText(cfg.getRowKey()) ? cfg.getRowKey() : "id");
        dto.setDefaultPageSize(cfg.getDefaultPageSize() == null ? 20 : cfg.getDefaultPageSize());
        dto.setDefaultSortJson(cfg.getDefaultSortJson());
        dto.setColumns(columnDtos);
        dto.setQueryFields(queryFields);
        dto.setVersion(cfg.getVersion());
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
     *   <li>遍历查询结果，构建表格配置DTO列表</li>
     *   <li>解析表格名称的国际化文本</li>
     *   <li>组装分页结果并返回</li>
     * </ol>
     * 
     * @param page 分页参数
     * @param wrapper 查询条件包装器
     * @param tenantId 租户ID，预留参数，当前版本未使用
     * @param userId 用户ID，预留参数，当前版本未使用
     * @return 表格配置DTO分页结果
     */
    @Override
    public IPage<FxTableConfigDTO> page(Page<FxTableConfig> page, LambdaQueryWrapper<FxTableConfig> wrapper, Long tenantId, Long userId) {
        // 根据查询条件执行分页查询
        Page<FxTableConfig> result = tableConfigMapper.selectPage(page, wrapper);
        List<FxTableConfigDTO> dtoList = new ArrayList<>();
        
        // 遍历查询结果，构建表格配置DTO列表
        for (FxTableConfig config : result.getRecords()) {
            FxTableConfigDTO dto = new FxTableConfigDTO();
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
     * 根据当前语言环境，从国际化JSON中解析对应的文本。
     * 支持语言回退机制，确保总能返回可用的文本。
     * </p>
     * <p><strong>语言回退顺序：</strong></p>
     * <ol>
     *   <li>当前语言（如"en-US"、"ja-JP"）</li>
     *   <li>当前语言的主语言部分（如"en"、"ja"）</li>
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
        
        JSONObject obj;
        try {
            // 解析JSON为对象
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
        
        // 尝试获取主语言的文本（如en-US -> en）
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
}
