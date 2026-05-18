package com.forgex.basic.label.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.basic.label.domain.param.IdParam;
import com.forgex.basic.label.domain.param.LabelTypeQueryParam;
import com.forgex.basic.label.domain.param.LabelTypeSaveParam;
import com.forgex.basic.label.domain.param.LabelTypeUpdateParam;
import com.forgex.basic.label.domain.vo.LabelTypeVO;
import com.forgex.basic.label.service.LabelTypeService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "标签类型管理")
@RestController
@RequestMapping("/label/type")
@RequiredArgsConstructor
public class LabelTypeController {

    private final LabelTypeService labelTypeService;

    @Operation(summary = "分页查询标签类型")
    @RequirePerm("label:type:query")
    @PostMapping("/page")
    public R<IPage<LabelTypeVO>> page(@RequestBody LabelTypeQueryParam param) {
        return R.ok(labelTypeService.pageTypes(param, TenantContext.get()));
    }

    @Operation(summary = "查询标签类型详情")
    @RequirePerm("label:type:query")
    @PostMapping("/detail")
    public R<LabelTypeVO> detail(@RequestBody IdParam param) {
        return R.ok(labelTypeService.getById(param.getId(), TenantContext.get()));
    }

    @Operation(summary = "新增标签类型")
    @RequirePerm("label:type:add")
    @PostMapping("/add")
    public R<Long> add(@RequestBody @Validated LabelTypeSaveParam param) {
        return R.ok(labelTypeService.addType(param, TenantContext.get()));
    }

    @Operation(summary = "更新标签类型")
    @RequirePerm("label:type:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated LabelTypeUpdateParam param) {
        labelTypeService.updateType(param, TenantContext.get());
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    @Operation(summary = "删除标签类型")
    @RequirePerm("label:type:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody @Validated IdParam param) {
        labelTypeService.deleteType(param.getId(), TenantContext.get());
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    @Operation(summary = "批量删除标签类型")
    @RequirePerm("label:type:batchDelete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        labelTypeService.batchDeleteTypes(body.get("ids"), TenantContext.get());
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    @Operation(summary = "启停标签类型")
    @RequirePerm("label:type:edit")
    @PostMapping("/enable")
    public R<Void> enable(@RequestBody Map<String, Object> body) {
        LabelTypeUpdateParam param = new LabelTypeUpdateParam();
        param.setId(Long.valueOf(String.valueOf(body.get("id"))));
        param.setIsEnabled(Boolean.valueOf(String.valueOf(body.get("isEnabled"))));
        labelTypeService.updateType(param, TenantContext.get());
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    @Operation(summary = "标签类型下拉")
    @RequirePerm("label:type:query")
    @PostMapping("/options")
    public R<List<LabelTypeVO>> options() {
        return R.ok(labelTypeService.listEnabled(TenantContext.get()));
    }
}
