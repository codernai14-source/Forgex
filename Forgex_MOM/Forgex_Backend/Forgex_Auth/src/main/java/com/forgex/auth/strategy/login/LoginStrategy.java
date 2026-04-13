package com.forgex.auth.strategy.login;

import com.forgex.auth.domain.param.LoginParam;
import com.forgex.auth.domain.vo.TenantVO;
import com.forgex.common.web.R;

import java.util.List;

public interface LoginStrategy {

    boolean supports(String loginTerminal, String loginType);

    R<List<TenantVO>> login(LoginParam param);
}
