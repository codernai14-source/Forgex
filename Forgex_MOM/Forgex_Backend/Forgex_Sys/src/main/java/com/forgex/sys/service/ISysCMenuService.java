/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysCMenuDTO;
import com.forgex.sys.domain.dto.SysCMenuQueryDTO;
import com.forgex.sys.domain.entity.SysCMenu;
import com.forgex.sys.domain.vo.CMenuTreeVO;

import java.util.List;

/**
 * C 端菜单服务接口
 *
 * @author Forgex Team
 * @since 2026-04-11
 *
 * @version 1.0.0
 */
public interface ISysCMenuService extends IService<SysCMenu> {

    /** 分页查询 */
    /**
     * 执行sysc菜单的分页menus操作。
     *
     * @param page 分页对象
     * @param query 查询参数
     * @return 处理结果
     */
    IPage<SysCMenuDTO> pageMenus(Page<SysCMenu> page, SysCMenuQueryDTO query);

    /** 菜单列表（不分页） */
    /**
     * 执行sysc菜单的列表menus操作。
     *
     * @param query 查询参数
     * @return 列表数据
     */
    List<SysCMenuDTO> listMenus(SysCMenuQueryDTO query);

    /** 菜单树 */
    /**
     * 获取菜单树。
     *
     * @param tenantId 租户 ID
     * @param moduleId 模块 ID
     * @return 列表数据
     */
    List<CMenuTreeVO> getMenuTree(Long tenantId, Long moduleId);

    /** 详情 */
    /**
     * 获取菜单byID。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    SysCMenuDTO getMenuById(Long id);

    /** 新增 */
    /**
     * 执行sysc菜单的add菜单操作。
     *
     * @param dto 数据传输对象
     */
    void addMenu(SysCMenuDTO dto);

    /** 更新 */
    /**
     * 更新菜单。
     *
     * @param dto 数据传输对象
     */
    void updateMenu(SysCMenuDTO dto);

    /** 删除 */
    /**
     * 删除菜单。
     *
     * @param id 主键 ID
     */
    void deleteMenu(Long id);

    /** 批量删除 */
    /**
     * 执行sysc菜单的batchdeletemenus操作。
     *
     * @param ids 主键 ID 集合
     */
    void batchDeleteMenus(List<Long> ids);

    // ---- 角色菜单授权 ----

    /** 查询角色已授权的 C 端菜单 ID 列表 */
    /**
     * 获取角色c菜单ids。
     *
     * @param tenantId 租户 ID
     * @param roleId 角色 ID
     * @return 列表数据
     */
    List<Long> getRoleCMenuIds(Long tenantId, Long roleId);

    /** 获取模块下菜单树（含角色勾选状态） */
    /**
     * 获取认证菜单树。
     *
     * @param tenantId 租户 ID
     * @param moduleId 模块 ID
     * @param roleId 角色 ID
     * @return 列表数据
     */
    List<CMenuTreeVO> getAuthMenuTree(Long tenantId, Long moduleId, Long roleId);

    /** 角色授权 C 端菜单 */
    /**
     * 执行sysc菜单的grant角色cmenus操作。
     *
     * @param tenantId 租户 ID
     * @param roleId 角色 ID
     * @param menuIds 菜单ids
     */
    void grantRoleCMenus(Long tenantId, Long roleId, List<Long> menuIds);

    // ---- 工作台/收藏（C 端 App 用） ----

    /** 获取当前用户工作台模块列表 */
    /**
     * 获取workbenchmodules。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @return 列表数据
     */
    List<CMenuTreeVO> getWorkbenchModules(Long userId, Long tenantId);

    /** 获取指定模块下用户有权的菜单 */
    /**
     * 获取workbenchmenus。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param moduleId 模块 ID
     * @return 列表数据
     */
    List<CMenuTreeVO> getWorkbenchMenus(Long userId, Long tenantId, Long moduleId);

    /** 获取用户收藏菜单 */
    /**
     * 获取用户favorites。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @return 列表数据
     */
    List<CMenuTreeVO> getUserFavorites(Long userId, Long tenantId);

    /** 切换收藏状态 */
    /**
     * 执行sysc菜单的togglefavorite操作。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @param cMenuId c菜单 ID
     * @return 是否处理成功
     */
    boolean toggleFavorite(Long userId, Long tenantId, Long cMenuId);
}

