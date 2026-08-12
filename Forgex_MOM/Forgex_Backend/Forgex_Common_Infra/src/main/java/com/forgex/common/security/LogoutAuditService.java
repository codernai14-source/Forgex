package com.forgex.common.security;

/**
 * 登出审计服务。
 * <p>
 * 该接口用于在不同模块中实现“按 token 回写登出时间/原因”的统一能力，
 * 以便在全局异常处理器等位置进行解耦调用。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
public interface LogoutAuditService {

    /**
     * 按 token 回写登出信息。
     *
     * @param tokenValue   tokenValue
     * @param logoutReason 登出原因
     * @return 是否更新成功
     */
    boolean recordLogoutByToken(String tokenValue, LogoutReason logoutReason);
}

