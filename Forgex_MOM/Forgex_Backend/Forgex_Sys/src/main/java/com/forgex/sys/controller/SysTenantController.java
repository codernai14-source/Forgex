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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.tenant.SysTenantDTO;
import com.forgex.sys.domain.dto.tenant.SysTenantQueryDTO;
import com.forgex.sys.domain.dto.tenant.SysTenantSaveParam;
import com.forgex.sys.domain.dto.TenantHierarchyDTO;
import com.forgex.sys.service.SysTenantService;
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
 * 租户 Controller
 * <p>
 * 提供租户管理的 HTTP 接口，包含租户的增删改查功能。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #list(SysTenantQueryDTO)} - 查询租户列表</li>
 *   <li>{@link #page(SysTenantQueryDTO)} - 分页查询租户列表</li>
 *   <li>{@link #get(Map)} - 获取租户详情</li>
 *   <li>{@link #getMainTenant()} - 获取主租户</li>
 *   <li>{@link #create(SysTenantSaveParam)} - 新增租户</li>
 *   <li>{@link #update(SysTenantSaveParam)} - 更新租户</li>
 *   <li>{@link #delete(Map)} - 删除租户</li>
 * </ul>
 * <p>接口说明：</p>
 * <ul>
 *   <li>所有接口路径：/sys/tenant/*</li>
 *   <li>所有接口均为 POST 请求</li>
 *   <li>需要登录认证（@PreAuthorize）</li>
 *   <li>返回格式统一为 {@link R} 类型</li>
 * </ul>
 * 
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.sys.service.SysTenantService
 * @see com.forgex.common.web.R
 */
@Slf4j
@RestController
@RequestMapping("/sys/tenant")
@RequiredArgsConstructor
public class SysTenantController {
    
    private final SysTenantService tenantService;
    
    /**
     * 查询租户列表
     * <p>
     * 根据查询条件查询所有符合条件的租户列表。
     * </p>
     * <p>接口路径：POST /sys/tenant/list</p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收查询参数（SysTenantQueryDTO）</li>
     *   <li>调用 Service 的 list 方法查询租户列表</li>
     *   <li>返回租户 DTO 列表</li>
     * </ol>
     * 
     * @param queryDTO 查询参数，包含租户名称、租户编码等过滤条件
     * @return {@link R} 包含租户 DTO 列表的统一返回结构
     * @see SysTenantQueryDTO
     * @see SysTenantDTO
     */
    @PostMapping("/list")
    public R<List<SysTenantDTO>> list(@RequestBody SysTenantQueryDTO queryDTO) {
        List<SysTenantDTO> list = tenantService.list(queryDTO);
        return R.ok(list);
    }
    
    /**
     * 分页查询租户列表
     * <p>
     * 根据查询条件和分页参数查询租户列表。
     * </p>
     * <p>接口路径：POST /sys/tenant/page</p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收查询参数（包含分页参数）</li>
     *   <li>调用 Service 的 page 方法分页查询租户</li>
     *   <li>返回分页结果（包含租户列表和总数）</li>
     * </ol>
     * 
     * @param queryDTO 查询参数，包含租户名称、租户编码等过滤条件和分页参数
     * @return {@link R} 包含租户分页数据的统一返回结构
     * @see SysTenantQueryDTO
     * @see SysTenantDTO
     */
    @PostMapping("/page")
    public R<Page<SysTenantDTO>> page(@RequestBody SysTenantQueryDTO queryDTO) {
        Page<SysTenantDTO> page = tenantService.page(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 获取租户详情
     * <p>
     * 根据租户 ID 查询租户详细信息。
     * </p>
     * <p>接口路径：POST /sys/tenant/get</p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从请求参数中提取租户 ID</li>
     *   <li>调用 Service 的 getById 方法查询租户详情</li>
     *   <li>判断租户是否存在，不存在返回 404</li>
     *   <li>返回租户详情 DTO</li>
     * </ol>
     *
     * @param params 请求参数，包含 id 字段（租户 ID）
     * @return {@link R} 包含租户详情 DTO 的统一返回结构；若不存在返回 404
     * @see SysTenantDTO
     */
    @PostMapping("/get")
    public R<SysTenantDTO> get(@RequestBody Map<String, Object> params) {
        // 从参数中提取租户 ID
        Long id = Long.valueOf(params.get("id").toString());

        // 查询租户详情
        SysTenantDTO tenant = tenantService.getById(id);

        // 租户不存在，返回 404
        if (tenant == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }

        return R.ok(tenant);
    }
    
    /**
     * 获取主租户
     * <p>
     * 查询系统的主租户信息。
     * </p>
     * <p>接口路径：POST /sys/tenant/getMainTenant</p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>调用 Service 的 getMainTenant 方法查询主租户</li>
     *   <li>判断主租户是否存在，不存在返回 404</li>
     *   <li>返回主租户详情 DTO</li>
     * </ol>
     *
     * @return {@link R} 包含主租户详情 DTO 的统一返回结构；若不存在返回 404
     * @see SysTenantDTO
     */
    @PostMapping("/getMainTenant")
    public R<SysTenantDTO> getMainTenant() {
        // 查询主租户
        SysTenantDTO tenant = tenantService.getMainTenant();

        // 主租户不存在，返回 404
        if (tenant == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }

        return R.ok(tenant);
    }
    
    /**
     * 获取租户层级关系
     * <p>
     * 获取指定租户的层级关系，包含当前租户、父租户和子租户列表。
     * </p>
     * <p>接口路径：POST /sys/tenant/hierarchy</p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收租户 ID 参数</li>
     *   <li>调用 Service 的 getTenantHierarchy 方法查询层级关系</li>
     *   <li>返回租户层级关系 DTO</li>
     * </ol>
     *
     * @param param 参数，包含 tenantId
     * @return {@link R} 包含租户层级关系 DTO 的统一返回结构
     * @see TenantHierarchyDTO
     */
    @PostMapping("/hierarchy")
    public R<TenantHierarchyDTO> getHierarchy(@RequestBody Map<String, Object> param) {
        Long tenantId = param == null ? null : (Long) param.get("tenantId");
        
        if (tenantId == null) {
            return R.fail(CommonPrompt.BAD_REQUEST, "租户 ID 不能为空");
        }
        
        TenantHierarchyDTO dto = tenantService.getTenantHierarchy(tenantId);
        return R.ok(dto);
    }
    
    /**
     * 获取子租户列表
     * <p>
     * 获取指定父租户下的所有子租户列表。
     * </p>
     * <p>接口路径：POST /sys/tenant/children</p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收父租户 ID 参数</li>
     *   <li>调用 Service 的 getChildTenants 方法查询子租户列表</li>
     *   <li>返回子租户 DTO 列表</li>
     * </ol>
     *
     * @param param 参数，包含 parentTenantId
     * @return {@link R} 包含子租户 DTO 列表的统一返回结构
     * @see SysTenantDTO
     */
    @PostMapping("/children")
    public R<List<SysTenantDTO>> getChildren(@RequestBody Map<String, Object> param) {
        Long parentTenantId = param == null ? null : (Long) param.get("parentTenantId");
        
        if (parentTenantId == null) {
            return R.fail(CommonPrompt.BAD_REQUEST, "父租户 ID 不能为空");
        }
        
        List<SysTenantDTO> children = tenantService.getChildTenants(parentTenantId);
        return R.ok(children);
    }
    
    /**
     * 新增租户
     * <p>
     * 创建新的租户记录。
     * </p>
     * <p>接口路径：POST /sys/tenant/create</p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收并校验租户参数（@Validated）</li>
     *   <li>调用 Service 的 create 方法创建租户</li>
     *   <li>返回新创建的租户 ID</li>
     * </ol>
     *
     * @param param 租户参数，包含租户名称、租户编码等必填字段
     * @return {@link R} 包含新创建租户 ID 的统一返回结构
     * @throws IllegalArgumentException 当必填字段缺失时抛出
     * @see SysTenantSaveParam
     */
    @PostMapping("/create")
    public R<Long> create(@Validated @RequestBody SysTenantSaveParam param) {
        // 创建租户并返回 ID
        Long id = tenantService.create(param);
        return R.ok(CommonPrompt.CREATE_SUCCESS, id);
    }

    /**
     * 更新租户
     * <p>
     * 更新已有租户的信息。
     * </p>
     * <p>接口路径：POST /sys/tenant/update</p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收并校验租户参数（@Validated）</li>
     *   <li>调用 Service 的 update 方法更新租户</li>
     *   <li>返回更新结果</li>
     * </ol>
     *
     * @param param 租户参数，包含租户 ID 和需要更新的字段
     * @return {@link R} 包含更新是否成功的统一返回结构
     * @throws IllegalArgumentException 当租户 ID 为空时抛出
     * @see SysTenantSaveParam
     */
    @PostMapping("/update")
    public R<Boolean> update(@Validated @RequestBody SysTenantSaveParam param) {
        // 更新租户
        Boolean success = tenantService.update(param);
        return R.ok(CommonPrompt.UPDATE_SUCCESS, success);
    }

    /**
     * 删除租户
     * <p>
     * 根据租户 ID 删除租户记录。
     * </p>
     * <p>接口路径：POST /sys/tenant/delete</p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从请求参数中提取租户 ID</li>
     *   <li>调用 Service 的 delete 方法删除租户</li>
     *   <li>返回删除结果</li>
     * </ol>
     *
     * @param params 请求参数，包含 id 字段（租户 ID）
     * @return {@link R} 包含删除是否成功的统一返回结构
     * @see CommonPrompt#DELETE_SUCCESS
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        // 从参数中提取租户 ID
        Long id = Long.valueOf(params.get("id").toString());

        // 删除租户
        Boolean success = tenantService.delete(id);
        return R.ok(CommonPrompt.DELETE_SUCCESS, success);
    }
}
