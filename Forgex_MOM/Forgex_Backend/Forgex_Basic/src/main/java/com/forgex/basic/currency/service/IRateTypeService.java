package com.forgex.basic.currency.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.currency.domain.entity.MdmExchangeRateType;
import com.forgex.basic.currency.domain.param.RateTypePageParam;

import java.util.List;

public interface IRateTypeService extends IService<MdmExchangeRateType> {
    Page<MdmExchangeRateType> page(RateTypePageParam param);
    List<MdmExchangeRateType> list(RateTypePageParam param);
    Long create(MdmExchangeRateType param);
    Boolean update(MdmExchangeRateType param);
    Boolean delete(Long id);
    Boolean setDefault(Long id);
}
