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

import lombok.Data;

import java.time.LocalDateTime;

/**
 * C 端菜单 DTO
 *
 * @author Forgex Team
 * @since 2026-04-11
 */
@Data
public class SysCMenuDTO {

    private Long id;
    private Long moduleId;
    private String moduleName;
    private Long parentId;
    private String parentName;
    private String type;
    private Integer menuLevel;
    private String path;
    private String name;
    private String nameI18nJson;
    private String icon;
    private String componentKey;
    private String permKey;
    private String menuMode;
    private String externalUrl;
    private Integer orderNum;
    private Boolean visible;
    private Boolean status;
    private String tenantType;
    private String deviceType;
    private Long tenantId;
    private LocalDateTime createTime;
    private String createBy;
    private LocalDateTime updateTime;
    private String updateBy;
}

