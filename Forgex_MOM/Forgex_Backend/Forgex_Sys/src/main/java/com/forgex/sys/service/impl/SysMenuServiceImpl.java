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
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.sys.domain.dto.SysMenuDTO;
import com.forgex.sys.domain.dto.SysMenuQueryDTO;
import com.forgex.sys.domain.entity.*;
import com.forgex.sys.domain.vo.MenuTreeVO;
import com.forgex.sys.domain.vo.UserRoutesVO;
import com.forgex.sys.mapper.*;
import com.forgex.sys.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单Service实现类
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> 
    implements ISysMenuService {
    
    private final SysMenuMapper menuMapper;
    private final SysModuleMapper moduleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    
    @Override
    public UserRoutesVO getUserRoutes(String account, Long tenantId) {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getAccount, account)
        );
        
        if (user == null) {
            return createEmptyRoutes();
        }
        
        // 2. 查询用户角色
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getId())
                .eq(SysUserRole::getTenantId, tenantId)
        );
        
        Set<Long> roleIds = userRoles.stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toSet());
        
        if (roleIds.isEmpty()) {
            return createEmptyRoutes();
        }
        
        // 3. 查询角色菜单权限
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
            new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, roleIds)
                .eq(SysRoleMenu::getTenantId, tenantId)
        );
        
        Set<Long> menuIds = roleMenus.stream()
            .map(SysRoleMenu::getMenuId)
            .collect(Collectors.toSet());
        
        if (menuIds.isEmpty()) {
            return createEmptyRoutes();
        }
        
        // 4. 查询菜单列表
        List<SysMenu> menus = menuMapper.selectList(
            new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getTenantId, tenantId)
                .eq(SysMenu::getVisible, 1)
                .eq(SysMenu::getStatus, 1)
        );
        
        // 5. 查询模块列表
        Set<Long> moduleIds = menus.stream()
            .map(SysMenu::getModuleId)
            .collect(Collectors.toSet());
        
        List<SysModule> modules = moduleIds.isEmpty() ? Collections.emptyList() :
            moduleMapper.selectList(
                new LambdaQueryWrapper<SysModule>()
                    .in(SysModule::getId, moduleIds)
                    .eq(SysModule::getTenantId, tenantId)
                    .eq(SysModule::getVisible, 1)
                    .eq(SysModule::getStatus, 1)
            );
        
        // 6. 构建返回数据
        return buildUserRoutes(modules, menus);
    }
    
    @Override
    public List<MenuTreeVO> getMenuTree(Long tenantId, Long moduleId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(tenantId != null, SysMenu::getTenantId, tenantId);
        wrapper.eq(moduleId != null, SysMenu::getModuleId, moduleId);
        wrapper.orderByAsc(SysMenu::getOrderNum);
        
        List<SysMenu> menus = menuMapper.selectList(wrapper);
        return buildMenuTree(menus, 0L);
    }
    
    @Override
    public IPage<SysMenuDTO> pageMenus(Page<SysMenu> page, SysMenuQueryDTO query) {
        LambdaQueryWrapper<SysMenu> wrapper = buildQueryWrapper(query);
        IPage<SysMenu> menuPage = menuMapper.selectPage(page, wrapper);
        return menuPage.convert(this::convertToDTO);
    }
    
    @Override
    public List<SysMenuDTO> listMenus(SysMenuQueryDTO query) {
        LambdaQueryWrapper<SysMenu> wrapper = buildQueryWrapper(query);
        List<SysMenu> menus = menuMapper.selectList(wrapper);
        return menus.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public SysMenuDTO getMenuById(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        return menu != null ? convertToDTO(menu) : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(SysMenuDTO menuDTO) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);
        menuMapper.insert(menu);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(SysMenuDTO menuDTO) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);
        menuMapper.updateById(menu);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        // 删除菜单
        menuMapper.deleteById(id);
        
        // 级联删除子菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        menuMapper.delete(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteMenus(List<Long> ids) {
        for (Long id : ids) {
            deleteMenu(id);
        }
    }
    
    @Override
    public boolean existsById(Long id) {
        return menuMapper.selectById(id) != null;
    }
    
    @Override
    public boolean hasChildren(Long id) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        return menuMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 创建空路由
     */
    private UserRoutesVO createEmptyRoutes() {
        UserRoutesVO vo = new UserRoutesVO();
        vo.setModules(Collections.emptyList());
        vo.setRoutes(Collections.emptyList());
        vo.setButtons(Collections.emptyList());
        return vo;
    }
    
    /**
     * 构建用户路由
     */
    private UserRoutesVO buildUserRoutes(List<SysModule> modules, List<SysMenu> menus) {
        UserRoutesVO vo = new UserRoutesVO();
        
        // 构建模块列表
        List<Map<String, Object>> moduleList = modules.stream()
            .sorted(Comparator.comparing(m -> m.getOrderNum() == null ? 0 : m.getOrderNum()))
            .map(this::buildModuleMap)
            .collect(Collectors.toList());
        
        // 构建路由列表
        List<Map<String, Object>> routeList = new ArrayList<>();
        for (SysModule module : modules) {
            Map<String, Object> moduleRoute = buildModuleRoute(module, menus);
            routeList.add(moduleRoute);
        }
        
        // 提取按钮权限
        List<String> buttons = menus.stream()
            .filter(m -> "button".equalsIgnoreCase(m.getType()))
            .filter(m -> StringUtils.hasText(m.getPermKey()))
            .map(SysMenu::getPermKey)
            .distinct()
            .collect(Collectors.toList());
        
        vo.setModules(moduleList);
        vo.setRoutes(routeList);
        vo.setButtons(buttons);
        return vo;
    }
    
    /**
     * 构建模块Map
     */
    private Map<String, Object> buildModuleMap(SysModule module) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", module.getCode());
        map.put("name", module.getName());
        map.put("icon", module.getIcon());
        map.put("order", module.getOrderNum() == null ? 0 : module.getOrderNum());
        return map;
    }
    
    /**
     * 构建模块路由
     */
    private Map<String, Object> buildModuleRoute(SysModule module, List<SysMenu> allMenus) {
        Map<String, Object> route = new HashMap<>();
        route.put("path", module.getCode());
        route.put("name", capitalize(module.getCode()));
        route.put("meta", buildMeta(module.getName(), module.getIcon(), module.getCode()));
        
        // 获取该模块下的根菜单
        List<SysMenu> rootMenus = allMenus.stream()
            .filter(m -> Objects.equals(m.getModuleId(), module.getId()))
            .filter(m -> m.getParentId() == null || m.getParentId() == 0)
            .filter(m -> !"button".equalsIgnoreCase(m.getType()))
            .sorted(Comparator.comparing(m -> m.getOrderNum() == null ? 0 : m.getOrderNum()))
            .collect(Collectors.toList());
        
        List<Map<String, Object>> children = new ArrayList<>();
        for (SysMenu menu : rootMenus) {
            children.add(buildMenuRoute(menu, allMenus, module.getCode()));
        }
        
        route.put("children", children);
        return route;
    }
    
    /**
     * 构建菜单路由
     */
    private Map<String, Object> buildMenuRoute(SysMenu menu, List<SysMenu> allMenus, String moduleCode) {
        Map<String, Object> route = new HashMap<>();
        route.put("path", menu.getPath());
        route.put("name", menu.getName());
        route.put("component", menu.getComponentKey());
        
        // 获取该菜单下的按钮权限
        List<String> perms = allMenus.stream()
            .filter(m -> Objects.equals(m.getParentId(), menu.getId()))
            .filter(m -> "button".equalsIgnoreCase(m.getType()))
            .filter(m -> StringUtils.hasText(m.getPermKey()))
            .map(SysMenu::getPermKey)
            .distinct()
            .collect(Collectors.toList());
        
        route.put("meta", buildMeta(menu.getName(), menu.getIcon(), moduleCode, menu.getMenuLevel(), perms));
        
        // 获取子菜单
        List<SysMenu> childMenus = allMenus.stream()
            .filter(m -> Objects.equals(m.getParentId(), menu.getId()))
            .filter(m -> !"button".equalsIgnoreCase(m.getType()))
            .sorted(Comparator.comparing(m -> m.getOrderNum() == null ? 0 : m.getOrderNum()))
            .collect(Collectors.toList());
        
        if (!childMenus.isEmpty()) {
            List<Map<String, Object>> children = new ArrayList<>();
            for (SysMenu child : childMenus) {
                children.add(buildMenuRoute(child, allMenus, moduleCode));
            }
            route.put("children", children);
        }
        
        return route;
    }
    
    /**
     * 构建Meta信息
     */
    private Map<String, Object> buildMeta(String title, String icon, String module) {
        return buildMeta(title, icon, module, null, null);
    }
    
    private Map<String, Object> buildMeta(String title, String icon, String module, 
                                         Integer menuLevel, List<String> perms) {
        Map<String, Object> meta = new HashMap<>();
        if (title != null) meta.put("title", title);
        if (icon != null) meta.put("icon", icon);
        if (module != null) meta.put("module", module);
        if (menuLevel != null) meta.put("menuLevel", menuLevel);
        if (perms != null && !perms.isEmpty()) meta.put("perms", perms);
        return meta;
    }
    
    /**
     * 首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * 构建菜单树
     */
    private List<MenuTreeVO> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
            .filter(m -> Objects.equals(m.getParentId(), parentId))
            .map(m -> {
                MenuTreeVO vo = new MenuTreeVO();
                vo.setId(m.getId());
                vo.setParentId(m.getParentId());
                vo.setName(m.getName());
                vo.setPath(m.getPath());
                vo.setIcon(m.getIcon());
                vo.setType(m.getType());
                vo.setMenuLevel(m.getMenuLevel());
                vo.setOrderNum(m.getOrderNum());
                vo.setChildren(buildMenuTree(menus, m.getId()));
                return vo;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<SysMenu> buildQueryWrapper(SysMenuQueryDTO query) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        
        if (query != null) {
            wrapper.eq(query.getModuleId() != null, 
                SysMenu::getModuleId, query.getModuleId());
            wrapper.like(StringUtils.hasText(query.getName()), 
                SysMenu::getName, query.getName());
            wrapper.eq(StringUtils.hasText(query.getType()), 
                SysMenu::getType, query.getType());
            wrapper.eq(query.getStatus() != null, 
                SysMenu::getStatus, query.getStatus());
            wrapper.eq(query.getTenantId() != null, 
                SysMenu::getTenantId, query.getTenantId());
        }
        
        wrapper.orderByAsc(SysMenu::getOrderNum);
        return wrapper;
    }
    
    /**
     * 实体转DTO
     */
    private SysMenuDTO convertToDTO(SysMenu menu) {
        SysMenuDTO dto = new SysMenuDTO();
        BeanUtils.copyProperties(menu, dto);
        
        // 关联查询模块名称
        if (menu.getModuleId() != null) {
            SysModule module = moduleMapper.selectById(menu.getModuleId());
            if (module != null) {
                dto.setModuleName(module.getName());
            }
        }
        
        // 关联查询父菜单名称
        if (menu.getParentId() != null && menu.getParentId() > 0) {
            SysMenu parentMenu = menuMapper.selectById(menu.getParentId());
            if (parentMenu != null) {
                dto.setParentName(parentMenu.getName());
            }
        }
        
        return dto;
    }
}
