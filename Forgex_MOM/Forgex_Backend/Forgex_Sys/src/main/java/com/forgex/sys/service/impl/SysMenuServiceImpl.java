/* Copyright 2026 coder_nai@163.com

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

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.i18n.LangContext;
import com.forgex.sys.domain.dto.SysMenuDTO;
import com.forgex.sys.domain.dto.SysMenuQueryDTO;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.entity.SysModule;
import com.forgex.sys.domain.entity.SysRoleMenu;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.vo.MenuTreeVO;
import com.forgex.sys.domain.vo.UserRoutesVO;
import com.forgex.sys.mapper.SysMenuMapper;
import com.forgex.sys.mapper.SysModuleMapper;
import com.forgex.sys.mapper.SysRoleMenuMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单 Service 实现类
 * <p>
 * 实现菜单管理相关的业务操作，包括用户路由生成、菜单树构建、菜单增删改查等功能。
 * </p>
 * <p>核心功能：</p>
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
 * </ul>
 * <p>技术特点：</p>
 * <ul>
 *   <li>支持多租户数据隔离</li>
 *   <li>基于角色权限动态生成用户路由</li>
 *   <li>支持国际化文本解析</li>
 *   <li>递归构建菜单树形结构</li>
 *   <li>关联查询模块和父级菜单信息</li>
 * </ul>
 * <p>使用说明：</p>
 * <ul>
 *   <li>菜单类型：catalog（目录）、menu（菜单）、button（按钮）</li>
 *   <li>路由生成逻辑：用户 -> 角色 -> 菜单 -> 模块</li>
 *   <li>国际化字段格式：name_i18n_json，存储多语言 JSON</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-07
 * @see ISysMenuService
 * @see com.forgex.sys.domain.dto.SysMenuDTO
 * @see com.forgex.sys.domain.vo.MenuTreeVO
 * @see com.forgex.sys.domain.vo.UserRoutesVO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysMenuMapper menuMapper;
    private final SysModuleMapper moduleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    /**
     * 获取用户路由（仅返回当前租户、当前角色显式授权的模块/菜单/按钮）。
     * <p>
     * 完整流程：
     * </p>
     * <ol>
     *   <li>参数校验：检查账号和租户 ID 是否为空，为空则返回空路由</li>
     *   <li>用户查询：根据账号查询用户信息</li>
     *   <li>角色查询：查询用户在当前租户下的所有角色</li>
     *   <li>角色 ID 提取：从用户角色关系中提取角色 ID 集合（使用 LinkedHashSet 保持顺序）</li>
     *   <li>菜单查询：根据角色 ID 集合查询角色 - 菜单关系</li>
     *   <li>菜单 ID 提取：从角色 - 菜单关系中提取菜单 ID 集合</li>
     *   <li>菜单列表查询：根据菜单 ID 集合查询可见、启用的菜单</li>
     *   <li>模块 ID 提取：从菜单中提取模块 ID 集合</li>
     *   <li>模块列表查询：根据模块 ID 集合查询可见、启用的模块</li>
     *   <li>构建路由：调用 buildUserRoutes() 构建用户路由对象</li>
     * </ol>
     * <p>
     * 权限控制：
     * </p>
     * <ul>
     *   <li>仅返回当前租户的菜单和模块</li>
     *   <li>仅返回用户角色显式授权的菜单</li>
     *   <li>仅返回可见（visible=true）且启用（status=true）的菜单和模块</li>
     * </ul>
     *
     * @param account  用户账号
     * @param tenantId 租户 ID
     * @return 用户路由信息，包含模块列表、路由列表、按钮权限列表
     * @see com.forgex.sys.domain.vo.UserRoutesVO
     * @see com.forgex.sys.domain.entity.SysUser
     * @see com.forgex.sys.domain.entity.SysUserRole
     * @see com.forgex.sys.domain.entity.SysRoleMenu
     * @see com.forgex.sys.domain.entity.SysMenu
     * @see com.forgex.sys.domain.entity.SysModule
     */
    @Override
    public UserRoutesVO getUserRoutes(String account, Long tenantId) {
        if (!StringUtils.hasText(account) || tenantId == null) {
            return createEmptyRoutes();
        }

        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, account));
        if (user == null) {
            return createEmptyRoutes();
        }

        List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getId())
                .eq(SysUserRole::getTenantId, tenantId));
        Set<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            return createEmptyRoutes();
        }

        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getTenantId, tenantId)
                .in(SysRoleMenu::getRoleId, roleIds));
        Set<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (menuIds.isEmpty()) {
            return createEmptyRoutes();
        }

        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getTenantId, tenantId)
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getVisible, true)
                .eq(SysMenu::getStatus, true)
                .orderByAsc(SysMenu::getOrderNum));
        if (menus.isEmpty()) {
            return createEmptyRoutes();
        }

        Set<Long> moduleIds = menus.stream()
                .map(SysMenu::getModuleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<SysModule> modules = moduleIds.isEmpty()
                ? Collections.emptyList()
                : moduleMapper.selectList(new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getTenantId, tenantId)
                        .in(SysModule::getId, moduleIds)
                        .eq(SysModule::getVisible, true)
                        .eq(SysModule::getStatus, true)
                        .orderByAsc(SysModule::getOrderNum));

        return buildUserRoutes(modules, menus);
    }

    /**
     * 获取菜单树结构。
     *
     * @param tenantId 租户 ID
     * @param moduleId 模块 ID
     * @return 菜单树结构
     */
    @Override
    public List<MenuTreeVO> getMenuTree(Long tenantId, Long moduleId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(tenantId != null, SysMenu::getTenantId, tenantId);
        wrapper.eq(moduleId != null, SysMenu::getModuleId, moduleId);
        wrapper.orderByAsc(SysMenu::getOrderNum);

        List<SysMenu> menus = menuMapper.selectList(wrapper);

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
        menuMapper.deleteById(id);

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

    @Override
    public boolean hasRoleAssociation(Long id) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getMenuId, id);
        return roleMenuMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByPermKey(String permKey, Long tenantId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(tenantId != null, SysMenu::getTenantId, tenantId);
        wrapper.eq(SysMenu::getPermKey, permKey);
        return menuMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByPermKeyExcludeId(String permKey, Long tenantId, Long excludeId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(tenantId != null, SysMenu::getTenantId, tenantId);
        wrapper.eq(SysMenu::getPermKey, permKey);
        wrapper.ne(SysMenu::getId, excludeId);
        return menuMapper.selectCount(wrapper) > 0;
    }

    /**
     * 创建空的路由对象。
     * <p>
     * 用于参数校验失败或用户无权限时返回空的路由结构。
     * </p>
     *
     * @return 空的用户路由对象，modules、routes、buttons 均为空列表
     * @see com.forgex.sys.domain.vo.UserRoutesVO
     */
    private UserRoutesVO createEmptyRoutes() {
        UserRoutesVO vo = new UserRoutesVO();
        vo.setModules(Collections.emptyList());
        vo.setRoutes(Collections.emptyList());
        vo.setButtons(Collections.emptyList());
        return vo;
    }

    /**
     * 构建用户路由对象。
     * <p>
     * 将模块和菜单列表转换为用户路由对象，包含：
     * </p>
     * <ul>
     *   <li>modules：模块列表，包含模块编码、名称、图标、排序等信息</li>
     *   <li>routes：路由树，按模块组织的菜单树形结构</li>
     *   <li>buttons：按钮权限列表，所有菜单下的按钮 permKey 集合</li>
     * </ul>
     *
     * @param modules 模块列表
     * @param menus 菜单列表
     * @return 用户路由对象
     * @see com.forgex.sys.domain.vo.UserRoutesVO
     * @see #buildModuleMap(SysModule)
     * @see #buildModuleRoute(SysModule, List)
     */
    private UserRoutesVO buildUserRoutes(List<SysModule> modules, List<SysMenu> menus) {
        UserRoutesVO vo = new UserRoutesVO();

        List<Map<String, Object>> moduleList = modules.stream()
                .sorted(Comparator.comparing(m -> m.getOrderNum() == null ? 0 : m.getOrderNum()))
                .map(this::buildModuleMap)
                .collect(Collectors.toList());

        List<Map<String, Object>> routeList = new ArrayList<>();
        for (SysModule module : modules) {
            Map<String, Object> moduleRoute = buildModuleRoute(module, menus);
            if (moduleRoute != null) {
                routeList.add(moduleRoute);
            }
        }

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
     * 构建模块 Map 对象。
     * <p>
     * 将模块实体转换为前端路由所需的 Map 结构，包含：
     * </p>
     * <ul>
     *   <li>code：模块编码</li>
     *   <li>name：模块名称（支持国际化解析）</li>
     *   <li>icon：模块图标</li>
     *   <li>order：排序号</li>
     * </ul>
     *
     * @param module 模块实体
     * @return 模块 Map 对象
     * @see #resolveI18nText(String, String)
     */
    private Map<String, Object> buildModuleMap(SysModule module) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", module.getCode());
        map.put("name", resolveI18nText(module.getNameI18nJson(), module.getName()));
        map.put("icon", module.getIcon());
        map.put("order", module.getOrderNum() == null ? 0 : module.getOrderNum());
        return map;
    }

    /**
     * 构建模块路由。
     * <p>
     * 将模块及其下属菜单转换为前端路由结构，包含：
     * </p>
     * <ul>
     *   <li>path：模块编码作为路由路径</li>
     *   <li>name：模块编码首字母大写作为路由名称</li>
     *   <li>meta：路由元信息，包含标题、图标、模块编码等</li>
     *   <li>children：子路由列表，由模块下的根菜单构建</li>
     * </ul>
     *
     * @param module 模块实体
     * @param allMenus 所有菜单列表
     * @return 模块路由 Map 对象，如果模块下没有根菜单则返回 null
     * @see #buildMeta(String, String, String, Integer, String, List)
     * @see #buildMenuRoute(SysMenu, List, String)
     */
    private Map<String, Object> buildModuleRoute(SysModule module, List<SysMenu> allMenus) {
        log.info("=== buildModuleRoute 开始 ===");
        log.info("模块信息 - id: {}, code: {}, name: {}", module.getId(), module.getCode(), module.getName());
        log.info("所有菜单总数: {}", allMenus.size());

        List<SysMenu> rootMenus = allMenus.stream()
                .filter(m -> Objects.equals(m.getModuleId(), module.getId()))
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .filter(m -> !"button".equalsIgnoreCase(m.getType()))
                .sorted(Comparator.comparing(m -> m.getOrderNum() == null ? 0 : m.getOrderNum()))
                .collect(Collectors.toList());
        if (rootMenus.isEmpty()) {
            return null;
        }

        log.info("模块{}下的根菜单数量: {}", module.getCode(), rootMenus.size());
        for (SysMenu rootMenu : rootMenus) {
            log.info("根菜单 - id: {}, name: {}, type: {}, path: {}, parent_id: {}",
                    rootMenu.getId(), rootMenu.getName(), rootMenu.getType(), rootMenu.getPath(), rootMenu.getParentId());
        }

        Map<String, Object> route = new HashMap<>();
        route.put("path", module.getCode());
        route.put("name", capitalize(module.getCode()));
        route.put("meta", buildMeta(
                resolveI18nText(module.getNameI18nJson(), module.getName()),
                module.getIcon(),
                module.getCode(),
                null,
                null,
                Collections.emptyList()
        ));

        List<Map<String, Object>> children = new ArrayList<>();
        for (SysMenu menu : rootMenus) {
            children.add(buildMenuRoute(menu, allMenus, module.getCode()));
        }

        route.put("children", children);
        log.info("=== buildModuleRoute 结束，children数量: {} ===", children.size());
        return route;
    }

    /**
     * 构建菜单路由。
     * <p>
     * 将菜单实体转换为前端路由结构，递归处理子菜单，包含：
     * </p>
     * <ul>
     *   <li>path：菜单路径</li>
     *   <li>name：菜单名称</li>
     *   <li>component：组件路径（仅 menu 和 button 类型设置）</li>
     *   <li>meta：路由元信息，包含标题、图标、模块编码、菜单层级、类型、按钮权限等</li>
     *   <li>children：子路由列表（如果存在子菜单）</li>
     * </ul>
     *
     * @param menu 菜单实体
     * @param allMenus 所有菜单列表
     * @param moduleCode 模块编码
     * @return 菜单路由 Map 对象
     * @see #buildMeta(String, String, String, Integer, String, List)
     * @see #buildMenuTree(List, Long)
     */
    private Map<String, Object> buildMenuRoute(SysMenu menu, List<SysMenu> allMenus, String moduleCode) {
        log.info("  buildMenuRoute - id: {}, name: {}, type: {}, path: {}, parent_id: {}",
                menu.getId(), menu.getName(), menu.getType(), menu.getPath(), menu.getParentId());

        Map<String, Object> route = new HashMap<>();
        route.put("path", menu.getPath());
        route.put("name", menu.getName());

        if (!"catalog".equalsIgnoreCase(menu.getType())) {
            route.put("component", menu.getComponentKey());
        } else {
            log.info("    菜单{}是 catalog 类型，跳过 component 设置", menu.getName());
        }

        List<String> perms = allMenus.stream()
                .filter(m -> Objects.equals(m.getParentId(), menu.getId()))
                .filter(m -> "button".equalsIgnoreCase(m.getType()))
                .filter(m -> StringUtils.hasText(m.getPermKey()))
                .map(SysMenu::getPermKey)
                .distinct()
                .collect(Collectors.toList());

        route.put("meta", buildMeta(
                resolveI18nText(menu.getNameI18nJson(), menu.getName()),
                menu.getIcon(),
                moduleCode,
                menu.getMenuLevel(),
                menu.getType(),
                perms
        ));

        List<SysMenu> childMenus = allMenus.stream()
                .filter(m -> Objects.equals(m.getParentId(), menu.getId()))
                .filter(m -> !"button".equalsIgnoreCase(m.getType()))
                .sorted(Comparator.comparing(m -> m.getOrderNum() == null ? 0 : m.getOrderNum()))
                .collect(Collectors.toList());

        log.info("    菜单{}的子菜单数量: {}", menu.getName(), childMenus.size());
        for (SysMenu child : childMenus) {
            log.info("      子菜单 - id: {}, name: {}, type: {}, path: {}",
                    child.getId(), child.getName(), child.getType(), child.getPath());
        }

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
     * 构建路由元信息。
     * <p>
     * 构建前端路由的 meta 对象，包含路由的展示和权限信息：
     * </p>
     * <ul>
     *   <li>title：路由标题（支持国际化）</li>
     *   <li>icon：路由图标</li>
     *   <li>module：所属模块编码</li>
     *   <li>menuLevel：菜单层级</li>
     *   <li>type：菜单类型（catalog/menu/button）</li>
     *   <li>perms：按钮权限列表</li>
     * </ul>
     *
     * @param title 路由标题
     * @param icon 路由图标
     * @param module 所属模块编码
     * @param menuLevel 菜单层级
     * @param type 菜单类型
     * @param perms 按钮权限列表
     * @return 路由元信息 Map 对象
     */
    private Map<String, Object> buildMeta(String title, String icon, String module,
                                          Integer menuLevel, String type, List<String> perms) {
        Map<String, Object> meta = new HashMap<>();
        if (title != null) {
            meta.put("title", title);
        }
        if (icon != null) {
            meta.put("icon", icon);
        }
        if (module != null) {
            meta.put("module", module);
        }
        if (menuLevel != null) {
            meta.put("menuLevel", menuLevel);
        }
        if (type != null) {
            meta.put("type", type);
        }
        if (perms != null && !perms.isEmpty()) {
            meta.put("perms", perms);
        }
        return meta;
    }

    /**
     * 字符串首字母大写。
     * <p>
     * 将字符串的首字母转换为大写，用于生成路由名称。
     * </p>
     *
     * @param str 输入字符串
     * @return 首字母大写后的字符串
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 构建菜单树。
     * <p>
     * 递归构建菜单树形结构，将菜单列表转换为树形 VO 列表：
     * </p>
     * <ul>
     *   <li>筛选出指定 parentId 的菜单作为当前层级的节点</li>
     *   <li>为每个菜单节点构建 MenuTreeVO 对象</li>
     *   <li>递归调用自身构建子节点列表</li>
     *   <li>支持国际化名称解析</li>
     * </ul>
     *
     * @param menus 菜单列表
     * @param parentId 父级菜单 ID，0 表示根节点
     * @return 菜单树 VO 列表
     * @see com.forgex.sys.domain.vo.MenuTreeVO
     * @see #resolveI18nText(String, String)
     */
    private List<MenuTreeVO> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .map(m -> {
                    MenuTreeVO vo = new MenuTreeVO();
                    vo.setId(m.getId());
                    vo.setParentId(m.getParentId());
                    vo.setName(resolveI18nText(m.getNameI18nJson(), m.getName()));
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
     * 解析国际化文本。
     * <p>
     * 根据当前语言环境从国际化 JSON 中解析对应的文本值：
     * </p>
     * <ul>
     *   <li>优先匹配完整语言代码（如 zh-CN）</li>
     *   <li>其次匹配语言前缀（如 zh）</li>
     *   <li>最后返回第一个非空值作为回退</li>
     *   <li>如果所有解析都失败，返回 fallback 值</li>
     * </ul>
     *
     * @param i18nJson 国际化 JSON 字符串
     * @param fallback 回退文本，当解析失败时返回
     * @return 解析后的国际化文本
     * @see com.forgex.common.i18n.LangContext
     */
    private String resolveI18nText(String i18nJson, String fallback) {
        if (!StringUtils.hasText(i18nJson)) {
            return fallback;
        }
        JSONObject obj;
        try {
            obj = JSONUtil.parseObj(i18nJson);
        } catch (Exception e) {
            return fallback;
        }
        String lang = LangContext.get();
        if (StringUtils.hasText(lang) && obj.containsKey(lang)) {
            String v = obj.getStr(lang);
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        if (StringUtils.hasText(lang)) {
            String prefix = lang;
            int idx = prefix.indexOf('-');
            if (idx > 0) {
                prefix = prefix.substring(0, idx);
            }
            if (StringUtils.hasText(prefix) && obj.containsKey(prefix)) {
                String v = obj.getStr(prefix);
                if (StringUtils.hasText(v)) {
                    return v;
                }
            }
        }
        try {
            for (String key : obj.keySet()) {
                String v = obj.getStr(key);
                if (StringUtils.hasText(v)) {
                    return v;
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    /**
     * 构建菜单查询条件。
     * <p>
     * 根据查询 DTO 动态构建 MyBatis-Plus 查询条件：
     * </p>
     * <ul>
     *   <li>tenantId：租户 ID 精确匹配</li>
     *   <li>moduleId：模块 ID 精确匹配</li>
     *   <li>name：菜单名称模糊匹配</li>
     *   <li>type：菜单类型精确匹配</li>
     *   <li>status：状态精确匹配</li>
     * </ul>
     * <p>
     * 默认按 OrderNum 升序排序。
     * </p>
     *
     * @param query 菜单查询 DTO
     * @return MyBatis-Plus 查询条件包装器
     * @see com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
     */
    private LambdaQueryWrapper<SysMenu> buildQueryWrapper(SysMenuQueryDTO query) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();

        if (query != null) {
            wrapper.eq(query.getTenantId() != null, SysMenu::getTenantId, query.getTenantId());
            wrapper.eq(query.getModuleId() != null, SysMenu::getModuleId, query.getModuleId());
            wrapper.like(StringUtils.hasText(query.getName()), SysMenu::getName, query.getName());
            wrapper.eq(StringUtils.hasText(query.getType()), SysMenu::getType, query.getType());
            wrapper.eq(query.getStatus() != null, SysMenu::getStatus, query.getStatus());
        }

        wrapper.orderByAsc(SysMenu::getOrderNum);
        return wrapper;
    }

    /**
     * 将菜单实体转换为 DTO。
     * <p>
     * 将 SysMenu 实体转换为 SysMenuDTO，并补充关联信息：
     * </p>
     * <ul>
     *   <li>复制基本属性</li>
     *   <li>设置 visible 和 status 字段（Boolean 类型）</li>
     *   <li>查询并设置模块名称（如果 moduleId 不为空）</li>
     *   <li>查询并设置父级菜单名称（如果 parentId 不为空）</li>
     * </ul>
     *
     * @param menu 菜单实体
     * @return 菜单 DTO
     * @see com.forgex.sys.domain.dto.SysMenuDTO
     * @see com.forgex.sys.domain.entity.SysMenu
     * @see com.forgex.sys.domain.entity.SysModule
     */
    private SysMenuDTO convertToDTO(SysMenu menu) {
        SysMenuDTO dto = new SysMenuDTO();
        BeanUtils.copyProperties(menu, dto);
        dto.setVisible(menu.getVisible());
        dto.setStatus(menu.getStatus());

        if (menu.getModuleId() != null) {
            SysModule module = moduleMapper.selectById(menu.getModuleId());
            if (module != null) {
                dto.setModuleName(module.getName());
            }
        }

        if (menu.getParentId() != null && menu.getParentId() > 0) {
            SysMenu parentMenu = menuMapper.selectById(menu.getParentId());
            if (parentMenu != null) {
                dto.setParentName(parentMenu.getName());
            }
        }

        return dto;
    }
}
