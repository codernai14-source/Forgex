package com.forgex.basic.currency.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.currency.domain.entity.MdmCurrencyExchangeRate;
import com.forgex.basic.currency.domain.entity.MdmExchangeRateLog;
import com.forgex.basic.currency.domain.param.ExchangeRateApprovalStartParam;
import com.forgex.basic.currency.domain.param.ExchangeRateCurrentParam;
import com.forgex.basic.currency.domain.param.ExchangeRatePageParam;
import com.forgex.basic.currency.domain.param.ExchangeRateWorkflowCallbackParam;

public interface IExchangeRateService extends IService<MdmCurrencyExchangeRate> {
    Page<MdmCurrencyExchangeRate> page(ExchangeRatePageParam param);
    MdmCurrencyExchangeRate detail(Long id);
    Long create(MdmCurrencyExchangeRate param);
    Boolean update(MdmCurrencyExchangeRate param);
    Boolean delete(Long id);
    Long startApproval(ExchangeRateApprovalStartParam param);
    Boolean handleWorkflowCallback(ExchangeRateWorkflowCallbackParam param);
    MdmCurrencyExchangeRate current(ExchangeRateCurrentParam param);
    Page<MdmExchangeRateLog> logPage(ExchangeRatePageParam param);
}
