package com.forgex.basic.label.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.basic.label.domain.param.IdParam;
import com.forgex.basic.label.domain.param.LabelFieldQueryParam;
import com.forgex.basic.label.domain.param.LabelFieldSaveParam;
import com.forgex.basic.label.domain.param.LabelFieldUpdateParam;
import com.forgex.basic.label.domain.vo.LabelFieldVO;
import com.forgex.basic.label.service.LabelFieldService;
import com.forgex.common.domain.dto.excel.TemplateOption;
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

@Tag(name = "标签字段管理")
@RestController
@RequestMapping("/label/field")
@RequiredArgsConstructor
public class LabelFieldController {

    private final LabelFieldService labelFieldService;

    @Operation(summary = "分页查询标签字段")
    @RequirePerm("label:field:query")
    @PostMapping("/page")
    public R<IPage<LabelFieldVO>> page(@RequestBody LabelFieldQueryParam param) {
        return R.ok(labelFieldService.pageFields(param, TenantContext.get()));
    }

    @Operation(summary = "查询标签字段详情")
    @RequirePerm("label:field:query")
    @PostMapping("/detail")
    public R<LabelFieldVO> detail(@RequestBody @Validated IdParam param) {
        return R.ok(labelFieldService.getById(param.getId(), TenantContext.get()));
    }

    @Operation(summary = "新增标签字段")
    @RequirePerm("label:field:add")
    @PostMapping("/add")
    public R<Long> add(@RequestBody @Validated LabelFieldSaveParam param) {
        return R.ok(labelFieldService.addField(param, TenantContext.get()));
    }

    @Operation(summary = "更新标签字段")
    @RequirePerm("label:field:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated LabelFieldUpdateParam param) {
        labelFieldService.updateField(param, TenantContext.get());
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    @Operation(summary = "删除标签字段")
    @RequirePerm("label:field:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody @Validated IdParam param) {
        labelFieldService.deleteField(param.getId(), TenantContext.get());
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    @Operation(summary = "启停标签字段")
    @RequirePerm("label:field:edit")
    @PostMapping("/enable")
    public R<Void> enable(@RequestBody Map<String, Object> body) {
        LabelFieldUpdateParam param = new LabelFieldUpdateParam();
        param.setId(Long.valueOf(String.valueOf(body.get("id"))));
        param.setIsEnabled(Boolean.valueOf(String.valueOf(body.get("isEnabled"))));
        labelFieldService.updateField(param, TenantContext.get());
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    @Operation(summary = "标签字段下拉")
    @RequirePerm("label:field:query")
    @PostMapping("/options")
    public R<List<TemplateOption>> options() {
        return R.ok(labelFieldService.options(TenantContext.get()));
    }
}
