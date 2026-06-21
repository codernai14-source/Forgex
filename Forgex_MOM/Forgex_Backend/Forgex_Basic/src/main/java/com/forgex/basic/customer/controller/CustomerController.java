package com.forgex.basic.customer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.customer.domain.dto.CustomerDTO;
import com.forgex.basic.customer.domain.param.CustomerApprovalStartParam;
import com.forgex.basic.customer.domain.param.CustomerPageParam;
import com.forgex.basic.customer.domain.param.CustomerSaveParam;
import com.forgex.basic.customer.domain.param.CustomerWorkflowCallbackParam;
import com.forgex.basic.customer.service.ICustomerService;
import com.forgex.common.api.dto.CustomerAggregateDTO;
import com.forgex.common.api.dto.CustomerThirdPartyInvokeDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncResultDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.service.excel.FxExcelImportHandler;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 客户主数据控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Tag(name = "客户主数据", description = "客户主数据管理接口")
@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final ICustomerService customerService;
    @Qualifier("basicCustomerImportHandler")
    private final FxExcelImportHandler basicCustomerImportHandler;

    /**
     * 分页查询客户主数据。
     *
     * @param param 分页查询参数
     * @return 客户分页结果
     */
    @Operation(summary = "分页查询客户")
    @RequirePerm("basic:customer:query")
    @PostMapping("/page")
    public R<Page<CustomerDTO>> page(@RequestBody(required = false) CustomerPageParam param) {
        return R.ok(customerService.page(param));
    }

    /**
     * 查询客户主数据列表。
     *
     * @param param 查询参数
     * @return 客户列表
     */
    @Operation(summary = "查询客户列表")
    @PostMapping("/list")
    public R<List<CustomerDTO>> list(@RequestBody(required = false) CustomerPageParam param) {
        return R.ok(customerService.list(param));
    }

    /**
     * 获取客户详情。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @Operation(summary = "获取客户详情")
    @RequirePerm("basic:customer:query")
    @PostMapping("/detail")
    public R<CustomerDTO> detail(@RequestBody Map<String, Object> params) {
        return R.ok(customerService.getDetailById(Long.valueOf(String.valueOf(params.get("id")))));
    }

    /**
     * 新增客户。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @Operation(summary = "新增客户")
    @RequirePerm("basic:customer:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody CustomerSaveParam param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, customerService.create(param));
    }

    /**
     * 修改客户。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @Operation(summary = "修改客户")
    @RequirePerm("basic:customer:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody CustomerSaveParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, customerService.update(param));
    }

    /**
     * 删除客户。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @Operation(summary = "删除客户")
    @RequirePerm("basic:customer:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, customerService.delete(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @Operation(summary = "批量删除客户")
    @RequirePerm("basic:customer:batchDelete")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody Map<String, List<Long>> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, customerService.batchDelete(params.get("ids")));
    }

    /**
     * 导入客户主数据。
     *
     * @param file Excel 文件
     * @return 导入结果
     * @throws IOException 读取文件失败时抛出
     */
    @Operation(summary = "导入客户")
    @RequirePerm("basic:customer:import")
    @PostMapping("/import")
    public R<CustomerThirdPartySyncResultDTO> importExcel(MultipartFile file) throws IOException {
        return R.ok(CommonPrompt.IMPORT_SUCCESS, customerService.importExcel(file));
    }

    /**
     * 下载客户导入模板。
     *
     * @param response HTTP 响应
     * @throws IOException 写出文件失败时抛出
     */
    @Operation(summary = "下载客户导入模板")
    @RequirePerm("basic:customer:import")
    @PostMapping("/import-template")
    public void importTemplate(HttpServletResponse response) throws IOException {
        prepareExcelResponse(response, "客户主数据导入模板.xlsx");
        customerService.writeImportTemplate(response.getOutputStream());
    }

    /**
     * 导出客户主数据。
     *
     * @param param 查询参数
     * @param response HTTP 响应
     * @throws IOException 写出文件失败时抛出
     */
    @Operation(summary = "导出客户")
    @RequirePerm("basic:customer:export")
    @PostMapping("/export")
    public void export(@RequestBody(required = false) CustomerPageParam param, HttpServletResponse response) throws IOException {
        prepareExcelResponse(response, "客户主数据.xlsx");
        customerService.exportExcel(param, response.getOutputStream());
    }

    /**
     * 创建客户租户。
     *
     * @param params 请求参数
     * @return 统一响应结果
     */
    @Operation(summary = "创建客户租户")
    @RequirePerm("basic:customer:generateTenant")
    @PostMapping("/generate-tenant")
    public R<String> generateTenant(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, customerService.generateTenant(Long.valueOf(String.valueOf(params.get("id")))));
    }

    /**
     * 发起客户审批。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @Operation(summary = "发起客户审批")
    @RequirePerm("basic:customer:approval")
    @PostMapping("/approval/start")
    public R<Long> startApproval(@RequestBody CustomerApprovalStartParam param) {
        return R.ok(CommonPrompt.SUBMIT_SUCCESS, customerService.startApproval(param));
    }

    /**
     * 同步客户主数据到第三方。
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @Operation(summary = "同步客户到第三方")
    @RequirePerm("basic:customer:sync")
    @PostMapping("/sync-third-party")
    public R<CustomerThirdPartySyncResultDTO> syncThirdParty(@RequestBody(required = false) CustomerThirdPartyInvokeDTO request) {
        return R.ok(CommonPrompt.SYNC_SUCCESS, customerService.syncToThirdParty(request));
    }

    /**
     * 从第三方拉取客户主数据。
     *
     * @param request 拉取请求
     * @return 写入结果
     */
    @Operation(summary = "从第三方拉取客户")
    @RequirePerm("basic:customer:pullThirdParty")
    @PostMapping("/pull-from-third-party")
    public R<CustomerThirdPartySyncResultDTO> pullFromThirdParty(@RequestBody(required = false) CustomerThirdPartyInvokeDTO request) {
        return R.ok(CommonPrompt.SYNC_SUCCESS, customerService.pullFromThirdParty(request));
    }

    /**
     * 工作流回调：客户审批。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @Operation(summary = "工作流回调：客户审批")
    @PostMapping("/workflow/callback")
    public R<Boolean> workflowCallback(@RequestBody CustomerWorkflowCallbackParam param) {
        return R.ok(customerService.handleWorkflowCallback(param));
    }

    /**
     * 内部接口：同步第三方客户数据。
     *
     * @param request 同步请求
     * @return 写入结果
     */
    @PostMapping("/internal/sync-third-party-customers")
    public R<CustomerThirdPartySyncResultDTO> internalSyncThirdPartyCustomers(@RequestBody CustomerThirdPartySyncRequestDTO request) {
        return R.ok(customerService.syncThirdPartyCustomers(request));
    }

    /**
     * 内部接口：导出第三方客户数据。
     *
     * @param request 导出请求
     * @return 客户聚合数据
     */
    @PostMapping("/internal/export-third-party-customers")
    public R<List<CustomerAggregateDTO>> internalExportThirdPartyCustomers(@RequestBody CustomerThirdPartySyncRequestDTO request) {
        return R.ok(customerService.exportThirdPartyCustomers(request));
    }

    /**
     * 内部接口：执行客户公共导入。
     *
     * @param param 导入参数
     * @return 导入结果
     */
    @PostMapping("/internal/import/execute")
    public R<FxExcelImportResultDTO> internalImportExecute(@RequestBody FxExcelImportExecuteParam param) {
        return R.ok(basicCustomerImportHandler.handle(param));
    }

    private void prepareExcelResponse(HttpServletResponse response, String fileName) {
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName);
    }
}
