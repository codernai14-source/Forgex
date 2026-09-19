package com.forgex.auth.strategy.tenant;

import com.forgex.auth.domain.dto.SysUserDTO;
import com.forgex.auth.domain.param.TenantChoiceParam;
import com.forgex.common.web.R;

public interface ChooseTenantStrategy {

    boolean supports(String loginTerminal);

    R<SysUserDTO> chooseTenant(TenantChoiceParam param);
}
