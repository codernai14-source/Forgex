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
 * 角色授权查询参数
 * <p>
 * 用于查询已授权列表时的参数封装
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2026-04-06
 * @see com.forgex.sys.service.ISysRoleUserService#getGrantedList
 */
@Data
public class RoleGrantQueryDTO {

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 授权类型：USER=用户，DEPARTMENT=部门，POSITION=职位（可选，不传则查询所有类型）
     */
    private String grantType;

    /**
     * 搜索关键字（可选，用于搜索用户名/账号/部门名/职位名）
     */
    private String keyword;

    /**
     * 页码，默认 1
     */
    private Integer pageNum = 1;

    /**
     * 每页大小，默认 20
     */
    private Integer pageSize = 20;
}
