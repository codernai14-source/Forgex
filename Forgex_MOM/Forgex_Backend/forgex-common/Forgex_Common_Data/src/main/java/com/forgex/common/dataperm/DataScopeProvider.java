package com.forgex.common.dataperm;

/**
 * 数据权限范围提供者。
 * <p>
 * 由业务模块（通常是 Sys）实现，避免 Common_Data 通过反射访问 Mapper。
 * 未注册实现时拦截器保持失败关闭，不会放行未过滤结果。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DataPermissionInterceptor
 */
public interface DataScopeProvider {

    /**
     * 加载指定用户的数据权限范围与部门集合。
     *
     * @param userId 用户 ID
     * @return 数据权限信息，无法解析时返回 {@code null}
     */
    DataPermissionHelper.DataPermissionInfo load(Long userId);
}
