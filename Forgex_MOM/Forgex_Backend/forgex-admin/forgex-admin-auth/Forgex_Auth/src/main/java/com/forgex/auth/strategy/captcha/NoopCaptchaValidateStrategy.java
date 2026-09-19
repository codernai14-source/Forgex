package com.forgex.auth.strategy.captcha;

import com.forgex.auth.domain.param.LoginParam;
import org.springframework.stereotype.Component;

@Component
public class NoopCaptchaValidateStrategy implements CaptchaValidateStrategy {

    @Override
    public boolean supports(String mode) {
        return mode == null || mode.isBlank() || "none".equalsIgnoreCase(mode);
    }

    @Override
    public CaptchaValidationResult validate(LoginParam param) {
        return CaptchaValidationResult.success();
    }
}
