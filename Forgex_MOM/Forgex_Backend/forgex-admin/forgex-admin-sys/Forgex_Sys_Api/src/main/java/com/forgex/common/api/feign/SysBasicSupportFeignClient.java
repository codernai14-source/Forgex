package com.forgex.common.api.feign;

import com.forgex.common.api.dto.SysDictValueRequest;
import com.forgex.common.api.dto.SysDataScopeDTO;
import com.forgex.common.api.dto.SysEmployeeReferenceDTO;
import com.forgex.common.api.dto.SysEmployeeReferenceRequest;
import com.forgex.common.api.dto.SysEmployeeUserRequest;
import com.forgex.common.api.dto.SysEmployeeUserResult;
import com.forgex.common.api.dto.SysModuleSummaryDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 基础资料服务需要的平台能力。调用时透传登录 token 与租户上下文。
 */
@FeignClient(name = "forgex-sys", contextId = "sysBasicSupportFeignClient", path = "/sys/internal/basic-support")
public interface SysBasicSupportFeignClient {

    /**
     * 加载当前登录用户的数据权限，服务端核对用户与租户会话。
     *
     * @param userId 当前用户 ID
     * @return 当前租户的数据权限快照
     */
    @PostMapping("/data-scope")
    R<SysDataScopeDTO> dataScope(@RequestBody Long userId);

    /**
     * 在平台内原子创建或更新用户及租户绑定，可按人员业务标识重复调用。
     *
     * @param request 人员信息
     * @return 已提交的用户结果
     */
    @PostMapping("/sync-employee-user")
    R<SysEmployeeUserResult> syncEmployeeUser(@RequestBody SysEmployeeUserRequest request);

    /**
     * 批量读取当前租户的部门、岗位和人员对应用户。
     *
     * @param requests 查询项
     * @return 人员关联信息
     */
    @PostMapping("/employee-references")
    R<List<SysEmployeeReferenceDTO>> employeeReferences(@RequestBody List<SysEmployeeReferenceRequest> requests);

    /**
     * 校验字典值；未配置字典时保留原有允许保存的行为。
     *
     * @param request 字典条件
     * @return 是否有效
     */
    @PostMapping("/dict-value-valid")
    R<Boolean> dictValueValid(@RequestBody SysDictValueRequest request);

    /**
     * 获取当前租户可用模块摘要。
     *
     * @param moduleId 模块 ID
     * @return 模块摘要，不存在时为 null
     */
    @PostMapping("/module")
    R<SysModuleSummaryDTO> module(@RequestBody Long moduleId);
}
