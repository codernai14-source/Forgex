/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forgex.sys.domain.dto.invitecode;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请注册记录DTO
 *
 * 用于邀请注册记录的数据传输
 */
@Data
public class SysInviteRecordDTO {

    /** 主键ID */
    private Long id;

    /** 邀请码主表ID */
    private Long inviteId;

    /** 邀请码 */
    private String inviteCode;

    /** 注册用户ID */
    private Long userId;

    /** 注册账号 */
    private String account;

    /** 用户名 */
    private String username;

    /** 注册落入部门ID */
    private Long departmentId;

    /** 注册落入职位ID */
    private Long positionId;

    /** 注册绑定角色ID */
    private Long roleId;

    /** 注册绑定角色名称 */
    private String roleName;

    /** 注册IP */
    private String registerIp;

    /** 注册地区 */
    private String registerRegion;

    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime registerTime;

    /** 状态 */
    private Integer status;
}

