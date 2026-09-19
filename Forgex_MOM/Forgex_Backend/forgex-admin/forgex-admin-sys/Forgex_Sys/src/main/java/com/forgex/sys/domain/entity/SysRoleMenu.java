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

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单关联实体。
 * <p>
 * 映射表：{@code sys_role_menu}。用于记录角色与菜单的关联关系。
 * 字段：
 * - {@code id} 主键ID；
 * - {@code tenantId} 租户ID；
 * - {@code roleId} 角色ID；
 * - {@code menuId} 菜单ID。
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {
    /** 主键ID */
    @TableId
    private Long id;
    /** 租户ID */
    private Long tenantId;
    /** 角色ID */
    private Long roleId;
    /** 菜单ID */
    private Long menuId;
}

