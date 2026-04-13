package com.forgex.auth.strategy.captcha;

import com.forgex.common.i18n.CommonPrompt;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CaptchaValidationResult {

    private final boolean success;
    private final CommonPrompt prompt;
    private final String logMessage;

    public static CaptchaValidationResult success() {
        return new CaptchaValidationResult(true, null, null);
    }

    public static CaptchaValidationResult failure(CommonPrompt prompt, String logMessage) {
        return new CaptchaValidationResult(false, prompt, logMessage);
    }
}
