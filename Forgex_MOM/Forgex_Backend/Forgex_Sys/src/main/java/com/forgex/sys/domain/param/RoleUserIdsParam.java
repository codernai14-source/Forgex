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

import java.util.List;

/**
 * 角色用户批量授权/取消授权请求参数。
 * <p>
 * 对应接口 {@code POST /sys/role/user/grant}、{@code POST /sys/role/user/revoke}，
 * 通过用户 ID 列表维护角色与用户的绑定关系。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-06
 * @see com.forgex.sys.controller.SysRoleUserController#grant
 * @see com.forgex.sys.controller.SysRoleUserController#revoke
 */
@Data
public class RoleUserIdsParam {

    /**
     * 角色 ID（必填）
     */
    private Long roleId;

    /**
     * 租户 ID（可选，默认从租户上下文或当前登录用户解析）
     */
    private Long tenantId;

    /**
     * 用户 ID 列表（必填）
     */
    private List<Long> userIds;
}
