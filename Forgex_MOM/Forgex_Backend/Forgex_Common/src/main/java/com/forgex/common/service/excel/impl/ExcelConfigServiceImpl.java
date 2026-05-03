package com.forgex.common.service.excel.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigItemDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigItemDTO;
import com.forgex.common.domain.entity.excel.FxExcelExportConfig;
import com.forgex.common.domain.entity.excel.FxExcelExportConfigItem;
import com.forgex.common.domain.entity.excel.FxExcelImportConfig;
import com.forgex.common.domain.entity.excel.FxExcelImportConfigItem;
import com.forgex.common.mapper.excel.FxExcelExportConfigItemMapper;
import com.forgex.common.mapper.excel.FxExcelExportConfigMapper;
import com.forgex.common.mapper.excel.FxExcelImportConfigItemMapper;
import com.forgex.common.mapper.excel.FxExcelImportConfigMapper;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.provider.TemplateOptionProviderRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Excel 导入导出配置服务实现。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ExcelConfigServiceImpl implements ExcelConfigService {

    private final FxExcelExportConfigMapper exportConfigMapper;
    private final FxExcelExportConfigItemMapper exportItemMapper;
    private final FxExcelImportConfigMapper importConfigMapper;
    private final FxExcelImportConfigItemMapper importItemMapper;
    private final TemplateOptionProviderRegistry providerRegistry;

    @Override
    public IPage<FxExcelExportConfigDTO> pageExportConfig(Page<FxExcelExportConfigDTO> page, String tableName, String tableCode, Boolean enabled) {
        Page<FxExcelExportConfig> p = new Page<>(page.getCurrent(), page.getSize());
        IPage<FxExcelExportConfig> entityPage = exportConfigMapper.selectPage(p, new LambdaQueryWrapper<FxExcelExportConfig>()
                .like(StringUtils.hasText(tableName), FxExcelExportConfig::getTableName, tableName)
                .like(StringUtils.hasText(tableCode), FxExcelExportConfig::getTableCode, tableCode)
                .eq(enabled != null, FxExcelExportConfig::getEnabled, enabled)
                .orderByDesc(FxExcelExportConfig::getId));

        Page<FxExcelExportConfigDTO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(this::toExportDtoNoItems).collect(Collectors.toList()));
        return result;
    }

    @Override
    public FxExcelExportConfigDTO getExportConfig(Long id) {
        FxExcelExportConfig cfg = exportConfigMapper.selectById(id);
        if (cfg == null) {
            return null;
        }
        FxExcelExportConfigDTO dto = toExportDtoNoItems(cfg);
        dto.setItems(listExportItems(cfg.getId()));
        return dto;
    }

    @Override
    public FxExcelExportConfigDTO getExportConfigByCode(String tableCode) {
        if (!StringUtils.hasText(tableCode)) {
            return null;
        }
        FxExcelExportConfig cfg = exportConfigMapper.selectOne(new LambdaQueryWrapper<FxExcelExportConfig>()
                .eq(FxExcelExportConfig::getTableCode, tableCode)
                .last("limit 1"));
        if (cfg == null) {
            return null;
        }
        FxExcelExportConfigDTO dto = toExportDtoNoItems(cfg);
        dto.setItems(listExportItems(cfg.getId()));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveExportConfig(FxExcelExportConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        FxExcelExportConfig entity;
        if (dto.getId() == null) {
            entity = new FxExcelExportConfig();
        } else {
            entity = exportConfigMapper.selectById(dto.getId());
            if (entity == null) {
                entity = new FxExcelExportConfig();
                entity.setId(dto.getId());
            }
        }
        entity.setTableName(dto.getTableName());
        entity.setTableCode(dto.getTableCode());
        entity.setHeaderStyleJson(dto.getHeaderStyleJson());
        entity.setTitle(dto.getTitle());
        entity.setSubtitle(dto.getSubtitle());
        entity.setExportFormat(dto.getExportFormat());
        entity.setEnabled(dto.getEnabled() == null ? Boolean.TRUE : dto.getEnabled());
        entity.setEnableTotal(dto.getEnableTotal());
        entity.setVersion(dto.getVersion());

        if (entity.getId() == null) {
            exportConfigMapper.insert(entity);
        } else {
            exportConfigMapper.updateById(entity);
            exportItemMapper.delete(new LambdaQueryWrapper<FxExcelExportConfigItem>().eq(FxExcelExportConfigItem::getConfigId, entity.getId()));
        }

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (FxExcelExportConfigItemDTO item : dto.getItems()) {
                FxExcelExportConfigItem it = new FxExcelExportConfigItem();
                it.setConfigId(entity.getId());
                it.setExportField(item.getExportField());
                it.setFieldName(item.getFieldName());
                it.setI18nJson(item.getI18nJson());
                it.setHeaderStyleJson(item.getHeaderStyleJson());
                it.setCellStyleJson(item.getCellStyleJson());
                it.setOrderNum(item.getOrderNum());
                exportItemMapper.insert(it);
            }
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteExportConfig(Long id) {
        if (id == null) {
            return false;
        }
        exportItemMapper.delete(new LambdaQueryWrapper<FxExcelExportConfigItem>().eq(FxExcelExportConfigItem::getConfigId, id));
        return exportConfigMapper.deleteById(id) > 0;
    }

    @Override
    public IPage<FxExcelImportConfigDTO> pageImportConfig(Page<FxExcelImportConfigDTO> page, String tableName, String tableCode) {
        Page<FxExcelImportConfig> p = new Page<>(page.getCurrent(), page.getSize());
        IPage<FxExcelImportConfig> entityPage = importConfigMapper.selectPage(p, new LambdaQueryWrapper<FxExcelImportConfig>()
                .like(StringUtils.hasText(tableName), FxExcelImportConfig::getTableName, tableName)
                .like(StringUtils.hasText(tableCode), FxExcelImportConfig::getTableCode, tableCode)
                .orderByDesc(FxExcelImportConfig::getId));

        Page<FxExcelImportConfigDTO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(this::toImportDtoNoItems).collect(Collectors.toList()));
        return result;
    }

    @Override
    public FxExcelImportConfigDTO getImportConfig(Long id) {
        FxExcelImportConfig cfg = importConfigMapper.selectById(id);
        if (cfg == null) {
            return null;
        }
        FxExcelImportConfigDTO dto = toImportDtoNoItems(cfg);
        dto.setItems(listImportItems(cfg.getId()));
        return dto;
    }

    @Override
    public FxExcelImportConfigDTO getImportConfigByCode(String tableCode) {
        if (!StringUtils.hasText(tableCode)) {
            return null;
        }
        FxExcelImportConfig cfg = importConfigMapper.selectOne(new LambdaQueryWrapper<FxExcelImportConfig>()
                .eq(FxExcelImportConfig::getTableCode, tableCode)
                .last("limit 1"));
        if (cfg == null) {
            return null;
        }
        FxExcelImportConfigDTO dto = toImportDtoNoItems(cfg);
        dto.setItems(listImportItems(cfg.getId()));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveImportConfig(FxExcelImportConfigDTO dto) {
        if (dto == null) {
            return null;
        }
        FxExcelImportConfig entity;
        if (dto.getId() == null) {
            entity = new FxExcelImportConfig();
        } else {
            entity = importConfigMapper.selectById(dto.getId());
            if (entity == null) {
                entity = new FxExcelImportConfig();
                entity.setId(dto.getId());
            }
        }
        entity.setTableName(dto.getTableName());
        entity.setTableCode(dto.getTableCode());
        entity.setHandlerBeanName(dto.getHandlerBeanName());
        entity.setImportPermission(dto.getImportPermission());
        entity.setTitle(dto.getTitle());
        entity.setTitleI18nJson(dto.getTitleI18nJson());
        entity.setSubtitle(dto.getSubtitle());
        entity.setSubtitleI18nJson(dto.getSubtitleI18nJson());
        entity.setSubtitleStyleJson(dto.getSubtitleStyleJson());
        entity.setVersion(dto.getVersion());

        if (entity.getId() == null) {
            importConfigMapper.insert(entity);
        } else {
            importConfigMapper.updateById(entity);
            importItemMapper.delete(new LambdaQueryWrapper<FxExcelImportConfigItem>().eq(FxExcelImportConfigItem::getConfigId, entity.getId()));
        }

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (FxExcelImportConfigItemDTO item : dto.getItems()) {
                FxExcelImportConfigItem it = new FxExcelImportConfigItem();
                it.setConfigId(entity.getId());
                it.setSheetCode(item.getSheetCode());
                it.setSheetName(item.getSheetName());
                it.setI18nJson(item.getI18nJson());
                it.setImportField(item.getImportField());
                it.setFieldType(item.getFieldType());
                it.setDictCode(item.getDictCode());
                it.setDataSourceType(item.getDataSourceType());
                it.setDataSourceValue(item.getDataSourceValue());
                it.setDependsOnFieldKey(item.getDependsOnFieldKey());
                it.setSeparator(item.getSeparator());
                it.setFieldRemark(item.getFieldRemark());
                it.setRequired(item.getRequired());
                it.setOrderNum(item.getOrderNum());
                importItemMapper.insert(it);
            }
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteImportConfig(Long id) {
        if (id == null) {
            return false;
        }
        importItemMapper.delete(new LambdaQueryWrapper<FxExcelImportConfigItem>().eq(FxExcelImportConfigItem::getConfigId, id));
        return importConfigMapper.deleteById(id) > 0;
    }

    private FxExcelExportConfigDTO toExportDtoNoItems(FxExcelExportConfig cfg) {
        FxExcelExportConfigDTO dto = new FxExcelExportConfigDTO();
        dto.setId(cfg.getId());
        dto.setTableName(cfg.getTableName());
        dto.setTableCode(cfg.getTableCode());
        dto.setHeaderStyleJson(cfg.getHeaderStyleJson());
        dto.setTitle(cfg.getTitle());
        dto.setSubtitle(cfg.getSubtitle());
        dto.setExportFormat(cfg.getExportFormat());
        dto.setEnabled(cfg.getEnabled());
        dto.setEnableTotal(cfg.getEnableTotal());
        dto.setVersion(cfg.getVersion());
        return dto;
    }

    private List<FxExcelExportConfigItemDTO> listExportItems(Long configId) {
        if (configId == null) {
            return Collections.emptyList();
        }
        List<FxExcelExportConfigItem> list = exportItemMapper.selectList(new LambdaQueryWrapper<FxExcelExportConfigItem>()
                .eq(FxExcelExportConfigItem::getConfigId, configId)
                .orderByAsc(FxExcelExportConfigItem::getOrderNum)
                .orderByAsc(FxExcelExportConfigItem::getId));
        return list.stream().map(it -> {
            FxExcelExportConfigItemDTO dto = new FxExcelExportConfigItemDTO();
            dto.setId(it.getId());
            dto.setConfigId(it.getConfigId());
            dto.setExportField(it.getExportField());
            dto.setFieldName(it.getFieldName());
            dto.setI18nJson(it.getI18nJson());
            dto.setHeaderStyleJson(it.getHeaderStyleJson());
            dto.setCellStyleJson(it.getCellStyleJson());
            dto.setOrderNum(it.getOrderNum());
            return dto;
        }).collect(Collectors.toList());
    }

    private FxExcelImportConfigDTO toImportDtoNoItems(FxExcelImportConfig cfg) {
        FxExcelImportConfigDTO dto = new FxExcelImportConfigDTO();
        dto.setId(cfg.getId());
        dto.setTableName(cfg.getTableName());
        dto.setTableCode(cfg.getTableCode());
        dto.setHandlerBeanName(cfg.getHandlerBeanName());
        dto.setImportPermission(cfg.getImportPermission());
        dto.setTitle(cfg.getTitle());
        dto.setTitleI18nJson(cfg.getTitleI18nJson());
        dto.setSubtitle(cfg.getSubtitle());
        dto.setSubtitleI18nJson(cfg.getSubtitleI18nJson());
        dto.setSubtitleStyleJson(cfg.getSubtitleStyleJson());
        dto.setVersion(cfg.getVersion());
        return dto;
    }

    private List<FxExcelImportConfigItemDTO> listImportItems(Long configId) {
        if (configId == null) {
            return Collections.emptyList();
        }
        List<FxExcelImportConfigItem> list = importItemMapper.selectList(new LambdaQueryWrapper<FxExcelImportConfigItem>()
                .eq(FxExcelImportConfigItem::getConfigId, configId)
                .orderByAsc(FxExcelImportConfigItem::getSheetCode)
                .orderByAsc(FxExcelImportConfigItem::getOrderNum)
                .orderByAsc(FxExcelImportConfigItem::getId));
        return list.stream().map(it -> {
            FxExcelImportConfigItemDTO dto = new FxExcelImportConfigItemDTO();
            dto.setId(it.getId());
            dto.setConfigId(it.getConfigId());
            dto.setSheetCode(it.getSheetCode());
            dto.setSheetName(it.getSheetName());
            dto.setI18nJson(it.getI18nJson());
            dto.setImportField(it.getImportField());
            dto.setFieldType(it.getFieldType());
            dto.setDictCode(it.getDictCode());
            dto.setDataSourceType(it.getDataSourceType());
            dto.setDataSourceValue(it.getDataSourceValue());
            dto.setDependsOnFieldKey(it.getDependsOnFieldKey());
            dto.setSeparator(it.getSeparator());
            dto.setFieldRemark(it.getFieldRemark());
            dto.setRequired(it.getRequired());
            dto.setOrderNum(it.getOrderNum());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public java.util.List<String> listProviderCodes() {
        return providerRegistry.getAllProviderCodes();
    }
}
