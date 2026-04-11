package com.forgex.auth.strategy.tenant;

import com.forgex.auth.domain.dto.SysUserDTO;
import com.forgex.auth.domain.param.TenantChoiceParam;
import com.forgex.auth.service.impl.AuthServiceImpl;
import com.forgex.auth.strategy.AuthTerminalConstants;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CChooseTenantStrategy implements ChooseTenantStrategy {

    private final AuthServiceImpl authService;

    @Override
    public boolean supports(String loginTerminal) {
        return AuthTerminalConstants.C.equalsIgnoreCase(loginTerminal);
    }

    @Override
    public R<SysUserDTO> chooseTenant(TenantChoiceParam param) {
        return authService.doChooseTenant(param);
    }
}
