package com.forgex.auth.strategy.tenant;

import com.forgex.auth.domain.dto.SysUserDTO;
import com.forgex.auth.domain.param.TenantChoiceParam;
import com.forgex.auth.service.impl.AuthServiceImpl;
import com.forgex.auth.strategy.AuthTerminalConstants;
import com.forgex.common.web.R;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class BChooseTenantStrategy implements ChooseTenantStrategy {

    private final ObjectProvider<AuthServiceImpl> authServiceProvider;

    public BChooseTenantStrategy(ObjectProvider<AuthServiceImpl> authServiceProvider) {
        this.authServiceProvider = authServiceProvider;
    }

    @Override
    public boolean supports(String loginTerminal) {
        return AuthTerminalConstants.B.equalsIgnoreCase(loginTerminal);
    }

    @Override
    public R<SysUserDTO> chooseTenant(TenantChoiceParam param) {
        return authServiceProvider.getObject().doChooseTenant(param);
    }
}
