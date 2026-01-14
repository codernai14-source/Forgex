package com.forgex.common.util;

import cn.dev33.satoken.stp.StpUtil;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import org.springframework.util.StringUtils;

/**
 * 当前登录人工具类。
 * <p>
 * 优先从线程上下文 {@link UserContext}/{@link TenantContext} 读取（网关透传 Header 场景），\n
 * 若上下文为空，则尝试从 Sa-Token Session 中读取（Auth 场景）。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
public class CurrentUserUtils {

    private static final String KEY_USER_ID = "LOGIN_USER_ID";
    private static final String KEY_TENANT_ID = "LOGIN_TENANT_ID";
    private static final String KEY_ACCOUNT = "LOGIN_ACCOUNT";

    private CurrentUserUtils() {
    }

    /**
     * 获取当前用户ID。
     *
     * @return 用户ID（无返回 null）
     */
    public static Long getUserId() {
        Long uid = UserContext.get();
        if (uid != null) {
            return uid;
        }
        try {
            Object v = StpUtil.getSession(false).get(KEY_USER_ID);
            return parseLong(v);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 获取当前租户ID。
     *
     * @return 租户ID（无返回 null）
     */
    public static Long getTenantId() {
        Long tid = TenantContext.get();
        if (tid != null) {
            return tid;
        }
        try {
            Object v = StpUtil.getSession(false).get(KEY_TENANT_ID);
            return parseLong(v);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 获取当前账号。
     *
     * @return 账号（无返回 null）
     */
    public static String getAccount() {
        try {
            Object v = StpUtil.getSession(false).get(KEY_ACCOUNT);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.valueOf((String) obj);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}

