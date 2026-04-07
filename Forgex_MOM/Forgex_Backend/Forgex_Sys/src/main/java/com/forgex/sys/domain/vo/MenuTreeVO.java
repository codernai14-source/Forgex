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

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单树VO
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Data
public class MenuTreeVO {
    
    /**
     * 菜单ID
     */
    private Long id;
    
    /**
     * 父菜单ID
     */
    private Long parentId;
    
    /**
     * 菜单名称
     */
    private String name;
    
    /**
     * 菜单路径
     */
    private String path;
    
    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 组件键
     */
    private String componentKey;
    
    /**
     * 菜单类型
     */
    private String type;
    
    /**
     * 菜单层级
     */
    private Integer menuLevel;
    
    /**
     * 排序号
     */
    private Integer orderNum;
    
    /**
     * 菜单模式：embedded=内嵌，external=外联
     */
    private String menuMode;
    
    /**
     * 权限标识
     */
    private String permKey;
    
    /**
     * 是否可见：false=隐藏，true=显示
     */
    private Boolean visible;
    
    /**
     * 状态：false=禁用，true=启用
     */
    private Boolean status;
    
    /**
     * 是否已授权（用于前端勾选框）
     */
    private Boolean checked;
    
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
     * 子菜单列表
     */
    private List<MenuTreeVO> children;
}
