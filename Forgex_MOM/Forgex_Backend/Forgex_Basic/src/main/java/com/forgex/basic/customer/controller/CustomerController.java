package com.forgex.basic.customer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.customer.domain.dto.CustomerDTO;
import com.forgex.basic.customer.domain.param.CustomerApprovalStartParam;
import com.forgex.basic.customer.domain.param.CustomerPageParam;
import com.forgex.basic.customer.domain.param.CustomerSaveParam;
import com.forgex.basic.customer.domain.param.CustomerWorkflowCallbackParam;
import com.forgex.basic.customer.service.ICustomerService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/basic/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final ICustomerService customerService;

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
}
