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
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * C 端菜单实体。
 * <p>
 * 映射表：{@code sys_c_menu}。用于手机端、Pad 端的菜单配置，独立于 B 端 {@code sys_menu}。
 * </p>
 *
 * @author Forgex Team
 * @since 2026-04-11
 */
@Data
@TableName("sys_c_menu")
public class SysCMenu extends BaseEntity {

    /** 所属模块 ID（关联 sys_module） */
    private Long moduleId;

    /** 父菜单 ID，顶级为 0 */
    private Long parentId;

    /** 菜单类型：catalog=目录, menu=菜单, button=按钮 */
    private String type;

    /** 菜单层级：1=一级, 2=二级, 3=三级 */
    private Integer menuLevel;

    /** 路由路径 */
    private String path;

    /** 菜单名称 */
    private String name;

    /** 国际化名称 JSON */
    private String nameI18nJson;

    /** 菜单图标 */
    private String icon;

    /** 组件键（原生端页面映射标识） */
    private String componentKey;

    /** 权限键 */
    private String permKey;

    /** 菜单模式：embedded=内嵌, external=外联 */
    private String menuMode;

    /** 外联 URL */
    private String externalUrl;

    /** 排序号（越小越靠前） */
    private Integer orderNum;

    /** 是否可见：false=隐藏, true=显示 */
    private Boolean visible;

    /** 状态：false=禁用, true=启用 */
    private Boolean status;

    /** 适用租户类型 */
    private String tenantType;

    /** 设备类型：MOBILE=手机, TABLET=Pad, ALL=通用 */
    private String deviceType;
}

