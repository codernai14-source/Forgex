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

/**
 * 管理库角色实体（含租户维度）。
 * <p>
 * 映射表：{@code sys_role}。字段：
 * - {@code roleName} 角色名称；
 * - {@code roleKey} 角色键；
 * - {@code description} 描述；
 * - {@code status} 状态；
 * - {@code tenantId} 租户ID。
 */
@Data
@TableName("sys_role")
public class SysRole extends BaseEntity {
    /** 角色名称 */
    private String roleName;
    
    /** 角色键（角色编码） */
    private String roleKey;
    
    /** 描述 */
    private String description;
    
    /** 状态：false=禁用，true=启用 */
    private Boolean status;
    
    /** 数据权限范围：ALL-全部数据,DEPT_AND_CHILD-本部门及下级,DEPT-本部门,SELF-仅本人,CUSTOM-自定义 */
    private String dataScope;
}
