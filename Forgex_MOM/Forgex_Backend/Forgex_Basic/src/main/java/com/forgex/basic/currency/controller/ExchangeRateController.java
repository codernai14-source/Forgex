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

/**
 * 汇率管理控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/basic/currency/exchange-rate")
@RequiredArgsConstructor
public class ExchangeRateController {
    private final IExchangeRateService exchangeRateService;

    /**
     * 分页查询汇率数据。
     *
     * @param param 分页查询参数
     * @return 汇率分页结果
     */
    @RequirePerm("basic:exchangeRate:query")
    @PostMapping("/page")
    public R<Page<MdmCurrencyExchangeRate>> page(@RequestBody(required = false) ExchangeRatePageParam param) {
        return R.ok(exchangeRateService.page(param));
    }

    /**
     * 查询数据详情。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:exchangeRate:query")
    @PostMapping("/detail")
    public R<MdmCurrencyExchangeRate> detail(@RequestBody Map<String, Object> params) {
        return R.ok(exchangeRateService.detail(Long.valueOf(String.valueOf(params.get("id")))));
    }

    /**
     * 创建数据。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:exchangeRate:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody MdmCurrencyExchangeRate param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, exchangeRateService.create(param));
    }

    /**
     * 更新数据。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:exchangeRate:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody MdmCurrencyExchangeRate param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, exchangeRateService.update(param));
    }

    /**
     * 删除数据。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:exchangeRate:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, exchangeRateService.delete(Long.valueOf(String.valueOf(params.get("id")))));
    }

    /**
     * 发起审批流程。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:exchangeRate:approval")
    @PostMapping("/approval/start")
    public R<Long> startApproval(@RequestBody ExchangeRateApprovalStartParam param) {
        return R.ok(CommonPrompt.SUBMIT_SUCCESS, exchangeRateService.startApproval(param));
    }

    /**
     * 处理汇率审批工作流回调。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @PostMapping("/workflow/callback")
    public R<Boolean> workflowCallback(@RequestBody ExchangeRateWorkflowCallbackParam param) {
        return R.ok(exchangeRateService.handleWorkflowCallback(param));
    }

    /**
     * 查询当前有效汇率。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @PostMapping("/current")
    public R<MdmCurrencyExchangeRate> current(@RequestBody ExchangeRateCurrentParam param) {
        return R.ok(exchangeRateService.current(param));
    }

    /**
     * 分页查询汇率变更日志。
     *
     * @param param 分页查询参数
     * @return 汇率日志分页结果
     */
    @RequirePerm("basic:exchangeRate:query")
    @PostMapping("/log/page")
    public R<Page<MdmExchangeRateLog>> logPage(@RequestBody(required = false) ExchangeRatePageParam param) {
        return R.ok(exchangeRateService.logPage(param));
    }
}
