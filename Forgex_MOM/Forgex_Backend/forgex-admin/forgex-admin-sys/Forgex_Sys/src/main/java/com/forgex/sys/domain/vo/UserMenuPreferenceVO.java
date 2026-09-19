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

package com.forgex.sys.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户菜单偏好视图对象。
 * <p>
 * 用于返回个人首页“常用菜单”和“我的收藏”卡片所需的菜单展示信息。
 * 同一结构同时适配常用菜单、收藏菜单两类场景。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-12
 */
@Data
public class UserMenuPreferenceVO {

    /**
     * 菜单完整路由路径。
     */
    private String path;

    /**
     * 菜单标题。
     */
    private String title;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 模块名称。
     */
    private String moduleName;

    /**
     * 菜单图标。
     */
    private String icon;

    /**
     * 访问次数。
     * 仅常用菜单场景会返回该值。
     */
    private Integer visitCount;

    /**
     * 是否已收藏。
     * 用于前端展示星标状态。
     */
    private Boolean favorite;

    /**
     * 最近访问时间。
     * 仅常用菜单场景会返回该值。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastVisitedAt;
}

