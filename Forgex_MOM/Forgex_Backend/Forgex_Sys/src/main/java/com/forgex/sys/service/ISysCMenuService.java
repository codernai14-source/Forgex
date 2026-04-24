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
 */
public interface ISysCMenuService extends IService<SysCMenu> {

    /** 分页查询 */
    IPage<SysCMenuDTO> pageMenus(Page<SysCMenu> page, SysCMenuQueryDTO query);

    /** 菜单列表（不分页） */
    List<SysCMenuDTO> listMenus(SysCMenuQueryDTO query);

    /** 菜单树 */
    List<CMenuTreeVO> getMenuTree(Long tenantId, Long moduleId);

    /** 详情 */
    SysCMenuDTO getMenuById(Long id);

    /** 新增 */
    void addMenu(SysCMenuDTO dto);

    /** 更新 */
    void updateMenu(SysCMenuDTO dto);

    /** 删除 */
    void deleteMenu(Long id);

    /** 批量删除 */
    void batchDeleteMenus(List<Long> ids);

    // ---- 角色菜单授权 ----

    /** 查询角色已授权的 C 端菜单 ID 列表 */
    List<Long> getRoleCMenuIds(Long tenantId, Long roleId);

    /** 获取模块下菜单树（含角色勾选状态） */
    List<CMenuTreeVO> getAuthMenuTree(Long tenantId, Long moduleId, Long roleId);

    /** 角色授权 C 端菜单 */
    void grantRoleCMenus(Long tenantId, Long roleId, List<Long> menuIds);

    // ---- 工作台/收藏（C 端 App 用） ----

    /** 获取当前用户工作台模块列表 */
    List<CMenuTreeVO> getWorkbenchModules(Long userId, Long tenantId);

    /** 获取指定模块下用户有权的菜单 */
    List<CMenuTreeVO> getWorkbenchMenus(Long userId, Long tenantId, Long moduleId);

    /** 获取用户收藏菜单 */
    List<CMenuTreeVO> getUserFavorites(Long userId, Long tenantId);

    /** 切换收藏状态 */
    boolean toggleFavorite(Long userId, Long tenantId, Long cMenuId);
}

