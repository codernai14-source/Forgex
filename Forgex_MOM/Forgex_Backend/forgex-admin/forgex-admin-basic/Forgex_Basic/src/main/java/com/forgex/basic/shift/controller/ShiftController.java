package com.forgex.basic.shift.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.shift.domain.dto.ShiftDTO;
import com.forgex.basic.shift.domain.param.ShiftPageParam;
import com.forgex.basic.shift.service.IShiftService;
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
 * 班次主数据控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@RestController
@RequestMapping("/shift")
@RequiredArgsConstructor
public class ShiftController {

    private final IShiftService shiftService;

    @RequirePerm("basic:shift:query")
    @PostMapping("/page")
    public R<Page<ShiftDTO>> page(@RequestBody(required = false) ShiftPageParam param) {
        return R.ok(shiftService.page(param));
    }

    @PostMapping("/list")
    public R<List<ShiftDTO>> list(@RequestBody(required = false) ShiftPageParam param) {
        return R.ok(shiftService.list(param));
    }

    @RequirePerm("basic:shift:query")
    @PostMapping("/detail")
    public R<ShiftDTO> detail(@RequestBody Map<String, Object> params) {
        return R.ok(shiftService.detail(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:shift:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody ShiftDTO param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, shiftService.create(param));
    }

    @RequirePerm("basic:shift:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody ShiftDTO param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, shiftService.update(param));
    }

    @RequirePerm("basic:shift:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, shiftService.delete(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:shift:batchDelete")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody Map<String, List<Long>> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, shiftService.batchDelete(params.get("ids")));
    }
}
