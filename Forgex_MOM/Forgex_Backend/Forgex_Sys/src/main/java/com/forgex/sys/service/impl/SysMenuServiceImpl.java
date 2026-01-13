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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单Service实现类
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> 
    implements ISysMenuService {
    
    private final SysMenuMapper menuMapper;
    private final SysModuleMapper moduleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    
    /**
     * 获取用户路由信息
     * <p>
     * 根据用户账号和租户ID获取可访问的菜单路由、模块信息和按钮权限
     * 超级管理员（admin账号）自动拥有所有权限
     * </p>
     * 
     * @param account   用户账号
     * @param tenantId  租户ID
     * @return 用户路由信息，包含模块列表、路由列表和按钮权限列表
     */
    @Override
    public UserRoutesVO getUserRoutes(String account, Long tenantId) {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getAccount, account)
        );
        
        if (user == null) {
            return createEmptyRoutes();
        }
        
        // 2. 判断是否为超级管理员（admin账号拥有所有权限）
        if ("admin".equalsIgnoreCase(account)) {
            log.info("检测到超级管理员账号: {}, 返回所有权限", account);
            return getSuperAdminRoutes(tenantId);
        }
        
        // 3. 查询用户角色
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
        
        // 4. 查询角色菜单权限
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
        
        // 5. 查询菜单列表
        List<SysMenu> menus = menuMapper.selectList(
            new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getTenantId, tenantId)
                .eq(SysMenu::getVisible, true)
                .eq(SysMenu::getStatus, true)
        );
        
        // 6. 查询模块列表
        Set<Long> moduleIds = menus.stream()
            .map(SysMenu::getModuleId)
            .collect(Collectors.toSet());
        
        List<SysModule> modules = moduleIds.isEmpty() ? Collections.emptyList() :
            moduleMapper.selectList(
                new LambdaQueryWrapper<SysModule>()
                    .in(SysModule::getId, moduleIds)
                    .eq(SysModule::getTenantId, tenantId)
                    .eq(SysModule::getVisible, true)
                    .eq(SysModule::getStatus, true)
            );
        
        // 7. 构建返回数据
        return buildUserRoutes(modules, menus);
    }
    
    /**
     * 获取超级管理员的路由信息
     * <p>
     * 超级管理员拥有所有菜单和按钮权限
     * </p>
     * 
     * @param tenantId 租户ID
     * @return 超级管理员路由信息
     */
    private UserRoutesVO getSuperAdminRoutes(Long tenantId) {
        // 1. 查询所有启用的模块
        List<SysModule> modules = moduleMapper.selectList(
            new LambdaQueryWrapper<SysModule>()
                .eq(SysModule::getTenantId, tenantId)
                .eq(SysModule::getVisible, true)
                .eq(SysModule::getStatus, true)
        );
        
        // 2. 查询所有启用的菜单（包含按钮）
        List<SysMenu> menus = menuMapper.selectList(
            new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getTenantId, tenantId)
                .eq(SysMenu::getVisible, true)
                .eq(SysMenu::getStatus, true)
        );
        
        // 3. 构建返回数据
        return buildUserRoutes(modules, menus);
    }
    
    /**
     * 获取菜单树结构
     * <p>
     * 根据租户ID和模块ID获取菜单列表，并构建成树形结构
     * </p>
     * 
     * @param tenantId  租户ID，可为null
     * @param moduleId  模块ID，可为null
     * @return 菜单树结构列表
     */
    @Override
    public List<MenuTreeVO> getMenuTree(Long tenantId, Long moduleId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(tenantId != null, SysMenu::getTenantId, tenantId);
        wrapper.eq(moduleId != null, SysMenu::getModuleId, moduleId);
        wrapper.orderByAsc(SysMenu::getOrderNum);
        
        List<SysMenu> menus = menuMapper.selectList(wrapper);
        
        // 调试日志
        log.info("=== 菜单树查询调试 ===");
        log.info("查询参数 - tenantId: {}, moduleId: {}", tenantId, moduleId);
        log.info("查询结果总数: {}", menus.size());
        long buttonCount = menus.stream().filter(m -> "button".equals(m.getType())).count();
        long menuCount = menus.stream().filter(m -> "menu".equals(m.getType())).count();
        long catalogCount = menus.stream().filter(m -> "catalog".equals(m.getType())).count();
        log.info("按钮数量: {}, 菜单数量: {}, 目录数量: {}", buttonCount, menuCount, catalogCount);
        
        List<MenuTreeVO> tree = buildMenuTree(menus, 0L);
        log.info("构建的树形结构根节点数: {}", tree.size());
        
        return tree;
    }
    
    /**
     * 分页查询菜单列表
     * <p>
     * 根据查询条件分页获取菜单列表，并转换为DTO对象
     * </p>
     * 
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页菜单列表
     */
    @Override
    public IPage<SysMenuDTO> pageMenus(Page<SysMenu> page, SysMenuQueryDTO query) {
        LambdaQueryWrapper<SysMenu> wrapper = buildQueryWrapper(query);
        IPage<SysMenu> menuPage = menuMapper.selectPage(page, wrapper);
        return menuPage.convert(this::convertToDTO);
    }
    
    /**
     * 查询菜单列表
     * <p>
     * 根据查询条件获取菜单列表，并转换为DTO对象
     * </p>
     * 
     * @param query 查询条件
     * @return 菜单列表
     */
    @Override
    public List<SysMenuDTO> listMenus(SysMenuQueryDTO query) {
        LambdaQueryWrapper<SysMenu> wrapper = buildQueryWrapper(query);
        List<SysMenu> menus = menuMapper.selectList(wrapper);
        return menus.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取菜单详情
     * <p>
     * 根据菜单ID查询菜单信息，并转换为DTO对象
     * </p>
     * 
     * @param id 菜单ID
     * @return 菜单DTO对象，不存在则返回null
     */
    @Override
    public SysMenuDTO getMenuById(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        return menu != null ? convertToDTO(menu) : null;
    }
    
    /**
     * 添加菜单
     * <p>
     * 根据菜单DTO创建新的菜单记录
     * </p>
     * 
     * @param menuDTO 菜单DTO对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(SysMenuDTO menuDTO) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);
        menuMapper.insert(menu);
    }
    
    /**
     * 更新菜单
     * <p>
     * 根据菜单DTO更新现有菜单记录
     * </p>
     * 
     * @param menuDTO 菜单DTO对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(SysMenuDTO menuDTO) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);
        menuMapper.updateById(menu);
    }
    
    /**
     * 删除菜单
     * <p>
     * 根据菜单ID删除菜单及其子菜单
     * </p>
     * 
     * @param id 菜单ID
     */
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
    
    /**
     * 批量删除菜单
     * <p>
     * 批量删除多个菜单及其子菜单
     * </p>
     * 
     * @param ids 菜单ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteMenus(List<Long> ids) {
        for (Long id : ids) {
            deleteMenu(id);
        }
    }
    
    /**
     * 检查菜单是否存在
     * <p>
     * 根据菜单ID检查菜单是否存在
     * </p>
     * 
     * @param id 菜单ID
     * @return 菜单是否存在
     */
    @Override
    public boolean existsById(Long id) {
        return menuMapper.selectById(id) != null;
    }
    
    /**
     * 检查菜单是否有子菜单
     * <p>
     * 根据菜单ID检查是否存在子菜单
     * </p>
     * 
     * @param id 菜单ID
     * @return 是否有子菜单
     */
    @Override
    public boolean hasChildren(Long id) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId, id);
        return menuMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查菜单是否已被角色授权
     * <p>
     * 根据菜单ID检查是否存在角色授权记录
     * </p>
     * 
     * @param id 菜单ID
     * @return 是否已被角色授权
     */
    @Override
    public boolean hasRoleAssociation(Long id) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getMenuId, id);
        return roleMenuMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查权限标识是否已存在
     * <p>
     * 根据权限标识和租户ID检查是否存在相同的权限标识
     * </p>
     * 
     * @param permKey 权限标识
     * @param tenantId 租户ID
     * @return 是否已存在
     */
    @Override
    public boolean existsByPermKey(String permKey, Long tenantId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getPermKey, permKey);
        wrapper.eq(SysMenu::getTenantId, tenantId);
        return menuMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 检查权限标识是否已存在（排除指定ID）
     * <p>
     * 根据权限标识和租户ID检查是否存在相同的权限标识，排除指定的菜单ID
     * </p>
     * 
     * @param permKey 权限标识
     * @param tenantId 租户ID
     * @param excludeId 排除的菜单ID
     * @return 是否已存在
     */
    @Override
    public boolean existsByPermKeyExcludeId(String permKey, Long tenantId, Long excludeId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getPermKey, permKey);
        wrapper.eq(SysMenu::getTenantId, tenantId);
        wrapper.ne(SysMenu::getId, excludeId);
        return menuMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 创建空路由对象
     * <p>
     * 创建一个包含空模块列表、空路由列表和空按钮权限列表的UserRoutesVO对象
     * </p>
     * 
     * @return 空路由对象
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
     * <p>
     * 根据模块列表和菜单列表构建用户路由信息，包括模块列表、路由列表和按钮权限列表
     * </p>
     * 
     * @param modules 模块列表
     * @param menus   菜单列表
     * @return 用户路由信息
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
     * 构建模块Map对象
     * <p>
     * 将SysModule对象转换为包含模块基本信息的Map对象
     * </p>
     * 
     * @param module 模块实体
     * @return 模块Map对象
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
     * 构建模块路由对象
     * <p>
     * 根据模块信息和菜单列表构建模块级别的路由对象
     * </p>
     * 
     * @param module   模块实体
     * @param allMenus 所有菜单列表
     * @return 模块路由对象
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
     * 构建菜单路由对象
     * <p>
     * 根据菜单信息和模块代码构建菜单级别的路由对象，包含子菜单和按钮权限
     * </p>
     * 
     * @param menu        菜单实体
     * @param allMenus    所有菜单列表
     * @param moduleCode  模块代码
     * @return 菜单路由对象
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
     * 构建路由Meta信息
     * <p>
     * 构建包含标题、图标和模块信息的路由Meta对象
     * </p>
     * 
     * @param title  标题
     * @param icon   图标
     * @param module 模块
     * @return 路由Meta对象
     */
    private Map<String, Object> buildMeta(String title, String icon, String module) {
        return buildMeta(title, icon, module, null, null);
    }
    
    /**
     * 构建路由Meta信息
     * <p>
     * 构建包含标题、图标、模块、菜单级别和权限信息的路由Meta对象
     * </p>
     * 
     * @param title      标题
     * @param icon       图标
     * @param module     模块
     * @param menuLevel  菜单级别
     * @param perms      权限列表
     * @return 路由Meta对象
     */
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
     * 将字符串首字母大写
     * <p>
     * 将输入字符串的第一个字符转换为大写，其余字符保持不变
     * </p>
     * 
     * @param str 输入字符串
     * @return 首字母大写的字符串
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * 构建菜单树结构
     * <p>
     * 根据菜单列表和父菜单ID递归构建菜单树结构
     * </p>
     * 
     * @param menus     菜单列表
     * @param parentId  父菜单ID
     * @return 菜单树结构列表
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
                vo.setMenuMode(m.getMenuMode());
                vo.setPermKey(m.getPermKey());
                vo.setVisible(m.getVisible());
                vo.setStatus(m.getStatus());
                vo.setCreateTime(m.getCreateTime());
                vo.setCreateBy(m.getCreateBy());
                vo.setUpdateTime(m.getUpdateTime());
                vo.setUpdateBy(m.getUpdateBy());
                vo.setChildren(buildMenuTree(menus, m.getId()));
                return vo;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 构建菜单查询条件
     * <p>
     * 根据查询DTO构建菜单查询条件
     * </p>
     * 
     * @param query 查询DTO
     * @return 菜单查询条件
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
     * 菜单实体转DTO对象
     * <p>
     * 将SysMenu实体转换为SysMenuDTO对象，并关联查询模块名称和父菜单名称
     * </p>
     * 
     * @param menu 菜单实体
     * @return 菜单DTO对象
     */
    private SysMenuDTO convertToDTO(SysMenu menu) {
        SysMenuDTO dto = new SysMenuDTO();
        BeanUtils.copyProperties(menu, dto);
        
        // 确保visible和status字段被正确复制
        dto.setVisible(menu.getVisible());
        dto.setStatus(menu.getStatus());
        
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
