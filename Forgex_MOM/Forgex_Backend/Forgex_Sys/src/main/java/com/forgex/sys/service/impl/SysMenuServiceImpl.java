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
import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.service.i18n.I18nLanguageTypeService;
import com.forgex.sys.domain.dto.SysMenuDTO;
import com.forgex.sys.domain.dto.SysMenuQueryDTO;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.entity.SysModule;
import com.forgex.sys.domain.entity.SysRoleMenu;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserMenuCommon;
import com.forgex.sys.domain.entity.SysUserMenuFavorite;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.vo.MenuTreeVO;
import com.forgex.sys.domain.vo.UserMenuPreferenceVO;
import com.forgex.sys.domain.vo.UserRoutesVO;
import com.forgex.sys.mapper.SysMenuMapper;
import com.forgex.sys.mapper.SysModuleMapper;
import com.forgex.sys.mapper.SysRoleMenuMapper;
import com.forgex.sys.mapper.SysUserMenuCommonMapper;
import com.forgex.sys.mapper.SysUserMenuFavoriteMapper;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.time.LocalDateTime;
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
 * @since 2026-04-12
 * @see ISysMenuService
 * @see com.forgex.sys.domain.dto.SysMenuDTO
 * @see com.forgex.sys.domain.vo.MenuTreeVO
 * @see com.forgex.sys.domain.vo.UserRoutesVO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    /**
     * 常用菜单默认返回条数。
     */
    private static final int DEFAULT_COMMON_MENU_LIMIT = 6;

    /**
     * 收藏菜单默认返回条数。
     */
    private static final int DEFAULT_FAVORITE_MENU_LIMIT = 6;

    private final SysMenuMapper menuMapper;
    private final SysModuleMapper moduleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserMenuCommonMapper userMenuCommonMapper;
    private final SysUserMenuFavoriteMapper userMenuFavoriteMapper;
    private final I18nLanguageTypeService languageTypeService;

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
     * 获取用户最常用菜单。
     * <p>
     * 执行步骤：
     * 1. 校验用户与租户上下文是否完整；
     * 2. 将结果收敛为固定 Top 6，保证首页展示与数据量控制一致；
     * 3. 查询当前用户仍有权限访问的菜单快照，作为结果基准；
     * 4. 查询用户收藏路径集合，为前端补齐星标状态；
     * 5. 先执行自动裁剪，再按访问次数与最近访问时间查询常用菜单表并转换为首页展示对象。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param limit 兼容保留参数，当前实现固定返回 Top 6
     * @return 常用菜单 Top 6 列表
     */
    @Override
    public List<UserMenuPreferenceVO> getUserCommonMenus(Long userId, Long tenantId, Integer limit) {
        // 1. 校验上下文参数。
        // 当前方法必须基于用户和租户双维度查询数据，缺少任一条件都直接返回空结果。
        if (userId == null || tenantId == null) {
            return Collections.emptyList();
        }

        // 2. 固定返回 Top 6。
        // 常用菜单已改为固定 Top 6 规则，这里不再接受前端自定义条数，避免展示规则和存储规则不一致。
        int safeLimit = DEFAULT_COMMON_MENU_LIMIT;

        // 3. 查询当前用户仍有权限访问的菜单快照。
        // 后续只会在该授权集合内组装结果，避免把历史无权菜单展示给前端。
        Map<String, AuthorizedMenuSnapshot> authorizedMenus = getAuthorizedMenuSnapshots(userId, tenantId);
        if (authorizedMenus.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 先执行自动裁剪。
        // 在查询前主动清理 Top 6 之外的旧低频记录，确保常用菜单表数据量始终收敛。
        trimUserCommonMenus(userId, tenantId);

        // 5. 查询当前用户已收藏的菜单路径集合。
        // 该集合会用于给常用菜单补充 favorite 状态，方便前端直接显示星标。
        Set<String> favoritePaths = getFavoritePathSet(userId, tenantId);

        // 6. 查询常用菜单表并组装结果。
        // 使用访问次数和最近访问时间排序，最终只保留当前仍具备权限的菜单。
        LambdaQueryWrapper<SysUserMenuCommon> wrapper = new LambdaQueryWrapper<SysUserMenuCommon>()
                .eq(SysUserMenuCommon::getTenantId, tenantId)
                .eq(SysUserMenuCommon::getUserId, userId)
                .orderByDesc(SysUserMenuCommon::getVisitCount)
                .orderByDesc(SysUserMenuCommon::getLastVisitedAt)
                .last("limit " + safeLimit);

        return userMenuCommonMapper.selectList(wrapper).stream()
                .map(record -> {
                    AuthorizedMenuSnapshot snapshot = authorizedMenus.get(record.getMenuPath());
                    if (snapshot == null) {
                        return null;
                    }
                    return toUserMenuPreferenceVO(snapshot, record.getVisitCount(), record.getLastVisitedAt(), favoritePaths.contains(snapshot.path));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户收藏菜单。
     * <p>
     * 执行步骤：
     * 1. 校验用户与租户上下文；
     * 2. 计算安全条数上限；
     * 3. 查询当前用户的授权菜单快照，过滤无权限菜单；
     * 4. 先补齐排序号，再按用户自定义顺序查询收藏表；
     * 5. 转换为首页展示对象并返回。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param limit 返回条数上限
     * @return 收藏菜单列表
     */
    @Override
    public List<UserMenuPreferenceVO> getUserFavoriteMenus(Long userId, Long tenantId, Integer limit) {
        // 1. 校验上下文参数。
        // 未登录或租户信息缺失时不返回任何收藏数据，保证数据隔离安全。
        if (userId == null || tenantId == null) {
            return Collections.emptyList();
        }

        // 2. 解析安全条数上限。
        // 收藏列表默认返回 6 条，同时限制最大值，避免前端误传超大分页参数。
        int safeLimit = resolveSafeLimit(limit, DEFAULT_FAVORITE_MENU_LIMIT);

        // 3. 查询授权菜单快照。
        // 收藏记录只是历史行为，最终返回前仍需校验当前是否还拥有访问权限。
        Map<String, AuthorizedMenuSnapshot> authorizedMenus = getAuthorizedMenuSnapshots(userId, tenantId);
        if (authorizedMenus.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 补齐排序号。
        // 历史收藏数据可能还没有排序号，先进行一次轻量归一化，保证首页与管理页排序稳定。
        ensureFavoriteOrderNumbers(userId, tenantId);

        // 5. 查询收藏表并组装结果。
        // 先按用户自定义顺序返回，创建时间仅作为兜底排序，保证首页与管理页顺序一致。
        LambdaQueryWrapper<SysUserMenuFavorite> wrapper = new LambdaQueryWrapper<SysUserMenuFavorite>()
                .eq(SysUserMenuFavorite::getTenantId, tenantId)
                .eq(SysUserMenuFavorite::getUserId, userId)
                .orderByAsc(SysUserMenuFavorite::getOrderNum)
                .orderByDesc(SysUserMenuFavorite::getCreateTime)
                .last("limit " + safeLimit);

        return userMenuFavoriteMapper.selectList(wrapper).stream()
                .map(record -> {
                    AuthorizedMenuSnapshot snapshot = authorizedMenus.get(record.getMenuPath());
                    if (snapshot == null) {
                        return null;
                    }
                    return toUserMenuPreferenceVO(snapshot, null, null, Boolean.TRUE);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户收藏管理列表。
     * <p>
     * 执行步骤：
     * 1. 校验用户与租户上下文；
     * 2. 查询当前用户授权菜单快照；
     * 3. 补齐旧数据排序号，保证管理页排序稳定；
     * 4. 查询全部收藏记录；
     * 5. 转换为前端管理页可直接使用的数据结构。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @return 收藏管理列表
     */
    @Override
    public List<UserMenuPreferenceVO> getUserFavoriteManageMenus(Long userId, Long tenantId) {
        // 1. 校验上下文参数。
        // 收藏管理页必须基于当前用户和租户查询数据，缺失时直接返回空列表。
        if (userId == null || tenantId == null) {
            return Collections.emptyList();
        }

        // 2. 查询授权菜单快照。
        // 只保留当前仍有权限访问的收藏菜单，避免管理页出现无权访问的旧收藏。
        Map<String, AuthorizedMenuSnapshot> authorizedMenus = getAuthorizedMenuSnapshots(userId, tenantId);
        if (authorizedMenus.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 补齐历史排序号。
        // 旧收藏数据可能没有 order_num，先完成一次归一化，后续列表查询才能稳定按顺序返回。
        ensureFavoriteOrderNumbers(userId, tenantId);

        // 4. 查询全部收藏记录。
        // 按用户自定义顺序优先，再按创建时间倒序兜底，保证管理页和首页展示顺序一致。
        return userMenuFavoriteMapper.selectList(new LambdaQueryWrapper<SysUserMenuFavorite>()
                        .eq(SysUserMenuFavorite::getTenantId, tenantId)
                        .eq(SysUserMenuFavorite::getUserId, userId)
                        .orderByAsc(SysUserMenuFavorite::getOrderNum)
                        .orderByDesc(SysUserMenuFavorite::getCreateTime))
                .stream()
                .map(record -> {
                    AuthorizedMenuSnapshot snapshot = authorizedMenus.get(record.getMenuPath());
                    if (snapshot == null) {
                        return null;
                    }
                    return toUserMenuPreferenceVO(snapshot, null, null, Boolean.TRUE);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 切换用户收藏菜单状态。
     * <p>
     * 执行步骤：
     * 1. 校验上下文参数；
     * 2. 标准化前端上报路径，去掉查询参数等噪声；
     * 3. 校验该路径是否属于当前用户可访问菜单；
     * 4. 如果已存在收藏记录则删除；
     * 5. 如果不存在收藏记录则新增一条收藏数据。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param fullPath 前端上报的菜单完整路径
     * @return true=已收藏，false=已取消收藏
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleUserFavoriteMenu(Long userId, Long tenantId, String fullPath) {
        // 1. 校验上下文参数。
        // 收藏行为必须绑定当前用户与租户，任一参数为空都不执行数据库写操作。
        if (userId == null || tenantId == null) {
            return false;
        }

        // 2. 标准化菜单路径。
        // 去掉查询参数与首尾空格，确保同一路由不会因为 URL 附带参数而产生多条收藏记录。
        String normalizedPath = normalizeReportedMenuPath(fullPath);
        if (!StringUtils.hasText(normalizedPath)) {
            return false;
        }

        // 3. 校验菜单权限。
        // 只允许收藏当前用户确实拥有访问权限的菜单，避免伪造路径写入数据库。
        AuthorizedMenuSnapshot snapshot = getAuthorizedMenuSnapshots(userId, tenantId).get(normalizedPath);
        if (snapshot == null) {
            return false;
        }

        // 4. 补齐排序号。
        // 先修复历史收藏数据中的空排序号，避免新增收藏时出现排序断层。
        ensureFavoriteOrderNumbers(userId, tenantId);

        // 5. 查询现有收藏记录。
        // 如果记录已存在，则本次操作视为取消收藏。
        LambdaQueryWrapper<SysUserMenuFavorite> wrapper = new LambdaQueryWrapper<SysUserMenuFavorite>()
                .eq(SysUserMenuFavorite::getTenantId, tenantId)
                .eq(SysUserMenuFavorite::getUserId, userId)
                .eq(SysUserMenuFavorite::getMenuPath, normalizedPath)
                .last("limit 1");
        SysUserMenuFavorite existing = userMenuFavoriteMapper.selectOne(wrapper);
        if (existing != null) {
            userMenuFavoriteMapper.deleteById(existing.getId());
            return false;
        }

        // 6. 新增收藏记录。
        // 使用授权快照中的菜单标题、模块名称、图标等信息写入收藏表，减少前端再次拼装负担。
        SysUserMenuFavorite favorite = new SysUserMenuFavorite();
        favorite.setTenantId(tenantId);
        favorite.setUserId(userId);
        favorite.setMenuPath(snapshot.path);
        favorite.setMenuTitle(snapshot.title);
        favorite.setModuleCode(snapshot.moduleCode);
        favorite.setModuleName(snapshot.moduleName);
        favorite.setMenuIcon(snapshot.icon);
        favorite.setOrderNum(resolveNextFavoriteOrderNum(userId, tenantId));
        userMenuFavoriteMapper.insert(favorite);
        return true;
    }

    /**
     * 批量取消用户收藏菜单。
     * <p>
     * 执行步骤：
     * 1. 校验用户与租户上下文；
     * 2. 标准化并去重前端传入的路径列表；
     * 3. 按用户、租户和路径列表批量删除收藏记录。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param fullPaths 待取消收藏的菜单路径列表
     * @return 实际取消收藏数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchCancelUserFavoriteMenus(Long userId, Long tenantId, List<String> fullPaths) {
        // 1. 校验上下文参数。
        // 批量取消收藏必须明确当前用户与租户，缺少任一条件时直接返回 0。
        if (userId == null || tenantId == null || fullPaths == null || fullPaths.isEmpty()) {
            return 0;
        }

        // 2. 标准化并去重菜单路径。
        // 统一路径格式后再执行批量删除，避免同一路径因为 query 参数不同而漏删。
        List<String> normalizedPaths = fullPaths.stream()
                .map(this::normalizeReportedMenuPath)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (normalizedPaths.isEmpty()) {
            return 0;
        }

        // 3. 执行批量删除。
        // 删除条件同时限定 tenantId 和 userId，确保不会误删其他用户的收藏记录。
        return userMenuFavoriteMapper.delete(new LambdaQueryWrapper<SysUserMenuFavorite>()
                .eq(SysUserMenuFavorite::getTenantId, tenantId)
                .eq(SysUserMenuFavorite::getUserId, userId)
                .in(SysUserMenuFavorite::getMenuPath, normalizedPaths));
    }

    /**
     * 保存用户收藏菜单排序。
     * <p>
     * 执行步骤：
     * 1. 校验用户与租户上下文；
     * 2. 查询当前用户全部收藏记录；
     * 3. 按前端传入顺序重建排序号；
     * 4. 将未提交的旧收藏追加到末尾，避免遗漏；
     * 5. 逐条更新排序号。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param orderedPaths 前端提交的目标顺序路径列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortUserFavoriteMenus(Long userId, Long tenantId, List<String> orderedPaths) {
        // 1. 校验上下文参数。
        // 排序保存必须在当前用户和租户上下文下执行，参数不完整时直接忽略。
        if (userId == null || tenantId == null || orderedPaths == null || orderedPaths.isEmpty()) {
            return;
        }

        // 2. 查询当前用户全部收藏记录。
        // 先拿到现有收藏记录映射，后续会根据前端顺序逐条回写排序号。
        List<SysUserMenuFavorite> favorites = userMenuFavoriteMapper.selectList(new LambdaQueryWrapper<SysUserMenuFavorite>()
                .eq(SysUserMenuFavorite::getTenantId, tenantId)
                .eq(SysUserMenuFavorite::getUserId, userId)
                .orderByAsc(SysUserMenuFavorite::getOrderNum)
                .orderByDesc(SysUserMenuFavorite::getCreateTime));
        if (favorites.isEmpty()) {
            return;
        }

        // 3. 构建“路径 -> 收藏记录”映射。
        // 为按照前端传入的路径顺序快速定位收藏记录做准备。
        Map<String, SysUserMenuFavorite> favoriteMap = favorites.stream()
                .filter(item -> StringUtils.hasText(item.getMenuPath()))
                .collect(Collectors.toMap(SysUserMenuFavorite::getMenuPath, item -> item, (left, right) -> left, LinkedHashMap::new));

        // 4. 先按前端提交顺序组装目标列表。
        // 只接受当前用户真实存在的收藏路径，并自动去重，保证排序结果安全可控。
        List<SysUserMenuFavorite> orderedFavorites = orderedPaths.stream()
                .map(this::normalizeReportedMenuPath)
                .filter(StringUtils::hasText)
                .distinct()
                .map(favoriteMap::remove)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 5. 将前端未提交的旧收藏追加到末尾。
        // 这样即使前端只提交了部分数据，也不会把剩余收藏的排序号丢失。
        orderedFavorites.addAll(favoriteMap.values());

        // 6. 逐条回写排序号。
        // 使用连续自然数作为排序号，保证首页和管理页能按同一规则稳定展示。
        for (int index = 0; index < orderedFavorites.size(); index++) {
            SysUserMenuFavorite favorite = orderedFavorites.get(index);
            favorite.setOrderNum(index + 1);
            userMenuFavoriteMapper.updateById(favorite);
        }
    }

    /**
     * 上报用户菜单访问记录。
     * <p>
     * 执行步骤：
     * 1. 校验用户与租户上下文；
     * 2. 标准化前端上报路径；
     * 3. 校验路径是否属于当前用户授权菜单；
     * 4. 查询是否已有访问统计记录；
     * 5. 无记录时插入首条数据，有记录时累加访问次数并刷新最近访问时间。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @param fullPath 前端上报的菜单完整路径
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportUserMenuVisit(Long userId, Long tenantId, String fullPath) {
        // 1. 校验上下文参数。
        // 访问统计必须明确归属到当前用户和租户，缺失时直接忽略本次上报。
        if (userId == null || tenantId == null) {
            return;
        }

        // 2. 标准化菜单路径。
        // 去掉查询参数和多余空格，确保同一页面的访问次数能够累计到同一条记录上。
        String normalizedPath = normalizeReportedMenuPath(fullPath);
        if (!StringUtils.hasText(normalizedPath)) {
            return;
        }

        // 3. 校验菜单权限。
        // 只统计当前用户有权限访问的菜单，避免把非法路径写入常用菜单统计表。
        AuthorizedMenuSnapshot snapshot = getAuthorizedMenuSnapshots(userId, tenantId).get(normalizedPath);
        if (snapshot == null) {
            return;
        }

        // 4. 查询现有访问统计记录。
        // 使用用户 + 租户 + 菜单路径定位唯一记录，决定执行插入还是更新逻辑。
        LambdaQueryWrapper<SysUserMenuCommon> wrapper = new LambdaQueryWrapper<SysUserMenuCommon>()
                .eq(SysUserMenuCommon::getTenantId, tenantId)
                .eq(SysUserMenuCommon::getUserId, userId)
                .eq(SysUserMenuCommon::getMenuPath, normalizedPath)
                .last("limit 1");
        SysUserMenuCommon existing = userMenuCommonMapper.selectOne(wrapper);
        LocalDateTime now = LocalDateTime.now();

        // 5. 首次访问时插入新记录。
        // 为后续常用菜单排序准备初始访问次数和最近访问时间。
        if (existing == null) {
            SysUserMenuCommon commonMenu = new SysUserMenuCommon();
            commonMenu.setTenantId(tenantId);
            commonMenu.setUserId(userId);
            commonMenu.setMenuPath(snapshot.path);
            commonMenu.setMenuTitle(snapshot.title);
            commonMenu.setModuleCode(snapshot.moduleCode);
            commonMenu.setModuleName(snapshot.moduleName);
            commonMenu.setMenuIcon(snapshot.icon);
            commonMenu.setVisitCount(1);
            commonMenu.setLastVisitedAt(now);
            userMenuCommonMapper.insert(commonMenu);
            trimUserCommonMenus(userId, tenantId);
            return;
        }

        // 6. 已存在记录时更新统计。
        // 在保留最新菜单元信息的同时累加访问次数，并刷新最近访问时间。
        existing.setMenuTitle(snapshot.title);
        existing.setModuleCode(snapshot.moduleCode);
        existing.setModuleName(snapshot.moduleName);
        existing.setMenuIcon(snapshot.icon);
        existing.setVisitCount(Math.max(existing.getVisitCount() == null ? 0 : existing.getVisitCount(), 0) + 1);
        existing.setLastVisitedAt(now);
        userMenuCommonMapper.updateById(existing);

        // 6. 更新后执行自动裁剪。
        // 每次访问上报后都主动删除 Top 6 之外的旧低频记录，持续收口常用菜单表数据量。
        trimUserCommonMenus(userId, tenantId);
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
     * 解析安全的返回条数上限。
     * <p>
     * 规则说明：
     * 1. 当前端未传值时使用默认值；
     * 2. 当前端传入小于等于 0 的值时回退到默认值；
     * 3. 最终结果限制在 20 条以内，避免一次返回过多数据。
     * </p>
     *
     * @param limit 前端传入的条数上限
     * @param defaultLimit 默认条数
     * @return 安全的条数上限
     */
    private int resolveSafeLimit(Integer limit, int defaultLimit) {
        int resolved = limit == null ? defaultLimit : limit;
        if (resolved <= 0) {
            resolved = defaultLimit;
        }
        return Math.min(resolved, 20);
    }

    /**
     * 补齐用户收藏菜单排序号。
     * <p>
     * 旧数据可能尚未写入 `order_num`，该方法会在首次读取或写入收藏数据前完成一次顺序归一化。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     */
    private void ensureFavoriteOrderNumbers(Long userId, Long tenantId) {
        // 1. 查询当前用户全部收藏记录。
        // 使用当前顺序作为排序号初始化参考，避免迁移后顺序出现不可预期跳变。
        List<SysUserMenuFavorite> favorites = userMenuFavoriteMapper.selectList(new LambdaQueryWrapper<SysUserMenuFavorite>()
                .eq(SysUserMenuFavorite::getTenantId, tenantId)
                .eq(SysUserMenuFavorite::getUserId, userId)
                .orderByAsc(SysUserMenuFavorite::getOrderNum)
                .orderByDesc(SysUserMenuFavorite::getCreateTime)
                .orderByDesc(SysUserMenuFavorite::getId));
        if (favorites.isEmpty()) {
            return;
        }

        // 2. 检查是否存在空排序号。
        // 只有在发现历史记录缺少 order_num 时才执行回写，减少不必要的数据库更新。
        boolean needFix = favorites.stream().anyMatch(item -> item.getOrderNum() == null || item.getOrderNum() <= 0);
        if (!needFix) {
            return;
        }

        // 3. 逐条回写连续排序号。
        // 采用 1 开始的连续整数，保证首页和管理页能按同一顺序稳定展示收藏菜单。
        for (int index = 0; index < favorites.size(); index++) {
            SysUserMenuFavorite favorite = favorites.get(index);
            favorite.setOrderNum(index + 1);
            userMenuFavoriteMapper.updateById(favorite);
        }
    }

    /**
     * 计算下一个收藏排序号。
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @return 下一个可用排序号
     */
    private int resolveNextFavoriteOrderNum(Long userId, Long tenantId) {
        // 1. 查询当前用户收藏列表。
        // 使用当前最大排序号作为新收藏的末尾位置，保证新增收藏默认追加到列表最后。
        List<SysUserMenuFavorite> favorites = userMenuFavoriteMapper.selectList(new LambdaQueryWrapper<SysUserMenuFavorite>()
                .eq(SysUserMenuFavorite::getTenantId, tenantId)
                .eq(SysUserMenuFavorite::getUserId, userId)
                .orderByDesc(SysUserMenuFavorite::getOrderNum)
                .orderByDesc(SysUserMenuFavorite::getCreateTime)
                .last("limit 1"));
        if (favorites.isEmpty()) {
            return 1;
        }

        // 2. 基于最大排序号追加新值。
        // 如果历史记录排序号为空，则从 1 开始重新兜底。
        Integer currentMax = favorites.get(0).getOrderNum();
        return Math.max(currentMax == null ? 0 : currentMax, 0) + 1;
    }

    /**
     * 裁剪用户常用菜单数据，只保留固定 Top 6。
     * <p>
     * 裁剪规则：按访问次数倒序、最近访问时间倒序排序，超过 6 条的尾部记录会被物理删除。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     */
    private void trimUserCommonMenus(Long userId, Long tenantId) {
        // 1. 查询当前用户全部常用菜单记录。
        // 先按常用菜单最终展示规则排序，为后续裁剪尾部低频记录做准备。
        List<SysUserMenuCommon> commonMenus = userMenuCommonMapper.selectList(new LambdaQueryWrapper<SysUserMenuCommon>()
                .eq(SysUserMenuCommon::getTenantId, tenantId)
                .eq(SysUserMenuCommon::getUserId, userId)
                .orderByDesc(SysUserMenuCommon::getVisitCount)
                .orderByDesc(SysUserMenuCommon::getLastVisitedAt)
                .orderByDesc(SysUserMenuCommon::getId));
        if (commonMenus.size() <= DEFAULT_COMMON_MENU_LIMIT) {
            return;
        }

        // 2. 提取需要淘汰的记录 ID。
        // 从第 7 条开始的记录都属于旧低频尾部数据，将在下一步统一删除。
        List<Long> deleteIds = commonMenus.stream()
                .skip(DEFAULT_COMMON_MENU_LIMIT)
                .map(SysUserMenuCommon::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (deleteIds.isEmpty()) {
            return;
        }

        // 3. 执行批量删除。
        // 删除 Top 6 之外的数据，确保常用菜单表在用户维度上始终保持固定小规模。
        userMenuCommonMapper.deleteBatchIds(deleteIds);
    }

    /**
     * 查询用户收藏路径集合。
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @return 收藏菜单路径集合
     */
    private Set<String> getFavoritePathSet(Long userId, Long tenantId) {
        return userMenuFavoriteMapper.selectList(new LambdaQueryWrapper<SysUserMenuFavorite>()
                        .eq(SysUserMenuFavorite::getTenantId, tenantId)
                        .eq(SysUserMenuFavorite::getUserId, userId))
                .stream()
                .map(SysUserMenuFavorite::getMenuPath)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    /**
     * 查询当前用户的授权菜单快照。
     * <p>
     * 该方法会串联“用户 -> 角色 -> 菜单 -> 模块”链路，
     * 只保留当前租户下、当前用户有权访问且处于启用可见状态的菜单。
     * 最终结果以完整路径为 Key，便于收藏切换和访问上报时快速定位菜单。
     * </p>
     *
     * @param userId 当前用户 ID
     * @param tenantId 当前租户 ID
     * @return 授权菜单快照映射，Key 为菜单完整路径
     */
    private Map<String, AuthorizedMenuSnapshot> getAuthorizedMenuSnapshots(Long userId, Long tenantId) {
        // 1. 校验上下文参数。
        // 缺少用户或租户信息时无法建立授权链路，直接返回空映射。
        if (userId == null || tenantId == null) {
            return Collections.emptyMap();
        }

        // 2. 查询用户角色。
        // 使用用户与租户维度拿到当前上下文中的角色集合，为后续菜单授权查询做准备。
        List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getTenantId, tenantId));
        Set<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 3. 查询角色授权菜单。
        // 从角色-菜单关系表提取所有有权访问的菜单 ID。
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getTenantId, tenantId)
                .in(SysRoleMenu::getRoleId, roleIds));
        Set<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (menuIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 4. 查询有效菜单。
        // 仅保留当前租户下处于可见且启用状态的菜单，过滤按钮型菜单的原始数据供后续组装使用。
        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getTenantId, tenantId)
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getVisible, true)
                .eq(SysMenu::getStatus, true)
                .orderByAsc(SysMenu::getOrderNum));
        if (menus.isEmpty()) {
            return Collections.emptyMap();
        }

        // 5. 构建菜单 ID 映射。
        // 为后续递归拼装完整路径时快速回溯父菜单链路做准备。
        Map<Long, SysMenu> menuMap = menus.stream()
                .filter(menu -> menu.getId() != null)
                .collect(Collectors.toMap(SysMenu::getId, menu -> menu, (left, right) -> left));

        // 6. 查询关联模块信息。
        // 完整路径拼装与首页展示都需要模块编码和模块名称，因此这里统一查询模块表。
        Set<Long> moduleIds = menus.stream()
                .map(SysMenu::getModuleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, SysModule> moduleMap = moduleIds.isEmpty()
                ? Collections.emptyMap()
                : moduleMapper.selectList(new LambdaQueryWrapper<SysModule>()
                        .eq(SysModule::getTenantId, tenantId)
                        .in(SysModule::getId, moduleIds)
                        .eq(SysModule::getVisible, true)
                        .eq(SysModule::getStatus, true))
                .stream()
                .filter(module -> module.getId() != null)
                .collect(Collectors.toMap(SysModule::getId, module -> module, (left, right) -> left));

        // 7. 组装授权快照结果。
        // 只保留真正可导航的 menu 类型菜单，并以完整路径作为 Key 输出给调用方使用。
        Map<String, AuthorizedMenuSnapshot> result = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            if (menu == null || "button".equalsIgnoreCase(menu.getType()) || "catalog".equalsIgnoreCase(menu.getType())) {
                continue;
            }
            SysModule module = moduleMap.get(menu.getModuleId());
            if (module == null || !StringUtils.hasText(module.getCode())) {
                continue;
            }
            String fullPath = buildFullMenuPath(menu, menuMap, module.getCode());
            if (!StringUtils.hasText(fullPath)) {
                continue;
            }
            result.put(fullPath, new AuthorizedMenuSnapshot(
                    fullPath,
                    resolveI18nText(menu.getNameI18nJson(), menu.getName()),
                    module.getCode(),
                    resolveI18nText(module.getNameI18nJson(), module.getName()),
                    menu.getIcon()
            ));
        }
        return result;
    }

    /**
     * 构建菜单完整路径。
     *
     * @param menu 当前菜单
     * @param menuMap 菜单映射，用于回溯父级链路
     * @param moduleCode 模块编码
     * @return 菜单完整路径
     */
    private String buildFullMenuPath(SysMenu menu, Map<Long, SysMenu> menuMap, String moduleCode) {
        if (menu == null || !StringUtils.hasText(menu.getPath()) || !StringUtils.hasText(moduleCode)) {
            return "";
        }
        String rawPath = menu.getPath().trim();
        if (rawPath.startsWith("/")) {
            return rawPath;
        }

        List<String> segments = new ArrayList<>();
        collectMenuSegments(menu, menuMap, segments, new LinkedHashSet<>());
        if (segments.isEmpty()) {
            return "";
        }
        return ("/workspace/" + moduleCode + "/" + String.join("/", segments)).replaceAll("/+", "/");
    }

    /**
     * 递归收集菜单路径片段。
     *
     * @param menu 当前菜单
     * @param menuMap 菜单映射
     * @param segments 路径片段结果集
     * @param visited 已访问菜单 ID 集合，用于防止循环引用
     */
    private void collectMenuSegments(SysMenu menu, Map<Long, SysMenu> menuMap, List<String> segments, Set<Long> visited) {
        if (menu == null || menu.getId() == null || !visited.add(menu.getId())) {
            return;
        }
        SysMenu parent = menuMap.get(menu.getParentId());
        if (parent != null && !Objects.equals(parent.getId(), menu.getId())) {
            collectMenuSegments(parent, menuMap, segments, visited);
        }

        String rawPath = menu.getPath();
        if (!StringUtils.hasText(rawPath) || rawPath.startsWith("/")) {
            return;
        }
        Arrays.stream(rawPath.split("/"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .forEach(segments::add);
    }

    /**
     * 标准化前端上报的菜单路径。
     *
     * @param fullPath 前端上报的路径
     * @return 去掉查询参数后的标准路径
     */
    private String normalizeReportedMenuPath(String fullPath) {
        if (!StringUtils.hasText(fullPath)) {
            return "";
        }
        String normalized = fullPath.trim();
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        return normalized;
    }

    /**
     * 将授权菜单快照转换为个人首页菜单偏好对象。
     *
     * @param snapshot 授权菜单快照
     * @param visitCount 访问次数
     * @param lastVisitedAt 最近访问时间
     * @param favorite 是否已收藏
     * @return 用户菜单偏好视图对象
     */
    private UserMenuPreferenceVO toUserMenuPreferenceVO(AuthorizedMenuSnapshot snapshot,
                                                        Integer visitCount,
                                                        LocalDateTime lastVisitedAt,
                                                        Boolean favorite) {
        UserMenuPreferenceVO vo = new UserMenuPreferenceVO();
        vo.setPath(snapshot.path);
        vo.setTitle(snapshot.title);
        vo.setModuleCode(snapshot.moduleCode);
        vo.setModuleName(snapshot.moduleName);
        vo.setIcon(snapshot.icon);
        vo.setVisitCount(visitCount);
        vo.setLastVisitedAt(lastVisitedAt);
        vo.setFavorite(favorite);
        return vo;
    }

    /**
     * 授权菜单快照。
     * <p>
     * 该对象是个人菜单偏好链路中的中间数据结构，
     * 用于缓存菜单路径、标题、模块和图标等前端展示所需的只读信息。
     * </p>
     */
    private static final class AuthorizedMenuSnapshot {

        /**
         * 菜单完整路径。
         */
        private final String path;

        /**
         * 菜单标题。
         */
        private final String title;

        /**
         * 模块编码。
         */
        private final String moduleCode;

        /**
         * 模块名称。
         */
        private final String moduleName;

        /**
         * 菜单图标。
         */
        private final String icon;

        /**
         * 构造授权菜单快照。
         *
         * @param path 菜单完整路径
         * @param title 菜单标题
         * @param moduleCode 模块编码
         * @param moduleName 模块名称
         * @param icon 菜单图标
         */
        private AuthorizedMenuSnapshot(String path, String title, String moduleCode, String moduleName, String icon) {
            this.path = path;
            this.title = title;
            this.moduleCode = moduleCode;
            this.moduleName = moduleName;
            this.icon = icon;
        }
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
        String zhCnValue = obj.getStr("zh-CN");
        if (StringUtils.hasText(zhCnValue)) {
            return zhCnValue;
        }
        String zhValue = obj.getStr("zh");
        if (StringUtils.hasText(zhValue)) {
            return zhValue;
        }
        for (String fallbackLanguage : resolveFallbackLanguages()) {
            String value = obj.getStr(fallbackLanguage);
            if (StringUtils.hasText(value)) {
                return value;
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

    private List<String> resolveFallbackLanguages() {
        LinkedHashSet<String> fallbackLanguages = new LinkedHashSet<>();
        try {
            FxI18nLanguageType defaultLanguage = languageTypeService.getDefault();
            if (defaultLanguage != null && StringUtils.hasText(defaultLanguage.getLangCode())) {
                fallbackLanguages.add(defaultLanguage.getLangCode());
            }
            List<FxI18nLanguageType> enabledLanguages = languageTypeService.listEnabled();
            if (enabledLanguages != null) {
                enabledLanguages.stream()
                        .map(FxI18nLanguageType::getLangCode)
                        .filter(StringUtils::hasText)
                        .forEach(fallbackLanguages::add);
            }
        } catch (Exception ex) {
            log.warn("加载启用语言列表失败，菜单国际化将使用默认兜底", ex);
        }
        fallbackLanguages.add("zh-CN");
        fallbackLanguages.add("zh");
        fallbackLanguages.add("en-US");
        fallbackLanguages.add("en");
        return new ArrayList<>(fallbackLanguages);
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
                dto.setModuleName(resolveI18nText(module.getNameI18nJson(), module.getName()));
            }
        }

        if (menu.getParentId() != null && menu.getParentId() > 0) {
            SysMenu parentMenu = menuMapper.selectById(menu.getParentId());
            if (parentMenu != null) {
                dto.setParentName(resolveI18nText(parentMenu.getNameI18nJson(), parentMenu.getName()));
            }
        }

        return dto;
    }
}
