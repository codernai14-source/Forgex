/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forgex.auth.domain.param;

import lombok.Data;

/**
 * 注册请求参数
 * <p>
 * 用于封装用户通过邀请码注册时提交的信息
 * </p>
 */
@Data
public class RegisterParam {

    /** 账号（必填） */
    private String account;

    /** 用户名（必填） */
    private String username;

    /** 密码（必填，SM2 加密） */
    private String password;

    /** 手机号（可选） */
    private String phone;

    /** 邮箱（可选） */
    private String email;

    /** 邀请码（必填） */
    private String inviteCode;

    /** 验证码（必填） */
    private String captcha;

    /** 验证码ID（必填） */
    private String captchaId;
}

