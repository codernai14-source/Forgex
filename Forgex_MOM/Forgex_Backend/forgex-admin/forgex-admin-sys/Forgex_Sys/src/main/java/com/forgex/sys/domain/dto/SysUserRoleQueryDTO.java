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
package com.forgex.sys.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户已分配角色查询参数。
 * <p>用于“用户管理-分配角色”场景：查询某个用户在当前租户下已绑定的角色ID列表。</p>
 *
 * @author coder_nai@163.com
 * @version 1.0
 * @see com.forgex.sys.controller.UserController
 * @see com.forgex.sys.service.ISysUserRoleService
 */
@Data
public class SysUserRoleQueryDTO {

    /**
     * 用户ID。
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}

