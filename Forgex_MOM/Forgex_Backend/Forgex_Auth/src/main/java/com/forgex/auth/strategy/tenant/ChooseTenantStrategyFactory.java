package com.forgex.auth.strategy.tenant;

import com.forgex.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ChooseTenantStrategyFactory {

    private final List<ChooseTenantStrategy> strategies;

    public ChooseTenantStrategy getStrategy(String loginTerminal) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(loginTerminal))
                .findFirst()
                .orElseThrow(() -> new BusinessException("暂不支持的租户选择终端: " + loginTerminal));
    }
}
