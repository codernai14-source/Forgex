package com.forgex.auth.mfa;

import com.forgex.auth.domain.vo.LoginResultVO;
import com.forgex.common.web.R;

import java.util.Map;

/**
 * MFA 绑定与校验服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface MfaService {

    /**
     * 开始绑定，返回 otpauth URI 与明文恢复码。
     *
     * @param userId  用户 ID
     * @param account 账号
     * @return 绑定信息
     */
    Map<String, Object> bind(Long userId, String account);

    /**
     * 用动态码确认绑定。
     *
     * @param userId 用户 ID
     * @param code   动态码
     */
    void confirmBind(Long userId, String code);

    /**
     * 解绑 MFA。
     *
     * @param userId 用户 ID
     */
    void unbind(Long userId);

    /**
     * 登录阶段校验动态码或恢复码，成功后继续签发交互码。
     *
     * @param challengeId 挑战票据
     * @param code        动态码或恢复码
     * @return 登录结果
     */
    R<LoginResultVO> verifyChallenge(String challengeId, String code);

    /**
     * 为已启用 MFA 的用户签发挑战票据。
     *
     * @param userId  用户 ID
     * @param account 账号
     * @return 挑战 ID
     */
    String issueChallenge(Long userId, String account);
}
