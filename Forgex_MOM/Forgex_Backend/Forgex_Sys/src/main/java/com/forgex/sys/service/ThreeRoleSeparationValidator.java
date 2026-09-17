package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.security.compliance.ComplianceContext;
import com.forgex.common.security.compliance.ComplianceLevel;
import com.forgex.common.security.perm.ThreeRoleSeparation;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.mapper.SysMenuMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 三员分立校验器。
 * <p>
 * 仅在 L3 生效：同一用户不得兼任两个保留角色族；审计员不得授予业务写权限，系统员不得授予审计查看权限。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ThreeRoleSeparationValidator {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    /**
     * 校验用户角色分配互斥。
     *
     * @param userId  用户 ID
     * @param roleIds 拟分配角色
     */
    public void validateUserRoles(Long userId, Set<Long> roleIds) {
        if (!ComplianceContext.atLeast(ComplianceLevel.L3) || roleIds == null || roleIds.isEmpty()) {
            return;
        }
        Set<String> families = new HashSet<>();
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds));
        for (SysRole role : roles) {
            if (ThreeRoleSeparation.isReserved(role.getRoleKey())) {
                families.add(ThreeRoleSeparation.family(role.getRoleKey()));
            }
        }
        if (families.size() > 1) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.THREE_ROLE_CONFLICT);
        }
        if (userId != null) {
            List<SysUserRole> existing = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId));
            for (SysUserRole bind : existing) {
                if (roleIds.contains(bind.getRoleId())) {
                    continue;
                }
                SysRole role = roleMapper.selectById(bind.getRoleId());
                if (role != null && ThreeRoleSeparation.isReserved(role.getRoleKey())) {
                    families.add(ThreeRoleSeparation.family(role.getRoleKey()));
                }
            }
            if (families.size() > 1) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.THREE_ROLE_CONFLICT);
            }
        }
    }

    /**
     * 校验角色菜单授权是否突破三员边界。
     *
     * @param roleId  角色 ID
     * @param menuIds 菜单 ID
     */
    public void validateRoleMenus(Long roleId, List<Long> menuIds) {
        if (!ComplianceContext.atLeast(ComplianceLevel.L3) || roleId == null || menuIds == null) {
            return;
        }
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || !ThreeRoleSeparation.isReserved(role.getRoleKey())) {
            return;
        }
        String family = ThreeRoleSeparation.family(role.getRoleKey());
        List<SysMenu> menus = menuIds.isEmpty() ? List.of() : menuMapper.selectBatchIds(menuIds);
        for (SysMenu menu : menus) {
            String perm = menu.getPermKey();
            if (ThreeRoleSeparation.AUDIT_ADMIN.equals(family) && perm != null && !ThreeRoleSeparation.isAuditPermission(perm)
                    && menu.getType() != null && !"M".equals(menu.getType())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.THREE_ROLE_AUDIT_WRITE_DENIED);
            }
            if (ThreeRoleSeparation.SYS_ADMIN.equals(family) && ThreeRoleSeparation.isAuditPermission(perm)) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.THREE_ROLE_SYS_AUDIT_DENIED);
            }
        }
    }

    /**
     * 判断当前用户是否可维护安全配置/密级。
     * <p>
     * 非 L3 不限制；L3 下仅安全管理员族可改。
     * </p>
     *
     * @return true 表示允许
     */
    public boolean currentUserIsSecAdmin() {
        if (!ComplianceContext.atLeast(ComplianceLevel.L3)) {
            return true;
        }
        Long userId = CurrentUserUtils.getUserId();
        if (userId == null) {
            return false;
        }
        List<SysUserRole> binds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        for (SysUserRole bind : binds) {
            SysRole role = roleMapper.selectById(bind.getRoleId());
            if (role != null && ThreeRoleSeparation.SEC_ADMIN.equals(ThreeRoleSeparation.family(role.getRoleKey()))) {
                return true;
            }
        }
        return false;
    }
}
