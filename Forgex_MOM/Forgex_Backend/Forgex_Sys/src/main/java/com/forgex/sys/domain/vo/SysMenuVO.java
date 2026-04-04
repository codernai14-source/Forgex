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
package com.forgex.sys.domain.vo;

import com.forgex.common.base.BaseEntity;
import com.forgex.common.dict.DictI18n;
import lombok.Data;

/**
 * 系统菜单视图对象
 * 用于向前端返回菜单数据，包含展示需要的衍生字段
 *
 * @author coder_nai
 * @version 1.0.0
 */
@Data
public class SysMenuVO extends BaseEntity {
    /** 模块ID */
    private Long moduleId;
    
    /** 模块名称（关联查询结果） */
    private String moduleName;
    
    /** 父菜单ID */
    private Long parentId;
    
    /** 父菜单名称（关联查询结果） */
    private String parentName;
    
    /** 菜单类型 */
    @DictI18n(nodePathConst = "menu_type", targetField = "typeText")
    private String type;
    
    /** 菜单类型文本（字典翻译结果） */
    private String typeText;
    
    /** 菜单层级：1=一级菜单(目录), 2=二级菜单, 3=三级菜单 */
    private Integer menuLevel;
    
    /** 菜单路径 */
    private String path;
    
    /** 菜单名称 */
    private String name;
    
    private String nameI18nJson;
    
    /** 菜单图标 */
    private String icon;
    
    /** 组件键 */
    private String componentKey;
    
    /** 权限键 */
    private String permKey;
    
    /** 菜单模式：embedded=内嵌，external=外联 */
    @DictI18n(nodePathConst = "menu_mode", targetField = "menuModeText")
    private String menuMode;
    
    /** 菜单模式文本（字典翻译结果） */
    private String menuModeText;
    
    /** 外联URL */
    private String externalUrl;
    
    /** 排序号 */
    private Integer orderNum;
    
    /** 是否可见：false=隐藏，true=显示 */
    @DictI18n(nodePathConst = "visible_status", targetField = "visibleText")
    private Boolean visible;
    
    /** 是否可见文本（字典翻译结果） */
    private String visibleText;
    
    /** 状态：false=禁用，true=启用 */
    @DictI18n(nodePathConst = "status", targetField = "statusText")
    private Boolean status;
    
    /** 状态文本（字典翻译结果） */
    private String statusText;
    
    /** 适用租户类型：MAIN_TENANT/CUSTOMER_TENANT/SUPPLIER_TENANT/PARTNER_TENANT/PUBLIC，PUBLIC 表示适用于所有租户类型 */
    private String tenantType;
}