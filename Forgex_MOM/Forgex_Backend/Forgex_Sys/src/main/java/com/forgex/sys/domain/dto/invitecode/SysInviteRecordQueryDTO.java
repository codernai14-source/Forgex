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

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 邀请注册记录查询DTO
 *
 * 用于查询某个邀请码下的注册记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysInviteRecordQueryDTO extends BaseGetParam {

    /** 租户ID */
    private Long tenantId;

    /** 邀请码主表ID */
    private Long inviteId;

    /** 邀请码 */
    private String inviteCode;
}

