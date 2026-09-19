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

import lombok.Data;

import java.util.List;

/**
 * 角色授权 DTO
 * <p>
 * 用于批量授予角色权限，支持用户、部门、职位三种授权类型
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2026-04-06
 * @see com.forgex.sys.controller.SysRoleUserController#grantBatch
 */
@Data
public class RoleGrantDTO {

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 授权类型：USER=用户，DEPARTMENT=部门，POSITION=职位
     */
    private String grantType;

    /**
     * 用户 ID 列表（当 grantType 为 USER 时使用）
     */
    private List<Long> userIds;

    /**
     * 部门 ID 列表（当 grantType 为 DEPARTMENT 时使用）
     */
    private List<Long> departmentIds;

    /**
     * 职位 ID 列表（当 grantType 为 POSITION 时使用）
     */
    private List<Long> positionIds;
}
