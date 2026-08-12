package com.forgex.auth.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录凭据校验结果。
 * <p>
 * 返回用户可选租户以及完成首次租户选择所需的短期交互码。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultVO {

    /** 首次选择租户时使用的短期一次性交互码。 */
    private String interactionCode;

    /** 当前用户绑定的租户列表。 */
    private List<TenantVO> tenants;
}
