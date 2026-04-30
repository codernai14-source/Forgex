package com.forgex.basic.currency.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.currency.domain.entity.MdmCurrency;
import com.forgex.basic.currency.domain.param.CurrencyPageParam;

import java.util.List;

public interface ICurrencyService extends IService<MdmCurrency> {
    Page<MdmCurrency> page(CurrencyPageParam param);
    List<MdmCurrency> list(CurrencyPageParam param);
    MdmCurrency detail(Long id);
    Long create(MdmCurrency param);
    Boolean update(MdmCurrency param);
    Boolean delete(Long id);
    Boolean setBase(Long id);
    Boolean enable(Long id);
    Boolean disable(Long id);
}
