package com.forgex.auth.domain.param;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginParam {
    private String account; // 账号
    private String username; // 用户名
    private String password; // 明文密码（后端进行校验）
    private String captcha;  // 验证码（是否必填由配置决定）
    private String captchaId; // 图片验证码ID
}
