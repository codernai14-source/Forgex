package com.forgex.basic.currency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.currency.domain.entity.MdmCurrency;
import com.forgex.basic.currency.domain.param.CurrencyPageParam;
import com.forgex.basic.currency.mapper.MdmCurrencyMapper;
import com.forgex.basic.currency.service.ICurrencyService;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 币种服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl extends ServiceImpl<MdmCurrencyMapper, MdmCurrency> implements ICurrencyService {

    private static final Pattern ISO_CODE = Pattern.compile("^[A-Z]{3}$");
    private static final Long PUBLIC_TENANT_ID = 0L;

    private final MdmCurrencyMapper currencyMapper;

    /**
     * 分页查询数据。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<MdmCurrency> page(CurrencyPageParam param) {
        CurrencyPageParam safeParam = param == null ? new CurrencyPageParam() : param;
        Page<MdmCurrency> page = new Page<>(safeParam.getPageNum(), safeParam.getPageSize());
        return currencyMapper.selectPage(page, wrapper(safeParam));
    }

    /**
     * 查询数据列表。
     *
     * @param param 请求参数
     * @return 列表数据
     */
    @Override
    public List<MdmCurrency> list(CurrencyPageParam param) {
        return currencyMapper.selectList(wrapper(param == null ? new CurrencyPageParam() : param));
    }

    /**
     * 查询数据详情。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @Override
    public MdmCurrency detail(Long id) {
        return id == null ? null : currencyMapper.selectById(id);
    }

    /**
     * 创建数据。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MdmCurrency param) {
        validate(param, true);
        param.setCurrencyCode(normalizeCode(param.getCurrencyCode()));
        param.setTenantId(PUBLIC_TENANT_ID);
        param.setStatus(param.getStatus() == null ? 1 : param.getStatus());
        param.setIsBaseCurrency(Boolean.TRUE.equals(param.getIsBaseCurrency()));
        if (Boolean.TRUE.equals(param.getIsBaseCurrency())) {
            clearBaseCurrency(null);
        }
        currencyMapper.insert(param);
        return param.getId();
    }

    /**
     * 更新数据。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(MdmCurrency param) {
        validate(param, false);
        MdmCurrency exists = requireCurrency(param.getId());
        if (!Objects.equals(exists.getCurrencyCode(), normalizeCode(param.getCurrencyCode()))) {
            throw ex(BasicPromptEnum.CURRENCY_CODE_IMMUTABLE);
        }
        if (Boolean.TRUE.equals(param.getIsBaseCurrency())) {
            clearBaseCurrency(param.getId());
        }
        param.setCurrencyCode(exists.getCurrencyCode());
        param.setTenantId(PUBLIC_TENANT_ID);
        currencyMapper.updateById(param);
        return true;
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    public Boolean delete(Long id) {
        requireCurrency(id);
        currencyMapper.deleteById(id);
        return true;
    }

    /**
     * 设置本位币。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setBase(Long id) {
        MdmCurrency currency = requireCurrency(id);
        clearBaseCurrency(id);
        currency.setIsBaseCurrency(true);
        currencyMapper.updateById(currency);
        return true;
    }

    /**
     * 启用数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    public Boolean enable(Long id) {
        MdmCurrency currency = requireCurrency(id);
        currency.setStatus(1);
        currencyMapper.updateById(currency);
        return true;
    }

    /**
     * 禁用数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    public Boolean disable(Long id) {
        MdmCurrency currency = requireCurrency(id);
        currency.setStatus(0);
        currencyMapper.updateById(currency);
        return true;
    }

    private LambdaQueryWrapper<MdmCurrency> wrapper(CurrencyPageParam param) {
        return new LambdaQueryWrapper<MdmCurrency>()
                .eq(MdmCurrency::getDeleted, false)
                .like(StringUtils.hasText(param.getCurrencyCode()), MdmCurrency::getCurrencyCode, param.getCurrencyCode())
                .like(StringUtils.hasText(param.getCurrencyNameCn()), MdmCurrency::getCurrencyNameCn, param.getCurrencyNameCn())
                .eq(param.getIsBaseCurrency() != null, MdmCurrency::getIsBaseCurrency, param.getIsBaseCurrency())
                .eq(param.getStatus() != null, MdmCurrency::getStatus, param.getStatus())
                .orderByDesc(MdmCurrency::getIsBaseCurrency)
                .orderByAsc(MdmCurrency::getCurrencyCode);
    }

    private void validate(MdmCurrency param, boolean create) {
        if (param == null || !ISO_CODE.matcher(normalizeCode(param.getCurrencyCode()) == null ? "" : normalizeCode(param.getCurrencyCode())).matches()) {
            throw ex(BasicPromptEnum.CURRENCY_CODE_INVALID);
        }
        MdmCurrency same = currencyMapper.selectOne(new LambdaQueryWrapper<MdmCurrency>()
                .eq(MdmCurrency::getCurrencyCode, normalizeCode(param.getCurrencyCode()))
                .eq(MdmCurrency::getDeleted, false)
                .last("LIMIT 1"));
        if (create && same != null) {
            throw ex(BasicPromptEnum.CURRENCY_CODE_EXISTS);
        }
        if (!create && param.getId() == null) {
            throw ex(BasicPromptEnum.CURRENCY_NOT_FOUND);
        }
    }

    /**
     * 根据币种编码获取币种，不存在时抛出业务异常。
     *
     * @param code 编码
     * @return 处理结果
     */
    public MdmCurrency requireCurrencyByCode(String code) {
        MdmCurrency currency = currencyMapper.selectOne(new LambdaQueryWrapper<MdmCurrency>()
                .eq(MdmCurrency::getCurrencyCode, normalizeCode(code))
                .eq(MdmCurrency::getDeleted, false)
                .last("LIMIT 1"));
        if (currency == null) {
            throw ex(BasicPromptEnum.CURRENCY_NOT_FOUND);
        }
        return currency;
    }

    private MdmCurrency requireCurrency(Long id) {
        MdmCurrency currency = id == null ? null : currencyMapper.selectById(id);
        if (currency == null) {
            throw ex(BasicPromptEnum.CURRENCY_NOT_FOUND);
        }
        return currency;
    }

    private void clearBaseCurrency(Long excludeId) {
        List<MdmCurrency> bases = currencyMapper.selectList(new LambdaQueryWrapper<MdmCurrency>()
                .eq(MdmCurrency::getIsBaseCurrency, true)
                .eq(MdmCurrency::getDeleted, false));
        for (MdmCurrency item : bases) {
            if (excludeId == null || !Objects.equals(excludeId, item.getId())) {
                item.setIsBaseCurrency(false);
                currencyMapper.updateById(item);
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
