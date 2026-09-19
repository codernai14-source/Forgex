package com.forgex.auth.strategy.captcha;

import com.forgex.auth.enums.AuthPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 验证码策略工厂。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CaptchaStrategyFactory {

    private final List<CaptchaValidateStrategy> strategies;

    /**
     * 获取匹配的策略实现。
     *
     * @param mode 模式
     * @return 处理结果
     */
    public CaptchaValidateStrategy getStrategy(String mode) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(mode))
                .findFirst()
                .orElseThrow(() -> new I18nBusinessException(
                        StatusCode.BUSINESS_ERROR,
                        AuthPromptEnum.CAPTCHA_MODE_NOT_SUPPORTED,
                        mode
                ));
    }
}
