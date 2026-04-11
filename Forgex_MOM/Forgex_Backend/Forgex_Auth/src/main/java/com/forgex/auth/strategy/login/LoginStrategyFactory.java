package com.forgex.auth.strategy.login;

import com.forgex.common.exception.BusinessException;
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
                .orElseThrow(() -> new BusinessException(
                        String.format("暂不支持的登录方式: terminal=%s, type=%s", loginTerminal, loginType)
                ));
    }
}
