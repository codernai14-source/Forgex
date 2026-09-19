/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.auth.domain.param;

import lombok.Data;

/**
 * 登录请求参数
 * <p>
 * 用于封装用户登录时提交的账号、密码、验证码等信息。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.controller.AuthController
 * @see com.forgex.auth.service.impl.AuthServiceImpl
 */
@Data
public class LoginParam {
    private String loginTerminal;

    private String loginType;
    /**
     * 账号
     * <p>用于登录的唯一标识（如用户名、手机号、邮箱等）</p>
     */
    private String account;

    /**
     * 用户名
     * <p>用户的显示名称（可选）</p>
     */
    private String username;

    /**
     * 明文密码
     * <p>用户输入的原始密码，后端进行加密校验</p>
     */
    private String password;

    /**
     * 验证码
     * <p>
     * 是否必填由系统配置决定<br>
     * 当启用验证码功能时必须填写
     * </p>
     */
    private String captcha;

    /**
     * 图片验证码 ID
     * <p>
     * 用于标识生成的图片验证码，与 Redis 中存储的验证码答案对应
     * </p>
     */
    private String captchaId;
}
