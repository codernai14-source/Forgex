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

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户常用菜单实体类。
 * <p>
 * 对应数据库表：sys_user_menu_common。
 * 用于记录用户在 PC 端菜单中的访问次数与最近访问时间，
 * 为个人首页“常用菜单”卡片提供排序数据。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-12
 */
@Data
@TableName("sys_user_menu_common")
public class SysUserMenuCommon {

    /**
     * 主键 ID。
     */
    @TableId
    private Long id;

    /**
     * 租户 ID。
     * 用于多租户数据隔离。
     */
    private Long tenantId;

    /**
     * 用户 ID。
     * 标识当前访问统计所属的用户。
     */
    private Long userId;

    /**
     * 菜单完整路由路径。
     * 例如：/workspace/sys/authorization/role。
     */
    private String menuPath;

    /**
     * 菜单标题。
     * 保存访问当时的菜单展示名称，便于首页直接展示。
     */
    private String menuTitle;

    /**
     * 模块编码。
     * 对应菜单所属模块的唯一编码。
     */
    private String moduleCode;

    /**
     * 模块名称。
     * 用于首页展示菜单所属模块。
     */
    private String moduleName;

    /**
     * 菜单图标。
     * 保存前端可识别的图标名称。
     */
    private String menuIcon;

    /**
     * 访问次数。
     * 用于按使用频率排序常用菜单。
     */
    private Integer visitCount;

    /**
     * 最近访问时间。
     * 用于在访问次数相同的情况下做二次排序。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastVisitedAt;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

