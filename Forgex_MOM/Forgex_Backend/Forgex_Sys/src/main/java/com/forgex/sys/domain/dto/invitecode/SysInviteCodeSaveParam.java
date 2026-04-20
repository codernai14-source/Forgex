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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邀请码保存参数
 *
 * 用于新增邀请码的参数封装
 */
@Data
public class SysInviteCodeSaveParam {

    /** 租户ID（Controller 自动填充） */
    private Long tenantId;

    /** 归属部门ID */
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;

    /** 归属职位ID（可选） */
    private Long positionId;

    /** 失效时间 */
    @NotNull(message = "失效时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /** 最大注册人数 */
    @NotNull(message = "最大注册人数不能为空")
    @Min(value = 1, message = "最大注册人数不能小于1")
    private Integer maxRegisterCount;

    /** 备注 */
    private String remark;
}

