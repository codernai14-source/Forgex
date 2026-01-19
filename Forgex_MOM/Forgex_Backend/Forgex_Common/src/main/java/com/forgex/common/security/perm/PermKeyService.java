package com.forgex.common.security.perm;

import java.util.Collections;
import java.util.Set;

/**
 * 按钮权限键（permKey）计算服务。
 * <p>
 * 约定：权限键来自菜单按钮节点（{@code sys_menu.type=button}）的 {@code perm_key} 字段；
 * 授权关系来自：
 * {@code sys_user_role -> sys_role_menu -> sys_menu}。
 * </p>
 *
 * <p>
 * 该接口放置在 Common 模块，便于 Auth/Sys 以及后续业务模块（Mes/Scada/Qms 等）实现同一套权限口径，
 * 进而在各自模块中实现统一的接口鉴权拦截。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
public interface PermKeyService {

    /**
     * 获取用户在指定租户下拥有的按钮权限键集合。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 权限键集合（去重）
     */
    Set<String> getPermKeys(Long userId, Long tenantId);

    /**
     * 判断用户是否拥有全部所需权限键。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param required 所需权限键集合
     * @return 是否满足全部
     */
    default boolean hasAllPerms(Long userId, Long tenantId, Set<String> required) {
        if (required == null || required.isEmpty()) {
            return true;
        }
        Set<String> owned = getPermKeys(userId, tenantId);
        if (owned == null) {
            owned = Collections.emptySet();
        }
        return owned.containsAll(required);
    }
}

