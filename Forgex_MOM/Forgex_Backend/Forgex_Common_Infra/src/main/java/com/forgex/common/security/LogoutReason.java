package com.forgex.common.security;

/**
 * 登出原因枚举。
 * <p>
 * 用于在登录日志中区分登出类型，例如主动登出、超时、踢下线等。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
public enum LogoutReason {

    /**
     * 主动登出（用户点击退出）。
     */
    MANUAL,

    /**
     * Token 超时（会话过期）。
     */
    TIMEOUT,

    /**
     * 被踢下线（管理员/系统强制下线）。
     */
    KICKOUT,

    /**
     * 被顶下线（同账号再次登录导致替换）。
     */
    REPLACED,

    /**
     * 账号被封禁/禁用导致下线。
     */
    DISABLE,

    /**
     * 未知原因。
     */
    UNKNOWN
}

