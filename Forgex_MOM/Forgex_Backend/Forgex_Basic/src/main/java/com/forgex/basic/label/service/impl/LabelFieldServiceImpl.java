package com.forgex.basic.label.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.label.domain.entity.LabelField;
import com.forgex.basic.label.domain.param.LabelFieldQueryParam;
import com.forgex.basic.label.domain.param.LabelFieldSaveParam;
import com.forgex.basic.label.domain.param.LabelFieldUpdateParam;
import com.forgex.basic.label.domain.vo.LabelFieldVO;
import com.forgex.basic.label.mapper.LabelFieldMapper;
import com.forgex.basic.label.service.LabelFieldService;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.domain.dto.excel.TemplateOption;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.SysModuleDTO;
import com.forgex.sys.service.ISysModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelFieldServiceImpl extends ServiceImpl<LabelFieldMapper, LabelField> implements LabelFieldService {

    private static final Set<String> FIELD_TYPES = Set.of("STRING", "NUMBER", "DATE", "DATETIME", "BOOLEAN");

    private final LabelFieldMapper labelFieldMapper;
    private final ISysModuleService moduleService;

    @Override
    public IPage<LabelFieldVO> pageFields(LabelFieldQueryParam param, Long tenantId) {
        Page<LabelField> page = new Page<>(param.getPageNum(), param.getPageSize());
        IPage<LabelField> entityPage = labelFieldMapper.selectPage(page, buildQuery(param, tenantId));
        Page<LabelFieldVO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return result;
    }

    @Override
    public LabelFieldVO getById(Long id, Long tenantId) {
        return toVO(requireField(id, tenantId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addField(LabelFieldSaveParam param, Long tenantId) {
        validateField(param.getFieldCode(), param.getFieldName(), param.getFieldType(), param.getModuleId());
        if (existsByCode(param.getFieldCode(), param.getModuleId(), tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_CODE_EXISTS, param.getFieldCode());
        }
        LabelField entity = new LabelField();
        BeanUtils.copyProperties(param, entity);
        entity.setTenantId(tenantId);
        entity.setIsEnabled(param.getIsEnabled() == null || param.getIsEnabled());
        labelFieldMapper.insert(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateField(LabelFieldUpdateParam param, Long tenantId) {
        LabelField entity = requireField(param.getId(), tenantId);
        String fieldCode = StringUtils.hasText(param.getFieldCode()) ? param.getFieldCode() : entity.getFieldCode();
        String fieldName = StringUtils.hasText(param.getFieldName()) ? param.getFieldName() : entity.getFieldName();
        String fieldType = StringUtils.hasText(param.getFieldType()) ? param.getFieldType() : entity.getFieldType();
        Long moduleId = param.getModuleId() == null ? entity.getModuleId() : param.getModuleId();
        validateField(fieldCode, fieldName, fieldType, moduleId);
        if (existsByCodeExcludeId(fieldCode, moduleId, entity.getId(), tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_CODE_EXISTS, fieldCode);
        }
        entity.setFieldCode(fieldCode);
        entity.setFieldName(fieldName);
        entity.setFieldType(fieldType);
        entity.setModuleId(moduleId);
        if (param.getIsEnabled() != null) {
            entity.setIsEnabled(param.getIsEnabled());
        }
        labelFieldMapper.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteField(Long id, Long tenantId) {
        requireField(id, tenantId);
        labelFieldMapper.update(null, new LambdaUpdateWrapper<LabelField>()
                .eq(LabelField::getId, id)
                .set(LabelField::getDeleted, true));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDeleteFields(List<Long> ids, Long tenantId) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            deleteField(id, tenantId);
        }
    }

    @Override
    public boolean existsByCode(String fieldCode, Long moduleId, Long tenantId) {
        return labelFieldMapper.selectCount(new LambdaQueryWrapper<LabelField>()
                .eq(LabelField::getTenantId, tenantId)
                .eq(LabelField::getFieldCode, fieldCode)
                .eq(LabelField::getModuleId, moduleId)
                .eq(LabelField::getDeleted, false)) > 0;
    }

    @Override
    public boolean existsByCodeExcludeId(String fieldCode, Long moduleId, Long excludeId, Long tenantId) {
        return labelFieldMapper.selectCount(new LambdaQueryWrapper<LabelField>()
                .eq(LabelField::getTenantId, tenantId)
                .eq(LabelField::getFieldCode, fieldCode)
                .eq(LabelField::getModuleId, moduleId)
                .ne(LabelField::getId, excludeId)
                .eq(LabelField::getDeleted, false)) > 0;
    }

    @Override
    public List<TemplateOption> options(Long tenantId) {
        return labelFieldMapper.selectList(new LambdaQueryWrapper<LabelField>()
                        .eq(LabelField::getTenantId, tenantId)
                        .eq(LabelField::getDeleted, false)
                        .eq(LabelField::getIsEnabled, true)
                        .orderByAsc(LabelField::getFieldCode))
                .stream()
                .map(field -> new TemplateOption(field.getFieldCode(), field.getFieldName()))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param) {
        FxExcelImportResultDTO result = new FxExcelImportResultDTO();
        List<Map<String, Object>> rows = param == null || param.getImportData() == null
                ? List.of()
                : param.getImportData().getOrDefault("main", List.of());
        result.setTotalCount(rows.size());
        Long tenantId = TenantContext.get();
        for (Map<String, Object> row : rows) {
            try {
                LabelFieldSaveParam saveParam = toSaveParam(row);
                LabelField existing = findByCode(tenantId, saveParam.getFieldCode(), saveParam.getModuleId());
                if (existing == null) {
                    if ("UPDATE".equalsIgnoreCase(param.getImportMode())) {
                        result.increaseSkipped();
                    } else {
                        addField(saveParam, tenantId);
                        result.increaseCreated();
                    }
                } else if ("ADD".equalsIgnoreCase(param.getImportMode())) {
                    result.increaseSkipped();
                } else {
                    LabelFieldUpdateParam updateParam = new LabelFieldUpdateParam();
                    BeanUtils.copyProperties(saveParam, updateParam);
                    updateParam.setId(existing.getId());
                    updateField(updateParam, tenantId);
                    result.increaseUpdated();
                }
            } catch (Exception ex) {
                log.warn("标签字段导入失败，row={}", row, ex);
                result.addError(String.valueOf(row));
            }
        }
        return result;
    }

    private LabelFieldSaveParam toSaveParam(Map<String, Object> row) {
        LabelFieldSaveParam param = new LabelFieldSaveParam();
        param.setFieldCode(str(row.get("fieldCode")));
        param.setFieldName(str(row.get("fieldName")));
        param.setFieldType(normalizeType(str(row.get("fieldType"))));
        param.setModuleId(toLong(row.get("moduleId")));
        param.setIsEnabled(toBoolean(row.get("isEnabled")));
        return param;
    }

    private LambdaQueryWrapper<LabelField> buildQuery(LabelFieldQueryParam param, Long tenantId) {
        LambdaQueryWrapper<LabelField> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelField::getTenantId, tenantId)
                .eq(LabelField::getDeleted, false);
        if (param != null) {
            wrapper.like(StringUtils.hasText(param.getFieldCode()), LabelField::getFieldCode, param.getFieldCode())
                    .like(StringUtils.hasText(param.getFieldName()), LabelField::getFieldName, param.getFieldName())
                    .eq(StringUtils.hasText(param.getFieldType()), LabelField::getFieldType, param.getFieldType())
                    .eq(param.getModuleId() != null, LabelField::getModuleId, param.getModuleId())
                    .eq(param.getIsEnabled() != null, LabelField::getIsEnabled, param.getIsEnabled());
        }
        return wrapper.orderByDesc(LabelField::getCreateTime);
    }

    private void validateField(String fieldCode, String fieldName, String fieldType, Long moduleId) {
        if (!StringUtils.hasText(fieldCode) || !StringUtils.hasText(fieldName) || !StringUtils.hasText(fieldType) || moduleId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        if (!FIELD_TYPES.contains(normalizeType(fieldType))) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        if (moduleService.getModuleById(moduleId) == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
    }

    private LabelField requireField(Long id, Long tenantId) {
        LabelField entity = id == null ? null : labelFieldMapper.selectById(id);
        if (entity == null || !Objects.equals(entity.getTenantId(), tenantId) || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_NOT_FOUND);
        }
        return entity;
    }

    private LabelField findByCode(Long tenantId, String fieldCode, Long moduleId) {
        return labelFieldMapper.selectOne(new LambdaQueryWrapper<LabelField>()
                .eq(LabelField::getTenantId, tenantId)
                .eq(LabelField::getFieldCode, fieldCode)
                .eq(LabelField::getModuleId, moduleId)
                .eq(LabelField::getDeleted, false)
                .last("LIMIT 1"));
    }

    private LabelFieldVO toVO(LabelField entity) {
        LabelFieldVO vo = new LabelFieldVO();
        BeanUtils.copyProperties(entity, vo);
        SysModuleDTO module = entity.getModuleId() == null ? null : moduleService.getModuleById(entity.getModuleId());
        if (module != null) {
            vo.setModuleName(module.getName());
        }
        return vo;
    }

    private String normalizeType(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private String str(Object value) {
        return value == null ? null : value.toString().trim();
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null || !StringUtils.hasText(value.toString())) {
            return null;
        }
        return Long.parseLong(value.toString());
    }

    private Boolean toBoolean(Object value) {
        if (value == null) {
            return true;
        }
        String text = value.toString().trim();
        return "1".equals(text) || "true".equalsIgnoreCase(text) || "是".equals(text) || "启用".equals(text);
    }
}
