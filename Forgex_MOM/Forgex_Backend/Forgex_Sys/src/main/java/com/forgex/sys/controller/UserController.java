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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.forgex.sys.domain.vo.SysUserVO;
import com.forgex.sys.service.ISysUserService;
import com.forgex.sys.service.ISysUserRoleService;
import com.forgex.sys.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 用户管理Controller
 * 
 * 职责：
 * - 接收HTTP请求
 * - 参数校验（调用Validator）
 * - 调用Service层方法
 * - 返回响应结果
 * 
 * 接口规范：
 * - 所有接口统一使用 POST 方法
 * - 参数统一封装为对象
 * - 分页查询使用 BaseGetParam（pageNum/pageSize）
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class UserController {
    
    private final ISysUserService userService;
    private final ISysUserRoleService userRoleService;
    private final UserValidator userValidator;
    
    /**
     * 分页查询用户列表
     */
    @PostMapping("/page")
    public R<IPage<SysUserVO>> page(@RequestBody SysUserQueryDTO query) {
        // 使用 BaseGetParam 中的 pageNum 和 pageSize
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        return R.ok(userService.pageUserVOs(page, query));
    }
    
    /**
     * 查询用户列表（不分页）
     */
    @PostMapping("/list")
    public R<List<SysUserVO>> list(@RequestBody SysUserQueryDTO query) {
        return R.ok(userService.listUserVOs(query));
    }
    
    /**
     * 根据ID获取用户详情
     */
    @PostMapping("/detail")
    public R<SysUserVO> detail(@RequestBody IdParam param) {
        userValidator.validateId(param.getId());
        return R.ok(userService.getUserVOById(param.getId()));
    }
    
    /**
     * 新增用户
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.ADD, detailTemplateCode = "USER_CREATE", detailFields = {"account", "username"})
    @RequirePerm("sys:user:create")
    @PostMapping("/create")
    public R<Void> create(@RequestBody @Validated SysUserDTO userDTO) {
        // 1. 数据校验
        userValidator.validateForAdd(userDTO);
        
        // 2. 调用Service
        userService.addUser(userDTO);
        
        // 3. 返回结果
        return R.ok(CommonPrompt.CREATE_SUCCESS);
    }

    /**
     * 查询用户在当前租户下已分配的角色ID列表。
     *
     * @param param 查询参数（用户ID）
     * @return 返回对象：assignedRoleIds（角色ID列表）、tenantId（当前租户ID）
     * @throws com.forgex.common.exception.BusinessException 参数校验失败时抛出
     */
    @RequirePerm("sys:user:assignRole")
    @PostMapping("/role/listByUser")
    public R<Map<String, Object>> listUserRoles(@RequestBody @Validated SysUserRoleQueryDTO param) {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            tenantId = CurrentUserUtils.getTenantId();
        }
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
     * 保存用户在当前租户下的角色分配结果（清空并重建绑定）。
     *
     * @param param 保存参数（用户ID、角色ID列表）
     * @return 保存结果
     * @throws com.forgex.common.exception.BusinessException 用户不存在、角色不存在或跨租户时抛出
     */
    @RequirePerm("sys:user:assignRole")
    @PostMapping("/role/saveByUser")
    public R<Void> saveUserRoles(@RequestBody @Validated SysUserRoleSaveDTO param) {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            tenantId = CurrentUserUtils.getTenantId();
        }
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        userValidator.validateId(param.getUserId());
        userRoleService.saveUserRoles(param.getUserId(), tenantId, param.getRoleIds());
        return R.ok(CommonPrompt.ASSIGN_SUCCESS);
    }
    
    /**
     * 更新用户
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.UPDATE, detailTemplateCode = "USER_UPDATE", detailFields = {"id", "account", "username"})
    @RequirePerm("sys:user:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated SysUserDTO userDTO) {
        // 1. 数据校验
        userValidator.validateForUpdate(userDTO);
        
        // 2. 调用Service
        userService.updateUser(userDTO);
        
        // 3. 返回结果
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }
    
    /**
     * 删除用户
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.DELETE, detailTemplateCode = "USER_DELETE", detailFields = {"id"})
    @RequirePerm("sys:user:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody IdParam param) {
        // 1. 数据校验
        userValidator.validateId(param.getId());
        userValidator.validateForDelete(param.getId());
        
        // 2. 调用Service
        userService.deleteUser(param.getId());
        
        // 3. 返回结果
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }
    
    /**
     * 批量删除用户
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.DELETE, detailTemplateCode = "USER_BATCH_DELETE", detailFields = {"ids"})
    @RequirePerm("sys:user:delete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody BatchIdsParam param) {
        // 1. 解析参数
        List<Long> ids = param.getIds();
        
        // 2. 校验每个ID
        for (Long id : ids) {
            userValidator.validateForDelete(id);
        }
        
        // 3. 调用Service
        userService.batchDeleteUsers(ids);
        
        // 4. 返回结果
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }
    
    /**
     * 重置密码
     */
    @RequirePerm("sys:user:resetPwd")
    @PostMapping("/resetPassword")
    public R<Void> resetPassword(@RequestBody IdParam param) {
        userValidator.validateId(param.getId());
        userService.resetPassword(param.getId());
        return R.ok(CommonPrompt.RESET_SUCCESS);
    }
    
    /**
     * 更新用户状态
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
     * 重置admin用户密码（临时接口）
     */
    @RequirePerm("sys:user:resetPwd")
    @GetMapping("/resetAdminPassword")
    public R<Void> resetAdminPassword() {
        Long adminId = userService.getUserIdByAccount("admin");
        if (adminId == null) {
            return R.fail(CommonPrompt.ADMIN_NOT_FOUND);
        }
        userService.resetPassword(adminId);
        return R.ok(CommonPrompt.RESET_SUCCESS);
    }
    
    /**
     * 解析Long类型参数
     */
    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
