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
 * 映射表：{@code sys_login_log}。用于记录用户的登录行为。
 * </p>
 * 
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-14
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
    private LocalDateTime loginTime;

    /**
     * 登出时间
     */
    private LocalDateTime logoutTime;

    /**
     * tokenValue（用于回写登出日志定位会话）。
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
    private LocalDateTime createTime;
}
