package com.forgex.auth.strategy.captcha;

import com.forgex.auth.domain.param.LoginParam;

public interface CaptchaValidateStrategy {

    boolean supports(String mode);

    CaptchaValidationResult validate(LoginParam param);
}
