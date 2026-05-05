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
import com.forgex.sys.domain.dto.SysModuleDTO;
import com.forgex.sys.domain.dto.SysModuleQueryDTO;
import com.forgex.sys.domain.entity.SysModule;
import com.forgex.sys.service.ISysModuleService;
import com.forgex.sys.validator.ModuleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 模块管理 Controller
 * <p>
 * 提供模块的增删改查功能，包括模块的分页查询、详情获取、新增、修改、删除等操作。
 * 所有接口统一使用 POST 方法，参数统一封装为对象。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #page(SysModuleQueryDTO)} - 分页查询模块列表</li>
 *   <li>{@link #list(SysModuleQueryDTO)} - 查询所有模块列表（不分页）</li>
 *   <li>{@link #detail(Map)} - 根据 ID 获取模块详情</li>
 *   <li>{@link #create(SysModuleDTO)} - 新增模块</li>
 *   <li>{@link #update(SysModuleDTO)} - 更新模块</li>
 *   <li>{@link #delete(Map)} - 删除模块</li>
 *   <li>{@link #batchDelete(Map)} - 批量删除模块</li>
 * </ul>
 * <p>接口说明：</p>
 * <ul>
 *   <li>所有接口路径：/sys/module/*</li>
 *   <li>所有接口均为 POST 请求</li>
 *   <li>需要登录认证</li>
 *   <li>新增、修改、删除操作需要对应权限</li>
 *   <li>返回格式统一为 {@link R} 类型</li>
 * </ul>
 *
 * 接口规范：
 * - 所有接口统一使用 POST 方法
 * - 参数统一封装为对象
 * - 分页查询使用 BaseGetParam（pageNum/pageSize）
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-07
 * @see ISysModuleService
 * @see ModuleValidator
 * @see SysModuleDTO
 */
@RestController
@RequestMapping("/sys/module")
@RequiredArgsConstructor
public class SysModuleController {

    private final ISysModuleService moduleService;
    private final ModuleValidator moduleValidator;

    /**
     * 分页查询模块列表
     * <p>
     * 接口路径：POST /sys/module/page
     * </p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收分页参数和查询条件（pageNum/pageSize/keyword 等）</li>
     *   <li>创建 MyBatis-Plus 分页对象</li>
     *   <li>调用 Service 层分页查询模块</li>
     *   <li>返回分页结果，包含模块列表和总数</li>
     * </ol>
     *
     * @param query 查询参数对象，包含分页信息和查询条件
     *              - pageNum: 页码（必填，从 1 开始）
     *              - pageSize: 每页大小（必填）
     *              - moduleName: 模块名称（可选，模糊查询）
     *              - moduleCode: 模块编码（可选，精确查询）
     *              - status: 状态（可选）
     * @return {@link R} 包含模块分页结果的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 分页结果（IPage&lt;SysModuleDTO&gt;）
     *         - message: 提示信息
     * @see SysModuleQueryDTO
     * @see SysModuleDTO
     */
    @RequirePerm("sys:module:view")
    @PostMapping("/page")
    public R<IPage<SysModuleDTO>> page(@RequestBody SysModuleQueryDTO query) {
        // 1. 使用 BaseGetParam 中的 pageNum 和 pageSize 创建分页对象
        Page<SysModule> page = new Page<>(query.getPageNum(), query.getPageSize());
        // 2. 调用 Service 层分页查询模块
        // 3. 返回分页结果
        return R.ok(moduleService.pageModules(page, query));
    }

    /**
     * 查询所有模块列表（不分页）
     * <p>
     * 接口路径：POST /sys/module/list
     * </p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收查询条件（keyword/status 等）</li>
     *   <li>调用 Service 层查询所有符合条件的模块</li>
     *   <li>返回模块列表，不分页</li>
     * </ol>
     *
     * @param query 查询参数对象，包含查询条件
     *              - moduleName: 模块名称（可选，模糊查询）
     *              - moduleCode: 模块编码（可选，精确查询）
     *              - status: 状态（可选）
     * @return {@link R} 包含模块列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 模块列表（List&lt;SysModuleDTO&gt;）
     *         - message: 提示信息
     * @see SysModuleQueryDTO
     * @see SysModuleDTO
     */
    @RequirePerm("sys:module:view")
    @PostMapping("/list")
    public R<List<SysModuleDTO>> list(@RequestBody SysModuleQueryDTO query) {
        // 1. 调用 Service 层查询所有符合条件的模块
        // 2. 返回模块列表
        return R.ok(moduleService.listModules(query));
    }

    /**
     * 根据 ID 获取模块详情
     * <p>
     * 接口路径：POST /sys/module/detail
     * </p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从请求体中解析模块 ID</li>
     *   <li>调用 Validator 校验 ID 是否合法</li>
     *   <li>调用 Service 层根据 ID 查询模块详情</li>
     *   <li>返回模块详细信息</li>
     * </ol>
     *
     * @param body 请求体，包含模块 ID
     *             - id: 模块 ID（必填，正整数）
     * @return {@link R} 包含模块详情的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 模块详情对象（SysModuleDTO）
     *         - message: 提示信息
     * @throws I18nBusinessException ID 不合法或模块不存在时抛出
     * @see SysModuleDTO
     */
    @RequirePerm("sys:module:view")
    @PostMapping("/detail")
    public R<SysModuleDTO> detail(@RequestBody Map<String, Object> body) {
        // 1. 从请求体中解析模块 ID
        Long id = parseLong(body.get("id"));
        // 2. 调用 Validator 校验 ID 是否合法
        moduleValidator.validateId(id);
        // 3. 调用 Service 层查询模块详情
        // 4. 返回模块详细信息
        return R.ok(moduleService.getModuleById(id));
    }

    /**
     * 新增模块
     * <p>
     * 接口路径：POST /sys/module/create
     * </p>
     * <p>认证要求：是</p>
     * <p>权限要求：sys:module:add</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收模块数据并进行数据校验（@Validated）</li>
     *   <li>调用 Validator 验证新增模块的数据合法性</li>
     *   <li>调用 Service 层保存模块</li>
     *   <li>返回成功响应</li>
     * </ol>
     *
     * @param moduleDTO 模块数据传输对象
     *                  - moduleName: 模块名称（必填）
     *                  - moduleCode: 模块编码（必填，唯一）
     *                  - description: 模块描述（可选）
     *                  - orderNum: 排序号（可选）
     *                  - status: 状态（可选，默认启用）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: null
     *         - message: "创建成功"
     * @throws I18nBusinessException 数据校验失败或模块编码已存在时抛出
     * @see SysModuleDTO
     */
    @RequirePerm("sys:module:add")
    @PostMapping("/create")
    public R<Void> create(@RequestBody @Validated SysModuleDTO moduleDTO) {
        // 1. 数据校验：检查必填字段、唯一性等
        moduleValidator.validateForAdd(moduleDTO);
        // 2. 调用 Service 层保存模块
        moduleService.addModule(moduleDTO);
        // 3. 返回成功响应
        return R.ok(CommonPrompt.CREATE_SUCCESS);
    }

    /**
     * 更新模块
     * <p>
     * 接口路径：POST /sys/module/update
     * </p>
     * <p>认证要求：是</p>
     * <p>权限要求：sys:module:edit</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收模块数据并进行数据校验（@Validated）</li>
     *   <li>调用 Validator 验证更新模块的数据合法性</li>
     *   <li>调用 Service 层更新模块</li>
     *   <li>返回成功响应</li>
     * </ol>
     *
     * @param moduleDTO 模块数据传输对象
     *                  - id: 模块 ID（必填）
     *                  - moduleName: 模块名称（可选）
     *                  - moduleCode: 模块编码（可选，唯一）
     *                  - description: 模块描述（可选）
     *                  - orderNum: 排序号（可选）
     *                  - status: 状态（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: null
     *         - message: "更新成功"
     * @throws I18nBusinessException 数据校验失败或模块不存在时抛出
     * @see SysModuleDTO
     */
    @RequirePerm("sys:module:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated SysModuleDTO moduleDTO) {
        // 1. 数据校验：检查必填字段、唯一性等
        moduleValidator.validateForUpdate(moduleDTO);
        // 2. 调用 Service 层更新模块
        moduleService.updateModule(moduleDTO);
        // 3. 返回成功响应
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    /**
     * 删除模块
     * <p>
     * 接口路径：POST /sys/module/delete
     * </p>
     * <p>认证要求：是</p>
     * <p>权限要求：sys:module:delete</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从请求体中解析模块 ID</li>
     *   <li>调用 Validator 验证删除操作是否合法（检查是否被使用）</li>
     *   <li>调用 Service 层删除模块</li>
     *   <li>返回成功响应</li>
     * </ol>
     *
     * @param body 请求体，包含模块 ID
     *             - id: 模块 ID（必填，正整数）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: null
     *         - message: "删除成功"
     * @throws I18nBusinessException ID 不合法或模块不存在或已被使用时抛出
     */
    @RequirePerm("sys:module:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, Object> body) {
        // 1. 从请求体中解析模块 ID
        Long id = parseLong(body.get("id"));
        // 2. 调用 Validator 验证删除操作是否合法
        moduleValidator.validateForDelete(id);
        // 3. 调用 Service 层删除模块
        moduleService.deleteModule(id);
        // 4. 返回成功响应
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    /**
     * 批量删除数据。
     * <p>
     * 接口路径：POST /sys/module/batchDelete
     * </p>
     * <p>认证要求：是</p>
     * <p>权限要求：sys:module:delete</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从请求体中解析模块 ID 列表</li>
     *   <li>遍历并校验每个 ID 的合法性</li>
     *   <li>调用 Service 层批量删除模块</li>
     *   <li>返回成功响应</li>
     * </ol>
     *
     * @param body 请求体，包含模块 ID 列表
     *             - ids: 模块 ID 列表（必填，至少包含一个 ID）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: null
     *         - message: "删除成功"
     * @throws I18nBusinessException ID 列表为空或包含非法 ID 时抛出
     */
    @RequirePerm("sys:module:delete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody Map<String, Object> body) {
        // 1. 从请求体中解析模块 ID 列表
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        // 2. 遍历并校验每个 ID 的合法性
        ids.forEach(moduleValidator::validateForDelete);
        // 3. 调用 Service 层批量删除模块
        moduleService.batchDeleteModules(ids);
        // 4. 返回成功响应
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    /**
     * 解析 Long 类型参数
     * <p>
     * 将 Object 类型的参数转换为 Long 类型，支持 Number 和 String 类型。
     * </p>
     *
     * @param obj 待转换的对象
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
                // 4. 解析失败返回 null
                return null;
            }
        }
        // 5. 其他类型返回 null
        return null;
    }
}
