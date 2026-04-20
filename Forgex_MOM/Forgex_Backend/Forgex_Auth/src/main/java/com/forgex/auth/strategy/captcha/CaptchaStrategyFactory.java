package com.forgex.auth.strategy.captcha;

import com.forgex.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CaptchaStrategyFactory {

    private final List<CaptchaValidateStrategy> strategies;

    public CaptchaValidateStrategy getStrategy(String mode) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(mode))
                .findFirst()
                .orElseThrow(() -> new BusinessException("暂不支持的验证码模式: " + mode));
    }
}
