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

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色部门关联实体。
 * <p>
 * 映射表：{@code sys_role_dept}，用于自定义数据权限（CUSTOM）下的角色-部门多对多关系。
 * 该表是授权关联表，只有 {@code create_time}/{@code create_by}，没有 {@code update_time}/{@code deleted}，
 * 因此不能继承 {@link com.forgex.common.base.BaseEntity}，否则 MyBatis-Plus 会选出不存在的列。
 * 写法与 {@link SysUserRole}、{@link SysRoleMenu} 一致。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-06
 * @see SysRole
 * @see SysUserRole
 * @see SysRoleMenu
 */
@Data
@TableName("sys_role_dept")
public class SysRoleDept {

    /**
     * 主键 ID，对应表自增列 {@code id}。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色 ID，对应 {@code role_id}。
     */
    private Long roleId;

    /**
     * 部门 ID，对应 {@code dept_id}。
     */
    private Long deptId;

    /**
     * 租户 ID，对应 {@code tenant_id}。
     */
    private Long tenantId;

    /**
     * 创建时间，对应 {@code create_time}，插入时自动填充。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人，对应 {@code create_by}，插入时自动填充。
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;
}
