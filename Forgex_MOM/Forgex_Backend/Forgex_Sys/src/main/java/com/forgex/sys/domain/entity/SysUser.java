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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDate;

/**
 * 管理库用户实体。
 * <p>
 * 映射表：{@code sys_user}。用于描述平台用户的基础信息与登录状态。
 * 字段：
 * - {@code account} 账号；
 * - {@code username} 显示名；
 * - {@code password} 密码（哈希或密文）；
 * - {@code email} 邮箱；
 * - {@code phone} 手机号；
 * - {@code gender} 性别（0=未知，1=男，2=女）；
 * - {@code entryDate} 入职时间；
 * - {@code departmentId} 所属部门ID；
 * - {@code positionId} 职位ID；
 * - {@code status} 状态（1启用/0禁用）。
 */
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {
    /** 账号 */
    private String account;
    /** 显示名 */
    private String username;
    /** 密码（哈希或密文） */
    private String password;
    /** 邮箱 */
    private String email;
    /** 手机号 */
    private String phone;
    /** 性别（0=未知，1=男，2=女） */
    private Integer gender;
    /** 入职时间 */
    private LocalDate entryDate;
    /** 所属部门ID */
    private Long departmentId;
    /** 职位ID */
    private Long positionId;
    /** 状态（1启用/0禁用） */
    private Integer status;

}
