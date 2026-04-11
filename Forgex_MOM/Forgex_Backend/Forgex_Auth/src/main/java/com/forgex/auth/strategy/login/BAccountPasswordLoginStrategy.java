package com.forgex.auth.strategy.login;

import com.forgex.auth.domain.param.LoginParam;
import com.forgex.auth.domain.vo.TenantVO;
import com.forgex.auth.service.impl.AuthServiceImpl;
import com.forgex.auth.strategy.AuthTerminalConstants;
import com.forgex.auth.strategy.LoginTypeConstants;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BAccountPasswordLoginStrategy implements LoginStrategy {

    private final AuthServiceImpl authService;

    @Override
    public boolean supports(String loginTerminal, String loginType) {
        return AuthTerminalConstants.B.equalsIgnoreCase(loginTerminal)
                && LoginTypeConstants.ACCOUNT_PASSWORD.equalsIgnoreCase(loginType);
    }

    @Override
    public R<List<TenantVO>> login(LoginParam param) {
        return authService.doAccountPasswordLogin(param);
    }
}
