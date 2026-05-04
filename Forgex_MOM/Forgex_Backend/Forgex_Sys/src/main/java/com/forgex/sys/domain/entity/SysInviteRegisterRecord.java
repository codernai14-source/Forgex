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

package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请注册关系记录实体
 *
 * 映射表：sys_invite_register_record
 * 记录通过邀请码注册的用户信息
 */
@Data
@TableName("sys_invite_register_record")
public class SysInviteRegisterRecord extends BaseEntity {

    /** 邀请码主表ID */
    private Long inviteId;

    /** 冗余邀请码 */
    private String inviteCode;

    /** 注册成功用户ID */
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

    /** 注册IP */
    private String registerIp;

    /** 注册地区 */
    private String registerRegion;

    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime registerTime;

    /** 注册结果状态：1=成功 0=失败 */
    private Integer status;

    /** 备注 */
    private String remark;
}

