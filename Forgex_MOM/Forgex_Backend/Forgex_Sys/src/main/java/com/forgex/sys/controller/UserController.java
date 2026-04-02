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
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 用户管理 Controller
 * 
 * 职责：
 * - 接收 HTTP 请求
 * - 参数校验（调用 UserValidator）
 * - 调用 Service 层方法
 * - 返回响应结果
 * 
 * 接口规范：
 * - 所有接口统一使用 POST 方法
 * - 参数统一封装为对象
 * - 分页查询使用 BaseGetParam（pageNum/pageSize）
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
    
    /**
     * 分页查询用户列表
     * <p>
     * 接口路径：POST /sys/user/page
     * 需要权限：sys:user:query
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收查询参数（包含 pageNum、pageSize 和筛选条件）</li>
     *   <li>创建 MyBatis-Plus 分页对象</li>
     *   <li>调用 Service 层分页查询用户 VO 列表</li>
     *   <li>返回分页结果</li>
     * </ol>
     *
     * @param query 查询参数
     *              - pageNum: 页码（必填，从 1 开始）
     *              - pageSize: 每页大小（必填）
     *              - account: 账号（可选，模糊查询）
     *              - username: 用户名（可选，模糊查询）
     *              - status: 用户状态（可选，0=禁用，1=启用）
     *              - departmentId: 部门 ID（可选）
     * @return {@link R} 包含用户分页列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 分页结果（IPage&lt;SysUserVO&gt;）
     *         - message: 提示信息
     * @see SysUserQueryDTO
     * @see SysUserVO
     */
    @PostMapping("/page")
    public R<IPage<SysUserVO>> page(@RequestBody SysUserQueryDTO query) {
        // 1. 创建 MyBatis-Plus 分页对象，使用查询参数中的 pageNum 和 pageSize
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        // 2. 委派给 Service 层查询用户 VO 分页列表
        return R.ok(userService.pageUserVOs(page, query));
    }
    
    /**
     * 查询用户列表（不分页）
     * <p>
     * 接口路径：POST /sys/user/list
     * 需要权限：sys:user:query
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收查询参数（包含筛选条件）</li>
     *   <li>调用 Service 层查询用户 VO 列表</li>
     *   <li>返回用户列表</li>
     * </ol>
     *
     * @param query 查询参数
     *              - account: 账号（可选，模糊查询）
     *              - username: 用户名（可选，模糊查询）
     *              - status: 用户状态（可选，0=禁用，1=启用）
     *              - departmentId: 部门 ID（可选）
     * @return {@link R} 包含用户列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 用户列表（List&lt;SysUserVO&gt;）
     *         - message: 提示信息
     * @see SysUserQueryDTO
     * @see SysUserVO
     */
    @PostMapping("/list")
    public R<List<SysUserVO>> list(@RequestBody SysUserQueryDTO query) {
        // 委派给 Service 层查询用户 VO 列表（不分页）
        return R.ok(userService.listUserVOs(query));
    }
    
    /**
     * 根据 ID 获取用户详情
     * <p>
     * 接口路径：POST /sys/user/detail
     * 需要权限：sys:user:query
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 ID 参数</li>
     *   <li>调用 Validator 校验 ID 合法性</li>
     *   <li>调用 Service 层获取用户 VO 详情</li>
     *   <li>返回用户详情</li>
     * </ol>
     *
     * @param param 用户 ID 参数
     *              - id: 用户 ID（必填）
     * @return {@link R} 包含用户详情的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 用户详情（SysUserVO）
     *         - message: 提示信息
     * @throws BusinessException 用户 ID 为空或用户不存在时抛出
     * @see IdParam
     * @see SysUserVO
     * @see UserValidator#validateId(Long)
     */
    @PostMapping("/detail")
    public R<SysUserVO> detail(@RequestBody IdParam param) {
        // 1. 调用 Validator 校验用户 ID 合法性
        userValidator.validateId(param.getId());
        // 2. 委派给 Service 层获取用户 VO 详情
        return R.ok(userService.getUserVOById(param.getId()));
    }
    
    /**
     * 新增用户
     * <p>
     * 接口路径：POST /sys/user/create
     * 需要权限：sys:user:add
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 DTO 参数</li>
     *   <li>调用 Validator 校验数据（账号唯一性、必填字段等）</li>
     *   <li>调用 Service 层添加用户</li>
     *   <li>记录操作日志（USER_CREATE）</li>
     *   <li>返回创建成功提示</li>
     * </ol>
     *
     * @param userDTO 用户创建参数
     *                - account: 账号（必填）
     *                - username: 用户名（必填）
     *                - password: 密码（必填）
     *                - email: 邮箱（可选）
     *                - phone: 手机号（可选）
     *                - departmentId: 部门 ID（可选）
     *                - status: 用户状态（可选，默认启用）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（创建成功）
     * @throws BusinessException 账号已存在或参数校验失败时抛出
     * @see SysUserDTO
     * @see UserValidator#validateForAdd(SysUserDTO)
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.ADD, detailTemplateCode = "USER_CREATE", detailFields = {"account", "username"})
    @RequirePerm("sys:user:add")
    @PostMapping("/create")
    public R<Void> create(@RequestBody @Validated SysUserDTO userDTO) {
        // 1. 调用 Validator 进行数据校验：检查账号唯一性、必填字段等
        userValidator.validateForAdd(userDTO);
        
        // 2. 调用 Service 层添加用户
        userService.addUser(userDTO);
        
        // 3. 返回创建成功提示
        return R.ok(CommonPrompt.CREATE_SUCCESS);
    }

    /**
     * 查询用户在当前租户下已分配的角色 ID 列表
     * <p>
     * 接口路径：POST /sys/user/role/listByUser
     * 需要权限：sys:user:assignRole
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>如果租户 ID 为空，从 CurrentUserUtils 获取</li>
     *   <li>校验租户 ID 是否为空</li>
     *   <li>调用 Validator 校验用户 ID 合法性</li>
     *   <li>调用 Service 层查询用户已分配角色 ID 列表</li>
     *   <li>返回角色 ID 列表和租户 ID</li>
     * </ol>
     *
     * @param param 查询参数
     *              - userId: 用户 ID（必填）
     * @return {@link R} 包含角色分配信息的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: Map 对象
     *           - assignedRoleIds: 已分配角色 ID 列表
     *           - tenantId: 当前租户 ID
     *         - message: 提示信息
     * @throws BusinessException 参数校验失败或租户 ID 为空时抛出
     * @see SysUserRoleQueryDTO
     * @see UserValidator#validateId(Long)
     */
    @RequirePerm("sys:user:assignRole")
    @PostMapping("/role/listByUser")
    public R<Map<String, Object>> listUserRoles(@RequestBody @Validated SysUserRoleQueryDTO param) {
        // 1. 从 TenantContext 获取当前租户 ID
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            tenantId = CurrentUserUtils.getTenantId();
        }
        // 2. 校验租户 ID 是否为空
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        // 3. 调用 Validator 校验用户 ID 合法性
        userValidator.validateId(param.getUserId());
        // 4. 调用 Service 层查询用户已分配角色 ID 列表
        List<Long> assignedRoleIds = userRoleService.listAssignedRoleIds(param.getUserId(), tenantId);
        // 5. 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("assignedRoleIds", assignedRoleIds);
        data.put("tenantId", tenantId);
        // 6. 返回角色分配信息
        return R.ok(data);
    }

    /**
     * 保存用户在当前租户下的角色分配结果（清空并重建绑定）
     * <p>
     * 接口路径：POST /sys/user/role/saveByUser
     * 需要权限：sys:user:assignRole
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>如果租户 ID 为空，从 CurrentUserUtils 获取</li>
     *   <li>校验租户 ID 是否为空</li>
     *   <li>调用 Validator 校验用户 ID 合法性</li>
     *   <li>调用 Service 层保存用户角色分配</li>
     *   <li>返回分配成功提示</li>
     * </ol>
     *
     * @param param 保存参数
     *              - userId: 用户 ID（必填）
     *              - roleIds: 角色 ID 列表（可选，为空表示清空所有角色）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（分配成功）
     * @throws BusinessException 用户不存在、角色不存在或跨租户时抛出
     * @see SysUserRoleSaveDTO
     * @see UserValidator#validateId(Long)
     */
    @RequirePerm("sys:user:assignRole")
    @PostMapping("/role/saveByUser")
    public R<Void> saveUserRoles(@RequestBody @Validated SysUserRoleSaveDTO param) {
        // 1. 从 TenantContext 获取当前租户 ID
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            tenantId = CurrentUserUtils.getTenantId();
        }
        // 2. 校验租户 ID 是否为空
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        // 3. 调用 Validator 校验用户 ID 合法性
        userValidator.validateId(param.getUserId());
        // 4. 调用 Service 层保存用户角色分配（清空并重建）
        userRoleService.saveUserRoles(param.getUserId(), tenantId, param.getRoleIds());
        // 5. 返回分配成功提示
        return R.ok(CommonPrompt.ASSIGN_SUCCESS);
    }
    
    /**
     * 更新用户
     * <p>
     * 接口路径：POST /sys/user/update
     * 需要权限：sys:user:edit
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 DTO 参数</li>
     *   <li>调用 Validator 校验数据（用户存在性、账号唯一性等）</li>
     *   <li>调用 Service 层更新用户</li>
     *   <li>记录操作日志（USER_UPDATE）</li>
     *   <li>返回更新成功提示</li>
     * </ol>
     *
     * @param userDTO 用户更新参数
     *                - id: 用户 ID（必填）
     *                - account: 账号（可选）
     *                - username: 用户名（可选）
     *                - email: 邮箱（可选）
     *                - phone: 手机号（可选）
     *                - departmentId: 部门 ID（可选）
     *                - status: 用户状态（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（更新成功）
     * @throws BusinessException 用户不存在或参数校验失败时抛出
     * @see SysUserDTO
     * @see UserValidator#validateForUpdate(SysUserDTO)
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.UPDATE, detailTemplateCode = "USER_UPDATE", detailFields = {"id", "account", "username"})
    @RequirePerm("sys:user:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated SysUserDTO userDTO) {
        // 1. 调用 Validator 进行数据校验：检查用户存在性、账号唯一性等
        userValidator.validateForUpdate(userDTO);
        
        // 2. 调用 Service 层更新用户
        userService.updateUser(userDTO);
        
        // 3. 返回更新成功提示
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }
    
    /**
     * 删除用户
     * <p>
     * 接口路径：POST /sys/user/delete
     * 需要权限：sys:user:delete
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 ID 参数</li>
     *   <li>调用 Validator 校验 ID 合法性</li>
     *   <li>调用 Validator 校验是否允许删除（如检查是否为管理员）</li>
     *   <li>调用 Service 层删除用户（逻辑删除）</li>
     *   <li>记录操作日志（USER_DELETE）</li>
     *   <li>返回删除成功提示</li>
     * </ol>
     *
     * @param param 用户 ID 参数
     *              - id: 用户 ID（必填）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（删除成功）
     * @throws BusinessException 用户 ID 为空或不允许删除时抛出
     * @see IdParam
     * @see UserValidator#validateForDelete(Long)
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.DELETE, detailTemplateCode = "USER_DELETE", detailFields = {"id"})
    @RequirePerm("sys:user:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody IdParam param) {
        // 1. 调用 Validator 校验用户 ID 合法性
        userValidator.validateId(param.getId());
        // 2. 调用 Validator 校验是否允许删除
        userValidator.validateForDelete(param.getId());
        
        // 3. 调用 Service 层删除用户（逻辑删除）
        userService.deleteUser(param.getId());
        
        // 4. 返回删除成功提示
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }
    
    /**
     * 批量删除用户
     * <p>
     * 接口路径：POST /sys/user/batchDelete
     * 需要权限：sys:user:delete
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 ID 列表参数</li>
     *   <li>遍历 ID 列表，逐个校验是否允许删除</li>
     *   <li>调用 Service 层批量删除用户</li>
     *   <li>记录操作日志（USER_BATCH_DELETE）</li>
     *   <li>返回删除成功提示</li>
     * </ol>
     *
     * @param param 批量删除参数
     *              - ids: 用户 ID 列表（必填，不能为空）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（删除成功）
     * @throws BusinessException 参数校验失败或不允许删除时抛出
     * @see BatchIdsParam
     * @see UserValidator#validateForDelete(Long)
     */
    @OperationLog(module = "sys", menuPath = "/system/user", operationType = OperationType.DELETE, detailTemplateCode = "USER_BATCH_DELETE", detailFields = {"ids"})
    @RequirePerm("sys:user:delete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody BatchIdsParam param) {
        // 1. 解析参数，获取用户 ID 列表
        List<Long> ids = param.getIds();
        
        // 2. 遍历 ID 列表，逐个校验是否允许删除
        for (Long id : ids) {
            userValidator.validateForDelete(id);
        }
        
        // 3. 调用 Service 层批量删除用户
        userService.batchDeleteUsers(ids);
        
        // 4. 返回删除成功提示
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }
    
    /**
     * 重置密码
     * <p>
     * 接口路径：POST /sys/user/resetPassword
     * 需要权限：sys:user:resetPwd
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 ID 参数</li>
     *   <li>调用 Validator 校验 ID 合法性</li>
     *   <li>调用 Service 层重置密码为默认密码（123456）</li>
     *   <li>返回重置成功提示</li>
     * </ol>
     *
     * @param param 用户 ID 参数
     *              - id: 用户 ID（必填）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（重置成功）
     * @throws BusinessException 用户 ID 为空时抛出
     * @see IdParam
     * @see UserValidator#validateId(Long)
     */
    @RequirePerm("sys:user:resetPwd")
    @PostMapping("/resetPassword")
    public R<Void> resetPassword(@RequestBody IdParam param) {
        // 1. 调用 Validator 校验用户 ID 合法性
        userValidator.validateId(param.getId());
        // 2. 调用 Service 层重置密码为默认密码
        userService.resetPassword(param.getId());
        // 3. 返回重置成功提示
        return R.ok(CommonPrompt.RESET_SUCCESS);
    }
    
    /**
     * 更新用户状态
     * <p>
     * 接口路径：POST /sys/user/updateStatus
     * 需要权限：sys:user:edit
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 ID 和状态参数</li>
     *   <li>调用 Validator 校验 ID 合法性</li>
     *   <li>校验状态参数是否为空</li>
     *   <li>调用 Service 层更新用户状态</li>
     *   <li>返回更新成功提示</li>
     * </ol>
     *
     * @param param 状态更新参数
     *              - id: 用户 ID（必填）
     *              - status: 用户状态（必填，0=禁用，1=启用）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（更新成功）
     * @throws BusinessException 参数校验失败时抛出
     * @see UserStatusUpdateParam
     * @see UserValidator#validateId(Long)
     */
    @RequirePerm("sys:user:edit")
    @PostMapping("/updateStatus")
    public R<Void> updateStatus(@RequestBody UserStatusUpdateParam param) {
        // 1. 调用 Validator 校验用户 ID 合法性
        userValidator.validateId(param.getId());
        // 2. 校验状态参数是否为空
        if (param.getStatus() == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        
        // 3. 调用 Service 层更新用户状态
        userService.updateStatus(param.getId(), param.getStatus());
        // 4. 返回更新成功提示
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }
    
    /**
     * 重置 admin 用户密码（临时接口）
     * <p>
     * 接口路径：POST /sys/user/resetAdminPassword
     * 需要权限：sys:user:resetPwd
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>根据账号"admin"查询用户 ID</li>
     *   <li>校验 admin 用户是否存在</li>
     *   <li>调用 Service 层重置 admin 密码</li>
     *   <li>返回重置成功提示</li>
     * </ol>
     *
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（重置成功）
     * @throws BusinessException admin 用户不存在时抛出
     * @see CommonPrompt#ADMIN_NOT_FOUND
     */
    @RequirePerm("sys:user:resetPwd")
    @PostMapping("/resetAdminPassword")
    public R<Void> resetAdminPassword() {
        // 1. 根据账号"admin"查询用户 ID
        Long adminId = userService.getUserIdByAccount("admin");
        // 2. 校验 admin 用户是否存在
        if (adminId == null) {
            return R.fail(CommonPrompt.ADMIN_NOT_FOUND);
        }
        // 3. 调用 Service 层重置 admin 密码
        userService.resetPassword(adminId);
        // 4. 返回重置成功提示
        return R.ok(CommonPrompt.RESET_SUCCESS);
    }
    
    /**
     * 解析 Long 类型参数（私有工具方法）
     * 
     * <p>
     * 支持的输入类型：
     * - null: 返回 null
     * - Number 及其子类：直接转换为 long
     * - String: 尝试解析为 long
     * </p>
     *
     * @param obj 要转换的对象
     * @return 转换后的 Long 值，转换失败返回 null
     */
    private Long parseLong(Object obj) {
        // 1. null 值检查
        if (obj == null) {
            return null;
        }
        // 2. Number 类型直接转换
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        // 3. String 类型尝试解析
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                // 解析失败返回 null
                return null;
            }
        }
        // 4. 其他类型返回 null
        return null;
    }
    
    // ==================== Feign 调用接口 ====================
    
    /**
     * 根据用户 ID 获取用户信息（Feign 调用）
     * <p>
     * 接口路径：GET /sys/user/info/{userId}
     * 需要认证：是（服务间调用）
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 ID 参数</li>
     *   <li>调用 Service 层获取用户实体</li>
     *   <li>用户不存在时返回错误提示</li>
     *   <li>将用户实体转换为 UserInfoDTO</li>
     *   <li>返回用户信息</li>
     * </ol>
     *
     * @param userId 用户 ID
     * @return {@link R} 包含用户信息的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 用户信息（UserInfoDTO）
     *         - message: 提示信息
     * @throws BusinessException 用户不存在时抛出
     * @see UserInfoDTO
     */
    @GetMapping("/info/{userId}")
    public R<UserInfoDTO> getUserById(@PathVariable Long userId) {
        // 1. 调用 Service 层获取用户实体
        SysUser user = userService.getById(userId);
        // 2. 用户不存在时返回错误提示
        if (user == null) {
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        
        // 3. 将用户实体转换为 UserInfoDTO
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
        // 4. 返回用户信息
        return R.ok(dto);
    }
    
    /**
     * 根据账号获取用户信息（Feign 调用）
     * <p>
     * 接口路径：GET /sys/user/info/account/{account}
     * 需要认证：是（服务间调用）
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收账号参数</li>
     *   <li>调用 Service 层根据账号查询用户</li>
     *   <li>用户不存在时返回错误提示</li>
     *   <li>将用户实体转换为 UserInfoDTO</li>
     *   <li>返回用户信息</li>
     * </ol>
     *
     * @param account 账号
     * @return {@link R} 包含用户信息的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 用户信息（UserInfoDTO）
     *         - message: 提示信息
     * @throws BusinessException 用户不存在时抛出
     * @see UserInfoDTO
     */
    @GetMapping("/info/account/{account}")
    public R<UserInfoDTO> getUserByAccount(@PathVariable String account) {
        // 1. 调用 Service 层根据账号查询用户
        SysUser user = userService.getOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, account)
        );
        
        // 2. 用户不存在时返回错误提示
        if (user == null) {
            return R.fail(CommonPrompt.USER_NOT_FOUND);
        }
        
        // 3. 将用户实体转换为 UserInfoDTO
        UserInfoDTO dto = new UserInfoDTO();
        BeanUtils.copyProperties(user, dto);
        // 4. 返回用户信息
        return R.ok(dto);
    }
    
    /**
     * 批量获取用户信息（Feign 调用）
     * <p>
     * 接口路径：POST /sys/user/info/batch
     * 需要认证：是（服务间调用）
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 ID 列表参数</li>
     *   <li>校验 ID 列表是否为空</li>
     *   <li>调用 Service 层批量获取用户</li>
     *   <li>将用户实体列表转换为 UserInfoDTO 列表</li>
     *   <li>返回用户信息列表</li>
     * </ol>
     *
     * @param userIds 用户 ID 列表
     * @return {@link R} 包含用户信息列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 用户信息列表（List&lt;UserInfoDTO&gt;）
     *         - message: 提示信息
     * @see UserInfoDTO
     */
    @PostMapping("/info/batch")
    public R<List<UserInfoDTO>> getUsersByIds(@RequestBody List<Long> userIds) {
        // 1. 校验 ID 列表是否为空
        if (userIds == null || userIds.isEmpty()) {
            return R.ok(List.of());
        }
        
        // 2. 调用 Service 层批量获取用户
        List<SysUser> users = userService.listByIds(userIds);
        // 3. 将用户实体列表转换为 UserInfoDTO 列表
        List<UserInfoDTO> dtos = users.stream()
            .map(user -> {
                UserInfoDTO dto = new UserInfoDTO();
                BeanUtils.copyProperties(user, dto);
                return dto;
            })
            .collect(Collectors.toList());
        
        // 4. 返回用户信息列表
        return R.ok(dtos);
    }
    
    /**
     * 批量获取用户名映射（Feign 调用）
     * <p>
     * 接口路径：POST /sys/user/info/username-map
     * 需要认证：是（服务间调用）
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收用户 ID 列表参数</li>
     *   <li>校验 ID 列表是否为空</li>
     *   <li>调用 Service 层批量获取用户</li>
     *   <li>将用户列表转换为 ID 到用户名的映射</li>
     *   <li>返回用户名映射</li>
     * </ol>
     *
     * @param userIds 用户 ID 列表
     * @return {@link R} 包含用户名映射的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 用户 ID 到用户名的映射（Map&lt;Long, String&gt;）
     *         - message: 提示信息
     */
    @PostMapping("/info/username-map")
    public R<Map<Long, String>> getUsernameMap(@RequestBody List<Long> userIds) {
        // 1. 校验 ID 列表是否为空
        if (userIds == null || userIds.isEmpty()) {
            return R.ok(Map.of());
        }
        
        // 2. 调用 Service 层批量获取用户
        List<SysUser> users = userService.listByIds(userIds);
        // 3. 将用户列表转换为 ID 到用户名的映射
        Map<Long, String> map = users.stream()
            .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername));
        
        // 4. 返回用户名映射
        return R.ok(map);
    }
}
