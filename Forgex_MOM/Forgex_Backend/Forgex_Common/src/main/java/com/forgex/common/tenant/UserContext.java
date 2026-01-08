package com.forgex.common.tenant;

/**
 * 用户上下文。
 * <p>
 * 使用 ThreadLocal 保存当前请求线程的用户ID，便于在业务代码中随时获取当前登录用户，
 * 配合 {@link TenantContext} 一起完成多租户环境下的用户与租户双重隔离。
 *
 * <p>使用约定：由 Web 拦截器在请求进入时设置用户ID，在请求完成后统一清理。</p>
 *
 * @author Forgex
 * @version 1.0.0
 * @see TenantContext
 */
public final class UserContext {

    /**
     * 当前线程持有的用户ID。
     */
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 设置当前用户ID。
     *
     * @param userId 用户ID
     */
    public static void set(Long userId) {
        HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID。
     *
     * @return 当前线程保存的用户ID，可能为 null
     */
    public static Long get() {
        return HOLDER.get();
    }

    /**
     * 清理当前用户ID。
     * <p>
     * 在请求完成后调用，防止线程复用导致的用户上下文泄漏。
     */
    public static void clear() {
        HOLDER.remove();
    }
}

