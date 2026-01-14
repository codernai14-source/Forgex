package com.forgex.sys.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.security.perm.PermKeyService;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.entity.SysRoleMenu;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.mapper.SysMenuMapper;
import com.forgex.sys.mapper.SysRoleMenuMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sys 模块 permKey 计算服务实现。
 * <p>
 * 计算口径与 Auth/其它业务模块保持一致：\n
 * {@code sys_user_role -> sys_role_menu -> sys_menu(type=button).perm_key}。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see PermKeyService
 */
@Service
@RequiredArgsConstructor
public class SysPermKeyServiceImpl implements PermKeyService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    /**
     * 获取用户在指定租户下拥有的按钮权限键集合。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 权限键集合（去重）
     */
    @Override
    public Set<String> getPermKeys(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return Collections.emptySet();
        }

        List<SysUserRole> binds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getTenantId, tenantId));

        List<Long> roleIds = binds.stream()
                .map(SysUserRole::getRoleId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, roleIds)
                .eq(SysRoleMenu::getTenantId, tenantId));

        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<SysMenu> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getTenantId, tenantId)
                .eq(SysMenu::getDeleted, false));

        return menus.stream()
                .filter(m -> "button".equalsIgnoreCase(m.getType()))
                .map(SysMenu::getPermKey)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
}

