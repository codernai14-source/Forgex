package com.forgex.auth.controller;

import com.forgex.auth.domain.vo.LoginResultVO;
import com.forgex.auth.enums.AuthPromptEnum;
import com.forgex.auth.mfa.MfaService;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MFA 绑定与登录二次校验接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see MfaService
 */
@RestController
@RequestMapping("/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;

    /**
     * 开始绑定 MFA。
     *
     * @return otpauth URI 与恢复码
     */
    @OperationLog(module = "auth", menuPath = "/system/profile", operationType = OperationType.UPDATE, detailTemplateCode = "MFA_BIND")
    @PostMapping("/bind")
    public R<Map<String, Object>> bind() {
        return R.ok(mfaService.bind(CurrentUserUtils.getUserId(), CurrentUserUtils.getAccount()));
    }

    /**
     * 确认绑定。
     *
     * @param body 含 code
     * @return 空结果
     */
    @OperationLog(module = "auth", menuPath = "/system/profile", operationType = OperationType.UPDATE, detailTemplateCode = "MFA_CONFIRM")
    @PostMapping("/confirm")
    public R<Void> confirm(@RequestBody Map<String, String> body) {
        mfaService.confirmBind(CurrentUserUtils.getUserId(), body == null ? null : body.get("code"));
        return R.ok();
    }

    /**
     * 解绑 MFA。
     *
     * @return 空结果
     */
    @OperationLog(module = "auth", menuPath = "/system/profile", operationType = OperationType.UPDATE, detailTemplateCode = "MFA_UNBIND")
    @PostMapping("/unbind")
    public R<Void> unbind() {
        mfaService.unbind(CurrentUserUtils.getUserId());
        return R.ok();
    }

    /**
     * 登录阶段校验动态码。
     *
     * @param body challengeId / code
     * @return 租户列表
     */
    @PostMapping("/verify")
    public R<LoginResultVO> verify(@RequestBody Map<String, String> body) {
        if (body == null) {
            return R.fail(AuthPromptEnum.MFA_CODE_INVALID);
        }
        return mfaService.verifyChallenge(body.get("challengeId"), body.get("code"));
    }
}
