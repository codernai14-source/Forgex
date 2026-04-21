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
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysRoleDTO;
import com.forgex.sys.domain.dto.SysRoleQueryDTO;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.service.ISysRoleService;
import com.forgex.sys.validator.RoleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理 Controller
 * 
 * 职责：
 * - 接收 HTTP 请求
 * - 参数校验（调用 Validator）
 * - 调用 Service 层方法
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
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {
    
    private final ISysRoleService roleService;
    private final RoleValidator roleValidator;
    
    /**
     * 分页查询角色列表
     * 
     * <p>接口路径：POST /sys/role/page</p>
     * 
     * <p>认证要求：需要登录，需要 sys:role:view 权限</p>
     * 
     * <p>执行步骤：</p>
     * <ol>
     *     <li>接收分页参数和查询条件（pageNum/pageSize/keyword 等）</li>
     *     <li>调用 Service 层分页查询角色</li>
     *     <li>返回分页结果，包含角色列表和总数</li>
     * </ol>
     * 
     * <p>参数说明：</p>
     * <ul>
     *     <li>- query.pageNum: 页码，从 1 开始</li>
     *     <li>- query.pageSize: 每页条数，默认 10</li>
     *     <li>- query.keyword: 搜索关键词（可选），支持角色名称、角色编码模糊查询</li>
     *     <li>- query.status: 状态筛选（可选），true=启用，false=禁用</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *     <li>- code: 响应码，0 表示成功</li>
     *     <li>- data: 分页结果，包含 records（角色列表）、total（总数）、pages（总页数）等</li>
     *     <li>- message: 响应消息</li>
     * </ul>
     * 
     * @param query 查询参数对象，包含分页信息和查询条件
     * @return 分页结果，包含角色列表和总数
     * @see SysRoleQueryDTO
     * @see SysRoleDTO
     */
    @RequirePerm("sys:role:view")
    @PostMapping("/page")
    public R<IPage<SysRoleDTO>> page(@RequestBody SysRoleQueryDTO query) {
        // 使用 BaseGetParam 中的 pageNum 和 pageSize
        Page<SysRole> page = new Page<>(query.getPageNum(), query.getPageSize());
        return R.ok(roleService.pageRoles(page, query));
    }
    
    /**
     * 查询角色列表（不分页）
     * 
     * <p>接口路径：POST /sys/role/list</p>
     * 
     * <p>认证要求：需要登录，需要 sys:role:view 权限</p>
     * 
     * <p>执行步骤：</p>
     * <ol>
     *     <li>接收查询条件（keyword/status 等）</li>
     *     <li>调用 Service 层查询所有符合条件的角色</li>
     *     <li>返回角色列表，不分页</li>
     * </ol>
     * 
     * <p>参数说明：</p>
     * <ul>
     *     <li>- query.keyword: 搜索关键词（可选），支持角色名称、角色编码模糊查询</li>
     *     <li>- query.status: 状态筛选（可选），true=启用，false=禁用</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *     <li>- code: 响应码，0 表示成功</li>
     *     <li>- data: 角色列表，每个元素包含 id、roleCode、roleName、status 等字段</li>
     *     <li>- message: 响应消息</li>
     * </ul>
     * 
     * <p>使用场景：下拉框选择角色、批量操作等需要获取全部角色的场景</p>
     * 
     * @param query 查询参数对象，包含查询条件
     * @return 角色列表
     * @see SysRoleQueryDTO
     * @see SysRoleDTO
     */
    @RequirePerm("sys:role:view")
    @PostMapping("/list")
    public R<List<SysRoleDTO>> list(@RequestBody SysRoleQueryDTO query) {
        return R.ok(roleService.listRoles(query));
    }
    
    /**
     * 根据 ID 获取角色详情
     * 
     * <p>接口路径：POST /sys/role/detail</p>
     * 
     * <p>认证要求：需要登录，需要 sys:role:view 权限</p>
     * 
     * <p>执行步骤：</p>
     * <ol>
     *     <li>从请求体中解析角色 ID</li>
     *     <li>校验 ID 是否合法（不为空且为正整数）</li>
     *     <li>调用 Service 层根据 ID 查询角色详情</li>
     *     <li>返回角色详细信息</li>
     * </ol>
     * 
     * <p>参数说明：</p>
     * <ul>
     *     <li>- body.id: 角色 ID，必填，正整数</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *     <li>- code: 响应码，0 表示成功</li>
     *     <li>- data: 角色详情对象，包含完整的角色信息</li>
     *     <li>- message: 响应消息</li>
     * </ul>
     * 
     * <p>异常说明：</p>
     * <ul>
     *     <li>- IllegalArgumentException: ID 不合法时抛出</li>
     *     <li>- BusinessException: 角色不存在时抛出</li>
     * </ul>
     * 
     * @param body 请求体，包含角色 ID
     * @return 角色详情对象
     * @see SysRoleDTO
     */
    @RequirePerm("sys:role:view")
    @PostMapping("/detail")
    public R<SysRoleDTO> detail(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        roleValidator.validateId(id);
        return R.ok(roleService.getRoleById(id));
    }
    
    /**
     * 新增角色
     * 
     * <p>接口路径：POST /sys/role/create</p>
     * 
     * <p>认证要求：需要登录，需要 sys:role:add 权限</p>
     * 
     * <p>执行步骤：</p>
     * <ol>
     *     <li>接收角色数据并进行数据校验（@Validated）</li>
     *     <li>调用 Validator 验证新增角色的数据合法性</li>
     *     <li>调用 Service 层保存角色</li>
     *     <li>返回成功响应</li>
     * </ol>
     * 
     * <p>参数说明：</p>
     * <ul>
     *     <li>- roleDTO.roleCode: 角色编码，必填，唯一</li>
     *     <li>- roleDTO.roleName: 角色名称，必填</li>
     *     <li>- roleDTO.roleDesc: 角色描述（可选）</li>
     *     <li>- roleDTO.status: 状态，true=启用，false=禁用</li>
     *     <li>- roleDTO.sort: 排序号（可选）</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *     <li>- code: 响应码，0 表示成功</li>
     *     <li>- data: null</li>
     *     <li>- message: "创建成功"</li>
     * </ul>
     * 
     * <p>异常说明：</p>
     * <ul>
     *     <li>- ConstraintViolationException: 数据校验失败时抛出</li>
     *     <li>- BusinessException: 角色编码已存在时抛出</li>
     * </ul>
     * 
     * @param roleDTO 角色数据传输对象
     * @return 操作结果
     * @see SysRoleDTO
     */
    @RequirePerm("sys:role:add")
    @PostMapping("/create")
    public R<Void> create(@RequestBody @Validated SysRoleDTO roleDTO) {
        // 1. 数据校验
        roleValidator.validateForAdd(roleDTO);
        
        // 2. 调用 Service
        roleService.addRole(roleDTO);
        
        // 3. 返回结果
        return R.ok(CommonPrompt.CREATE_SUCCESS);
    }
    
    /**
     * 更新角色
     * 
     * <p>接口路径：POST /sys/role/update</p>
     * 
     * <p>认证要求：需要登录，需要 sys:role:edit 权限</p>
     * 
     * <p>执行步骤：</p>
     * <ol>
     *     <li>接收角色数据并进行数据校验（@Validated）</li>
     *     <li>调用 Validator 验证更新角色的数据合法性</li>
     *     <li>调用 Service 层更新角色</li>
     *     <li>返回成功响应</li>
     * </ol>
     * 
     * <p>参数说明：</p>
     * <ul>
     *     <li>- roleDTO.id: 角色 ID，必填</li>
     *     <li>- roleDTO.roleCode: 角色编码，必填，唯一</li>
     *     <li>- roleDTO.roleName: 角色名称，必填</li>
     *     <li>- roleDTO.roleDesc: 角色描述（可选）</li>
     *     <li>- roleDTO.status: 状态，true=启用，false=禁用</li>
     *     <li>- roleDTO.sort: 排序号（可选）</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *     <li>- code: 响应码，0 表示成功</li>
     *     <li>- data: null</li>
     *     <li>- message: "更新成功"</li>
     * </ul>
     * 
     * <p>异常说明：</p>
     * <ul>
     *     <li>- ConstraintViolationException: 数据校验失败时抛出</li>
     *     <li>- BusinessException: 角色不存在或角色编码已存在时抛出</li>
     * </ul>
     * 
     * @param roleDTO 角色数据传输对象
     * @return 操作结果
     * @see SysRoleDTO
     */
    @RequirePerm("sys:role:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated SysRoleDTO roleDTO) {
        // 1. 数据校验
        roleValidator.validateForUpdate(roleDTO);
        
        // 2. 调用 Service
        roleService.updateRole(roleDTO);
        
        // 3. 返回结果
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }
    
    /**
     * 删除角色
     * 
     * <p>接口路径：POST /sys/role/delete</p>
     * 
     * <p>认证要求：需要登录，需要 sys:role:delete 权限</p>
     * 
     * <p>执行步骤：</p>
     * <ol>
     *     <li>从请求体中解析角色 ID</li>
     *     <li>调用 Validator 验证删除操作是否合法（检查是否被使用）</li>
     *     <li>调用 Service 层删除角色</li>
     *     <li>返回成功响应</li>
     * </ol>
     * 
     * <p>参数说明：</p>
     * <ul>
     *     <li>- body.id: 角色 ID，必填，正整数</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *     <li>- code: 响应码，0 表示成功</li>
     *     <li>- data: null</li>
     *     <li>- message: "删除成功"</li>
     * </ul>
     * 
     * <p>异常说明：</p>
     * <ul>
     *     <li>- IllegalArgumentException: ID 不合法时抛出</li>
     *     <li>- BusinessException: 角色不存在或已被使用时抛出</li>
     * </ul>
     * 
     * @param body 请求体，包含角色 ID
     * @return 操作结果
     */
    @RequirePerm("sys:role:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, Object> body) {
        // 1. 解析参数
        Long id = parseLong(body.get("id"));
        
        // 2. 数据校验
        roleValidator.validateForDelete(id);
        
        // 3. 调用 Service
        roleService.deleteRole(id);
        
        // 4. 返回结果
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }
    
    /**
     * 批量删除角色
     * 
     * <p>接口路径：POST /sys/role/batchDelete</p>
     * 
     * <p>认证要求：需要登录，需要 sys:role:delete 权限</p>
     * 
     * <p>执行步骤：</p>
     * <ol>
     *     <li>从请求体中解析角色 ID 列表</li>
     *     <li>遍历并校验每个 ID 的合法性</li>
     *     <li>调用 Service 层批量删除角色</li>
     *     <li>返回成功响应</li>
     * </ol>
     * 
     * <p>参数说明：</p>
     * <ul>
     *     <li>- body.ids: 角色 ID 列表，必填，至少包含一个 ID</li>
     * </ul>
     * 
     * <p>返回值说明：</p>
     * <ul>
     *     <li>- code: 响应码，0 表示成功</li>
     *     <li>- data: null</li>
     *     <li>- message: "删除成功"</li>
     * </ul>
     * 
     * <p>异常说明：</p>
     * <ul>
     *     <li>- IllegalArgumentException: ID 列表为空或包含非法 ID 时抛出</li>
     *     <li>- BusinessException: 任一角色不存在或已被使用时抛出</li>
     * </ul>
     * 
     * @param body 请求体，包含角色 ID 列表
     * @return 操作结果
     */
    @RequirePerm("sys:role:delete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody Map<String, Object> body) {
        // 1. 解析参数
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        
        // 2. 校验每个 ID
        for (Long id : ids) {
            roleValidator.validateForDelete(id);
        }
        
        // 3. 调用 Service
        roleService.batchDeleteRoles(ids);
        
        // 4. 返回结果
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }
    
    /**
     * 解析 Long 类型参数
     * 
     * <p>将 Object 类型的参数转换为 Long 类型，支持 Number 和 String 类型。</p>
     * 
     * <p>执行步骤：</p>
     * <ol>
     *     <li>判断对象是否为 null，是则返回 null</li>
     *     <li>判断对象是否为 Number 类型，是则直接转换为 Long</li>
     *     <li>判断对象是否为 String 类型，是则尝试解析为 Long</li>
     *     <li>其他情况返回 null</li>
     * </ol>
     * 
     * @param obj 待转换的对象
     * @return 转换后的 Long 值，转换失败返回 null
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
