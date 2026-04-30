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
import com.forgex.sys.domain.dto.SysMenuDTO;
import com.forgex.sys.domain.dto.SysMenuQueryDTO;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.vo.MenuTreeVO;
import com.forgex.sys.domain.vo.UserMenuOpenReportVO;
import com.forgex.sys.domain.vo.UserMenuPreferenceVO;
import com.forgex.sys.domain.vo.UserRoutesVO;

import java.util.List;

/**
 * 菜单 Service 接口
 * <p>
 * 提供菜单管理相关的业务操作，包括菜单增删改查、菜单树构建、路由生成、权限校验等功能。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #getUserRoutes(String, Long)} - 获取用户路由（包含模块、菜单、按钮权限）</li>
 *   <li>{@link #getMenuTree(Long, Long)} - 获取菜单树</li>
 *   <li>{@link #pageMenus(Page, SysMenuQueryDTO)} - 分页查询菜单列表</li>
 *   <li>{@link #listMenus(SysMenuQueryDTO)} - 查询菜单列表</li>
 *   <li>{@link #getMenuById(Long)} - 根据 ID 获取菜单详情</li>
 *   <li>{@link #addMenu(SysMenuDTO)} - 新增菜单</li>
 *   <li>{@link #updateMenu(SysMenuDTO)} - 更新菜单</li>
 *   <li>{@link #deleteMenu(Long)} - 删除菜单（级联删除子菜单和按钮）</li>
 *   <li>{@link #batchDeleteMenus(List)} - 批量删除菜单</li>
 *   <li>{@link #existsById(Long)} - 检查菜单是否存在</li>
 *   <li>{@link #hasChildren(Long)} - 检查菜单是否有子菜单</li>
 *   <li>{@link #hasRoleAssociation(Long)} - 检查菜单是否已被角色授权</li>
 *   <li>{@link #existsByPermKey(String, Long)} - 检查权限标识是否已存在</li>
 *   <li>{@link #existsByPermKeyExcludeId(String, Long, Long)} - 检查权限标识是否已存在（排除指定 ID）</li>
 * </ul>
 * <p>使用说明：</p>
 * <ul>
 *   <li>菜单类型包括：catalog（目录）、menu（菜单）、button（按钮）</li>
 *   <li>菜单支持多级嵌套，通过 parentId 字段构建父子关系</li>
 *   <li>权限标识 permKey 用于前端按钮权限控制</li>
 *   <li>所有写操作（新增、更新、删除）均使用@Transactional 保证事务一致性</li>
 *   <li>状态字段 status：true=启用，false=禁用</li>
 *   <li>可见性字段 visible：true=可见，false=隐藏</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-12
 * @see com.forgex.sys.service.impl.SysMenuServiceImpl
 * @see com.forgex.sys.domain.dto.SysMenuDTO
 * @see com.forgex.sys.domain.vo.MenuTreeVO
 * @see com.forgex.sys.domain.vo.UserRoutesVO
 * @see com.forgex.sys.domain.entity.SysMenu
 */
public interface ISysMenuService extends IService<SysMenu> {
    
    /**
     * 获取用户路由（包含模块、菜单、按钮权限）
     * <p>
     * 根据用户账号和租户 ID 查询该用户有权访问的模块、菜单和按钮权限。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>根据用户账号查询用户信息</li>
     *   <li>查询用户关联的所有角色</li>
     *   <li>根据角色查询关联的菜单 ID 列表</li>
     *   <li>查询菜单详情（包括目录、菜单、按钮）</li>
     *   <li>查询菜单所属的模块信息</li>
     *   <li>构建路由结构并返回</li>
     * </ol>
     * 
     * @param account 用户账号，必填参数
     * @param tenantId 租户 ID，必填参数
     * @return 用户路由信息，包含模块列表、路由树、按钮权限列表
     * @see UserRoutesVO
     */
    UserRoutesVO getUserRoutes(String account, Long tenantId);
    
    /**
     * 获取菜单树
     * <p>
     * 根据租户 ID 和模块 ID 查询菜单树形结构。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>根据租户 ID 和模块 ID 查询所有菜单</li>
     *   <li>按照 parentId 构建父子关系</li>
     *   <li>递归构建完整的树形结构</li>
     *   <li>返回根节点列表</li>
     * </ol>
     * 
     * @param tenantId 租户 ID，必填参数
     * @param moduleId 模块 ID，可选参数；为 null 时查询所有模块的菜单
     * @return 菜单树列表，包含完整的父子层级关系
     * @see MenuTreeVO
     */
    List<MenuTreeVO> getMenuTree(Long tenantId, Long moduleId);
    
    /**
     * 分页查询菜单列表
     * <p>
     * 根据查询条件分页查询菜单列表，并将结果转换为 DTO 返回。
     * </p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>租户 ID 精确查询</li>
     *   <li>模块 ID 精确查询</li>
     *   <li>菜单名称模糊查询</li>
     *   <li>菜单类型精确查询</li>
     *   <li>状态精确查询</li>
     * </ul>
     * 
     * @param page 分页参数，包含页码和页面大小
     * @param query 查询条件，包含租户 ID、模块 ID、名称等过滤条件
     * @return 菜单分页数据，包含菜单 DTO 列表和总数
     * @see SysMenuQueryDTO
     * @see SysMenuDTO
     */
    IPage<SysMenuDTO> pageMenus(Page<SysMenu> page, SysMenuQueryDTO query);
    
    /**
     * 查询菜单列表
     * <p>
     * 根据查询条件查询所有符合条件的菜单列表，并将结果转换为 DTO 返回。
     * </p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>租户 ID 精确查询</li>
     *   <li>模块 ID 精确查询</li>
     *   <li>菜单名称模糊查询</li>
     *   <li>菜单类型精确查询</li>
     *   <li>状态精确查询</li>
     * </ul>
     * 
     * @param query 查询条件，包含租户 ID、模块 ID、名称等过滤条件
     * @return 菜单列表，包含所有符合条件的菜单 DTO
     * @see SysMenuQueryDTO
     * @see SysMenuDTO
     */
    List<SysMenuDTO> listMenus(SysMenuQueryDTO query);
    
    /**
     * 根据 ID 获取菜单详情
     * <p>
     * 根据菜单 ID 查询菜单详细信息，并将结果转换为 DTO 返回。
     * </p>
     * <p>关联查询：</p>
     * <ul>
     *   <li>根据模块 ID 查询模块名称</li>
     *   <li>根据父级菜单 ID 查询父级菜单名称</li>
     * </ul>
     * 
     * @param id 菜单 ID，必填参数
     * @return 菜单详情 DTO，包含菜单完整信息；若菜单不存在则返回 null
     * @see SysMenuDTO
     */
    SysMenuDTO getMenuById(Long id);
    
    /**
     * 新增菜单
     * <p>
     * 将菜单 DTO 转换为实体对象，并插入数据库。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>将 DTO 转换为实体对象</li>
     *   <li>插入菜单数据</li>
     * </ol>
     * 
     * @param menuDTO 菜单信息，包含菜单名称、类型、路径等必填字段
     * @throws IllegalArgumentException 当必填字段缺失时抛出
     * @see SysMenuDTO
     */
    void addMenu(SysMenuDTO menuDTO);
    
    /**
     * 更新菜单
     * <p>
     * 将菜单 DTO 转换为实体对象，并更新数据库。
     * </p>
     * 
     * @param menuDTO 菜单信息，包含菜单 ID 和需要更新的字段
     * @throws IllegalArgumentException 当菜单 ID 为空时抛出
     * @see SysMenuDTO
     */
    void updateMenu(SysMenuDTO menuDTO);
    
    /**
     * 删除菜单（级联删除子菜单和按钮）
     * <p>
     * 根据菜单 ID 删除菜单记录，并级联删除所有子菜单和按钮。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>删除当前菜单记录</li>
     *   <li>查询所有子菜单（递归）</li>
     *   <li>批量删除所有子菜单</li>
     * </ol>
     * 
     * @param id 菜单 ID，必填参数
     * @throws IllegalArgumentException 当菜单 ID 为空时抛出
     */
    void deleteMenu(Long id);
    
    /**
     * 批量删除菜单
     * <p>
     * 批量删除多个菜单记录。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>遍历菜单 ID 列表</li>
     *   <li>逐个调用 {@link #deleteMenu(Long)} 方法删除</li>
     * </ol>
     * 
     * @param ids 菜单 ID 列表，必填参数
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    void batchDeleteMenus(List<Long> ids);
    
    /**
     * 检查菜单是否存在
     * <p>
     * 根据菜单 ID 检查菜单是否存在。
     * </p>
     * 
     * @param id 菜单 ID，必填参数
     * @return true-菜单存在，false-菜单不存在
     */
    boolean existsById(Long id);
    
    /**
     * 检查菜单是否有子菜单
     * <p>
     * 根据菜单 ID 检查该菜单是否有子菜单。
     * </p>
     * <p>用途：用于删除菜单前判断是否可以删除。</p>
     * 
     * @param id 菜单 ID，必填参数
     * @return true-有子菜单，false-无子菜单
     */
    boolean hasChildren(Long id);
    
    /**
     * 检查菜单是否已被角色授权
     * <p>
     * 根据菜单 ID 检查该菜单是否已被角色关联。
     * </p>
     * <p>用途：用于删除菜单前判断是否可以删除。</p>
     * 
     * @param id 菜单 ID，必填参数
     * @return true-已被角色授权，false-未被角色授权
     */
    boolean hasRoleAssociation(Long id);
    
    /**
     * 检查权限标识是否已存在
     * <p>
     * 根据权限标识和租户 ID 检查菜单是否已存在。
     * </p>
     * <p>用途：用于新增菜单时验证权限标识唯一性。</p>
     * 
     * @param permKey 权限标识，必填参数
     * @param tenantId 租户 ID，必填参数
     * @return true-权限标识已存在，false-权限标识不存在
     */
    boolean existsByPermKey(String permKey, Long tenantId);
    
    /**
     * 检查权限标识是否已存在（排除指定 ID）
     * <p>
     * 根据权限标识和租户 ID 检查菜单是否已存在，排除指定的菜单 ID。
     * </p>
     * <p>用途：用于更新菜单时验证权限标识唯一性。</p>
     * 
     * @param permKey 权限标识，必填参数
     * @param tenantId 租户 ID，必填参数
     * @param excludeId 排除的菜单 ID，更新操作时传入当前菜单 ID
     * @return true-权限标识已存在（非当前菜单），false-权限标识不存在或是当前菜单
     */
    boolean existsByPermKeyExcludeId(String permKey, Long tenantId, Long excludeId);

    /**
     * 获取用户最常用菜单。
     * <p>
     * 按“访问次数倒序 + 最近访问时间倒序”返回当前用户的常用菜单列表。
     * 结果固定保留 Top 6，同时会自动过滤当前用户已经失去权限的菜单。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param limit 兼容保留参数，当前实现固定返回 Top 6
     * @return 当前用户常用菜单 Top 6 列表
     */
    List<UserMenuPreferenceVO> getUserCommonMenus(Long userId, Long tenantId, Integer limit);

    /**
     * 获取用户收藏菜单。
     * <p>
     * 返回当前用户主动收藏的菜单列表，并自动过滤当前用户已无权限访问的菜单。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param limit 返回条数上限，为空时使用系统默认值
     * @return 当前用户收藏菜单列表
     */
    List<UserMenuPreferenceVO> getUserFavoriteMenus(Long userId, Long tenantId, Integer limit);

    /**
     * 获取用户收藏管理列表。
     * <p>
     * 返回当前用户全部收藏菜单，并按照用户自定义排序顺序返回。
     * 结果会自动过滤当前用户已无权限访问的菜单。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @return 用户收藏管理列表
     */
    List<UserMenuPreferenceVO> getUserFavoriteManageMenus(Long userId, Long tenantId);

    /**
     * 切换用户收藏菜单。
     * <p>
     * 当目标菜单已被收藏时执行取消收藏；未收藏时执行新增收藏。
     * 服务层会先校验菜单路径是否有效且属于当前用户可访问的菜单。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param fullPath 前端上报的菜单完整路径
     * @return true=已收藏，false=已取消收藏
     */
    boolean toggleUserFavoriteMenu(Long userId, Long tenantId, String fullPath);

    /**
     * 批量取消用户收藏菜单。
     * <p>
     * 根据前端传入的菜单路径列表，批量删除当前用户的收藏记录。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param fullPaths 待取消收藏的菜单完整路径列表
     * @return 实际取消收藏的数量
     */
    int batchCancelUserFavoriteMenus(Long userId, Long tenantId, List<String> fullPaths);

    /**
     * 保存用户收藏菜单排序。
     * <p>
     * 前端按目标顺序提交菜单路径列表，服务端会将该顺序持久化到收藏表。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param orderedPaths 按目标顺序排列的菜单完整路径列表
     */
    void sortUserFavoriteMenus(Long userId, Long tenantId, List<String> orderedPaths);

    /**
     * 上报用户菜单访问记录。
     * <p>
     * 用于在用户进入菜单页面时写入访问统计，支撑个人首页常用菜单的动态排序。
     * 当目标路径无效或当前用户无权限访问时，方法会直接忽略本次上报。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param fullPath 前端上报的菜单完整路径
     */
    void reportUserMenuVisit(Long userId, Long tenantId, String fullPath);

    /**
     * 上报用户菜单打开次数。
     * <p>
     * 写入独立的菜单打开次数表，不受常用菜单 Top 6 裁剪影响，并返回是否首次打开。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param fullPath 前端上报的菜单完整路径
     * @return 打开次数上报结果
     */
    UserMenuOpenReportVO reportUserMenuOpen(Long userId, Long tenantId, String fullPath);
}
