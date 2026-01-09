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
package com.forgex.sys.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单DTO
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Data
public class SysMenuDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 模块ID
     */
    private Long moduleId;
    
    /**
     * 模块名称（关联查询）
     */
    private String moduleName;
    
    /**
     * 父菜单ID
     */
    private Long parentId;
    
    /**
     * 父菜单名称（关联查询）
     */
    private String parentName;
    
    /**
     * 类型：catalog=目录, menu=菜单, button=按钮
     */
    private String type;
    
    /**
     * 菜单层级：1=一级菜单(目录), 2=二级菜单, 3=三级菜单
     */
    private Integer menuLevel;
    
    /**
     * 路径
     */
    private String path;
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 组件Key
     */
    private String componentKey;
    
    /**
     * 权限标识
     */
    private String permKey;
    
    /**
     * 菜单模式：embedded=内嵌，external=外联
     */
    private String menuMode;
    
    /**
     * 外联URL
     */
    private String externalUrl;
    
    /**
     * 排序号
     */
    private Integer orderNum;
    
    /**
     * 是否可见：false=隐藏，true=显示
     */
    private Boolean visible;
    
    /**
     * 状态：false=禁用，true=启用
     */
    private Boolean status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}
