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
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.position.SysPositionDTO;
import com.forgex.sys.domain.dto.position.SysPositionQueryDTO;
import com.forgex.sys.domain.dto.position.SysPositionSaveParam;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.service.SysPositionService;
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
 * 职位 Controller
 * 
 * 提供职位管理的 HTTP 接口，包括职位的增删改查功能
 * 所有接口都需要租户上下文，自动从 TenantContext 获取租户 ID
 * 
 * @author Forgex Team
 * @version 1.0.0
 * @see SysPositionService
 * @see SysPositionDTO
 */
@Slf4j
@RestController
@RequestMapping("/sys/position")
@RequiredArgsConstructor
public class SysPositionController {
    
    private final SysPositionService positionService;
    
    /**
     * 查询职位列表（不分页）
     * <p>
     * 接口路径：POST /sys/position/list
     * 需要权限：sys:position:query
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收查询参数（包含筛选条件）</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>调用 Service 层查询职位 DTO 列表</li>
     *   <li>返回职位列表</li>
     * </ol>
     *
     * @param queryDTO 查询参数
     *                 - tenantId: 租户 ID（自动填充，无需前端传递）
     *                 - positionName: 职位名称（可选，模糊查询）
     *                 - positionCode: 职位编码（可选，精确查询）
     *                 - status: 职位状态（可选，0=禁用，1=启用）
     * @return {@link R} 包含职位列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 职位列表（List&lt;SysPositionDTO&gt;）
     *         - message: 提示信息
     * @see SysPositionQueryDTO
     * @see SysPositionDTO
     */
    @RequirePerm("sys:position:view")
    @PostMapping("/list")
    public R<List<SysPositionDTO>> list(@RequestBody SysPositionQueryDTO queryDTO) {
        // 1. 从 TenantContext 获取当前租户 ID 并设置
        queryDTO.setTenantId(TenantContext.get());
        // 2. 委派给 Service 层查询职位列表
        List<SysPositionDTO> list = positionService.list(queryDTO);
        // 3. 返回职位列表
        return R.ok(list);
    }
    
    /**
     * 分页查询职位列表
     * <p>
     * 接口路径：POST /sys/position/page
     * 需要权限：sys:position:query
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收查询参数（包含 pageNum、pageSize 和筛选条件）</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>创建 MyBatis-Plus 分页对象</li>
     *   <li>调用 Service 层分页查询职位 DTO 列表</li>
     *   <li>返回分页结果</li>
     * </ol>
     *
     * @param queryDTO 查询参数
     *                 - pageNum: 页码（必填，从 1 开始）
     *                 - pageSize: 每页大小（必填）
     *                 - tenantId: 租户 ID（自动填充，无需前端传递）
     *                 - positionName: 职位名称（可选，模糊查询）
     *                 - positionCode: 职位编码（可选，精确查询）
     * @return {@link R} 包含职位分页列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 分页结果（IPage&lt;SysPositionDTO&gt;）
     *         - message: 提示信息
     * @see SysPositionQueryDTO
     * @see SysPositionDTO
     */
    @RequirePerm("sys:position:view")
    @PostMapping("/page")
    public R<IPage<SysPositionDTO>> page(@RequestBody SysPositionQueryDTO queryDTO) {
        // 1. 从 TenantContext 获取当前租户 ID 并设置
        queryDTO.setTenantId(TenantContext.get());
        // 2. 创建 MyBatis-Plus 分页对象
        Page<SysPosition> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 3. 委派给 Service 层分页查询职位 DTO 列表
        return R.ok(positionService.pagePositions(page, queryDTO));
    }
    
    /**
     * 获取职位详情
     * <p>
     * 接口路径：POST /sys/position/get
     * 需要权限：sys:position:query
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收参数（包含职位 ID）</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>解析 ID 参数并转换为 Long 类型</li>
     *   <li>调用 Service 层获取职位详情</li>
     *   <li>返回职位详情，不存在则返回错误提示</li>
     * </ol>
     *
     * @param params 参数（Map 格式）
     *               - id: 职位 ID（必填）
     * @return {@link R} 包含职位详情的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 职位详情（SysPositionDTO）
     *         - message: 提示信息（职位不存在时返回 POSITION_NOT_FOUND）
     * @throws NumberFormatException ID 格式不正确时抛出
     * @see SysPositionDTO
     */
    @RequirePerm("sys:position:view")
    @PostMapping("/get")
    public R<SysPositionDTO> get(@RequestBody Map<String, Object> params) {
        // 1. 从 TenantContext 获取当前租户 ID
        Long id = Long.valueOf(params.get("id").toString());
        Long tenantId = TenantContext.get();
        
        // 2. 调用 Service 层获取职位详情
        SysPositionDTO position = positionService.getById(id, tenantId);

        // 3. 职位不存在时返回错误提示
        if (position == null) {
            return R.fail(CommonPrompt.POSITION_NOT_FOUND);
        }
        
        // 4. 返回职位详情
        return R.ok(position);
    }
    
    /**
     * 新增职位
     * <p>
     * 接口路径：POST /sys/position/create
     * 需要权限：sys:position:add
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收职位保存参数（包含职位名称、编码等）</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>调用 Service 层创建职位</li>
     *   <li>返回新增成功的提示（包含职位名称）</li>
     * </ol>
     *
     * @param param 职位保存参数
     *              - tenantId: 租户 ID（自动填充，无需前端传递）
     *              - positionName: 职位名称（必填）
     *              - positionCode: 职位编码（必填）
     *              - orderNum: 排序号（可选）
     *              - status: 职位状态（可选，默认启用）
     *              - remark: 备注（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: 新增的职位 ID
     *         - message: 提示信息（"新增职位 {职位名称} 成功"）
     * @see SysPositionSaveParam
     */
    @PostMapping("/create")
    @RequirePerm("sys:position:add")
    public R<Void> create(@Validated @RequestBody SysPositionSaveParam param) {
        // 1. 从 TenantContext 获取当前租户 ID 并设置
        param.setTenantId(TenantContext.get());
        // 2. 委派给 Service 层创建职位
        Long id = positionService.create(param);
        // 3. 使用职位名称填充"新增成功"提示的占位符参数
        return R.okWithArgs(CommonPrompt.CREATE_SUCCESS, param.getPositionName());
    }

    /**
     * 更新职位
     * <p>
     * 接口路径：POST /sys/position/update
     * 需要权限：sys:position:edit
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收职位保存参数（包含职位 ID、新名称等）</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>调用 Service 层更新职位</li>
     *   <li>返回更新成功的提示（包含职位名称）</li>
     * </ol>
     *
     * @param param 职位保存参数
     *              - id: 职位 ID（必填）
     *              - tenantId: 租户 ID（自动填充，无需前端传递）
     *              - positionName: 职位名称（必填）
     *              - positionCode: 职位编码（必填）
     *              - orderNum: 排序号（可选）
     *              - status: 职位状态（可选）
     *              - remark: 备注（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（"修改职位 {职位名称} 成功"）
     * @see SysPositionSaveParam
     */
    @PostMapping("/update")
    @RequirePerm("sys:position:edit")
    public R<Void> update(@Validated @RequestBody SysPositionSaveParam param) {
        // 1. 从 TenantContext 获取当前租户 ID 并设置
        param.setTenantId(TenantContext.get());
        // 2. 委派给 Service 层更新职位
        Boolean success = positionService.update(param);
        // 3. 使用职位名称填充"修改成功"提示的占位符参数
        return R.okWithArgs(CommonPrompt.UPDATE_SUCCESS, param.getPositionName());
    }

    /**
     * 删除职位
     * <p>
     * 接口路径：POST /sys/position/delete
     * 需要权限：sys:position:delete
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收参数（包含职位 ID）</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>解析 ID 参数并转换为 Long 类型</li>
     *   <li>调用 Service 层获取职位详情（用于获取职位名称）</li>
     *   <li>职位不存在则返回错误提示</li>
     *   <li>调用 Service 层删除职位</li>
     *   <li>返回删除成功的提示（包含职位名称）</li>
     * </ol>
     *
     * @param params 参数（Map 格式）
     *               - id: 职位 ID（必填）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（"删除职位 {职位名称} 成功"）
     * @throws NumberFormatException ID 格式不正确时抛出
     * @see CommonPrompt#POSITION_NOT_FOUND
     */
    @PostMapping("/delete")
    @RequirePerm("sys:position:delete")
    public R<Void> delete(@RequestBody Map<String, Object> params) {
        // 1. 从 TenantContext 获取当前租户 ID
        Long id = Long.valueOf(params.get("id").toString());
        Long tenantId = TenantContext.get();
        
        // 2. 删除前获取职位名称（用于成功提示）
        SysPositionDTO position = positionService.getById(id, tenantId);
        if (position == null) {
            return R.fail(CommonPrompt.POSITION_NOT_FOUND);
        }
        String positionName = position.getPositionName();
        
        // 3. 委派给 Service 层删除职位
        Boolean success = positionService.delete(id, tenantId);
        // 4. 检查删除是否成功
        if (!success) {
            return R.fail(CommonPrompt.OPERATION_FAILED);
        }
        // 5. 使用职位名称填充"删除成功"提示的占位符参数
        return R.okWithArgs(CommonPrompt.DELETE_SUCCESS, positionName);
    }
}
