package com.forgex.basic.currency.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.currency.domain.entity.MdmCurrency;
import com.forgex.basic.currency.domain.param.CurrencyPageParam;
import com.forgex.basic.currency.service.ICurrencyService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 币种控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/basic/currency")
@RequiredArgsConstructor
public class CurrencyController {
    private final ICurrencyService currencyService;

    /**
     * 分页查询币种主数据。
     *
     * @param param 分页查询参数
     * @return 币种分页结果
     */
    @RequirePerm("basic:currency:query")
    @PostMapping("/page")
    public R<Page<MdmCurrency>> page(@RequestBody(required = false) CurrencyPageParam param) {
        return R.ok(currencyService.page(param));
    }

    /**
     * 查询币种列表。
     *
     * @param param 查询参数
     * @return 币种列表
     */
    @PostMapping("/list")
    public R<List<MdmCurrency>> list(@RequestBody(required = false) CurrencyPageParam param) {
        return R.ok(currencyService.list(param));
    }

    /**
     * 查询数据详情。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:currency:query")
    @PostMapping("/detail")
    public R<MdmCurrency> detail(@RequestBody Map<String, Object> params) {
        return R.ok(currencyService.detail(Long.valueOf(String.valueOf(params.get("id")))));
    }

    /**
     * 创建数据。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:currency:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody MdmCurrency param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, currencyService.create(param));
    }

    /**
     * 更新数据。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:currency:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody MdmCurrency param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, currencyService.update(param));
    }

    /**
     * 删除数据。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:currency:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, currencyService.delete(Long.valueOf(String.valueOf(params.get("id")))));
    }

    /**
     * 设置本位币。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:currency:setBase")
    @PostMapping("/set-base")
    public R<Boolean> setBase(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, currencyService.setBase(Long.valueOf(String.valueOf(params.get("id")))));
    }

    /**
     * 启用数据。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:currency:edit")
    @PostMapping("/enable")
    public R<Boolean> enable(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, currencyService.enable(Long.valueOf(String.valueOf(params.get("id")))));
    }

    /**
     * 禁用数据。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:currency:edit")
    @PostMapping("/disable")
    public R<Boolean> disable(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, currencyService.disable(Long.valueOf(String.valueOf(params.get("id")))));
    }
}
