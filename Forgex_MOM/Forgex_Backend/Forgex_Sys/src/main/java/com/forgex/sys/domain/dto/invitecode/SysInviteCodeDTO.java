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
 * 邀请码DTO
 *
 * 用于邀请码信息的数据传输
 */
@Data
public class SysInviteCodeDTO {

    /** 主键ID */
    private Long id;

    /** 邀请码 */
    private String inviteCode;

    /** 归属部门ID */
    private Long departmentId;

    /** 归属部门名称 */
    private String departmentName;

    /** 归属职位ID */
    private Long positionId;

    /** 归属职位名称 */
    private String positionName;

    /** 失效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /** 最大注册人数 */
    private Integer maxRegisterCount;

    /** 已注册人数 */
    private Integer usedCount;

    /** 剩余可用人数 */
    private Integer remainCount;

    /** 状态：false=停用，true=启用 */
    private Boolean status;

    /** 计算状态标签：ACTIVE/DISABLED/EXPIRED/USED_UP */
    private String statusLabel;

    /** 备注 */
    private String remark;

    /** 租户ID */
    private Long tenantId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 创建人 */
    private String createBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /** 更新人 */
    private String updateBy;
}

