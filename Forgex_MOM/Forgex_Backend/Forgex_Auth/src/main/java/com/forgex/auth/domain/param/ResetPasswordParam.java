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
 * 重置密码请求参数
 * <p>
 * 用于封装重置密码时提交的参数信息。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.controller.AuthController
 * @see com.forgex.auth.service.AuthService
 */
@Data
public class ResetPasswordParam {
    /**
     * 用户 ID
     * <p>需要重置密码的用户 ID</p>
     */
    private Long userId;
}
