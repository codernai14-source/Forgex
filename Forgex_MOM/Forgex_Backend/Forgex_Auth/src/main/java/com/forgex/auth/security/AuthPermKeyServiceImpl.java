package com.forgex.auth.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.auth.domain.entity.SysMenuPerm;
import com.forgex.auth.domain.entity.SysRoleMenuPerm;
import com.forgex.auth.domain.entity.SysUserRolePerm;
import com.forgex.auth.mapper.SysMenuPermMapper;
import com.forgex.auth.mapper.SysRoleMenuPermMapper;
import com.forgex.auth.mapper.SysUserRolePermMapper;
import com.forgex.common.security.perm.PermKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Auth 模块 permKey 计算服务实现。
 * <p>
 * 计算口径与 Sys/其它业务模块保持一致：\n
 * {@code sys_user_role -> sys_role_menu -> sys_menu(type=button).perm_key}。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see PermKeyService
 */
@Service
@RequiredArgsConstructor
public class AuthPermKeyServiceImpl implements PermKeyService {

    private final SysUserRolePermMapper userRoleMapper;
    private final SysRoleMenuPermMapper roleMenuMapper;
    private final SysMenuPermMapper menuMapper;

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

        List<SysUserRolePerm> binds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRolePerm>()
                .eq(SysUserRolePerm::getUserId, userId)
                .eq(SysUserRolePerm::getTenantId, tenantId));

        List<Long> roleIds = binds.stream()
                .map(SysUserRolePerm::getRoleId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<SysRoleMenuPerm> roleMenus = roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenuPerm>()
                .in(SysRoleMenuPerm::getRoleId, roleIds)
                .eq(SysRoleMenuPerm::getTenantId, tenantId));

        List<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenuPerm::getMenuId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<SysMenuPerm> menus = menuMapper.selectList(new LambdaQueryWrapper<SysMenuPerm>()
                .in(SysMenuPerm::getId, menuIds)
                .eq(SysMenuPerm::getTenantId, tenantId)
                .eq(SysMenuPerm::getDeleted, false));

        return menus.stream()
                .filter(m -> "button".equalsIgnoreCase(m.getType()))
                .map(SysMenuPerm::getPermKey)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }
}

