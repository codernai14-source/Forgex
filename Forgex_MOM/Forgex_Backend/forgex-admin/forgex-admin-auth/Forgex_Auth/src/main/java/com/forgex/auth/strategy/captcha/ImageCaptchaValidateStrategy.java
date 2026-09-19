package com.forgex.auth.strategy.captcha;

import com.forgex.auth.domain.param.LoginParam;
import com.forgex.auth.service.CaptchaService;
import com.forgex.common.i18n.CommonPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class ImageCaptchaValidateStrategy implements CaptchaValidateStrategy {

    private final CaptchaService captchaService;

    @Override
    public boolean supports(String mode) {
        return "image".equalsIgnoreCase(mode);
    }

    @Override
    public CaptchaValidationResult validate(LoginParam param) {
        if (!StringUtils.hasText(param.getCaptchaId()) || !StringUtils.hasText(param.getCaptcha())) {
            return CaptchaValidationResult.failure(CommonPrompt.VERIFICATION_CODE_CANNOT_BE_EMPTY, "图片验证码缺失");
        }
        if (!captchaService.verifyImage(param.getCaptchaId(), param.getCaptcha())) {
            return CaptchaValidationResult.failure(CommonPrompt.VERIFICATION_CODE_INCORRECT, "图片验证码错误");
        }
        return CaptchaValidationResult.success();
    }
}
