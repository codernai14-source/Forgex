package com.forgex.sys.service.platform;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.LoginSessionKeys;
import com.forgex.common.security.LoginSessionSupport;
import com.forgex.common.security.perm.PermKeyService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * 基础资料内部 API 的身份边界：验证实际登录会话，并拒绝与会话不一致的透传头。
 */
@Component
@RequiredArgsConstructor
public class BasicPlatformRequestGuard {

    private final PermKeyService permKeyService;

    /**
     * 验证登录会话与租户、用户上下文。
     * @return 已验证的租户 ID
     */
    public Long requireTenant() {
        StpUtil.checkLogin();
        SaSession session = LoginSessionSupport.getCurrentSession();
        Long tenantId = session == null ? null : parseId(session.get(LoginSessionKeys.KEY_TENANT_ID));
        Long userId = session == null ? null : parseId(session.get(LoginSessionKeys.KEY_USER_ID));
        if (tenantId == null || userId == null || !Objects.equals(tenantId, TenantContext.get())
                || !Objects.equals(userId, UserContext.get())) {
            throw new I18nBusinessException(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
        }
        return tenantId;
    }

    /**
     * 验证人员同步权限及命令租户。
     * @param tenantId 命令租户
     * @return 已验证的租户 ID
     */
    public Long requireEmployeeSync(Long tenantId) {
        Long currentTenant = requireTenant();
        if (!Objects.equals(tenantId, currentTenant)
                || !permKeyService.hasAllPerms(UserContext.get(), currentTenant, Set.of("basic:employee:syncUser"))) {
            throw new I18nBusinessException(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
        }
        return currentTenant;
    }

    private Long parseId(Object value) {
        try {
            return value == null ? null : Long.valueOf(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
