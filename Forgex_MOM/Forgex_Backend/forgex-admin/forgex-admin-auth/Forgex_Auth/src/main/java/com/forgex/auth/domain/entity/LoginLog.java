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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体（Auth 模块）
 * <p>
 * 映射表：{@code sys_login_log}，用于记录用户的登录行为。
 * </p>
 *
 * <p>
 * 包括登录时间、登录 IP、登录地区、浏览器信息、登出时间等，用于安全审计和用户行为分析。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-01-14
 * @see com.forgex.auth.service.LoginLogService
 * @see com.forgex.auth.mapper.LoginLogMapper
 */
@Data
@TableName("sys_login_log")
public class LoginLog {

    /**
     * 主键 ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     * <p>
     * 关联到用户表的主键<br>
     * 登录失败时可能为空
     * </p>
     */
    private Long userId;

    /**
     * 登录账号
     * <p>用于登录的账号标识</p>
     */
    private String account;

    /**
     * 租户 ID
     * <p>用户登录时选择的租户 ID</p>
     */
    private Long tenantId;

    /**
     * 登录 IP 地址
     * <p>用户登录时使用的 IP 地址</p>
     */
    private String loginIp;

    /**
     * IP 归属地
     * <p>根据登录 IP 解析的地理位置信息</p>
     */
    private String loginRegion;

    /**
     * 浏览器 UA
     * <p>
     * User-Agent 字符串，包含浏览器类型、操作系统等信息<br>
     * 用于设备识别和安全分析
     * </p>
     */
    private String userAgent;

    /**
     * 登录时间
     * <p>用户成功登录的时间</p>
     */
    private LocalDateTime loginTime;

    /**
     * 登出时间
     * <p>用户退出登录的时间</p>
     */
    private LocalDateTime logoutTime;

    /**
     * Token 值
     * <p>
     * 用于回写登出日志时定位会话<br>
     * 在登出时填充
     * </p>
     */
    private String tokenValue;

    /**
     * 登出原因
     * <p>
     * 可选值：
     * <ul>
     *     <li>MANUAL：用户主动退出</li>
     *     <li>TIMEOUT：会话超时</li>
     *     <li>KICKOUT：被踢出</li>
     *     <li>REPLACED：被其他登录替换</li>
     * </ul>
     * </p>
     */
    private String logoutReason;

    /**
     * 登录状态
     * <p>
     * 1：登录成功<br>
     * 0：登录失败
     * </p>
     */
    private Integer status;

    /**
     * 失败原因
     * <p>
     * 当登录失败时，记录具体的失败原因<br>
     * 如：密码错误、账号被禁用等
     * </p>
     */
    private String reason;

    /**
     * 创建时间
     * <p>日志记录的创建时间</p>
     */
    private LocalDateTime createTime;
}
