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
 * 用户菜单收藏实体类。
 * <p>
 * 对应数据库表：sys_user_menu_favorite。
 * 用于记录用户在 PC 端主动收藏的菜单，支撑个人首页“我的收藏”卡片展示。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-12
 */
@Data
@TableName("sys_user_menu_favorite")
public class SysUserMenuFavorite {

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
     * 标识当前收藏记录所属的用户。
     */
    private Long userId;

    /**
     * 菜单完整路由路径。
     * 例如：/workspace/sys/authorization/role。
     */
    private String menuPath;

    /**
     * 菜单标题。
     * 保存收藏时的菜单展示名称。
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
     * 排序号。
     * 用于控制“我的收藏”卡片和收藏管理页的展示顺序。
     */
    @TableField("order_num")
    private Integer orderNum;

    /**
     * 创建时间。
     * 表示用户执行收藏操作的时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

