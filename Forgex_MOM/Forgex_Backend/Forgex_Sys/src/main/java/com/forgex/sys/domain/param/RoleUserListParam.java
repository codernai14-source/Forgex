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
package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 角色已授权用户列表查询参数。
 * <p>
 * 对应接口 {@code POST /sys/role/user/list}，用于按角色分页查询已绑定的用户。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-06
 * @see com.forgex.sys.controller.SysRoleUserController#list
 */
@Data
public class RoleUserListParam {

    /**
     * 角色 ID（必填）
     */
    private Long roleId;

    /**
     * 租户 ID（可选，默认从租户上下文或当前登录用户解析）
     */
    private Long tenantId;

    /**
     * 页码，默认 1
     */
    private Long pageNum = 1L;

    /**
     * 每页条数，默认 20
     */
    private Long pageSize = 20L;
}
