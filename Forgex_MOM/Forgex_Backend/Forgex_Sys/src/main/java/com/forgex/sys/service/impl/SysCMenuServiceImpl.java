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

package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.tenant.TenantContextIgnore;
import com.forgex.sys.domain.dto.SysCMenuDTO;
import com.forgex.sys.domain.dto.SysCMenuQueryDTO;
import com.forgex.sys.domain.entity.SysCMenu;
import com.forgex.sys.domain.entity.SysRoleCMenu;
import com.forgex.sys.domain.entity.SysUserCMenuFavorite;
import com.forgex.sys.domain.vo.CMenuTreeVO;
import com.forgex.sys.mapper.SysCMenuMapper;
import com.forgex.sys.mapper.SysRoleCMenuMapper;
import com.forgex.sys.mapper.SysUserCMenuFavoriteMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.service.ISysCMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * C 端菜单服务实现
 *
 * @author Forgex Team
 * @since 2026-04-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysCMenuServiceImpl extends ServiceImpl<SysCMenuMapper, SysCMenu> implements ISysCMenuService {

    private final SysRoleCMenuMapper roleCMenuMapper;
    private final SysUserCMenuFavoriteMapper favoriteMapper;
    private final SysUserRoleMapper userRoleMapper;

    // ======================== CRUD ========================

    @Override
    public IPage<SysCMenuDTO> pageMenus(Page<SysCMenu> page, SysCMenuQueryDTO query) {
        LambdaQueryWrapper<SysCMenu> wrapper = buildQueryWrapper(query);
        wrapper.orderByAsc(SysCMenu::getOrderNum);
        IPage<SysCMenu> result = this.page(page, wrapper);
        return result.convert(this::toDTO);
    }

    @Override
    public List<SysCMenuDTO> listMenus(SysCMenuQueryDTO query) {
        LambdaQueryWrapper<SysCMenu> wrapper = buildQueryWrapper(query);
        wrapper.orderByAsc(SysCMenu::getOrderNum);
        return this.list(wrapper).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CMenuTreeVO> getMenuTree(Long tenantId, Long moduleId) {
        LambdaQueryWrapper<SysCMenu> wrapper = new LambdaQueryWrapper<>();
        if (moduleId != null) {
            wrapper.eq(SysCMenu::getModuleId, moduleId);
        }
        applyMenuTenantScope(wrapper, tenantId);
        wrapper.eq(SysCMenu::getStatus, true)
               .orderByAsc(SysCMenu::getOrderNum);
        List<SysCMenu> menus = executeIgnoringTenant(() -> this.list(wrapper));
        return buildTree(menus, null);
    }

    @Override
    public SysCMenuDTO getMenuById(Long id) {
        SysCMenu menu = this.getById(id);
        return menu != null ? toDTO(menu) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(SysCMenuDTO dto) {
        SysCMenu entity = toEntity(dto);
        this.save(entity);
        log.info("新增 C 端菜单: {}", entity.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(SysCMenuDTO dto) {
        SysCMenu entity = toEntity(dto);
        entity.setId(dto.getId());
        this.updateById(entity);
        log.info("更新 C 端菜单: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        // 同时删除子菜单
        LambdaQueryWrapper<SysCMenu> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(SysCMenu::getParentId, id);
        this.remove(childWrapper);
        this.removeById(id);
        log.info("删除 C 端菜单: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteMenus(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) return;
        for (Long id : ids) {
            deleteMenu(id);
        }
    }

    // ======================== 角色授权 ========================

    @Override
    public List<Long> getRoleCMenuIds(Long tenantId, Long roleId) {
        LambdaQueryWrapper<SysRoleCMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleCMenu::getRoleId, roleId);
        applyRoleCMenuTenantScope(wrapper, tenantId);
        return executeIgnoringTenant(() -> roleCMenuMapper.selectList(wrapper).stream()
                .map(SysRoleCMenu::getCMenuId)
                .collect(Collectors.toList()));
    }

    @Override
    public List<CMenuTreeVO> getAuthMenuTree(Long tenantId, Long moduleId, Long roleId) {
        List<CMenuTreeVO> tree = getMenuTree(tenantId, moduleId);
        Set<Long> grantedIds = new HashSet<>(getRoleCMenuIds(tenantId, roleId));
        markChecked(tree, grantedIds);
        return tree;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantRoleCMenus(Long tenantId, Long roleId, List<Long> menuIds) {
        // 1. 删除旧关联
        LambdaQueryWrapper<SysRoleCMenu> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(SysRoleCMenu::getRoleId, roleId);
        applyRoleCMenuTenantScope(delWrapper, tenantId);
        runIgnoringTenant(() -> roleCMenuMapper.delete(delWrapper));

        // 2. 批量插入新关联
        if (!CollectionUtils.isEmpty(menuIds)) {
            runIgnoringTenant(() -> {
                for (Long menuId : menuIds) {
                    SysRoleCMenu rm = new SysRoleCMenu();
                    rm.setTenantId(tenantId);
                    rm.setRoleId(roleId);
                    rm.setCMenuId(menuId);
                    roleCMenuMapper.insert(rm);
                }
            });
        }
        log.info("角色 [{}] C 端菜单授权完成, 菜单数: {}", roleId, menuIds != null ? menuIds.size() : 0);
    }

    // ======================== 工作台 / 收藏 ========================

    @Override
    public List<CMenuTreeVO> getWorkbenchModules(Long userId, Long tenantId) {
        // 查询用户有权的所有顶级 C 端菜单（模块级别）
        List<Long> menuIds = getUserCMenuIds(userId, tenantId);
        if (CollectionUtils.isEmpty(menuIds)) return Collections.emptyList();

        LambdaQueryWrapper<SysCMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysCMenu::getId, menuIds)
               .eq(SysCMenu::getParentId, 0)
               .eq(SysCMenu::getVisible, true)
               .eq(SysCMenu::getStatus, true)
               .orderByAsc(SysCMenu::getOrderNum);
        applyMenuTenantScope(wrapper, tenantId);
        List<SysCMenu> modules = executeIgnoringTenant(() -> this.list(wrapper));
        return modules.stream().map(this::toTreeVO).collect(Collectors.toList());
    }

    @Override
    public List<CMenuTreeVO> getWorkbenchMenus(Long userId, Long tenantId, Long moduleId) {
        List<Long> menuIds = getUserCMenuIds(userId, tenantId);
        if (CollectionUtils.isEmpty(menuIds)) return Collections.emptyList();

        LambdaQueryWrapper<SysCMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysCMenu::getId, menuIds)
               .eq(SysCMenu::getVisible, true)
               .eq(SysCMenu::getStatus, true);
        applyMenuTenantScope(wrapper, tenantId);
        if (moduleId != null) {
            wrapper.and(w -> w.eq(SysCMenu::getParentId, moduleId)
                              .or().eq(SysCMenu::getModuleId, moduleId));
        }
        wrapper.orderByAsc(SysCMenu::getOrderNum);
        List<SysCMenu> menus = executeIgnoringTenant(() -> this.list(wrapper));
        return buildTree(menus, null);
    }

    @Override
    public List<CMenuTreeVO> getUserFavorites(Long userId, Long tenantId) {
        LambdaQueryWrapper<SysUserCMenuFavorite> fWrapper = new LambdaQueryWrapper<>();
        fWrapper.eq(SysUserCMenuFavorite::getUserId, userId);
        List<Long> favMenuIds = favoriteMapper.selectList(fWrapper).stream()
                .map(SysUserCMenuFavorite::getCMenuId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(favMenuIds)) return Collections.emptyList();

        LambdaQueryWrapper<SysCMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysCMenu::getId, favMenuIds)
               .eq(SysCMenu::getStatus, true)
               .orderByAsc(SysCMenu::getOrderNum);
        applyMenuTenantScope(wrapper, tenantId);
        return executeIgnoringTenant(() -> this.list(wrapper).stream().map(this::toTreeVO).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleFavorite(Long userId, Long tenantId, Long cMenuId) {
        LambdaQueryWrapper<SysUserCMenuFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserCMenuFavorite::getUserId, userId)
               .eq(SysUserCMenuFavorite::getCMenuId, cMenuId);
        SysUserCMenuFavorite existing = favoriteMapper.selectOne(wrapper);
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            log.info("用户 [{}] 取消收藏 C 端菜单 [{}]", userId, cMenuId);
            return false; // 已取消收藏
        } else {
            SysUserCMenuFavorite fav = new SysUserCMenuFavorite();
            fav.setUserId(userId);
            fav.setCMenuId(cMenuId);
            fav.setTenantId(tenantId);
            favoriteMapper.insert(fav);
            log.info("用户 [{}] 收藏 C 端菜单 [{}]", userId, cMenuId);
            return true; // 已收藏
        }
    }

    // ======================== 私有方法 ========================

    private List<Long> getUserCMenuIds(Long userId, Long tenantId) {
        // 1. 查询用户角色
        List<Long> roleIds = getUserRoleIds(userId, tenantId);
        if (CollectionUtils.isEmpty(roleIds)) return Collections.emptyList();

        // 2. 查询角色关联的 C 端菜单 ID
        LambdaQueryWrapper<SysRoleCMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysRoleCMenu::getRoleId, roleIds);
        applyRoleCMenuTenantScope(wrapper, tenantId);
        return executeIgnoringTenant(() -> roleCMenuMapper.selectList(wrapper).stream()
                .map(SysRoleCMenu::getCMenuId)
                .distinct()
                .collect(Collectors.toList()));
    }

    private List<Long> getUserRoleIds(Long userId, Long tenantId) {
        // 通过 sys_user_role 表查询
        try {
            LambdaQueryWrapper<com.forgex.sys.domain.entity.SysUserRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(com.forgex.sys.domain.entity.SysUserRole::getUserId, userId);
            applyUserRoleTenantScope(wrapper, tenantId);
            return executeIgnoringTenant(() -> userRoleMapper.selectList(wrapper).stream()
                    .map(com.forgex.sys.domain.entity.SysUserRole::getRoleId)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("查询用户角色失败: userId={}, error={}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private <T> T executeIgnoringTenant(Supplier<T> supplier) {
        boolean oldIgnore = TenantContextIgnore.isIgnore();
        TenantContextIgnore.setIgnore(true);
        try {
            return supplier.get();
        } finally {
            if (!oldIgnore) {
                TenantContextIgnore.clear();
            }
        }
    }

    private void runIgnoringTenant(Runnable runnable) {
        boolean oldIgnore = TenantContextIgnore.isIgnore();
        TenantContextIgnore.setIgnore(true);
        try {
            runnable.run();
        } finally {
            if (!oldIgnore) {
                TenantContextIgnore.clear();
            }
        }
    }

    private void applyMenuTenantScope(LambdaQueryWrapper<SysCMenu> wrapper, Long tenantId) {
        if (tenantId == null) {
            wrapper.isNull(SysCMenu::getTenantId);
            return;
        }
        wrapper.and(w -> w.isNull(SysCMenu::getTenantId).or().eq(SysCMenu::getTenantId, tenantId));
    }

    private void applyRoleCMenuTenantScope(LambdaQueryWrapper<SysRoleCMenu> wrapper, Long tenantId) {
        if (tenantId == null) {
            wrapper.isNull(SysRoleCMenu::getTenantId);
            return;
        }
        wrapper.and(w -> w.isNull(SysRoleCMenu::getTenantId).or().eq(SysRoleCMenu::getTenantId, tenantId));
    }

    private void applyUserRoleTenantScope(
            LambdaQueryWrapper<com.forgex.sys.domain.entity.SysUserRole> wrapper,
            Long tenantId
    ) {
        if (tenantId == null) {
            wrapper.isNull(com.forgex.sys.domain.entity.SysUserRole::getTenantId);
            return;
        }
        wrapper.and(w -> w.isNull(com.forgex.sys.domain.entity.SysUserRole::getTenantId)
                .or().eq(com.forgex.sys.domain.entity.SysUserRole::getTenantId, tenantId));
    }

    private LambdaQueryWrapper<SysCMenu> buildQueryWrapper(SysCMenuQueryDTO query) {
        LambdaQueryWrapper<SysCMenu> wrapper = new LambdaQueryWrapper<>();
        if (query.getModuleId() != null) {
            wrapper.eq(SysCMenu::getModuleId, query.getModuleId());
        }
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(SysCMenu::getName, query.getName());
        }
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(SysCMenu::getType, query.getType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysCMenu::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getDeviceType())) {
            wrapper.eq(SysCMenu::getDeviceType, query.getDeviceType());
        }
        return wrapper;
    }

    private List<CMenuTreeVO> buildTree(List<SysCMenu> menus, Set<Long> checkedIds) {
        List<CMenuTreeVO> voList = menus.stream().map(this::toTreeVO).collect(Collectors.toList());
        Map<Long, List<CMenuTreeVO>> parentMap = voList.stream()
                .collect(Collectors.groupingBy(v -> v.getParentId() != null ? v.getParentId() : 0L));

        List<CMenuTreeVO> roots = new ArrayList<>();
        for (CMenuTreeVO vo : voList) {
            vo.setChildren(parentMap.getOrDefault(vo.getId(), Collections.emptyList()));
            if (checkedIds != null) {
                vo.setChecked(checkedIds.contains(vo.getId()));
            }
            if (vo.getParentId() == null || vo.getParentId() == 0L) {
                roots.add(vo);
            }
        }
        return roots;
    }

    private void markChecked(List<CMenuTreeVO> tree, Set<Long> grantedIds) {
        if (tree == null) return;
        for (CMenuTreeVO node : tree) {
            node.setChecked(grantedIds.contains(node.getId()));
            markChecked(node.getChildren(), grantedIds);
        }
    }

    private SysCMenuDTO toDTO(SysCMenu entity) {
        SysCMenuDTO dto = new SysCMenuDTO();
        dto.setId(entity.getId());
        dto.setModuleId(entity.getModuleId());
        dto.setParentId(entity.getParentId());
        dto.setType(entity.getType());
        dto.setMenuLevel(entity.getMenuLevel());
        dto.setPath(entity.getPath());
        dto.setName(entity.getName());
        dto.setNameI18nJson(entity.getNameI18nJson());
        dto.setIcon(entity.getIcon());
        dto.setComponentKey(entity.getComponentKey());
        dto.setPermKey(entity.getPermKey());
        dto.setMenuMode(entity.getMenuMode());
        dto.setExternalUrl(entity.getExternalUrl());
        dto.setOrderNum(entity.getOrderNum());
        dto.setVisible(entity.getVisible());
        dto.setStatus(entity.getStatus());
        dto.setTenantType(entity.getTenantType());
        dto.setDeviceType(entity.getDeviceType());
        dto.setTenantId(entity.getTenantId());
        dto.setCreateTime(entity.getCreateTime());
        dto.setCreateBy(entity.getCreateBy());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.setUpdateBy(entity.getUpdateBy());
        return dto;
    }

    private SysCMenu toEntity(SysCMenuDTO dto) {
        SysCMenu entity = new SysCMenu();
        entity.setModuleId(dto.getModuleId());
        entity.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        entity.setType(dto.getType());
        entity.setMenuLevel(dto.getMenuLevel());
        entity.setPath(dto.getPath());
        entity.setName(dto.getName());
        entity.setNameI18nJson(dto.getNameI18nJson());
        entity.setIcon(dto.getIcon());
        entity.setComponentKey(dto.getComponentKey());
        entity.setPermKey(dto.getPermKey());
        entity.setMenuMode(dto.getMenuMode());
        entity.setExternalUrl(dto.getExternalUrl());
        entity.setOrderNum(dto.getOrderNum());
        entity.setVisible(dto.getVisible());
        entity.setStatus(dto.getStatus());
        entity.setTenantType(dto.getTenantType());
        entity.setDeviceType(dto.getDeviceType());
        entity.setTenantId(dto.getTenantId());
        return entity;
    }

    private CMenuTreeVO toTreeVO(SysCMenu menu) {
        CMenuTreeVO vo = new CMenuTreeVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setModuleId(menu.getModuleId());
        vo.setName(menu.getName());
        vo.setNameI18nJson(menu.getNameI18nJson());
        vo.setPath(menu.getPath());
        vo.setIcon(menu.getIcon());
        vo.setComponentKey(menu.getComponentKey());
        vo.setType(menu.getType());
        vo.setMenuLevel(menu.getMenuLevel());
        vo.setOrderNum(menu.getOrderNum());
        vo.setMenuMode(menu.getMenuMode());
        vo.setExternalUrl(menu.getExternalUrl());
        vo.setPermKey(menu.getPermKey());
        vo.setVisible(menu.getVisible());
        vo.setStatus(menu.getStatus());
        vo.setDeviceType(menu.getDeviceType());
        vo.setCreateTime(menu.getCreateTime());
        vo.setCreateBy(menu.getCreateBy());
        return vo;
    }
}

