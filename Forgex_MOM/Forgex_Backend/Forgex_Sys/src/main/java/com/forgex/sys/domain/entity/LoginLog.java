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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 * <p>
 * 映射表：{@code sys_login_log}。用于记录用户的登录行为，包括成功和失败的登录尝试。
 * 字段：
 * <ul>
 *   <li>{@code id} 主键</li>
 *   <li>{@code userId} 用户ID（登录失败时可能为空）</li>
 *   <li>{@code account} 登录账号</li>
 *   <li>{@code tenantId} 租户ID</li>
 *   <li>{@code loginIp} 登录IP</li>
 *   <li>{@code loginRegion} IP归属地</li>
 *   <li>{@code userAgent} 浏览器UA</li>
 *   <li>{@code loginTime} 登录时间</li>
 *   <li>{@code status} 登录状态（1-成功，0-失败）</li>
 *   <li>{@code reason} 失败原因</li>
 *   <li>{@code createTime} 创建时间</li>
 * </ul>
 * </p>
 * 
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-13
 */
@Data
@TableName("sys_login_log")
public class LoginLog {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID（登录失败时可能为空）
     */
    private Long userId;
    
    /**
     * 登录账号
     */
    private String account;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 登录IP
     */
    private String loginIp;
    
    /**
     * IP归属地
     */
    private String loginRegion;
    
    /**
     * 浏览器UA
     */
    private String userAgent;
    
    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    /**
     * 登出时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logoutTime;

    /**
     * tokenValue（用于回写登出日志定位会话）。
     * <p>
     * 该字段属于敏感会话信息，一般不对外返回，仅用于审计更新。
     * </p>
     */
    private String tokenValue;

    /**
     * 登出原因（例如：MANUAL/TIMEOUT/KICKOUT/REPLACED）。
     */
    private String logoutReason;

    /**
     * 登录状态：1-成功，0-失败
     */
    private Integer status;
    
    /**
     * 失败原因
     */
    private String reason;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
