package com.forgex.common.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.util.StringUtils;

/**
 * 登录会话读取工具，优先读取 token 作用域会话，兼容旧的 loginId 作用域会话。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public final class LoginSessionSupport {

    private LoginSessionSupport() {
    }

    /**
     * 获取当前会话。
     *
     * @return 处理结果
     */
    public static SaSession getCurrentSession() {
        SaSession tokenSession = getCurrentTokenSession();
        if (tokenSession != null) {
            return tokenSession;
        }
        try {
            return StpUtil.getSession(false);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 获取当前token会话。
     *
     * @return 处理结果
     */
    public static SaSession getCurrentTokenSession() {
        try {
            return StpUtil.getTokenSession();
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 获取会话bytoken。
     *
     * @param token token
     * @return 处理结果
     */
    public static SaSession getSessionByToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return StpUtil.getTokenSessionByToken(token);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 获取会话byloginID。
     *
     * @param loginId login ID
     * @param token token
     * @return 处理结果
     */
    public static SaSession getSessionByLoginId(Object loginId, String token) {
        SaSession tokenSession = getSessionByToken(token);
        if (tokenSession != null) {
            return tokenSession;
        }
        if (loginId == null) {
            return null;
        }
        try {
            return StpUtil.getSessionByLoginId(loginId, false);
        } catch (Exception ignored) {
            return null;
        }
    }
}
