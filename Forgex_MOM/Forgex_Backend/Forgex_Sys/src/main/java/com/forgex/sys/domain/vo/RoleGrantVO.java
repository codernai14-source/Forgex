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
package com.forgex.sys.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色授权 VO
 * <p>
 * 用于返回已授权列表数据，包含授权类型、授权对象信息等
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2026-04-06
 * @see com.forgex.sys.controller.SysRoleUserController#getGrantedList
 */
@Data
public class RoleGrantVO {

    /**
     * 授权记录 ID（sys_user_role 表主键）
     */
    private Long id;

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 授权类型：USER=用户，DEPARTMENT=部门，POSITION=职位
     */
    private String grantType;

    /**
     * 授权对象 ID（用户 ID/部门 ID/职位 ID）
     */
    private Long grantObjectId;

    /**
     * 授权对象名称（用户名/部门名/职位名）
     */
    private String grantObject;

    /**
     * 授权对象编码（用户账号/部门编码/职位编码）
     */
    private String grantObjectCode;

    /**
     * 授权时间
     */
    private LocalDateTime createTime;

    /**
     * 授权人
     */
    private String createBy;
}
