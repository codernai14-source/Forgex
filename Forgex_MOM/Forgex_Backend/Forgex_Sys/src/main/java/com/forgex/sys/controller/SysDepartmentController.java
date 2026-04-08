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

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.department.SysDepartmentDTO;
import com.forgex.sys.domain.dto.department.SysDepartmentQueryDTO;
import com.forgex.sys.domain.dto.department.SysDepartmentSaveParam;
import com.forgex.sys.service.SysDepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 部门 Controller
 * <p>
 * 提供部门管理的 HTTP 接口，包括部门树查询、部门列表、部门详情、新增、修改、删除等功能。
 * </p>
 * <p>
 * 接口路径：/sys/department
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.sys.service.SysDepartmentService
 */
@Slf4j
@RestController
@RequestMapping("/sys/department")
@RequiredArgsConstructor
public class SysDepartmentController {
    
    private final SysDepartmentService departmentService;
    
    /**
     * 获取部门树
     * <p>
     * 接口路径：POST /sys/department/tree
     * </p>
     * <p>
     * 认证要求：是
     * </p>
     * <p>
     * 功能描述：获取当前租户下的完整部门树形结构，支持按状态过滤。
     * </p>
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>从租户上下文获取当前租户 ID</li>
     *   <li>调用部门服务获取部门树</li>
     *   <li>返回部门树列表</li>
     * </ol>
     * </p>
     * 
     * @param params 请求参数
     *               - status: Integer（可选），部门状态（0=禁用，1=启用），不传则返回所有状态
     * @return 操作结果
     *         - code: 响应状态码
     *         - data: List&lt;SysDepartmentDTO&gt;，部门树列表
     *         - message: 响应消息
     * @throws RuntimeException 获取租户 ID 失败时抛出
     * @see com.forgex.sys.domain.dto.department.SysDepartmentDTO
     * @see com.forgex.common.tenant.TenantContext
     */
    @PostMapping("/tree")
    public R<List<SysDepartmentDTO>> tree(@RequestBody Map<String, Object> params) {
        Long tenantId = TenantContext.get();
        List<SysDepartmentDTO> tree = departmentService.getDepartmentTree(tenantId);
        return R.ok(tree);
    }
    
    /**
     * 查询部门列表
     * <p>
     * 接口路径：POST /sys/department/list
     * </p>
     * <p>
     * 认证要求：是
     * </p>
     * <p>
     * 功能描述：根据查询条件获取部门列表，支持分页查询。
     * </p>
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>从租户上下文获取当前租户 ID</li>
     *   <li>设置查询 DTO 的租户 ID</li>
     *   <li>调用部门服务查询部门列表</li>
     *   <li>返回部门列表</li>
     * </ol>
     * </p>
     * 
     * @param queryDTO 查询参数对象
     *                 - deptName: String（可选），部门名称（模糊查询）
     *                 - deptCode: String（可选），部门编码（精确查询）
     *                 - status: Integer（可选），部门状态（0=禁用，1=启用）
     *                 - pageNum: Integer（可选），页码，默认 1
     *                 - pageSize: Integer（可选），每页大小，默认 10
     * @return 操作结果
     *         - code: 响应状态码
     *         - data: List&lt;SysDepartmentDTO&gt;，部门列表
     *         - message: 响应消息
     * @throws RuntimeException 获取租户 ID 失败时抛出
     * @see com.forgex.sys.domain.dto.department.SysDepartmentQueryDTO
     * @see com.forgex.sys.domain.dto.department.SysDepartmentDTO
     */
    @PostMapping("/list")
    public R<List<SysDepartmentDTO>> list(@RequestBody SysDepartmentQueryDTO queryDTO) {
        queryDTO.setTenantId(TenantContext.get());
        List<SysDepartmentDTO> list = departmentService.list(queryDTO);
        return R.ok(list);
    }
    
    /**
     * 获取部门详情
     * <p>
     * 接口路径：POST /sys/department/get
     * </p>
     * <p>
     * 认证要求：是
     * </p>
     * <p>
     * 功能描述：根据部门 ID 获取部门详细信息。
     * </p>
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>从请求参数中获取部门 ID</li>
     *   <li>从租户上下文获取当前租户 ID</li>
     *   <li>调用部门服务查询部门详情</li>
     *   <li>部门不存在时返回错误提示</li>
     *   <li>返回部门详情</li>
     * </ol>
     * </p>
     * 
     * @param params 请求参数
     *               - id: Long（必填），部门 ID
     * @return 操作结果
     *         - code: 响应状态码
     *         - data: SysDepartmentDTO，部门详情对象
     *         - message: 响应消息
     * @throws RuntimeException 参数解析失败时抛出
     * @see com.forgex.sys.domain.dto.department.SysDepartmentDTO
     * @see com.forgex.common.i18n.CommonPrompt#DEPARTMENT_NOT_FOUND
     */
    @PostMapping("/get")
    public R<SysDepartmentDTO> get(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long tenantId = TenantContext.get();
        
        SysDepartmentDTO department = departmentService.getById(id, tenantId);

        if (department == null) {
            return R.fail(CommonPrompt.DEPARTMENT_NOT_FOUND);
        }
        
        return R.ok(department);
    }
    
    /**
     * 新增部门
     * <p>
     * 接口路径：POST /sys/department/create
     * </p>
     * <p>
     * 认证要求：是
     * </p>
     * <p>
     * 权限要求：sys:dept:add
     * </p>
     * <p>
     * 功能描述：新增部门信息，支持树形结构的父子关系。
     * </p>
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>从租户上下文获取当前租户 ID</li>
     *   <li>设置保存参数的租户 ID</li>
     *   <li>参数校验（@Validated）</li>
     *   <li>调用部门服务创建部门</li>
     *   <li>返回创建成功提示（包含部门名称）</li>
     * </ol>
     * </p>
     * 
     * @param param 部门保存参数对象
     *              - parentId: Long（可选），父部门 ID（0 表示顶级部门）
     *              - orgType: String（必填），组织类型（group=集团，company=公司，subsidiary=子公司，department=部门，team=班组）
     *              - orgLevel: Integer（可选），组织层级
     *              - deptName: String（必填），部门名称
     *              - deptCode: String（必填），部门编码
     *              - leader: String（可选），部门负责人
     *              - phone: String（可选），联系电话
     *              - email: String（可选），邮箱地址
     *              - orderNum: Integer（可选），排序号
     *              - status: Integer（可选），部门状态（0=禁用，1=启用），默认 1
     * @return 操作结果
     *         - code: 响应状态码
     *         - data: null
     *         - message: 操作消息（包含部门名称的成功提示）
     * @throws RuntimeException 参数校验失败或创建失败时抛出
     * @see com.forgex.sys.domain.dto.department.SysDepartmentSaveParam
     * @see com.forgex.common.security.perm.RequirePerm
     * @see com.forgex.common.i18n.CommonPrompt#CREATE_SUCCESS
     */
    @PostMapping("/create")
    @RequirePerm("sys:dept:add")
    public R<Void> create(@Validated @RequestBody SysDepartmentSaveParam param) {
        param.setTenantId(TenantContext.get());
        Long id = departmentService.create(param);
        // 使用部门名称填充"新增成功"提示的占位符参数
        return R.okWithArgs(CommonPrompt.CREATE_SUCCESS, param.getDeptName());
    }

    /**
     * 更新部门
     * <p>
     * 接口路径：POST /sys/department/update
     * </p>
     * <p>
     * 认证要求：是
     * </p>
     * <p>
     * 权限要求：sys:dept:edit
     * </p>
     * <p>
     * 功能描述：更新部门信息，支持部分字段更新。
     * </p>
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>从租户上下文获取当前租户 ID</li>
     *   <li>设置保存参数的租户 ID</li>
     *   <li>参数校验（@Validated）</li>
     *   <li>调用部门服务更新部门</li>
     *   <li>返回更新成功提示（包含部门名称）</li>
     * </ol>
     * </p>
     * 
     * @param param 部门保存参数对象
     *              - id: Long（必填），部门 ID
     *              - parentId: Long（可选），父部门 ID
     *              - orgType: String（可选），组织类型
     *              - orgLevel: Integer（可选），组织层级
     *              - deptName: String（可选），部门名称
     *              - deptCode: String（可选），部门编码
     *              - leader: String（可选），部门负责人
     *              - phone: String（可选），联系电话
     *              - email: String（可选），邮箱地址
     *              - orderNum: Integer（可选），排序号
     *              - status: Integer（可选），部门状态（0=禁用，1=启用）
     * @return 操作结果
     *         - code: 响应状态码
     *         - data: null
     *         - message: 操作消息（包含部门名称的成功提示）
     * @throws RuntimeException 参数校验失败或更新失败时抛出
     * @see com.forgex.sys.domain.dto.department.SysDepartmentSaveParam
     * @see com.forgex.common.security.perm.RequirePerm
     * @see com.forgex.common.i18n.CommonPrompt#UPDATE_SUCCESS
     */
    @PostMapping("/update")
    @RequirePerm("sys:dept:edit")
    public R<Void> update(@Validated @RequestBody SysDepartmentSaveParam param) {
        param.setTenantId(TenantContext.get());
        Boolean success = departmentService.update(param);
        // 使用部门名称填充"修改成功"提示的占位符参数
        return R.okWithArgs(CommonPrompt.UPDATE_SUCCESS, param.getDeptName());
    }

    /**
     * 删除部门
     * <p>
     * 接口路径：POST /sys/department/delete
     * </p>
     * <p>
     * 认证要求：是
     * </p>
     * <p>
     * 权限要求：sys:dept:delete
     * </p>
     * <p>
     * 功能描述：根据部门 ID 删除部门信息（逻辑删除）。
     * </p>
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>从请求参数中获取部门 ID</li>
     *   <li>从租户上下文获取当前租户 ID</li>
     *   <li>查询部门信息（用于获取部门名称）</li>
     *   <li>部门不存在时返回错误提示</li>
     *   <li>调用部门服务删除部门</li>
     *   <li>返回删除成功提示（包含部门名称）</li>
     * </ol>
     * </p>
     * 
     * @param params 请求参数
     *               - id: Long（必填），部门 ID
     * @return 操作结果
     *         - code: 响应状态码
     *         - data: null
     *         - message: 操作消息（包含部门名称的成功提示）
     * @throws RuntimeException 参数解析失败或删除失败时抛出
     * @see com.forgex.sys.domain.dto.department.SysDepartmentDTO
     * @see com.forgex.common.security.perm.RequirePerm
     * @see com.forgex.common.i18n.CommonPrompt#DEPARTMENT_NOT_FOUND
     * @see com.forgex.common.i18n.CommonPrompt#DELETE_SUCCESS
     */
    @PostMapping("/delete")
    @RequirePerm("sys:dept:delete")
    public R<Void> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long tenantId = TenantContext.get();
        
        // 删除前获取部门名称
        SysDepartmentDTO department = departmentService.getById(id, tenantId);
        if (department == null) {
            return R.fail(CommonPrompt.DEPARTMENT_NOT_FOUND);
        }
        String deptName = department.getDeptName();
        
        Boolean success = departmentService.delete(id, tenantId);
        if (!Boolean.TRUE.equals(success)) {
            return R.fail(CommonPrompt.OPERATION_FAILED);
        }
        // 使用部门名称填充"删除成功"提示的占位符参数
        return R.okWithArgs(CommonPrompt.DELETE_SUCCESS, deptName);
    }
}
