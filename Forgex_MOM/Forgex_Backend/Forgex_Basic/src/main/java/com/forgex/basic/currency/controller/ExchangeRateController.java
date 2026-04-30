package com.forgex.basic.currency.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.currency.domain.entity.MdmCurrencyExchangeRate;
import com.forgex.basic.currency.domain.entity.MdmExchangeRateLog;
import com.forgex.basic.currency.domain.param.ExchangeRateApprovalStartParam;
import com.forgex.basic.currency.domain.param.ExchangeRateCurrentParam;
import com.forgex.basic.currency.domain.param.ExchangeRatePageParam;
import com.forgex.basic.currency.domain.param.ExchangeRateWorkflowCallbackParam;
import com.forgex.basic.currency.service.IExchangeRateService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/basic/currency/exchange-rate")
@RequiredArgsConstructor
public class ExchangeRateController {
    private final IExchangeRateService exchangeRateService;

    @RequirePerm("basic:exchangeRate:query")
    @PostMapping("/page")
    public R<Page<MdmCurrencyExchangeRate>> page(@RequestBody(required = false) ExchangeRatePageParam param) {
        return R.ok(exchangeRateService.page(param));
    }

    @RequirePerm("basic:exchangeRate:query")
    @PostMapping("/detail")
    public R<MdmCurrencyExchangeRate> detail(@RequestBody Map<String, Object> params) {
        return R.ok(exchangeRateService.detail(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:exchangeRate:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody MdmCurrencyExchangeRate param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, exchangeRateService.create(param));
    }

    @RequirePerm("basic:exchangeRate:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody MdmCurrencyExchangeRate param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, exchangeRateService.update(param));
    }

    @RequirePerm("basic:exchangeRate:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, exchangeRateService.delete(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:exchangeRate:approval")
    @PostMapping("/approval/start")
    public R<Long> startApproval(@RequestBody ExchangeRateApprovalStartParam param) {
        return R.ok(CommonPrompt.SUBMIT_SUCCESS, exchangeRateService.startApproval(param));
    }

    @PostMapping("/workflow/callback")
    public R<Boolean> workflowCallback(@RequestBody ExchangeRateWorkflowCallbackParam param) {
        return R.ok(exchangeRateService.handleWorkflowCallback(param));
    }

    @PostMapping("/current")
    public R<MdmCurrencyExchangeRate> current(@RequestBody ExchangeRateCurrentParam param) {
        return R.ok(exchangeRateService.current(param));
    }

    @RequirePerm("basic:exchangeRate:query")
    @PostMapping("/log/page")
    public R<Page<MdmExchangeRateLog>> logPage(@RequestBody(required = false) ExchangeRatePageParam param) {
        return R.ok(exchangeRateService.logPage(param));
    }
}
