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

/**
 * 汇率类型控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/basic/currency/rate-type")
@RequiredArgsConstructor
public class RateTypeController {
    private final IRateTypeService rateTypeService;

    /**
     * 分页查询汇率类型。
     *
     * @param param 分页查询参数
     * @return 汇率类型分页结果
     */
    @RequirePerm("basic:currency:query")
    @PostMapping("/page")
    public R<Page<MdmExchangeRateType>> page(@RequestBody(required = false) RateTypePageParam param) {
        return R.ok(rateTypeService.page(param));
    }

    /**
     * 查询汇率类型列表。
     *
     * @param param 查询参数
     * @return 汇率类型列表
     */
    @PostMapping("/list")
    public R<List<MdmExchangeRateType>> list(@RequestBody(required = false) RateTypePageParam param) {
        return R.ok(rateTypeService.list(param));
    }

    /**
     * 创建数据。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:currency:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody MdmExchangeRateType param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, rateTypeService.create(param));
    }

    /**
     * 更新数据。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:currency:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody MdmExchangeRateType param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, rateTypeService.update(param));
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
        return R.ok(CommonPrompt.DELETE_SUCCESS, rateTypeService.delete(Long.valueOf(String.valueOf(params.get("id")))));
    }

    /**
     * 设置默认汇率类型。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("basic:currency:edit")
    @PostMapping("/set-default")
    public R<Boolean> setDefault(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, rateTypeService.setDefault(Long.valueOf(String.valueOf(params.get("id")))));
    }
}
