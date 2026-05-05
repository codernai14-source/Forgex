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
 * 邀请码实体
 *
 * 映射表：sys_invite_code
 * 用于邀请码管理，支持邀请注册功能
 */
@Data
@TableName("sys_invite_code")
public class SysInviteCode extends BaseEntity {

    /** 邀请码（唯一） */
    private String inviteCode;

    /** 归属部门ID */
    private Long departmentId;

    /** 归属职位ID（可选） */
    private Long positionId;

    /** 注册后绑定角色ID */
    private Long roleId;

    /** 失效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /** 最大注册人数 */
    private Integer maxRegisterCount;

    /** 已注册人数 */
    private Integer usedCount;

    /** 状态：false=停用，true=启用 */
    private Boolean status;

    /** 备注 */
    private String remark;
}

