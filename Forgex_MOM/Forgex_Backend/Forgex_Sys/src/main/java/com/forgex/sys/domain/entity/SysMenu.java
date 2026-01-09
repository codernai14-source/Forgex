/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 系统菜单实体。
 * <p>
 * 映射表：{@code sys_menu}。用于记录系统菜单的基本信息和层级关系。
 * 字段：
 * - {@code moduleId} 模块ID；
 * - {@code parentId} 父菜单ID；
 * - {@code type} 菜单类型；
 * - {@code menuLevel} 菜单层级；
 * - {@code path} 菜单路径；
 * - {@code name} 菜单名称；
 * - {@code icon} 菜单图标；
 * - {@code componentKey} 组件键；
 * - {@code permKey} 权限键；
 * - {@code menuMode} 菜单模式；
 * - {@code externalUrl} 外联URL；
 * - {@code orderNum} 排序号；
 * - {@code visible} 是否可见；
 * - {@code status} 状态。
 */
@Data
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    /** 模块ID */
    private Long moduleId;
    /** 父菜单ID */
    private Long parentId;
    /** 菜单类型 */
    private String type;
    /** 菜单层级：1=一级菜单(目录), 2=二级菜单, 3=三级菜单 */
    private Integer menuLevel;
    /** 菜单路径 */
    private String path;
    /** 菜单名称 */
    private String name;
    /** 菜单图标 */
    private String icon;
    /** 组件键 */
    private String componentKey;
    /** 权限键 */
    private String permKey;
    /** 菜单模式：embedded=内嵌，external=外联 */
    private String menuMode;
    /** 外联URL */
    private String externalUrl;
    /** 排序号 */
    private Integer orderNum;
    /** 是否可见：false=隐藏，true=显示 */
    private Boolean visible;
    /** 状态：false=禁用，true=启用 */
    private Boolean status;
}
