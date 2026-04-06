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
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色部门关联实体类
 * <p>
 * 映射表：{@code sys_role_dept}。用于维护角色与部门之间的多对多关联关系。
 * 一个角色可以关联多个部门，一个部门也可以被多个角色关联。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-06
 * @see SysRole
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_dept")
public class SysRoleDept extends BaseEntity {

    /** 角色 ID */
    private Long roleId;

    /** 部门 ID */
    private Long deptId;
}
