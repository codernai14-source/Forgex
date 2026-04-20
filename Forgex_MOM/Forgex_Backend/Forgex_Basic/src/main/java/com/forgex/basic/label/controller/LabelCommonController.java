package com.forgex.basic.label.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.basic.label.domain.param.PrintExceptionQueryParam;
import com.forgex.basic.label.domain.vo.PlaceholderVO;
import com.forgex.basic.label.domain.vo.PrintExceptionVO;
import com.forgex.basic.label.domain.vo.TemplateValidateResultVO;
import com.forgex.basic.label.service.LabelCommonService;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签通用工具 Controller
 */
@Tag(name = "标签通用工具")
@RestController
@RequestMapping("/label")
@RequiredArgsConstructor
public class LabelCommonController {

    private final LabelCommonService labelCommonService;

    /**
     * 获取标准占位符列表
     */
    @Operation(summary = "获取标准占位符列表")
    @PostMapping("/placeholder/list")
    public R<List<PlaceholderVO>> getStandardPlaceholders(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long factoryId) {
        if (tenantId == null) {
            tenantId = TenantContext.get();
        }
        List<PlaceholderVO> list = labelCommonService.getStandardPlaceholders(tenantId);
        return R.ok(list);
    }

    /**
     * 校验模板 JSON 合法性
     */
    @Operation(summary = "校验模板 JSON 合法性")
    @PostMapping("/template/validateJson")
    public R<TemplateValidateResultVO> validateTemplateJson(
            @RequestBody Map<String, Object> params) {
        String templateContent = (String) params.get("templateContent");
        Long tenantId = params.get("tenantId") != null
                ? Long.valueOf(params.get("tenantId").toString())
                : TenantContext.get();

        TemplateValidateResultVO result = labelCommonService.validateTemplateJson(templateContent, tenantId);
        return R.ok(result);
    }

    /**
     * 分页查询打印异常日志
     */
    @Operation(summary = "分页查询打印异常日志")
    @RequirePerm("label:exception:page")
    @PostMapping("/exception/page")
    public R<IPage<PrintExceptionVO>> pageExceptions(
            @Valid PrintExceptionQueryParam param,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long factoryId) {
        if (tenantId == null) {
            tenantId = TenantContext.get();
        }
        IPage<PrintExceptionVO> page = labelCommonService.pageExceptions(param, tenantId);
        return R.ok(page);
    }
}
