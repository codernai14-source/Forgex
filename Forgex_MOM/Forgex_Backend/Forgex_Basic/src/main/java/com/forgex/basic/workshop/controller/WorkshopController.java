package com.forgex.basic.workshop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.workshop.domain.dto.WorkshopDTO;
import com.forgex.basic.workshop.domain.entity.BasicWorkshop;
import com.forgex.basic.workshop.domain.param.WorkshopPageParam;
import com.forgex.basic.workshop.service.IWorkshopService;
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
 * 车间主数据控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@RestController
@RequestMapping("/workshop")
@RequiredArgsConstructor
public class WorkshopController {

    private final IWorkshopService workshopService;

    @RequirePerm("basic:workshop:query")
    @PostMapping("/page")
    public R<Page<WorkshopDTO>> page(@RequestBody(required = false) WorkshopPageParam param) {
        return R.ok(workshopService.page(param));
    }

    @PostMapping("/list")
    public R<List<WorkshopDTO>> list(@RequestBody(required = false) WorkshopPageParam param) {
        return R.ok(workshopService.list(param));
    }

    @RequirePerm("basic:workshop:query")
    @PostMapping("/detail")
    public R<WorkshopDTO> detail(@RequestBody Map<String, Object> params) {
        return R.ok(workshopService.detail(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:workshop:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody BasicWorkshop param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, workshopService.create(param));
    }

    @RequirePerm("basic:workshop:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody BasicWorkshop param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, workshopService.update(param));
    }

    @RequirePerm("basic:workshop:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, workshopService.delete(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:workshop:delete")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody Map<String, List<Long>> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, workshopService.batchDelete(params.get("ids")));
    }
}
