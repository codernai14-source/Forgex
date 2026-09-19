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
import com.forgex.sys.domain.dto.SysRoleDTO;
import com.forgex.sys.domain.dto.SysRoleQueryDTO;
import com.forgex.sys.domain.entity.SysRole;

import java.util.List;

/**
 * 角色 Service 接口
 * <p>
 * 提供角色管理相关的业务操作，包括角色增删改查、批量操作、唯一性校验、关联检查等功能。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #pageRoles(Page, SysRoleQueryDTO)} - 分页查询角色列表</li>
 *   <li>{@link #listRoles(SysRoleQueryDTO)} - 查询角色列表</li>
 *   <li>{@link #getRoleById(Long)} - 根据 ID 获取角色详情</li>
 *   <li>{@link #addRole(SysRoleDTO)} - 新增角色</li>
 *   <li>{@link #updateRole(SysRoleDTO)} - 更新角色</li>
 *   <li>{@link #deleteRole(Long)} - 删除角色</li>
 *   <li>{@link #batchDeleteRoles(List)} - 批量删除角色</li>
 *   <li>{@link #existsById(Long)} - 检查角色是否存在</li>
 *   <li>{@link #existsByRoleKey(String)} - 检查角色键是否存在</li>
 *   <li>{@link #existsByRoleKeyExcludeId(String, Long)} - 检查角色键是否存在（排除指定 ID）</li>
 *   <li>{@link #existsByRoleCode(String, Long)} - 检查角色编码是否存在</li>
 *   <li>{@link #existsByRoleCodeExcludeId(String, Long, Long)} - 检查角色编码是否存在（排除指定 ID）</li>
 *   <li>{@link #existsByRoleName(String, Long)} - 检查角色名称是否存在</li>
 *   <li>{@link #existsByRoleNameExcludeId(String, Long, Long)} - 检查角色名称是否存在（排除指定 ID）</li>
 *   <li>{@link #hasUserAssociation(Long)} - 检查角色是否有关联用户</li>
 *   <li>{@link #getRoleCodeById(Long)} - 根据 ID 获取角色编码</li>
 * </ul>
 * <p>使用说明：</p>
 * <ul>
 *   <li>角色键 roleKey：用于 SaToken 权限认证的角色标识</li>
 *   <li>角色编码 roleCode：角色的业务编码，同一租户下唯一</li>
 *   <li>角色名称 roleName：角色的显示名称，支持国际化</li>
 *   <li>所有写操作（新增、更新、删除）均使用@Transactional 保证事务一致性</li>
 *   <li>状态字段 status：true=启用，false=禁用</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-07
 * @see com.forgex.sys.service.impl.SysRoleServiceImpl
 * @see com.forgex.sys.domain.dto.SysRoleDTO
 * @see com.forgex.sys.domain.entity.SysRole
 */
public interface ISysRoleService extends IService<SysRole> {
    
    /**
     * 分页查询角色列表
     * <p>
     * 根据查询条件分页查询角色列表，并将结果转换为 DTO 返回。
     * </p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>租户 ID 精确查询</li>
     *   <li>角色名称模糊查询</li>
     *   <li>角色编码模糊查询</li>
     *   <li>状态精确查询</li>
     * </ul>
     * 
     * @param page 分页参数，包含页码和页面大小
     * @param query 查询条件，包含租户 ID、角色名称、角色编码等过滤条件
     * @return 角色分页数据，包含角色 DTO 列表和总数
     * @see SysRoleQueryDTO
     * @see SysRoleDTO
     */
    IPage<SysRoleDTO> pageRoles(Page<SysRole> page, SysRoleQueryDTO query);
    
    /**
     * 查询角色列表
     * <p>
     * 根据查询条件查询所有符合条件的角色列表，并将结果转换为 DTO 返回。
     * </p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>租户 ID 精确查询</li>
     *   <li>角色名称模糊查询</li>
     *   <li>角色编码模糊查询</li>
     *   <li>状态精确查询</li>
     * </ul>
     * 
     * @param query 查询条件，包含租户 ID、角色名称、角色编码等过滤条件
     * @return 角色列表，包含所有符合条件的角色 DTO
     * @see SysRoleQueryDTO
     * @see SysRoleDTO
     */
    List<SysRoleDTO> listRoles(SysRoleQueryDTO query);
    
    /**
     * 根据 ID 获取角色详情
     * <p>
     * 根据角色 ID 查询角色详细信息，并将结果转换为 DTO 返回。
     * </p>
     * 
     * @param id 角色 ID，必填参数
     * @return 角色详情 DTO，包含角色完整信息；若角色不存在则返回 null
     * @see SysRoleDTO
     */
    SysRoleDTO getRoleById(Long id);
    
    /**
     * 新增角色
     * <p>
     * 将角色 DTO 转换为实体对象，并插入数据库。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>将 DTO 转换为实体对象</li>
     *   <li>插入角色数据</li>
     * </ol>
     * 
     * @param roleDTO 角色信息，包含角色名称、角色编码、角色键等必填字段
     * @throws IllegalArgumentException 当必填字段缺失时抛出
     * @see SysRoleDTO
     */
    void addRole(SysRoleDTO roleDTO);
    
    /**
     * 更新角色
     * <p>
     * 将角色 DTO 转换为实体对象，并更新数据库。
     * </p>
     * 
     * @param roleDTO 角色信息，包含角色 ID 和需要更新的字段
     * @throws IllegalArgumentException 当角色 ID 为空时抛出
     * @see SysRoleDTO
     */
    void updateRole(SysRoleDTO roleDTO);
    
    /**
     * 删除角色
     * <p>
     * 根据角色 ID 删除角色记录。
     * </p>
     * <p>注意：删除前应先调用 {@link #hasUserAssociation(Long)} 检查是否有关联用户。</p>
     * 
     * @param id 角色 ID，必填参数
     * @throws IllegalArgumentException 当角色 ID 为空时抛出
     */
    void deleteRole(Long id);
    
    /**
     * 批量删除角色
     * <p>
     * 批量删除多个角色记录。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>遍历角色 ID 列表</li>
     *   <li>逐个调用 {@link #deleteRole(Long)} 方法删除</li>
     * </ol>
     * 
     * @param ids 角色 ID 列表，必填参数
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    void batchDeleteRoles(List<Long> ids);
    
    /**
     * 检查角色是否存在
     * <p>
     * 根据角色 ID 检查角色是否存在。
     * </p>
     * 
     * @param id 角色 ID，必填参数
     * @return true-角色存在，false-角色不存在
     */
    boolean existsById(Long id);
    
    /**
     * 检查角色键是否存在
     * <p>
     * 根据角色键检查角色是否存在。
     * </p>
     * <p>用途：用于新增角色时验证角色键唯一性。</p>
     * 
     * @param roleKey 角色键，必填参数
     * @return true-角色键已存在，false-角色键不存在
     */
    boolean existsByRoleKey(String roleKey);
    
    /**
     * 检查角色键是否存在（排除指定 ID）
     * <p>
     * 根据角色键检查角色是否存在，排除指定的角色 ID。
     * </p>
     * <p>用途：用于更新角色时验证角色键唯一性。</p>
     * 
     * @param roleKey 角色键，必填参数
     * @param excludeId 排除的角色 ID，更新操作时传入当前角色 ID
     * @return true-角色键已存在（非当前角色），false-角色键不存在或是当前角色
     */
    boolean existsByRoleKeyExcludeId(String roleKey, Long excludeId);
    
    /**
     * 检查角色编码是否存在
     * <p>
     * 根据角色编码和租户 ID 检查角色是否已存在。
     * </p>
     * <p>用途：用于新增角色时验证角色编码唯一性。</p>
     * 
     * @param roleCode 角色编码，必填参数
     * @param tenantId 租户 ID，必填参数
     * @return true-角色编码已存在，false-角色编码不存在
     */
    boolean existsByRoleCode(String roleCode, Long tenantId);
    
    /**
     * 检查角色编码是否存在（排除指定 ID）
     * <p>
     * 根据角色编码和租户 ID 检查角色是否已存在，排除指定的角色 ID。
     * </p>
     * <p>用途：用于更新角色时验证角色编码唯一性。</p>
     * 
     * @param roleCode 角色编码，必填参数
     * @param tenantId 租户 ID，必填参数
     * @param excludeId 排除的角色 ID，更新操作时传入当前角色 ID
     * @return true-角色编码已存在（非当前角色），false-角色编码不存在或是当前角色
     */
    boolean existsByRoleCodeExcludeId(String roleCode, Long tenantId, Long excludeId);
    
    /**
     * 检查角色名称是否存在
     * <p>
     * 根据角色名称和租户 ID 检查角色是否已存在。
     * </p>
     * <p>用途：用于新增角色时验证角色名称唯一性。</p>
     * 
     * @param roleName 角色名称，必填参数
     * @param tenantId 租户 ID，必填参数
     * @return true-角色名称已存在，false-角色名称不存在
     */
    boolean existsByRoleName(String roleName, Long tenantId);
    
    /**
     * 检查角色名称是否存在（排除指定 ID）
     * <p>
     * 根据角色名称和租户 ID 检查角色是否已存在，排除指定的角色 ID。
     * </p>
     * <p>用途：用于更新角色时验证角色名称唯一性。</p>
     * 
     * @param roleName 角色名称，必填参数
     * @param tenantId 租户 ID，必填参数
     * @param excludeId 排除的角色 ID，更新操作时传入当前角色 ID
     * @return true-角色名称已存在（非当前角色），false-角色名称不存在或是当前角色
     */
    boolean existsByRoleNameExcludeId(String roleName, Long tenantId, Long excludeId);
    
    /**
     * 检查角色是否有关联用户
     * <p>
     * 根据角色 ID 检查该角色是否有关联的用户。
     * </p>
     * <p>用途：用于删除角色前判断是否可以删除。</p>
     * 
     * @param id 角色 ID，必填参数
     * @return true-有关联用户，false-无关联用户
     */
    boolean hasUserAssociation(Long id);
    
    /**
     * 根据 ID 获取角色编码
     * <p>
     * 根据角色 ID 查询角色的编码。
     * </p>
     * 
     * @param id 角色 ID，必填参数
     * @return 角色编码；若角色不存在则返回 null
     */
    String getRoleCodeById(Long id);
}
