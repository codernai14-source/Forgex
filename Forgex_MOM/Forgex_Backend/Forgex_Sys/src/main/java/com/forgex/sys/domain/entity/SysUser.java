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
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理库用户实体。
 * <p>
 * 映射表：{@code sys_user}。用于描述平台用户的基础信息与登录状态。
 * 仅包含数据库表中实际存在的持久化字段。
 * 字段：
 * <ul>
 *   <li>{@code account} 账号</li>
 *   <li>{@code username} 显示名</li>
 *   <li>{@code password} 密码（哈希或密文）</li>
 *   <li>{@code email} 邮箱</li>
 *   <li>{@code phone} 手机号</li>
 *   <li>{@code gender} 性别（0=未知，1=男，2=女）</li>
 *   <li>{@code avatar} 头像URL</li>
 *   <li>{@code entryDate} 入职时间</li>
 *   <li>{@code departmentId} 所属部门ID</li>
 *   <li>{@code positionId} 职位ID</li>
 *   <li>{@code status} 状态（true启用/false禁用）</li>
 *   <li>{@code lastLoginIp} 最后登录IP</li>
 *   <li>{@code lastLoginRegion} 最后登录地区</li>
 *   <li>{@code lastLoginTime} 最后登录时间</li>
 * </ul>
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

    /** 头像URL */
    private String avatar;

    /** 入职时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate entryDate;

    /** 所属部门ID */
    private Long departmentId;

    /** 职位ID */
    private Long positionId;

    /** 状态：false=禁用，true=启用 */
    private Boolean status;
    
    /** 最后登录IP */
    private String lastLoginIp;
    
    /** 最后登录地区 */
    private String lastLoginRegion;
    
    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;
}
