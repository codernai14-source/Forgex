package com.forgex.basic.label.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.label.domain.dto.LabelTemplateDTO;
import com.forgex.basic.label.domain.entity.LabelTemplate;
import com.forgex.basic.label.domain.entity.LabelTemplateDetail;
import com.forgex.basic.label.domain.entity.LabelType;
import com.forgex.basic.label.domain.param.LabelPrintRenderParam;
import com.forgex.basic.label.domain.param.LabelTemplateDesignSaveParam;
import com.forgex.basic.label.domain.param.LabelTemplateDetailParam;
import com.forgex.basic.label.domain.param.LabelTemplateQueryParam;
import com.forgex.basic.label.domain.param.LabelTemplateSaveParam;
import com.forgex.basic.label.domain.param.LabelTemplateUpdateParam;
import com.forgex.basic.label.domain.vo.LabelRenderComponentVO;
import com.forgex.basic.label.domain.vo.LabelRenderVO;
import com.forgex.basic.label.domain.vo.LabelTemplateDesignVO;
import com.forgex.basic.label.domain.vo.TemplateVO;
import com.forgex.basic.label.mapper.LabelTemplateDetailMapper;
import com.forgex.basic.label.mapper.LabelTemplateMapper;
import com.forgex.basic.label.mapper.LabelTypeMapper;
import com.forgex.basic.label.service.LabelTemplateService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelTemplateServiceImpl extends ServiceImpl<LabelTemplateMapper, LabelTemplate> implements LabelTemplateService {

    private static final String DATA_SOURCE_FIELD = "FIELD";

    private final LabelTemplateMapper labelTemplateMapper;
    private final LabelTemplateDetailMapper detailMapper;
    private final LabelTypeMapper labelTypeMapper;
    private final ObjectMapper objectMapper;

    @Override
    public IPage<TemplateVO> pageTemplates(LabelTemplateQueryParam param, Long tenantId) {
        Page<LabelTemplate> page = new Page<>(param.getPageNum(), param.getPageSize());
        IPage<LabelTemplate> entityPage = labelTemplateMapper.selectPage(page, buildQuery(param, tenantId));
        Page<TemplateVO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return result;
    }

    @Override
    public LabelTemplateDTO getTemplateById(Long id, Long tenantId) {
        LabelTemplate template = requireTemplate(id, tenantId);
        LabelTemplateDTO dto = new LabelTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addTemplate(LabelTemplateSaveParam param, Long tenantId) {
        if (existsByCode(param.getTemplateCode(), tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_CODE_EXISTS, param.getTemplateCode());
        }
        validateType(param.getTypeId(), tenantId);
        LabelTemplate template = new LabelTemplate();
        BeanUtils.copyProperties(param, template);
        template.setTenantId(tenantId);
        template.setTemplateVersion(1);
        template.setIsDefault(false);
        template.setStatus(param.getStatus() == null ? 1 : param.getStatus());
        template.setIsEnabled(param.getIsEnabled() == null || param.getIsEnabled());
        template.setPaperWidth(param.getPaperWidth() == null ? 100 : param.getPaperWidth());
        template.setPaperHeight(param.getPaperHeight() == null ? 60 : param.getPaperHeight());
        template.setPaperSize(StringUtils.hasText(param.getPaperSize()) ? param.getPaperSize() : "CUSTOM");
        labelTemplateMapper.insert(template);
        return template.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTemplate(LabelTemplateUpdateParam param, Long tenantId) {
        LabelTemplate template = requireTemplate(param.getId(), tenantId);
        validateType(param.getTypeId(), tenantId);
        if (StringUtils.hasText(param.getTemplateName())) {
            template.setTemplateName(param.getTemplateName());
        }
        if (StringUtils.hasText(param.getTemplateType())) {
            template.setTemplateType(param.getTemplateType());
        }
        if (param.getTypeId() != null) {
            template.setTypeId(param.getTypeId());
        }
        if (param.getPaperWidth() != null) {
            template.setPaperWidth(param.getPaperWidth());
        }
        if (param.getPaperHeight() != null) {
            template.setPaperHeight(param.getPaperHeight());
        }
        if (StringUtils.hasText(param.getPaperSize())) {
            template.setPaperSize(param.getPaperSize());
        }
        if (param.getIsEnabled() != null) {
            template.setIsEnabled(param.getIsEnabled());
            template.setStatus(Boolean.TRUE.equals(param.getIsEnabled()) ? 1 : 0);
        }
        if (param.getStatus() != null) {
            template.setStatus(param.getStatus());
            template.setIsEnabled(param.getStatus() == 1);
        }
        if (param.getTemplateContent() != null) {
            template.setTemplateContent(param.getTemplateContent());
        }
        if (param.getDescription() != null) {
            template.setDescription(param.getDescription());
        }
        labelTemplateMapper.updateById(template);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTemplate(Long id, Long tenantId) {
        requireTemplate(id, tenantId);
        labelTemplateMapper.update(null, new LambdaUpdateWrapper<LabelTemplate>()
                .eq(LabelTemplate::getId, id)
                .set(LabelTemplate::getDeleted, true));
        detailMapper.update(null, new LambdaUpdateWrapper<LabelTemplateDetail>()
                .eq(LabelTemplateDetail::getTemplateId, id)
                .set(LabelTemplateDetail::getDeleted, true));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDeleteTemplates(List<Long> ids, Long tenantId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.forEach(id -> deleteTemplate(id, tenantId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setDefaultTemplate(Long id, String templateType, Long tenantId) {
        LabelTemplate template = requireTemplate(id, tenantId);
        String targetType = StringUtils.hasText(templateType) ? templateType : template.getTemplateType();
        labelTemplateMapper.update(null, new LambdaUpdateWrapper<LabelTemplate>()
                .eq(LabelTemplate::getTenantId, tenantId)
                .eq(LabelTemplate::getTemplateType, targetType)
                .set(LabelTemplate::getIsDefault, false));
        template.setIsDefault(true);
        labelTemplateMapper.updateById(template);
    }

    @Override
    public boolean existsByCode(String templateCode, Long tenantId) {
        return labelTemplateMapper.selectCount(new LambdaQueryWrapper<LabelTemplate>()
                .eq(LabelTemplate::getTenantId, tenantId)
                .eq(LabelTemplate::getTemplateCode, templateCode)
                .eq(LabelTemplate::getDeleted, false)) > 0;
    }

    @Override
    public boolean existsByCodeExcludeId(String templateCode, Long excludeId, Long tenantId) {
        return labelTemplateMapper.selectCount(new LambdaQueryWrapper<LabelTemplate>()
                .eq(LabelTemplate::getTenantId, tenantId)
                .eq(LabelTemplate::getTemplateCode, templateCode)
                .ne(LabelTemplate::getId, excludeId)
                .eq(LabelTemplate::getDeleted, false)) > 0;
    }

    @Override
    public LabelTemplate getDefaultTemplate(String templateType, Long tenantId) {
        return labelTemplateMapper.selectOne(new LambdaQueryWrapper<LabelTemplate>()
                .eq(LabelTemplate::getTenantId, tenantId)
                .eq(LabelTemplate::getTemplateType, templateType)
                .eq(LabelTemplate::getIsDefault, true)
                .eq(LabelTemplate::getDeleted, false)
                .orderByDesc(LabelTemplate::getTemplateVersion)
                .last("LIMIT 1"));
    }

    @Override
    public LabelTemplateDesignVO getDesignDetail(Long id, Long tenantId) {
        LabelTemplate template = requireTemplate(id, tenantId);
        return toDesignVO(template, listDetails(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveDesign(LabelTemplateDesignSaveParam param, Long tenantId) {
        LabelTemplate template = requireTemplate(param.getTemplateId(), tenantId);
        if (!Objects.equals(template.getTemplateCode(), param.getTemplateCode())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_NOT_FOUND);
        }
        detailMapper.update(null, new LambdaUpdateWrapper<LabelTemplateDetail>()
                .eq(LabelTemplateDetail::getTemplateId, template.getId())
                .set(LabelTemplateDetail::getDeleted, true));
        if (param.getDetails() == null) {
            return;
        }
        int index = 0;
        for (LabelTemplateDetailParam detailParam : param.getDetails()) {
            LabelTemplateDetail detail = new LabelTemplateDetail();
            BeanUtils.copyProperties(detailParam, detail);
            detail.setTemplateId(template.getId());
            detail.setTenantId(tenantId);
            detail.setSortNo(detail.getSortNo() == null ? index : detail.getSortNo());
            detailMapper.insert(detail);
            index++;
        }
    }

    @Override
    public LabelTemplateDesignVO preview(Long id, Long tenantId) {
        return getDesignDetail(id, tenantId);
    }

    @Override
    public LabelRenderVO render(LabelPrintRenderParam param, Long tenantId) {
        validatePrintCount(param.getPrintCount());
        LabelTemplate template = labelTemplateMapper.selectOne(new LambdaQueryWrapper<LabelTemplate>()
                .eq(LabelTemplate::getTenantId, tenantId)
                .eq(LabelTemplate::getTemplateCode, param.getTemplateCode())
                .eq(LabelTemplate::getDeleted, false)
                .last("LIMIT 1"));
        if (template == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_NOT_FOUND);
        }
        if (template.getStatus() != null && template.getStatus() != 1 || Boolean.FALSE.equals(template.getIsEnabled())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_DISABLED);
        }
        validateTemplateType(template, tenantId);
        Map<String, Object> data = objectMapper.convertValue(param.getData(), new TypeReference<Map<String, Object>>() {});
        LabelRenderVO vo = toRenderVO(template, listDetails(template.getId()), data);
        vo.setPrintCount(param.getPrintCount());
        return vo;
    }

    private LambdaQueryWrapper<LabelTemplate> buildQuery(LabelTemplateQueryParam param, Long tenantId) {
        LambdaQueryWrapper<LabelTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelTemplate::getTenantId, tenantId)
                .eq(LabelTemplate::getDeleted, false);
        if (param != null) {
            wrapper.like(StringUtils.hasText(param.getTemplateCode()), LabelTemplate::getTemplateCode, param.getTemplateCode())
                    .like(StringUtils.hasText(param.getTemplateName()), LabelTemplate::getTemplateName, param.getTemplateName())
                    .eq(StringUtils.hasText(param.getTemplateType()), LabelTemplate::getTemplateType, param.getTemplateType())
                    .eq(param.getTypeId() != null, LabelTemplate::getTypeId, param.getTypeId())
                    .eq(param.getIsDefault() != null, LabelTemplate::getIsDefault, param.getIsDefault())
                    .eq(param.getIsEnabled() != null, LabelTemplate::getIsEnabled, param.getIsEnabled())
                    .eq(param.getStatus() != null, LabelTemplate::getStatus, param.getStatus());
        }
        return wrapper.orderByDesc(LabelTemplate::getCreateTime);
    }

    private LabelTemplate requireTemplate(Long id, Long tenantId) {
        LabelTemplate template = id == null ? null : labelTemplateMapper.selectById(id);
        if (template == null || !Objects.equals(template.getTenantId(), tenantId) || Boolean.TRUE.equals(template.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_NOT_FOUND);
        }
        return template;
    }

    private void validateType(Long typeId, Long tenantId) {
        if (typeId == null) {
            return;
        }
        LabelType labelType = labelTypeMapper.selectById(typeId);
        if (labelType == null || !Objects.equals(labelType.getTenantId(), tenantId) || Boolean.TRUE.equals(labelType.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
    }

    private void validateTemplateType(LabelTemplate template, Long tenantId) {
        if (template.getTypeId() == null) {
            return;
        }
        LabelType labelType = labelTypeMapper.selectById(template.getTypeId());
        if (labelType == null || !Objects.equals(labelType.getTenantId(), tenantId) || Boolean.FALSE.equals(labelType.getIsEnabled())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_DISABLED);
        }
    }

    private void validatePrintCount(Integer printCount) {
        if (printCount == null || printCount <= 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_PRINT_COUNT_REQUIRED);
        }
        if (printCount > 1000) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_PRINT_COUNT_LIMIT_EXCEEDED);
        }
    }

    private List<LabelTemplateDetail> listDetails(Long templateId) {
        return detailMapper.selectList(new LambdaQueryWrapper<LabelTemplateDetail>()
                .eq(LabelTemplateDetail::getTemplateId, templateId)
                .eq(LabelTemplateDetail::getDeleted, false)
                .orderByAsc(LabelTemplateDetail::getSortNo)
                .orderByAsc(LabelTemplateDetail::getId));
    }

    private TemplateVO toVO(LabelTemplate entity) {
        TemplateVO vo = new TemplateVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private LabelTemplateDesignVO toDesignVO(LabelTemplate template, List<LabelTemplateDetail> details) {
        LabelTemplateDesignVO vo = new LabelTemplateDesignVO();
        BeanUtils.copyProperties(template, vo);
        vo.setComponents(details.stream().map(detail -> toRenderComponent(detail, null)).collect(Collectors.toList()));
        return vo;
    }

    private LabelRenderVO toRenderVO(LabelTemplate template, List<LabelTemplateDetail> details, Map<String, Object> data) {
        LabelRenderVO vo = new LabelRenderVO();
        vo.setTemplateCode(template.getTemplateCode());
        vo.setTemplateName(template.getTemplateName());
        vo.setPaperWidth(template.getPaperWidth());
        vo.setPaperHeight(template.getPaperHeight());
        vo.setPaperSize(template.getPaperSize());
        vo.setComponents(details.stream().map(detail -> toRenderComponent(detail, data)).collect(Collectors.toList()));
        return vo;
    }

    private LabelRenderComponentVO toRenderComponent(LabelTemplateDetail detail, Map<String, Object> data) {
        LabelRenderComponentVO vo = new LabelRenderComponentVO();
        vo.setComponentType(detail.getComponentType());
        vo.setPositionX(detail.getPositionX());
        vo.setPositionY(detail.getPositionY());
        vo.setComponentWidth(detail.getComponentWidth());
        vo.setComponentHeight(detail.getComponentHeight());
        vo.setDataSource(detail.getDataSource());
        vo.setFieldCode(detail.getFieldCode());
        vo.setStyleJson(detail.getStyleJson());
        vo.setSortNo(detail.getSortNo());
        if (data != null && DATA_SOURCE_FIELD.equalsIgnoreCase(detail.getDataSource()) && StringUtils.hasText(detail.getFieldCode())) {
            Object value = data.get(detail.getFieldCode());
            vo.setContent(value == null ? "" : String.valueOf(value));
        } else {
            vo.setContent(detail.getComponentContent());
        }
        return vo;
    }
}
