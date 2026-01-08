package com.forgex.common.tenant;

/**
 * 租户上下文
 * 使用 ThreadLocal 保存当前请求线程的租户ID，实现数据隔离
 */
public class TenantContext {
    /** 当前线程持有的租户ID */
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    /**
     * 设置当前租户ID
     * @param tenantId 租户ID
     */
    public static void set(Long tenantId) {
        HOLDER.set(tenantId);
    }

    /**
     * 获取当前租户ID
     * @return 当前线程保存的租户ID
     */
    public static Long get() {
        return HOLDER.get();
    }

    /**
     * 清理当前租户ID
     * @see TenantContext#set(Long)
     * @see TenantContext#get()
     */
    public static void clear() {
        HOLDER.remove();
    }
}

