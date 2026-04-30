package com.forgex.basic.currency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.currency.domain.entity.MdmExchangeRateType;
import com.forgex.basic.currency.domain.param.RateTypePageParam;
import com.forgex.basic.currency.mapper.MdmExchangeRateTypeMapper;
import com.forgex.basic.currency.service.IRateTypeService;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RateTypeServiceImpl extends ServiceImpl<MdmExchangeRateTypeMapper, MdmExchangeRateType> implements IRateTypeService {

    private static final Long PUBLIC_TENANT_ID = 0L;
    private final MdmExchangeRateTypeMapper rateTypeMapper;

    @Override
    public Page<MdmExchangeRateType> page(RateTypePageParam param) {
        RateTypePageParam safeParam = param == null ? new RateTypePageParam() : param;
        return rateTypeMapper.selectPage(new Page<>(safeParam.getPageNum(), safeParam.getPageSize()), wrapper(safeParam));
    }

    @Override
    public List<MdmExchangeRateType> list(RateTypePageParam param) {
        return rateTypeMapper.selectList(wrapper(param == null ? new RateTypePageParam() : param));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MdmExchangeRateType param) {
        validate(param, true);
        param.setRateTypeCode(normalizeCode(param.getRateTypeCode()));
        param.setTenantId(PUBLIC_TENANT_ID);
        param.setStatus(param.getStatus() == null ? 1 : param.getStatus());
        param.setIsDefault(Boolean.TRUE.equals(param.getIsDefault()));
        if (Boolean.TRUE.equals(param.getIsDefault())) {
            clearDefault(null);
        }
        rateTypeMapper.insert(param);
        return param.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(MdmExchangeRateType param) {
        validate(param, false);
        MdmExchangeRateType exists = requireRateType(param.getId());
        if (!Objects.equals(exists.getRateTypeCode(), normalizeCode(param.getRateTypeCode()))) {
            throw ex(BasicPromptEnum.RATE_TYPE_CODE_IMMUTABLE);
        }
        if (Boolean.TRUE.equals(param.getIsDefault())) {
            clearDefault(param.getId());
        }
        param.setRateTypeCode(exists.getRateTypeCode());
        param.setTenantId(PUBLIC_TENANT_ID);
        rateTypeMapper.updateById(param);
        return true;
    }

    @Override
    public Boolean delete(Long id) {
        requireRateType(id);
        rateTypeMapper.deleteById(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setDefault(Long id) {
        MdmExchangeRateType type = requireRateType(id);
        clearDefault(id);
        type.setIsDefault(true);
        rateTypeMapper.updateById(type);
        return true;
    }

    public MdmExchangeRateType requireRateTypeByCode(String code) {
        MdmExchangeRateType type = rateTypeMapper.selectOne(new LambdaQueryWrapper<MdmExchangeRateType>()
                .eq(MdmExchangeRateType::getRateTypeCode, normalizeCode(code))
                .eq(MdmExchangeRateType::getDeleted, false)
                .last("LIMIT 1"));
        if (type == null) {
            throw ex(BasicPromptEnum.RATE_TYPE_NOT_FOUND);
        }
        return type;
    }

    private LambdaQueryWrapper<MdmExchangeRateType> wrapper(RateTypePageParam param) {
        return new LambdaQueryWrapper<MdmExchangeRateType>()
                .eq(MdmExchangeRateType::getDeleted, false)
                .like(StringUtils.hasText(param.getRateTypeCode()), MdmExchangeRateType::getRateTypeCode, param.getRateTypeCode())
                .like(StringUtils.hasText(param.getRateTypeName()), MdmExchangeRateType::getRateTypeName, param.getRateTypeName())
                .eq(param.getIsDefault() != null, MdmExchangeRateType::getIsDefault, param.getIsDefault())
                .eq(param.getStatus() != null, MdmExchangeRateType::getStatus, param.getStatus())
                .orderByDesc(MdmExchangeRateType::getIsDefault)
                .orderByAsc(MdmExchangeRateType::getRateTypeCode);
    }

    private void validate(MdmExchangeRateType param, boolean create) {
        if (param == null || !StringUtils.hasText(param.getRateTypeCode())) {
            throw ex(BasicPromptEnum.RATE_TYPE_NOT_FOUND);
        }
        MdmExchangeRateType same = rateTypeMapper.selectOne(new LambdaQueryWrapper<MdmExchangeRateType>()
                .eq(MdmExchangeRateType::getRateTypeCode, normalizeCode(param.getRateTypeCode()))
                .eq(MdmExchangeRateType::getDeleted, false)
                .last("LIMIT 1"));
        if (create && same != null) {
            throw ex(BasicPromptEnum.RATE_TYPE_CODE_EXISTS);
        }
        if (!create && param.getId() == null) {
            throw ex(BasicPromptEnum.RATE_TYPE_NOT_FOUND);
        }
    }

    private MdmExchangeRateType requireRateType(Long id) {
        MdmExchangeRateType type = id == null ? null : rateTypeMapper.selectById(id);
        if (type == null) {
            throw ex(BasicPromptEnum.RATE_TYPE_NOT_FOUND);
        }
        return type;
    }

    private void clearDefault(Long excludeId) {
        List<MdmExchangeRateType> defaults = rateTypeMapper.selectList(new LambdaQueryWrapper<MdmExchangeRateType>()
                .eq(MdmExchangeRateType::getIsDefault, true)
                .eq(MdmExchangeRateType::getDeleted, false));
        for (MdmExchangeRateType item : defaults) {
            if (excludeId == null || !Objects.equals(excludeId, item.getId())) {
                item.setIsDefault(false);
                rateTypeMapper.updateById(item);
            }
        }
    }

    private String normalizeCode(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : null;
    }

    private I18nBusinessException ex(BasicPromptEnum prompt) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt);
    }
}
