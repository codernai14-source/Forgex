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
package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysModuleDTO;
import com.forgex.sys.domain.dto.SysModuleQueryDTO;
import com.forgex.sys.domain.entity.SysModule;

import java.util.List;

/**
 * 模块 Service 接口
 * <p>
 * 提供模块管理相关的业务操作，包括模块增删改查、批量操作、唯一性校验、关联检查等功能。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #pageModules(Page, SysModuleQueryDTO)} - 分页查询模块列表</li>
 *   <li>{@link #listModules(SysModuleQueryDTO)} - 查询所有模块列表</li>
 *   <li>{@link #getModuleById(Long)} - 根据 ID 获取模块详情</li>
 *   <li>{@link #addModule(SysModuleDTO)} - 新增模块</li>
 *   <li>{@link #updateModule(SysModuleDTO)} - 更新模块</li>
 *   <li>{@link #deleteModule(Long)} - 删除模块</li>
 *   <li>{@link #batchDeleteModules(List)} - 批量删除模块</li>
 *   <li>{@link #existsByCode(String)} - 检查模块编码是否存在</li>
 *   <li>{@link #existsByCodeExcludeId(String, Long)} - 检查模块编码是否存在（排除指定 ID）</li>
 *   <li>{@link #existsById(Long)} - 检查模块是否存在</li>
 *   <li>{@link #hasMenus(Long)} - 检查模块下是否有菜单</li>
 *   <li>{@link #existsByName(String, Long)} - 检查模块名称是否存在</li>
 *   <li>{@link #existsByNameExcludeId(String, Long, Long)} - 检查模块名称是否存在（排除指定 ID）</li>
 *   <li>{@link #hasRoleAssociationThroughMenus(Long)} - 检查模块的菜单是否已被角色授权</li>
 * </ul>
 * <p>使用说明：</p>
 * <ul>
 *   <li>模块编码 code：模块的唯一标识，用于路由构建</li>
 *   <li>模块名称 name：模块的显示名称，支持国际化</li>
 *   <li>所有写操作（新增、更新、删除）均使用@Transactional 保证事务一致性</li>
 *   <li>状态字段 status：true=启用，false=禁用</li>
 *   <li>可见性字段 visible：true=可见，false=隐藏</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-07
 * @see com.forgex.sys.service.impl.SysModuleServiceImpl
 * @see com.forgex.sys.domain.dto.SysModuleDTO
 * @see com.forgex.sys.domain.entity.SysModule
 */
public interface ISysModuleService extends IService<SysModule> {
    
    /**
     * 分页查询模块列表
     * <p>
     * 根据查询条件分页查询模块列表，并将结果转换为 DTO 返回。
     * </p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>租户 ID 精确查询</li>
     *   <li>模块名称模糊查询</li>
     *   <li>模块编码模糊查询</li>
     *   <li>状态精确查询</li>
     * </ul>
     * 
     * @param page 分页参数，包含页码和页面大小
     * @param query 查询条件，包含租户 ID、模块名称、模块编码等过滤条件
     * @return 模块分页数据，包含模块 DTO 列表和总数
     * @see SysModuleQueryDTO
     * @see SysModuleDTO
     */
    IPage<SysModuleDTO> pageModules(Page<SysModule> page, SysModuleQueryDTO query);
    
    /**
     * 查询所有模块列表
     * <p>
     * 根据查询条件查询所有符合条件的模块列表，并将结果转换为 DTO 返回。
     * </p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>租户 ID 精确查询</li>
     *   <li>模块名称模糊查询</li>
     *   <li>模块编码模糊查询</li>
     *   <li>状态精确查询</li>
     * </ul>
     * 
     * @param query 查询条件，包含租户 ID、模块名称、模块编码等过滤条件
     * @return 模块列表，包含所有符合条件的模块 DTO
     * @see SysModuleQueryDTO
     * @see SysModuleDTO
     */
    List<SysModuleDTO> listModules(SysModuleQueryDTO query);
    
    /**
     * 根据 ID 获取模块详情
     * <p>
     * 根据模块 ID 查询模块详细信息，并将结果转换为 DTO 返回。
     * </p>
     * 
     * @param id 模块 ID，必填参数
     * @return 模块详情 DTO，包含模块完整信息；若模块不存在则返回 null
     * @see SysModuleDTO
     */
    SysModuleDTO getModuleById(Long id);
    
    /**
     * 新增模块
     * <p>
     * 将模块 DTO 转换为实体对象，并插入数据库。
     * </p>
     * 
     * @param moduleDTO 模块信息，包含模块名称、模块编码等必填字段
     * @throws IllegalArgumentException 当必填字段缺失时抛出
     * @see SysModuleDTO
     */
    void addModule(SysModuleDTO moduleDTO);
    
    /**
     * 更新模块
     * <p>
     * 将模块 DTO 转换为实体对象，并更新数据库。
     * </p>
     * 
     * @param moduleDTO 模块信息，包含模块 ID 和需要更新的字段
     * @throws IllegalArgumentException 当模块 ID 为空时抛出
     * @see SysModuleDTO
     */
    void updateModule(SysModuleDTO moduleDTO);
    
    /**
     * 删除模块
     * <p>
     * 根据模块 ID 删除模块记录。
     * </p>
     * <p>注意：删除前应先调用 {@link #hasMenus(Long)} 检查是否有菜单，调用 {@link #hasRoleAssociationThroughMenus(Long)} 检查是否已被角色授权。</p>
     * 
     * @param id 模块 ID，必填参数
     * @throws IllegalArgumentException 当模块 ID 为空时抛出
     */
    void deleteModule(Long id);
    
    /**
     * 批量删除模块
     * <p>
     * 批量删除多个模块记录。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>遍历模块 ID 列表</li>
     *   <li>逐个调用 {@link #deleteModule(Long)} 方法删除</li>
     * </ol>
     * 
     * @param ids 模块 ID 列表，必填参数
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    void batchDeleteModules(List<Long> ids);
    
    /**
     * 检查模块编码是否存在
     * <p>
     * 根据模块编码检查模块是否存在。
     * </p>
     * <p>用途：用于新增模块时验证模块编码唯一性。</p>
     * 
     * @param code 模块编码，必填参数
     * @return true-模块编码已存在，false-模块编码不存在
     */
    boolean existsByCode(String code);
    
    /**
     * 检查模块编码是否存在（排除指定 ID）
     * <p>
     * 根据模块编码检查模块是否存在，排除指定的模块 ID。
     * </p>
     * <p>用途：用于更新模块时验证模块编码唯一性。</p>
     * 
     * @param code 模块编码，必填参数
     * @param excludeId 排除的模块 ID，更新操作时传入当前模块 ID
     * @return true-模块编码已存在（非当前模块），false-模块编码不存在或是当前模块
     */
    boolean existsByCodeExcludeId(String code, Long excludeId);
    
    /**
     * 检查模块是否存在
     * <p>
     * 根据模块 ID 检查模块是否存在。
     * </p>
     * 
     * @param id 模块 ID，必填参数
     * @return true-模块存在，false-模块不存在
     */
    boolean existsById(Long id);
    
    /**
     * 检查模块下是否有菜单
     * <p>
     * 根据模块 ID 检查该模块下是否有菜单。
     * </p>
     * <p>用途：用于删除模块前判断是否可以删除。</p>
     * 
     * @param id 模块 ID，必填参数
     * @return true-有菜单，false-无菜单
     */
    boolean hasMenus(Long id);
    
    /**
     * 检查模块名称是否存在
     * <p>
     * 根据模块名称和租户 ID 检查模块是否已存在。
     * </p>
     * <p>用途：用于新增模块时验证模块名称唯一性。</p>
     * 
     * @param name 模块名称，必填参数
     * @param tenantId 租户 ID，必填参数
     * @return true-模块名称已存在，false-模块名称不存在
     */
    boolean existsByName(String name, Long tenantId);
    
    /**
     * 检查模块名称是否存在（排除指定 ID）
     * <p>
     * 根据模块名称和租户 ID 检查模块是否已存在，排除指定的模块 ID。
     * </p>
     * <p>用途：用于更新模块时验证模块名称唯一性。</p>
     * 
     * @param name 模块名称，必填参数
     * @param tenantId 租户 ID，必填参数
     * @param excludeId 排除的模块 ID，更新操作时传入当前模块 ID
     * @return true-模块名称已存在（非当前模块），false-模块名称不存在或是当前模块
     */
    boolean existsByNameExcludeId(String name, Long tenantId, Long excludeId);
    
    /**
     * 检查模块的菜单是否已被角色授权
     * <p>
     * 根据模块 ID 检查该模块下的菜单是否已被角色关联。
     * </p>
     * <p>用途：用于删除模块前判断是否可以删除。</p>
     * 
     * @param id 模块 ID，必填参数
     * @return true-已被角色授权，false-未被角色授权
     */
    boolean hasRoleAssociationThroughMenus(Long id);
}
