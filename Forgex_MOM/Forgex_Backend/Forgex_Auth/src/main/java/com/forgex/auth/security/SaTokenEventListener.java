package com.forgex.auth.security;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.forgex.common.security.LogoutAuditService;
import com.forgex.common.security.LogoutReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 事件监听器（Auth 模块）。
 * <p>
 * 用于更严格地区分登出原因：
 * - 被踢下线（KICKOUT）
 * - 被顶下线（REPLACED）
 * - 被封禁导致下线（DISABLE）
 * </p>
 *
 * <p>
 * 注意：Token 超时无法在过期瞬间回调（通常在下一次访问时抛出未登录异常），
 * 本项目通过全局异常处理器触发 {@link LogoutAuditService} 回写 TIMEOUT。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-08
 * @see com.forgex.common.web.GlobalExceptionHandler
 * @see SaTokenListener
 */
@Component
@RequiredArgsConstructor
public class SaTokenEventListener implements SaTokenListener {

    /**
     * 登出审计服务，用于记录用户登出日志。
     */
    private final LogoutAuditService logoutAuditService;

    /**
     * 用户登录时触发。
     * <p>
     * 当前实现为空，预留用于处理登录成功后的业务逻辑。
     * </p>
     *
     * @param s                 登录类型
     * @param o                 登录 ID
     * @param s1                Token 值
     * @param saLoginParameter  登录参数
     * @see SaTokenListener#doLogin(String, Object, String, SaLoginParameter)
     */
    @Override
    public void doLogin(String s, Object o, String s1, SaLoginParameter saLoginParameter) {
        // 预留登录回调逻辑
    }

    /**
     * 用户主动登出时触发。
     * <p>
     * 记录用户主动登出的日志，登出原因标记为 MANUAL。
     * </p>
     *
     * @param loginType  登录类型
     * @param loginId    登录 ID
     * @param tokenValue Token 值
     * @see LogoutAuditService#recordLogoutByToken(String, LogoutReason)
     */
    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        logoutAuditService.recordLogoutByToken(tokenValue, LogoutReason.MANUAL);
    }

    /**
     * 用户被踢下线时触发。
     * <p>
     * 记录用户被强制踢下线的日志，登出原因标记为 KICKOUT。
     * </p>
     *
     * @param loginType  登录类型
     * @param loginId    登录 ID
     * @param tokenValue Token 值
     * @see LogoutAuditService#recordLogoutByToken(String, LogoutReason)
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        logoutAuditService.recordLogoutByToken(tokenValue, LogoutReason.KICKOUT);
    }

    /**
     * 用户被顶下线时触发。
     * <p>
     * 记录用户被其他登录顶替下线的日志，登出原因标记为 REPLACED。
     * </p>
     *
     * @param loginType  登录类型
     * @param loginId    登录 ID
     * @param tokenValue Token 值
     * @see LogoutAuditService#recordLogoutByToken(String, LogoutReason)
     */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        logoutAuditService.recordLogoutByToken(tokenValue, LogoutReason.REPLACED);
    }

    /**
     * 用户被封禁时触发。
     * <p>
     * 当前实现为空，预留用于处理封禁事件。
     * </p>
     *
     * @param loginType   登录类型
     * @param loginId     登录 ID
     * @param service     服务名
     * @param level       封禁级别
     * @param disableTime 封禁时长（毫秒）
     */
    @Override
    public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
        // 预留封禁回调逻辑
    }

    /**
     * 用户被解除封禁时触发。
     * <p>
     * 当前实现为空，预留用于处理解除封禁事件。
     * </p>
     *
     * @param loginType 登录类型
     * @param loginId   登录 ID
     * @param service   服务名
     */
    @Override
    public void doUntieDisable(String loginType, Object loginId, String service) {
        // 预留解除封禁回调逻辑
    }

    /**
     * 用户开启安全模式时触发。
     * <p>
     * 当前实现为空，预留用于处理安全模式开启事件。
     * </p>
     *
     * @param loginType  登录类型
     * @param tokenValue Token 值
     * @param service    服务名
     * @param safeTime   安全时长（毫秒）
     */
    @Override
    public void doOpenSafe(String loginType, String tokenValue, String service, long safeTime) {
        // 预留安全模式开启回调逻辑
    }

    /**
     * 用户关闭安全模式时触发。
     * <p>
     * 当前实现为空，预留用于处理安全模式关闭事件。
     * </p>
     *
     * @param loginType  登录类型
     * @param tokenValue Token 值
     * @param service    服务名
     */
    @Override
    public void doCloseSafe(String loginType, String tokenValue, String service) {
        // 预留安全模式关闭回调逻辑
    }

    /**
     * Session 创建时触发。
     * <p>
     * 当前实现为空，预留用于处理 Session 创建事件。
     * </p>
     *
     * @param id Session ID
     */
    @Override
    public void doCreateSession(String id) {
        // 预留 Session 创建回调逻辑
    }

    /**
     * Session 登出时触发。
     * <p>
     * 当前实现为空，预留用于处理 Session 登出事件。
     * </p>
     *
     * @param id Session ID
     */
    @Override
    public void doLogoutSession(String id) {
        // 预留 Session 登出回调逻辑
    }

    /**
     * Token 续期时触发。
     * <p>
     * 当前实现为空，预留用于处理 Token 续期事件。
     * </p>
     *
     * @param loginType  登录类型
     * @param loginId    登录 ID
     * @param tokenValue Token 值
     * @param timeout    新的超时时间（毫秒）
     */
    @Override
    public void doRenewTimeout(String loginType, Object loginId, String tokenValue, long timeout) {
        // 预留 Token 续期回调逻辑
    }
}
