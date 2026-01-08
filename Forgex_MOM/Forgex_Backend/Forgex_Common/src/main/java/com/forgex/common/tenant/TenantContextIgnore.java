package com.forgex.common.tenant;

/**
 * 方法级租户隔离忽略上下文
 * 通过 ThreadLocal 控制本次调用是否跳过租户隔离
 */
public final class TenantContextIgnore {
    private static final ThreadLocal<Boolean> HOLDER = new ThreadLocal<>();

    /** 设置是否忽略租户隔离 */
    public static void setIgnore(boolean ignore) {
        HOLDER.set(ignore);
    }

    /** 当前是否忽略租户隔离 */
    public static boolean isIgnore() {
        Boolean v = HOLDER.get();
        return v != null && v;
    }

    /** 清理状态 */
    public static void clear() {
        HOLDER.remove();
    }
}

