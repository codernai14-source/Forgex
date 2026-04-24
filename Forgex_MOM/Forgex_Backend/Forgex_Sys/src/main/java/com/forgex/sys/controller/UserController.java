/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.api.dto.UserInfoDTO;
import com.forgex.common.api.dto.UserThirdPartyInvokeDTO;
import com.forgex.common.api.dto.UserThirdPartyPullResultDTO;
import com.forgex.common.api.dto.UserThirdPartySyncDTO;
import com.forgex.common.api.dto.UserThirdPartySyncRequestDTO;
import com.forgex.common.api.feign.IntegrationInternalFeignClient;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.dto.SysUserRoleQueryDTO;
import com.forgex.sys.domain.dto.SysUserRoleSaveDTO;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.param.BatchIdsParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.param.UserStatusUpdateParam;
import com.forgex.sys.domain.param.UserThirdPartySyncParam;
import com.forgex.sys.domain.vo.SysUserVO;
import com.forgex.sys.service.ISysUserRoleService;
import com.forgex.sys.service.ISysUserService;
import com.forgex.sys.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理控制器。
 * <p>
 * 负责用户管理相关的页面接口、第三方用户同步接口以及供工作流模块调用的内部用户查询接口。
 * 工作流内部接口用于根据部门、角色、岗位解析有效审批人，按当前租户隔离数据。
 * </p>
 * <p>
 * 控制器只负责请求参数校验、租户解析和响应封装，用户保存、角色绑定、导入同步等业务规则由
 * Service 层统一处理。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-07
 * @see ISysUserService
 * @see ISysUserRoleService
 * @see UserValidator
 */
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class UserController {

    private final ISysUserService userService;
    private final ISysUserRoleService userRoleService;
    private final UserValidator userValidator;
    private final IntegrationInternalFeignClient integrationInternalFeignClient;

    /**
     * 分页查询用户列表。
     *
     * @param query 查询条件，包含分页参数和账号、姓名、部门、角色等筛选项
     * @return 分页用户列表
     */
    @RequirePerm("sys:user:view")
    @PostMapping("/page")
    public R<IPage<SysUserVO>> page(@RequestBody SysUserQueryDTO query) {
        // 创建分页对象后委派给服务层，保持列表、导出和角色筛选口径一致。
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        return R.ok(userService.pageUserVOs(page, query));
    }

    /**
     * 查询用户列表。
     *
     * @param query 查询条件
     * @return 用户列表
     */
    @RequirePerm("sys:user:view")
    @PostMapping("/list")
    public R<List<SysUserVO>> list(@RequestBody SysUserQueryDTO query) {
        return R.ok(userService.listUserVOs(query));
    }

    /**
     * 根据 ID 获取用户详情。
     *
     * @param param 用户 ID 参数
     * @return 用户详情
     */
    @RequirePerm("sys:user:view")
    @PostMapping("/detail")
    public R<SysUserVO> detail(@RequestBody IdParam param) {
        userValidator.validateId(param.getId());
        return R.ok(userService.getUserVOById(param.getId()));
    }

    /**
     * 新增用户。
     *
     * @param userDTO 用户创建参数
     * @return 创建结果
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.ADD, detailTemplateCode = "USER_CREATE", detailFields = {"account", "username"})
    @RequirePerm("sys:user:add")
    @PostMapping("/create")
    public R<Void> create(@RequestBody @Validated SysUserDTO userDTO) {
        userValidator.validateForAdd(userDTO);
        userService.addUser(userDTO);
        return R.ok(CommonPrompt.CREATE_SUCCESS);
    }

    /**
     * 查询用户在当前租户下已分配的角色 ID。
     *
     * @param param 用户角色查询参数
     * @return 已分配角色 ID 列表和当前租户 ID
     */
    @RequirePerm("sys:user:assignRole")
    @PostMapping("/role/listByUser")
    public R<Map<String, Object>> listUserRoles(@RequestBody @Validated SysUserRoleQueryDTO param) {
        Long tenantId = resolveCurrentTenantId();
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        userValidator.validateId(param.getUserId());
        List<Long> assignedRoleIds = userRoleService.listAssignedRoleIds(param.getUserId(), tenantId);
        Map<String, Object> data = new HashMap<>();
        data.put("assignedRoleIds", assignedRoleIds);
        data.put("tenantId", tenantId);
        return R.ok(data);
    }

    /**
     * 保存用户在当前租户下的角色分配。
     *
     * @param param 用户角色保存参数
     * @return 保存结果
     */
    @RequirePerm("sys:user:assignRole")
    @PostMapping("/role/saveByUser")
    public R<Void> saveUserRoles(@RequestBody @Validated SysUserRoleSaveDTO param) {
        Long tenantId = resolveCurrentTenantId();
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        userValidator.validateId(param.getUserId());
        userRoleService.saveUserRoles(param.getUserId(), tenantId, param.getRoleIds());
        return R.ok(CommonPrompt.ASSIGN_SUCCESS);
    }

    /**
     * 更新用户。
     *
     * @param userDTO 用户更新参数
     * @return 更新结果
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.UPDATE, detailTemplateCode = "USER_UPDATE", detailFields = {"id", "account", "username"})
    @RequirePerm("sys:user:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated SysUserDTO userDTO) {
        userValidator.validateForUpdate(userDTO);
        userService.updateUser(userDTO);
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    /**
     * 删除用户。
     *
     * @param param 用户 ID 参数
     * @return 删除结果
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.DELETE, detailTemplateCode = "USER_DELETE", detailFields = {"id"})
    @RequirePerm("sys:user:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody IdParam param) {
        userValidator.validateId(param.getId());
        userValidator.validateForDelete(param.getId());
        userService.deleteUser(param.getId());
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    /**
     * 批量删除用户。
     *
     * @param param 用户 ID 列表参数
     * @return 删除结果
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.DELETE, detailTemplateCode = "USER_BATCH_DELETE", detailFields = {"ids"})
    @RequirePerm("sys:user:batchDelete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody BatchIdsParam param) {
        List<Long> ids = param.getIds();
        for (Long id : ids) {
            userValidator.validateForDelete(id);
        }
        userService.batchDeleteUsers(ids);
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    /**
     * 重置指定用户密码。
     *
     * @param param 用户 ID 参数
     * @return 重置结果
     */
    @RequirePerm("sys:user:resetPwd")
    @PostMapping("/resetPassword")
    public R<Void> resetPassword(@RequestBody IdParam param) {
        userValidator.validateId(param.getId());
        userService.resetPassword(param.getId());
        return R.ok(CommonPrompt.RESET_SUCCESS);
    }

    /**
     * 更新用户启用状态。
     *
     * @param param 状态更新参数
     * @return 更新结果
     */
    @RequirePerm("sys:user:edit")
    @PostMapping("/updateStatus")
    public R<Void> updateStatus(@RequestBody UserStatusUpdateParam param) {
        userValidator.validateId(param.getId());
        if (param.getStatus() == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        userService.updateStatus(param.getId(), param.getStatus());
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    /**
     * 重置 admin 用户密码。
     *
     * @return 重置结果
     */
    @RequirePerm("sys:user:resetPwd")
    @PostMapping("/resetAdminPassword")
    public R<Void> resetAdminPassword() {
        Long adminId = userService.getUserIdByAccount("admin");
        if (adminId == null) {
            return R.fail(CommonPrompt.ADMIN_NOT_FOUND);
        }
        userService.resetPassword(adminId);
        return R.ok(CommonPrompt.RESET_SUCCESS);
    }

    /**
     * 触发第三方用户同步。
     *
     * @param param 第三方接口参数
     * @return 同步结果
     */
    @RequirePerm("sys:user:syncThirdParty")
    @PostMapping("/sync-third-party")
    public R<UserThirdPartyPullResultDTO> syncThirdParty(@RequestBody UserThirdPartySyncParam param) {
        Long tenantId = resolveCurrentTenantId();
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        UserThirdPartyInvokeDTO request = new UserThirdPartyInvokeDTO();
        request.setApiCode(param.getApiCode());
        request.setTenantId(tenantId);
        request.getPayload().put("tenantId", tenantId);
        return integrationInternalFeignClient.syncUsers(request);
    }

    /**
     * 从第三方系统拉取用户并写入本系统。
     *
     * @param param 第三方接口参数
     * @return 拉取结果
     */
    @RequirePerm("sys:user:pullThirdParty")
    @PostMapping("/pull-from-third-party")
    public R<UserThirdPartyPullResultDTO> pullFromThirdParty(@RequestBody UserThirdPartySyncParam param) {
        Long tenantId = resolveCurrentTenantId();
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        UserThirdPartyInvokeDTO request = new UserThirdPartyInvokeDTO();
        request.setApiCode(param.getApiCode());
        request.setTenantId(tenantId);
        request.getPayload().put("tenantId", tenantId);
        return integrationInternalFeignClient.pullUsers(request);
    }

    /**
     * 导入用户 Excel 文件。
     *
     * @param file 导入文件
     * @return 导入结果
     * @throws Exception 文件读取或解析异常
     */
    @RequirePerm("sys:user:import")
    @PostMapping("/import")
    public R<UserThirdPartyPullResultDTO> importUsers(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return R.fail(CommonPrompt.FILE_EMPTY);
        }
        Long tenantId = resolveCurrentTenantId();
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        return R.ok(CommonPrompt.IMPORT_SUCCESS, userService.importUsers(tenantId, file));
    }

    /**
     * 导出第三方用户同步数据。
     *
     * @param request 第三方用户同步请求
     * @return 第三方用户列表
     */
    @PostMapping("/internal/export-third-party-users")
    public R<List<UserThirdPartySyncDTO>> exportThirdPartyUsers(@RequestBody UserThirdPartySyncRequestDTO request) {
        return R.ok(userService.listThirdPartyUsers(request.getTenantId()));
    }

    /**
     * 同步第三方用户数据。
     *
     * @param request 第三方用户同步请求
     * @return 同步结果
     */
    @PostMapping("/internal/sync-third-party-users")
    public R<UserThirdPartyPullResultDTO> syncThirdPartyUsers(@RequestBody UserThirdPartySyncRequestDTO request) {
        return R.ok(userService.syncThirdPartyUsers(request.getTenantId(), request.getUsers()));
    }

    /**
     * 根据部门 ID 列表查询当前租户下有效用户 ID。
     * <p>
     * 供工作流审批节点按部门匹配审批人使用。
     * </p>
     *
     * @param deptIds 部门 ID 列表
     * @return 用户 ID 列表
     */
    @PostMapping("/internal/listUserIdsByDeptIds")
    public R<List<Long>> listUserIdsByDeptIds(@RequestBody List<Long> deptIds) {
        Long tenantId = resolveCurrentTenantId();
        if (tenantId == null) {
            return R.ok(List.of());
        }
        return R.ok(userService.listUserIdsByDeptIds(tenantId, deptIds));
    }

    /**
     * 根据角色 ID 列表查询当前租户下有效用户 ID。
     * <p>
     * 供工作流审批节点按角色匹配审批人使用。
     * </p>
     *
     * @param roleIds 角色 ID 列表
     * @return 用户 ID 列表
     */
    @PostMapping("/internal/listUserIdsByRoleIds")
    public R<List<Long>> listUserIdsByRoleIds(@RequestBody List<Long> roleIds) {
        Long tenantId = resolveCurrentTenantId();
        if (tenantId == null) {
            return R.ok(List.of());
        }
        return R.ok(userService.listUserIdsByRoleIds(tenantId, roleIds));
    }

    /**
     * 根据岗位 ID 列表查询当前租户下有效用户 ID。
     * <p>
     * 供工作流审批节点按岗位匹配审批人使用。
     * </p>
     *
     * @param positionIds 岗位 ID 列表
     * @return 用户 ID 列表
     */
    @PostMapping("/internal/listUserIdsByPositionIds")
    public R<List<Long>> listUserIdsByPositionIds(@RequestBody List<Long> positionIds) {
        Long tenantId = resolveCurrentTenantId();
        if (tenantId == null) {
            return R.ok(List.of());
        }
        return R.ok(userService.listUserIdsByPositionIds(tenantId, positionIds));
    }

    /**
     * 内部接口：根据用户 ID 查询用户基础信息。
     *
     * @param userId 用户 ID
     * @return 用户基础信息
     */
    @GetMapping("/info/{userId}")
    public R<UserInfoDTO> getUserById(@PathVariable Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null) {
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
        return R.ok(dto);
    }

    /**
     * 内部接口：根据账号查询用户基础信息。
     *
     * @param account 账号
     * @return 用户基础信息
     */
    @GetMapping("/info/account/{account}")
    public R<UserInfoDTO> getUserByAccount(@PathVariable String account) {
        SysUser user = userService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getAccount, account));
        if (user == null) {
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
        return R.ok(dto);
    }

    /**
     * 内部接口：批量查询用户基础信息。
     *
     * @param userIds 用户 ID 列表
     * @return 用户基础信息列表
     */
    @PostMapping("/info/batch")
    public R<List<UserInfoDTO>> getUsersByIds(@RequestBody List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return R.ok(List.of());
        }
        List<UserInfoDTO> dtos = userService.listByIds(userIds).stream()
            .map(user -> {
                UserInfoDTO dto = new UserInfoDTO();
                BeanUtils.copyProperties(user, dto);
                return dto;
            })
            .collect(Collectors.toList());
        return R.ok(dtos);
    }

    /**
     * 内部接口：批量查询用户名称映射。
     *
     * @param userIds 用户 ID 列表
     * @return 用户 ID 与用户名映射
     */
    @PostMapping("/info/username-map")
    public R<Map<Long, String>> getUsernameMap(@RequestBody List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return R.ok(Map.of());
        }
        Map<Long, String> map = userService.listByIds(userIds).stream()
            .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername));
        return R.ok(map);
    }

    /**
     * 解析当前租户 ID。
     * <p>
     * 优先使用租户上下文，缺失时回退当前登录用户租户。
     * </p>
     *
     * @return 当前租户 ID
     */
    private Long resolveCurrentTenantId() {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            return tenantId;
        }
        return CurrentUserUtils.getTenantId();
    }
}
