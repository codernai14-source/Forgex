package com.forgex.auth.strategy.login;

import com.forgex.auth.enums.AuthPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 登录策略工厂。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class LoginStrategyFactory {

    private final List<LoginStrategy> strategies;

    /**
     * 获取匹配的策略实现。
     *
     * @param loginTerminal loginterminal
     * @param loginType login类型
     * @return 处理结果
     */
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
