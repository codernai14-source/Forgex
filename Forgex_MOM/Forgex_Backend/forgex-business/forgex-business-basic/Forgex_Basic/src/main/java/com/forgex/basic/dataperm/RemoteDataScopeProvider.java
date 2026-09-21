package com.forgex.basic.dataperm;

import com.forgex.common.api.dto.SysDataScopeDTO;
import com.forgex.common.api.feign.SysBasicSupportFeignClient;
import com.forgex.common.dataperm.DataPermissionHelper;
import com.forgex.common.dataperm.DataScope;
import com.forgex.common.dataperm.DataScopeProvider;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/** 基础资料服务的数据权限适配器，权限计算由 Sys 服务完成。 */
@Component
@RequiredArgsConstructor
public class RemoteDataScopeProvider implements DataScopeProvider {
    private final SysBasicSupportFeignClient client;

    /**
     * 从 Sys 服务读取权限并校验用户、租户边界。
     * @param userId 用户 ID
     * @return 权限信息
     */
    @Override
    public DataPermissionHelper.DataPermissionInfo load(Long userId) {
        Long currentUserId = UserContext.get();
        Long currentTenantId = TenantContext.get();
        if (userId == null || !userId.equals(currentUserId) || currentTenantId == null) {
            throw new IllegalStateException("Invalid data permission context");
        }
        R<SysDataScopeDTO> response = client.dataScope(userId);
        SysDataScopeDTO dto = response == null ? null : response.getData();
        if (response == null || response.getCode() == null || response.getCode() != 200 || dto == null
                || !currentTenantId.equals(dto.tenantId()) || !userId.equals(dto.userId())) {
            throw new IllegalStateException("Unable to resolve data permission");
        }
        DataScope scope = DataScope.fromCode(dto.dataScope());
        if (!scope.getCode().equals(dto.dataScope())) {
            throw new IllegalStateException("Unknown data permission scope");
        }
        Set<Long> deptIds = dto.deptIds() == null ? Set.of() : Set.copyOf(dto.deptIds());
        DataPermissionHelper.cachePermission(userId, scope, deptIds);
        return new DataPermissionHelper.DataPermissionInfo(scope, deptIds, userId);
    }
}
