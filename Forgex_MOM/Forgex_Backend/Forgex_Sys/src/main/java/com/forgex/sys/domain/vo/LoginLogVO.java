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
package com.forgex.sys.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.forgex.common.dict.DictI18n;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志 VO
 * 
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-13
 */
@Data
public class LoginLogVO {
    
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 用户ID
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
     * 登出原因（例如：MANUAL/TIMEOUT/KICKOUT/REPLACED）。
     */
    private String logoutReason;
    
    /**
     * 登录状态：1-成功，0-失败
     */
    @DictI18n(nodePathConst = "login_status", targetField = "statusText")
    private Integer status;

    private String statusText;
    
    /**
     * 失败原因
     */
    private String reason;
}
