package com.forgex.auth.strategy.login;

import com.forgex.auth.enums.AuthPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LoginStrategyFactory {

    private final List<LoginStrategy> strategies;

    public LoginStrategy getStrategy(String loginTerminal, String loginType) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(loginTerminal, loginType))
                .findFirst()
                .orElseThrow(() -> new I18nBusinessException(
                        StatusCode.BUSINESS_ERROR,
                        AuthPromptEnum.LOGIN_METHOD_NOT_SUPPORTED,
                        loginTerminal,
                        loginType
                ));
    }
}
