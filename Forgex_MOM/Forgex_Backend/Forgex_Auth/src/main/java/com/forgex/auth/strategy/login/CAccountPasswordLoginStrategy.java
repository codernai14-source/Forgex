package com.forgex.auth.strategy.login;

import com.forgex.auth.domain.param.LoginParam;
import com.forgex.auth.domain.vo.TenantVO;
import com.forgex.auth.service.impl.AuthServiceImpl;
import com.forgex.auth.strategy.AuthTerminalConstants;
import com.forgex.auth.strategy.LoginTypeConstants;
import com.forgex.common.web.R;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CAccountPasswordLoginStrategy implements LoginStrategy {

    private final ObjectProvider<AuthServiceImpl> authServiceProvider;

    public CAccountPasswordLoginStrategy(ObjectProvider<AuthServiceImpl> authServiceProvider) {
        this.authServiceProvider = authServiceProvider;
    }

    @Override
    public boolean supports(String loginTerminal, String loginType) {
        return AuthTerminalConstants.C.equalsIgnoreCase(loginTerminal)
                && LoginTypeConstants.ACCOUNT_PASSWORD.equalsIgnoreCase(loginType);
    }

    @Override
    public R<List<TenantVO>> login(LoginParam param) {
        return authServiceProvider.getObject().doAccountPasswordLogin(param);
    }
}
