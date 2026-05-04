package com.forgex.common.util;

import cn.dev33.satoken.session.SaSession;
import com.forgex.common.security.LoginSessionKeys;
import com.forgex.common.security.LoginSessionSupport;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import org.springframework.util.StringUtils;

/**
 * 当前登录人工具类。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public class CurrentUserUtils {

    private CurrentUserUtils() {
    }

    /**
     * 获取用户ID。
     *
     * @return 数据主键 ID
     */
    public static Long getUserId() {
        Long uid = UserContext.get();
        if (uid != null) {
            return uid;
        }
        SaSession session = LoginSessionSupport.getCurrentSession();
        return session == null ? null : parseLong(session.get(LoginSessionKeys.KEY_USER_ID));
    }

    /**
     * 获取租户ID。
     *
     * @return 数据主键 ID
     */
    public static Long getTenantId() {
        Long tid = TenantContext.get();
        if (tid != null) {
            return tid;
        }
        SaSession session = LoginSessionSupport.getCurrentSession();
        return session == null ? null : parseLong(session.get(LoginSessionKeys.KEY_TENANT_ID));
    }

    /**
     * 获取账号。
     *
     * @return 字符串结果
     */
    public static String getAccount() {
        SaSession session = LoginSessionSupport.getCurrentSession();
        if (session == null) {
            return null;
        }
        Object value = session.get(LoginSessionKeys.KEY_ACCOUNT);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return StringUtils.hasText(text) ? text : null;
    }

    private static Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number number) {
            return number.longValue();
        }
        if (obj instanceof String text) {
            try {
                return Long.valueOf(text);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
