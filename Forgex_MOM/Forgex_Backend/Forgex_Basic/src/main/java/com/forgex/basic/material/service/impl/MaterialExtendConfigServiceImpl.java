package com.forgex.basic.material.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.material.domain.entity.BasicMaterialExtendConfig;
import com.forgex.basic.material.domain.entity.BasicMaterialExtendSchema;
import com.forgex.basic.material.domain.param.MaterialExtendFieldSortParam;
import com.forgex.basic.material.domain.response.MaterialExtendConfigVO;
import com.forgex.basic.material.domain.response.MaterialExtendSchemaVO;
import com.forgex.basic.material.mapper.BasicMaterialExtendConfigMapper;
import com.forgex.basic.material.mapper.BasicMaterialExtendSchemaMapper;
import com.forgex.basic.material.service.IMaterialExtendConfigService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 物料扩展配置服务实现类。
 * <p>
 * 负责维护字段行配置，并同步生成按模块和物料类型分组的 schema_json。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see IMaterialExtendConfigService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialExtendConfigServiceImpl extends ServiceImpl<BasicMaterialExtendConfigMapper, BasicMaterialExtendConfig>
        implements IMaterialExtendConfigService {

    private static final String DEFAULT_MATERIAL_TYPE = "RAW_MATERIAL";

    private final BasicMaterialExtendConfigMapper extendConfigMapper;
    private final BasicMaterialExtendSchemaMapper extendSchemaMapper;

    @Override
    public IPage<MaterialExtendConfigVO> pageExtendConfigs(Long tenantId, String module,
                                                           Integer pageNum, Integer pageSize) {
        return pageExtendConfigs(tenantId, module, null, pageNum, pageSize);
    }

    @Override
    public IPage<MaterialExtendConfigVO> pageExtendConfigs(Long tenantId, String module, String materialType,
                                                           Integer pageNum, Integer pageSize) {
        Page<BasicMaterialExtendConfig> entityPage = new Page<>(safePageNum(pageNum), safePageSize(pageSize));
        LambdaQueryWrapper<BasicMaterialExtendConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtendConfig::getTenantId, tenantId)
                .eq(BasicMaterialExtendConfig::getDeleted, false)
                .eq(StringUtils.hasText(module), BasicMaterialExtendConfig::getModule, module)
                .eq(StringUtils.hasText(materialType), BasicMaterialExtendConfig::getMaterialType, materialType)
                .orderByAsc(BasicMaterialExtendConfig::getModule)
                .orderByAsc(BasicMaterialExtendConfig::getMaterialType)
                .orderByAsc(BasicMaterialExtendConfig::getOrderNum)
                .orderByDesc(BasicMaterialExtendConfig::getCreateTime);

        Page<BasicMaterialExtendConfig> configPage = extendConfigMapper.selectPage(entityPage, wrapper);
        IPage<MaterialExtendConfigVO> voPage = new Page<>(configPage.getCurrent(), configPage.getSize(), configPage.getTotal());
        voPage.setRecords(configPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<MaterialExtendConfigVO> getConfigsByModule(Long tenantId, String module) {
        if (!StringUtils.hasText(module)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.MODULE_CODE_EMPTY);
        }
        return listConfigs(tenantId, module, null, true).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialExtendConfigVO> getConfigsByScope(Long tenantId, String module, String materialType) {
        if (!StringUtils.hasText(module)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.MODULE_CODE_EMPTY);
        }
        return listConfigs(tenantId, module, normalizeMaterialType(materialType), true).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public MaterialExtendSchemaVO getSchema(Long tenantId, String module, String materialType) {
        String safeMaterialType = normalizeMaterialType(materialType);
        BasicMaterialExtendSchema schema = findSchema(tenantId, module, safeMaterialType);
        MaterialExtendSchemaVO vo = new MaterialExtendSchemaVO();
        if (schema != null) {
            BeanUtils.copyProperties(schema, vo);
        } else {
            vo.setModule(module);
            vo.setMaterialType(safeMaterialType);
            vo.setVersion(1);
            vo.setStatus(1);
            vo.setSchemaJson("[]");
        }
        vo.setModuleName(getModuleName(module));
        vo.setFields(getConfigsByScope(tenantId, module, safeMaterialType));
        return vo;
    }

    @Override
    public MaterialExtendConfigVO getConfigById(Long tenantId, Long id) {
        BasicMaterialExtendConfig config = requireConfig(tenantId, id);
        return convertToVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConfig(Long tenantId, BasicMaterialExtendConfig config) {
        fillDefaults(tenantId, config);
        validateConfigUniqueness(tenantId, config.getModule(), config.getMaterialType(), config.getFieldName(), null);
        extendConfigMapper.insert(config);
        refreshSchema(tenantId, config.getModule(), config.getMaterialType());
        log.info("创建物料扩展配置成功，模块: {}, 物料类型: {}, 字段: {}, 配置 ID: {}",
                config.getModule(), config.getMaterialType(), config.getFieldName(), config.getId());
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long tenantId, BasicMaterialExtendConfig config) {
        BasicMaterialExtendConfig existing = requireConfig(tenantId, config.getId());
        fillDefaults(tenantId, config);
        validateConfigUniqueness(tenantId, config.getModule(), config.getMaterialType(), config.getFieldName(), config.getId());
        extendConfigMapper.updateById(config);
        refreshSchema(tenantId, existing.getModule(), existing.getMaterialType());
        if (!Objects.equals(existing.getModule(), config.getModule())
                || !Objects.equals(existing.getMaterialType(), config.getMaterialType())) {
            refreshSchema(tenantId, config.getModule(), config.getMaterialType());
        }
        log.info("更新物料扩展配置成功，配置 ID: {}", config.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long tenantId, Long id) {
        BasicMaterialExtendConfig config = requireConfig(tenantId, id);
        extendConfigMapper.deleteById(id);
        refreshSchema(tenantId, config.getModule(), config.getMaterialType());
        log.info("删除物料扩展配置成功，配置 ID: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteConfigs(Long tenantId, List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<BasicMaterialExtendConfig> existing = extendConfigMapper.selectList(new LambdaQueryWrapper<BasicMaterialExtendConfig>()
                .eq(BasicMaterialExtendConfig::getTenantId, tenantId)
                .in(BasicMaterialExtendConfig::getId, ids));
        extendConfigMapper.delete(new LambdaQueryWrapper<BasicMaterialExtendConfig>()
                .eq(BasicMaterialExtendConfig::getTenantId, tenantId)
                .in(BasicMaterialExtendConfig::getId, ids));
        existing.stream()
                .map(item -> item.getModule() + "|" + normalizeMaterialType(item.getMaterialType()))
                .distinct()
                .forEach(scope -> {
                    String[] parts = scope.split("\\|", 2);
                    refreshSchema(tenantId, parts[0], parts[1]);
                });
        log.info("批量删除物料扩展配置成功，数量: {}", ids.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigStatus(Long tenantId, Long id, Integer status) {
        BasicMaterialExtendConfig config = requireConfig(tenantId, id);
        config.setStatus(status == null ? 1 : status);
        extendConfigMapper.updateById(config);
        refreshSchema(tenantId, config.getModule(), config.getMaterialType());
        log.info("更新物料扩展配置状态成功，配置 ID: {}, 状态: {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortConfigs(Long tenantId, List<MaterialExtendFieldSortParam.Item> items) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        List<String> scopes = new ArrayList<>();
        for (MaterialExtendFieldSortParam.Item item : items) {
            if (item == null || item.getId() == null) {
                continue;
            }
            BasicMaterialExtendConfig config = requireConfig(tenantId, item.getId());
            config.setOrderNum(item.getOrderNum() == null ? 0 : item.getOrderNum());
            extendConfigMapper.updateById(config);
            scopes.add(config.getModule() + "|" + normalizeMaterialType(config.getMaterialType()));
        }
        scopes.stream().distinct().forEach(scope -> {
            String[] parts = scope.split("\\|", 2);
            refreshSchema(tenantId, parts[0], parts[1]);
        });
    }

    private List<BasicMaterialExtendConfig> listConfigs(Long tenantId, String module, String materialType, boolean onlyEnabled) {
        LambdaQueryWrapper<BasicMaterialExtendConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtendConfig::getTenantId, tenantId)
                .eq(StringUtils.hasText(module), BasicMaterialExtendConfig::getModule, module)
                .eq(StringUtils.hasText(materialType), BasicMaterialExtendConfig::getMaterialType, materialType)
                .eq(onlyEnabled, BasicMaterialExtendConfig::getStatus, 1)
                .eq(BasicMaterialExtendConfig::getDeleted, false)
                .orderByAsc(BasicMaterialExtendConfig::getOrderNum)
                .orderByAsc(BasicMaterialExtendConfig::getId);
        return extendConfigMapper.selectList(wrapper);
    }

    private BasicMaterialExtendConfig requireConfig(Long tenantId, Long id) {
        BasicMaterialExtendConfig config = id == null ? null : extendConfigMapper.selectById(id);
        if (config == null || !Objects.equals(config.getTenantId(), tenantId) || Boolean.TRUE.equals(config.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.MATERIAL_EXTEND_CONFIG_NOT_FOUND);
        }
        return config;
    }

    private void fillDefaults(Long tenantId, BasicMaterialExtendConfig config) {
        config.setTenantId(tenantId);
        config.setMaterialType(normalizeMaterialType(config.getMaterialType()));
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getRequired() == null) {
            config.setRequired(0);
        }
        if (config.getOrderNum() == null) {
            Integer maxOrder = listConfigs(tenantId, config.getModule(), config.getMaterialType(), false).stream()
                    .map(BasicMaterialExtendConfig::getOrderNum)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0);
            config.setOrderNum(maxOrder + 1);
        }
        if (!StringUtils.hasText(config.getFieldType())) {
            config.setFieldType("STRING");
        }
    }

    private void validateConfigUniqueness(Long tenantId, String module, String materialType, String fieldName, Long excludeId) {
        LambdaQueryWrapper<BasicMaterialExtendConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtendConfig::getTenantId, tenantId)
                .eq(BasicMaterialExtendConfig::getModule, module)
                .eq(BasicMaterialExtendConfig::getMaterialType, normalizeMaterialType(materialType))
                .eq(BasicMaterialExtendConfig::getFieldName, fieldName)
                .eq(BasicMaterialExtendConfig::getDeleted, false);

        if (excludeId != null) {
            wrapper.ne(BasicMaterialExtendConfig::getId, excludeId);
        }

        Long count = extendConfigMapper.selectCount(wrapper);
        if (count > 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.FIELD_NAME_EXISTS, module, fieldName);
        }
    }

    private void refreshSchema(Long tenantId, String module, String materialType) {
        if (!StringUtils.hasText(module)) {
            return;
        }
        String safeMaterialType = normalizeMaterialType(materialType);
        List<MaterialExtendConfigVO> fields = listConfigs(tenantId, module, safeMaterialType, true).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        BasicMaterialExtendSchema schema = findSchema(tenantId, module, safeMaterialType);
        if (schema == null) {
            schema = new BasicMaterialExtendSchema();
            schema.setTenantId(tenantId);
            schema.setModule(module);
            schema.setMaterialType(safeMaterialType);
            schema.setVersion(1);
            schema.setStatus(1);
            schema.setOrderNum(0);
            schema.setSchemaJson(toSchemaJson(fields));
            extendSchemaMapper.insert(schema);
            return;
        }
        schema.setSchemaJson(toSchemaJson(fields));
        schema.setVersion(schema.getVersion() == null ? 1 : schema.getVersion() + 1);
        schema.setStatus(1);
        extendSchemaMapper.updateById(schema);
    }

    private BasicMaterialExtendSchema findSchema(Long tenantId, String module, String materialType) {
        if (!StringUtils.hasText(module)) {
            return null;
        }
        return extendSchemaMapper.selectOne(new LambdaQueryWrapper<BasicMaterialExtendSchema>()
                .eq(BasicMaterialExtendSchema::getTenantId, tenantId)
                .eq(BasicMaterialExtendSchema::getModule, module)
                .eq(BasicMaterialExtendSchema::getMaterialType, normalizeMaterialType(materialType))
                .eq(BasicMaterialExtendSchema::getDeleted, false)
                .last("LIMIT 1"));
    }

    private String toSchemaJson(List<MaterialExtendConfigVO> fields) {
        List<Map<String, Object>> schema = fields.stream().map(field -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", field.getId());
            item.put("module", field.getModule());
            item.put("materialType", field.getMaterialType());
            item.put("fieldName", field.getFieldName());
            item.put("fieldLabel", field.getFieldLabel());
            item.put("fieldType", field.getFieldType());
            item.put("fieldTypeName", field.getFieldTypeName());
            item.put("options", field.getOptions());
            item.put("required", field.getRequired());
            item.put("validationRule", field.getValidationRule());
            item.put("defaultValue", field.getDefaultValue());
            item.put("orderNum", field.getOrderNum());
            item.put("status", field.getStatus());
            return item;
        }).collect(Collectors.toList());
        return JSON.toJSONString(schema, SerializerFeature.WriteMapNullValue);
    }

    private MaterialExtendConfigVO convertToVO(BasicMaterialExtendConfig config) {
        MaterialExtendConfigVO vo = new MaterialExtendConfigVO();
        BeanUtils.copyProperties(config, vo);
        vo.setModuleName(getModuleName(config.getModule()));
        vo.setFieldTypeName(getFieldTypeName(config.getFieldType()));
        vo.setOptions(parseOptions(config.getFieldOptions()));
        return vo;
    }

    private String normalizeMaterialType(String materialType) {
        return StringUtils.hasText(materialType) ? materialType : DEFAULT_MATERIAL_TYPE;
    }

    private int safePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int safePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 20 : pageSize;
    }

    private String getModuleName(String module) {
        if (!StringUtils.hasText(module)) {
            return "";
        }
        return switch (module) {
            case "PURCHASE" -> "采购";
            case "INVENTORY" -> "库存";
            case "PRODUCTION" -> "生产";
            case "SALES" -> "销售";
            default -> module;
        };
    }

    private String getFieldTypeName(String fieldType) {
        if (!StringUtils.hasText(fieldType)) {
            return "";
        }
        return switch (fieldType) {
            case "STRING" -> "字符串";
            case "NUMBER" -> "数字";
            case "DATE" -> "日期";
            case "BOOLEAN" -> "布尔";
            case "SELECT" -> "下拉框";
            case "MULTI_SELECT" -> "多选";
            case "TEXT" -> "长文本";
            default -> fieldType;
        };
    }

    private List<Map<String, String>> parseOptions(String fieldOptions) {
        if (!StringUtils.hasText(fieldOptions)) {
            return Collections.emptyList();
        }

        try {
            List<Map<String, String>> options = new ArrayList<>();
            List<Map> rawList = JSON.parseArray(fieldOptions, Map.class);
            for (Map map : rawList) {
                Map<String, String> optionMap = new HashMap<>();
                map.forEach((key, value) -> {
                    if (key instanceof String && value != null) {
                        optionMap.put((String) key, String.valueOf(value));
                    }
                });
                options.add(optionMap);
            }
            return options;
        } catch (Exception e) {
            log.error("解析字段选项 JSON 失败: {}", fieldOptions, e);
            return Collections.emptyList();
        }
    }
}
