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
 * 选择租户请求参数
 */
@Data
public class TenantChoiceParam {
    private Long tenantId; // 选择的租户ID
    private String account; // 当前登录账号
    private String username; // 当前登录用户名（完成最终登录）
}
