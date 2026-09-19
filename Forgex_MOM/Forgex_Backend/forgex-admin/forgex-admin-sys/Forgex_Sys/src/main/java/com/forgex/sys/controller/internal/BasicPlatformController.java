package com.forgex.sys.controller.internal;

import com.forgex.common.api.dto.SysDictValueRequest;
import com.forgex.common.api.dto.SysEmployeeReferenceDTO;
import com.forgex.common.api.dto.SysEmployeeReferenceRequest;
import com.forgex.common.api.dto.SysEmployeeUserRequest;
import com.forgex.common.api.dto.SysEmployeeUserResult;
import com.forgex.common.api.dto.SysModuleSummaryDTO;
import com.forgex.common.web.R;
import com.forgex.sys.service.platform.BasicPlatformReferenceService;
import com.forgex.sys.service.platform.EmployeeUserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 基础资料的平台内部 API。服务层验证实际登录会话、租户及同步权限。
 */
@RestController
@RequestMapping("/sys/internal/basic-support")
@RequiredArgsConstructor
public class BasicPlatformController {

    private final EmployeeUserSyncService employeeUserSyncService;
    private final BasicPlatformReferenceService referenceService;

    /**
     * 原子同步人员用户与租户绑定。
     * @param request 人员信息
     * @return 已提交的用户信息
     */
    @PostMapping("/sync-employee-user")
    public R<SysEmployeeUserResult> syncEmployeeUser(@RequestBody SysEmployeeUserRequest request) {
        return R.ok(employeeUserSyncService.sync(request));
    }

    /**
     * 批量读取人员平台关联。
     * @param requests 查询项
     * @return 关联列表
     */
    @PostMapping("/employee-references")
    public R<List<SysEmployeeReferenceDTO>> employeeReferences(@RequestBody List<SysEmployeeReferenceRequest> requests) {
        return R.ok(referenceService.employeeReferences(requests));
    }

    /**
     * 校验字典值。
     * @param request 字典条件
     * @return 是否有效
     */
    @PostMapping("/dict-value-valid")
    public R<Boolean> dictValueValid(@RequestBody SysDictValueRequest request) {
        return R.ok(referenceService.dictValueValid(request));
    }

    /**
     * 获取平台模块摘要。
     * @param moduleId 模块 ID
     * @return 模块摘要
     */
    @PostMapping("/module")
    public R<SysModuleSummaryDTO> module(@RequestBody Long moduleId) {
        return R.ok(referenceService.module(moduleId));
    }
}
