package com.forgex.basic.supplier.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.supplier.domain.dto.SupplierDTO;
import com.forgex.basic.supplier.domain.param.SupplierPageParam;
import com.forgex.basic.supplier.domain.param.SupplierReviewStartParam;
import com.forgex.basic.supplier.domain.param.SupplierSaveParam;
import com.forgex.basic.supplier.domain.param.SupplierWorkflowCallbackParam;
import com.forgex.basic.supplier.service.ISupplierService;
import com.forgex.common.api.dto.SupplierAggregateDTO;
import com.forgex.common.api.dto.SupplierOptionDTO;
import com.forgex.common.api.dto.SupplierQueryRequestDTO;
import com.forgex.common.api.dto.SupplierThirdPartyInvokeDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncResultDTO;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 供应商主数据控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 * @see ISupplierService
 */
@Tag(name = "供应商主数据", description = "供应商主数据管理接口")
@RestController
@RequestMapping("/basic/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final ISupplierService supplierService;

    @Operation(summary = "分页查询供应商")
    @RequirePerm("basic:supplier:query")
    @PostMapping("/page")
    public R<Page<SupplierDTO>> page(@RequestBody(required = false) SupplierPageParam param) {
        return R.ok(supplierService.page(param));
    }

    @Operation(summary = "查询供应商列表")
    @PostMapping("/list")
    public R<List<SupplierDTO>> list(@RequestBody(required = false) SupplierPageParam param) {
        return R.ok(supplierService.list(param));
    }

    @Operation(summary = "获取供应商详情")
    @RequirePerm("basic:supplier:query")
    @PostMapping("/detail")
    public R<SupplierDTO> detail(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(String.valueOf(params.get("id")));
        return R.ok(supplierService.getDetailById(id));
    }

    @Operation(summary = "新增供应商")
    @RequirePerm("basic:supplier:add")
    @PostMapping("/create")
    public R<Long> create(@Valid @RequestBody SupplierSaveParam param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, supplierService.create(param));
    }

    @Operation(summary = "修改供应商")
    @RequirePerm("basic:supplier:edit")
    @PostMapping("/update")
    public R<Boolean> update(@Valid @RequestBody SupplierSaveParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, supplierService.update(param));
    }

    @Operation(summary = "删除供应商")
    @RequirePerm("basic:supplier:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(String.valueOf(params.get("id")));
        return R.ok(CommonPrompt.DELETE_SUCCESS, supplierService.delete(id));
    }

    @Operation(summary = "下载供应商导入模板")
    @RequirePerm("basic:supplier:import")
    @PostMapping("/import-template")
    public void importTemplate(HttpServletResponse response) throws IOException {
        prepareExcelResponse(response, "供应商主数据导入模板.xlsx");
        supplierService.writeImportTemplate(response.getOutputStream());
    }

    @Operation(summary = "导入供应商")
    @RequirePerm("basic:supplier:import")
    @PostMapping("/import")
    public R<SupplierThirdPartySyncResultDTO> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return R.ok(CommonPrompt.IMPORT_SUCCESS, supplierService.importExcel(file));
    }

    @Operation(summary = "导出供应商")
    @RequirePerm("basic:supplier:export")
    @PostMapping("/export")
    public void export(@RequestBody(required = false) SupplierPageParam param, HttpServletResponse response) throws IOException {
        prepareExcelResponse(response, "供应商主数据.xlsx");
        supplierService.exportExcel(param, response.getOutputStream());
    }

    @Operation(summary = "生成供应商租户")
    @RequirePerm("basic:supplier:generateTenant")
    @PostMapping("/generate-tenant")
    public R<String> generateTenant(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(String.valueOf(params.get("id")));
        return R.ok(CommonPrompt.CREATE_SUCCESS, supplierService.generateTenant(id));
    }

    @Operation(summary = "发起供应商资质审查")
    @RequirePerm("basic:supplier:review")
    @PostMapping("/review/start")
    public R<Long> startReview(@RequestBody SupplierReviewStartParam param) {
        return R.ok(CommonPrompt.SUBMIT_SUCCESS, supplierService.startQualificationReview(param));
    }

    @Operation(summary = "同步供应商到第三方")
    @RequirePerm("basic:supplier:sync")
    @PostMapping("/sync-third-party")
    public R<SupplierThirdPartySyncResultDTO> syncThirdParty(@RequestBody(required = false) SupplierThirdPartyInvokeDTO request) {
        return R.ok(CommonPrompt.SYNC_SUCCESS, supplierService.syncToThirdParty(request));
    }

    @Operation(summary = "工作流回调：供应商审查结果")
    @PostMapping("/workflow/callback")
    public R<Boolean> workflowCallback(@RequestBody SupplierWorkflowCallbackParam param) {
        return R.ok(supplierService.handleWorkflowCallback(param));
    }

    @PostMapping("/internal/get-by-code")
    public R<SupplierAggregateDTO> internalGetByCode(@RequestBody SupplierQueryRequestDTO request) {
        return R.ok(supplierService.getByCode(request));
    }

    @PostMapping("/internal/list-by-codes")
    public R<List<SupplierAggregateDTO>> internalListByCodes(@RequestBody SupplierQueryRequestDTO request) {
        return R.ok(supplierService.listByCodes(request));
    }

    @PostMapping("/internal/options")
    public R<List<SupplierOptionDTO>> internalOptions(@RequestBody SupplierQueryRequestDTO request) {
        return R.ok(supplierService.listOptions(request));
    }

    @PostMapping("/internal/sync-third-party-suppliers")
    public R<SupplierThirdPartySyncResultDTO> internalSyncThirdPartySuppliers(@RequestBody SupplierThirdPartySyncRequestDTO request) {
        return R.ok(supplierService.syncThirdPartySuppliers(request));
    }

    @PostMapping("/internal/export-third-party-suppliers")
    public R<List<SupplierAggregateDTO>> internalExportThirdPartySuppliers(@RequestBody SupplierThirdPartySyncRequestDTO request) {
        return R.ok(supplierService.exportThirdPartySuppliers(request));
    }

    private void prepareExcelResponse(HttpServletResponse response, String fileName) {
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName);
    }
}
