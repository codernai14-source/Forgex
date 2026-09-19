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

package com.forgex.sys.domain.dto;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端菜单查询 DTO
 *
 * @author Forgex Team
 * @since 2026-04-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysCMenuQueryDTO extends BaseGetParam {

    /** 模块 ID */
    private Long moduleId;

    /** 菜单名称（模糊查询） */
    private String name;

    /** 菜单类型 */
    private String type;

    /** 状态 */
    private Boolean status;

    /** 租户 ID */
    private Long tenantId;

    /** 设备类型 */
    private String deviceType;
}

