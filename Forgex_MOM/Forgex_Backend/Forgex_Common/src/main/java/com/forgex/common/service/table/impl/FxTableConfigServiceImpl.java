package com.forgex.common.service.table.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

@Service
@RequiredArgsConstructor
public class FxTableConfigServiceImpl implements FxTableConfigService {
    private final FxTableConfigMapper tableConfigMapper;
    private final FxTableColumnConfigMapper tableColumnConfigMapper;

    @Override
    public FxTableConfigDTO getTableConfig(String tableCode, Long tenantId, Long userId) {
        if (!StringUtils.hasText(tableCode) || tenantId == null) {
            return null;
        }
        FxTableConfig cfg = tableConfigMapper.selectOne(new LambdaQueryWrapper<FxTableConfig>()
                .eq(FxTableConfig::getTableCode, tableCode)
                .eq(FxTableConfig::getDeleted, 0)
                .last("limit 1"));
        if (cfg == null || Boolean.FALSE.equals(cfg.getEnabled())) {
            return null;
        }
        List<FxTableColumnConfig> cols = tableColumnConfigMapper.selectList(new LambdaQueryWrapper<FxTableColumnConfig>()
                .eq(FxTableColumnConfig::getTableCode, tableCode)
                .eq(FxTableColumnConfig::getDeleted, 0));

        cols.sort(Comparator.comparing(c -> c.getOrderNum() == null ? 0 : c.getOrderNum()));

        List<FxTableColumnDTO> columnDtos = new ArrayList<>();
        List<FxTableQueryFieldDTO> queryFields = new ArrayList<>();

        for (FxTableColumnConfig c : cols) {
            if (Boolean.FALSE.equals(c.getEnabled())) {
                continue;
            }
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
            cd.setRenderType(c.getRenderType());
            cd.setPermKey(c.getPermKey());
            columnDtos.add(cd);

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

    private String resolveI18nText(String i18nJson, String fallback) {
        if (!StringUtils.hasText(i18nJson)) {
            return fallback;
        }
        JSONObject obj;
        try {
            obj = JSONUtil.parseObj(i18nJson);
        } catch (Exception e) {
            return fallback;
        }
        String lang = LangContext.get();
        if (StringUtils.hasText(lang) && obj.containsKey(lang)) {
            String v = obj.getStr(lang);
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
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
        try {
            for (String key : obj.keySet()) {
                String v = obj.getStr(key);
                if (StringUtils.hasText(v)) {
                    return v;
                }
            }
        } catch (Exception ignored) {}
        return fallback;
    }
}
