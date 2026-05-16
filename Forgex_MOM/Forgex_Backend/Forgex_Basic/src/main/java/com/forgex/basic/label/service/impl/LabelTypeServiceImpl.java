package com.forgex.basic.label.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.label.domain.entity.LabelType;
import com.forgex.basic.label.domain.param.LabelTypeQueryParam;
import com.forgex.basic.label.domain.param.LabelTypeSaveParam;
import com.forgex.basic.label.domain.param.LabelTypeUpdateParam;
import com.forgex.basic.label.domain.vo.LabelTypeVO;
import com.forgex.basic.label.mapper.LabelTypeMapper;
import com.forgex.basic.label.service.LabelTypeService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelTypeServiceImpl extends ServiceImpl<LabelTypeMapper, LabelType> implements LabelTypeService {

    private final LabelTypeMapper labelTypeMapper;

    @Override
    public IPage<LabelTypeVO> pageTypes(LabelTypeQueryParam param, Long tenantId) {
        Page<LabelType> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<LabelType> wrapper = buildQuery(param, tenantId);
        IPage<LabelType> entityPage = labelTypeMapper.selectPage(page, wrapper);
        Page<LabelTypeVO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        result.setRecords(entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return result;
    }

    @Override
    public LabelTypeVO getById(Long id, Long tenantId) {
        return toVO(requireType(id, tenantId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addType(LabelTypeSaveParam param, Long tenantId) {
        if (existsByCode(param.getTypeCode(), tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_CODE_EXISTS, param.getTypeCode());
        }
        LabelType entity = new LabelType();
        BeanUtils.copyProperties(param, entity);
        entity.setTenantId(tenantId);
        entity.setIsEnabled(param.getIsEnabled() == null || param.getIsEnabled());
        labelTypeMapper.insert(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateType(LabelTypeUpdateParam param, Long tenantId) {
        LabelType entity = requireType(param.getId(), tenantId);
        String typeCode = StringUtils.hasText(param.getTypeCode()) ? param.getTypeCode() : entity.getTypeCode();
        if (existsByCodeExcludeId(typeCode, entity.getId(), tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_CODE_EXISTS, typeCode);
        }
        if (StringUtils.hasText(param.getTypeCode())) {
            entity.setTypeCode(param.getTypeCode());
        }
        if (StringUtils.hasText(param.getTypeName())) {
            entity.setTypeName(param.getTypeName());
        }
        if (param.getIsEnabled() != null) {
            entity.setIsEnabled(param.getIsEnabled());
        }
        labelTypeMapper.updateById(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteType(Long id, Long tenantId) {
        requireType(id, tenantId);
        labelTypeMapper.update(null, new LambdaUpdateWrapper<LabelType>()
                .eq(LabelType::getId, id)
                .set(LabelType::getDeleted, true));
    }

    @Override
    public boolean existsByCode(String typeCode, Long tenantId) {
        return labelTypeMapper.selectCount(new LambdaQueryWrapper<LabelType>()
                .eq(LabelType::getTenantId, tenantId)
                .eq(LabelType::getTypeCode, typeCode)
                .eq(LabelType::getDeleted, false)) > 0;
    }

    @Override
    public boolean existsByCodeExcludeId(String typeCode, Long excludeId, Long tenantId) {
        return labelTypeMapper.selectCount(new LambdaQueryWrapper<LabelType>()
                .eq(LabelType::getTenantId, tenantId)
                .eq(LabelType::getTypeCode, typeCode)
                .ne(LabelType::getId, excludeId)
                .eq(LabelType::getDeleted, false)) > 0;
    }

    @Override
    public List<LabelTypeVO> listEnabled(Long tenantId) {
        return labelTypeMapper.selectList(new LambdaQueryWrapper<LabelType>()
                        .eq(LabelType::getTenantId, tenantId)
                        .eq(LabelType::getDeleted, false)
                        .eq(LabelType::getIsEnabled, true)
                        .orderByDesc(LabelType::getCreateTime))
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private LambdaQueryWrapper<LabelType> buildQuery(LabelTypeQueryParam param, Long tenantId) {
        LambdaQueryWrapper<LabelType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelType::getTenantId, tenantId)
                .eq(LabelType::getDeleted, false);
        if (param != null) {
            wrapper.like(StringUtils.hasText(param.getTypeCode()), LabelType::getTypeCode, param.getTypeCode())
                    .like(StringUtils.hasText(param.getTypeName()), LabelType::getTypeName, param.getTypeName())
                    .eq(param.getIsEnabled() != null, LabelType::getIsEnabled, param.getIsEnabled());
        }
        return wrapper.orderByDesc(LabelType::getCreateTime);
    }

    private LabelType requireType(Long id, Long tenantId) {
        LabelType entity = id == null ? null : labelTypeMapper.selectById(id);
        if (entity == null || !Objects.equals(entity.getTenantId(), tenantId) || Boolean.TRUE.equals(entity.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_NOT_FOUND);
        }
        return entity;
    }

    private LabelTypeVO toVO(LabelType entity) {
        LabelTypeVO vo = new LabelTypeVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
