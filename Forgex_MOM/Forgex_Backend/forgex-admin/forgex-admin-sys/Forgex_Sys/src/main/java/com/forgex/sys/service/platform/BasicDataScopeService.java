package com.forgex.sys.service.platform;

import com.forgex.common.api.dto.SysDataScopeDTO;
import com.forgex.common.dataperm.DataPermissionHelper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 为基础资料服务提供已校验会话下的数据权限快照。 */
@Service
@RequiredArgsConstructor
public class BasicDataScopeService {
    private final BasicPlatformRequestGuard requestGuard;
    private final com.forgex.sys.dataperm.SysDataScopeProvider provider;

    /**
     * 加载当前登录用户权限，拒绝跨用户调用。
     * @param userId 请求用户 ID
     * @return 权限快照
     */
    public SysDataScopeDTO load(Long userId) {
        Long currentUserId = UserContext.get();
        Long tenantId = requestGuard.requireTenant();
        if (userId == null || !userId.equals(currentUserId)) {
            throw new I18nBusinessException(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
        }
        DataPermissionHelper.DataPermissionInfo info = provider.load(userId);
        if (info == null || info.getDataScope() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.DATA_ACCESS_ERROR);
        }
        if (!tenantId.equals(TenantContext.get())) {
            throw new I18nBusinessException(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
        }
        return new SysDataScopeDTO(tenantId, userId, info.getDataScope().getCode(), info.getDeptIds());
    }
}
