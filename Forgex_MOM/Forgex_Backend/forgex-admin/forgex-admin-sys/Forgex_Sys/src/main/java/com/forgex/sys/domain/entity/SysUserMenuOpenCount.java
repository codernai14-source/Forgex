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
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户菜单打开次数实体。
 * <p>
 * 对应数据库表：sys_user_menu_open_count。该表独立记录每个菜单的累计打开次数，
 * 不受常用菜单 Top 6 裁剪规则影响，用于判断菜单是否首次打开并触发引导。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-29
 */
@Data
@TableName("sys_user_menu_open_count")
public class SysUserMenuOpenCount {

    /**
     * 主键 ID。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户 ID。
     */
    private Long tenantId;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 菜单完整路由路径。
     */
    private String menuPath;

    /**
     * 菜单标题。
     */
    private String menuTitle;

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
    private String menuIcon;

    /**
     * 打开次数。
     */
    private Integer openCount;

    /**
     * 首次打开时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime firstOpenAt;

    /**
     * 最近打开时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastOpenAt;

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

    /**
     * 创建人。
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人。
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标记。
     */
    private Boolean deleted;
}
