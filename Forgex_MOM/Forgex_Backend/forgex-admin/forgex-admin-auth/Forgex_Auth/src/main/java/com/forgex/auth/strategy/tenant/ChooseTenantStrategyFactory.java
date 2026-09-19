package com.forgex.auth.strategy.tenant;

import com.forgex.auth.enums.AuthPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 租户选择策略工厂。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class ChooseTenantStrategyFactory {

    private final List<ChooseTenantStrategy> strategies;

    /**
     * 获取匹配的策略实现。
     *
     * @param loginTerminal loginterminal
     * @return 处理结果
     */
    public ChooseTenantStrategy getStrategy(String loginTerminal) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(loginTerminal))
                .findFirst()
                .orElseThrow(() -> new I18nBusinessException(
                        StatusCode.BUSINESS_ERROR,
                        AuthPromptEnum.TENANT_TERMINAL_NOT_SUPPORTED,
                        loginTerminal
                ));
    }
}
