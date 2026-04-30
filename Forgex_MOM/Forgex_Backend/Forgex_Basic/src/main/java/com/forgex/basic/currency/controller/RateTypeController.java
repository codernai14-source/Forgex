package com.forgex.basic.currency.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.currency.domain.entity.MdmExchangeRateType;
import com.forgex.basic.currency.domain.param.RateTypePageParam;
import com.forgex.basic.currency.service.IRateTypeService;
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

@RestController
@RequestMapping("/basic/currency/rate-type")
@RequiredArgsConstructor
public class RateTypeController {
    private final IRateTypeService rateTypeService;

    @RequirePerm("basic:currency:query")
    @PostMapping("/page")
    public R<Page<MdmExchangeRateType>> page(@RequestBody(required = false) RateTypePageParam param) {
        return R.ok(rateTypeService.page(param));
    }

    @PostMapping("/list")
    public R<List<MdmExchangeRateType>> list(@RequestBody(required = false) RateTypePageParam param) {
        return R.ok(rateTypeService.list(param));
    }

    @RequirePerm("basic:currency:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody MdmExchangeRateType param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, rateTypeService.create(param));
    }

    @RequirePerm("basic:currency:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody MdmExchangeRateType param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, rateTypeService.update(param));
    }

    @RequirePerm("basic:currency:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, rateTypeService.delete(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:currency:edit")
    @PostMapping("/set-default")
    public R<Boolean> setDefault(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, rateTypeService.setDefault(Long.valueOf(String.valueOf(params.get("id")))));
    }
}
