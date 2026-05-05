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
package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 用户实体（Auth 模块持久化对象）
 * <p>
 * 对应管理库数据库表：sys_user，用于认证模块在登录、选择租户等场景下
 * 读取用户的基础信息（账号、姓名、联系方式、头像、状态等）。
 * </p>
 *
 * <p>
 * 该实体与 Sys 模块中的 SysUser 保持字段含义一致，避免跨模块直接依赖。
 * </p>
 *
 * @author coder_nai
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.service.impl.AuthServiceImpl
 * @see com.forgex.auth.mapper.SysUserMapper
 */
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {
    /**
     * 账号
     * <p>用于登录的唯一标识，不能为空</p>
     */
    private String account;

    /**
     * 用户名
     * <p>用户的显示名称，可以修改</p>
     */
    private String username;

    /**
     * 加密后的密码
     * <p>使用 BCrypt 加密存储，不可逆</p>
     */
    private String password;

    /**
     * 邮箱地址
     * <p>用于接收系统通知、找回密码等</p>
     */
    private String email;

    /**
     * 手机号码
     * <p>用于接收短信验证码、登录等</p>
     */
    private String phone;

    /**
     * 头像 URL
     * <p>用户头像的图片地址</p>
     */
    private String avatar;

    /**
     * 用户状态
     * <p>
     * false：禁用（无法登录）<br>
     * true：启用（正常状态）
     * </p>
     */
    private Boolean status;

    /**
     * 员工 ID。
     */
    private Long employeeId;

    /**
     * 用户来源。
     */
    private Integer userSource;

    /**
     * 最后登录 IP 地址
     * <p>记录用户最后一次登录的 IP 地址</p>
     */
    private String lastLoginIp;

    /**
     * 最后登录地区
     * <p>根据 IP 地址解析的最后登录地理位置</p>
     */
    private String lastLoginRegion;

    /**
     * 最后登录时间
     * <p>记录用户最后一次成功登录的时间</p>
     */
    private java.time.LocalDateTime lastLoginTime;

    /**
     * 租户 ID（非持久化字段）
     * <p>
     * 该字段不映射到数据库，用于在内存中存储当前登录用户选择的租户 ID
     * </p>
     */
    @TableField(exist = false)
    private Long tenantId;
}
