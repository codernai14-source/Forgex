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
import com.forgex.sys.domain.vo.UserRoutesVO;

import java.util.List;

/**
 * 菜单Service接口
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
public interface ISysMenuService extends IService<SysMenu> {
    
    /**
     * 获取用户路由（包含模块、菜单、按钮权限）
     * 
     * @param account 用户账号
     * @param tenantId 租户ID
     * @return 用户路由信息
     */
    UserRoutesVO getUserRoutes(String account, Long tenantId);
    
    /**
     * 获取菜单树
     * 
     * @param tenantId 租户ID
     * @param moduleId 模块ID（可选）
     * @return 菜单树列表
     */
    List<MenuTreeVO> getMenuTree(Long tenantId, Long moduleId);
    
    /**
     * 分页查询菜单列表
     * 
     * @param page 分页参数
     * @param query 查询条件
     * @return 菜单分页数据
     */
    IPage<SysMenuDTO> pageMenus(Page<SysMenu> page, SysMenuQueryDTO query);
    
    /**
     * 查询菜单列表
     * 
     * @param query 查询条件
     * @return 菜单列表
     */
    List<SysMenuDTO> listMenus(SysMenuQueryDTO query);
    
    /**
     * 根据ID获取菜单详情
     * 
     * @param id 菜单ID
     * @return 菜单详情
     */
    SysMenuDTO getMenuById(Long id);
    
    /**
     * 新增菜单
     * 
     * @param menuDTO 菜单信息
     */
    void addMenu(SysMenuDTO menuDTO);
    
    /**
     * 更新菜单
     * 
     * @param menuDTO 菜单信息
     */
    void updateMenu(SysMenuDTO menuDTO);
    
    /**
     * 删除菜单（级联删除子菜单和按钮）
     * 
     * @param id 菜单ID
     */
    void deleteMenu(Long id);
    
    /**
     * 批量删除菜单
     * 
     * @param ids 菜单ID列表
     */
    void batchDeleteMenus(List<Long> ids);
    
    /**
     * 检查菜单是否存在
     * 
     * @param id 菜单ID
     * @return 是否存在
     */
    boolean existsById(Long id);
    
    /**
     * 检查菜单是否有子菜单
     * 
     * @param id 菜单ID
     * @return 是否有子菜单
     */
    boolean hasChildren(Long id);
    
    /**
     * 检查菜单是否已被角色授权
     * 
     * @param id 菜单ID
     * @return 是否已被角色授权
     */
    boolean hasRoleAssociation(Long id);
    
    /**
     * 检查权限标识是否已存在
     * 
     * @param permKey 权限标识
     * @param tenantId 租户ID
     * @return 是否已存在
     */
    boolean existsByPermKey(String permKey, Long tenantId);
    
    /**
     * 检查权限标识是否已存在（排除指定ID）
     * 
     * @param permKey 权限标识
     * @param tenantId 租户ID
     * @param excludeId 排除的菜单ID
     * @return 是否已存在
     */
    boolean existsByPermKeyExcludeId(String permKey, Long tenantId, Long excludeId);
}
