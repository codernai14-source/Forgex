package com.forgex.basic.employee.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.employee.domain.dto.EmployeeDTO;
import com.forgex.basic.employee.domain.dto.EmployeeSyncUserResultDTO;
import com.forgex.basic.employee.domain.entity.BasicEmployee;
import com.forgex.basic.employee.domain.param.EmployeePageParam;
import com.forgex.basic.employee.service.IEmployeeService;
import com.forgex.common.api.dto.EmployeeThirdPartyInvokeDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncResultDTO;
import com.forgex.common.api.dto.EmployeeThirdPartySyncDTO;
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
 * 人员主数据控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final IEmployeeService employeeService;

    @RequirePerm("basic:employee:query")
    @PostMapping("/page")
    public R<Page<EmployeeDTO>> page(@RequestBody(required = false) EmployeePageParam param) {
        return R.ok(employeeService.page(param));
    }

    @PostMapping("/list")
    public R<List<EmployeeDTO>> list(@RequestBody(required = false) EmployeePageParam param) {
        return R.ok(employeeService.list(param));
    }

    @RequirePerm("basic:employee:query")
    @PostMapping("/detail")
    public R<EmployeeDTO> detail(@RequestBody Map<String, Object> params) {
        return R.ok(employeeService.detail(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:employee:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody BasicEmployee param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, employeeService.create(param));
    }

    @RequirePerm("basic:employee:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody BasicEmployee param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, employeeService.update(param));
    }

    @RequirePerm("basic:employee:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, employeeService.delete(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:employee:batchDelete")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody Map<String, List<Long>> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, employeeService.batchDelete(params.get("ids")));
    }

    @RequirePerm("basic:employee:syncUser")
    @PostMapping("/sync-user")
    public R<EmployeeSyncUserResultDTO> syncUser(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.SYNC_SUCCESS, employeeService.syncUser(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:employee:syncUser")
    @PostMapping("/batch-sync-user")
    public R<EmployeeSyncUserResultDTO> batchSyncUser(@RequestBody Map<String, List<Long>> params) {
        return R.ok(CommonPrompt.SYNC_SUCCESS, employeeService.batchSyncUser(params.get("ids")));
    }

    @RequirePerm("basic:employee:sync")
    @PostMapping("/sync-third-party")
    public R<EmployeeThirdPartySyncResultDTO> syncThirdParty(@RequestBody(required = false) EmployeeThirdPartyInvokeDTO request) {
        return R.ok(CommonPrompt.SYNC_SUCCESS, employeeService.syncToThirdParty(request));
    }

    @RequirePerm("basic:employee:pullThirdParty")
    @PostMapping("/pull-from-third-party")
    public R<EmployeeThirdPartySyncResultDTO> pullFromThirdParty(@RequestBody(required = false) EmployeeThirdPartyInvokeDTO request) {
        return R.ok(CommonPrompt.SYNC_SUCCESS, employeeService.pullFromThirdParty(request));
    }

    @PostMapping("/internal/sync-third-party-employees")
    public R<EmployeeThirdPartySyncResultDTO> internalSyncThirdPartyEmployees(@RequestBody EmployeeThirdPartySyncRequestDTO request) {
        return R.ok(employeeService.syncThirdPartyEmployees(request));
    }

    @PostMapping("/internal/export-third-party-employees")
    public R<List<EmployeeThirdPartySyncDTO>> internalExportThirdPartyEmployees(@RequestBody EmployeeThirdPartySyncRequestDTO request) {
        return R.ok(employeeService.exportThirdPartyEmployees(request));
    }
}
