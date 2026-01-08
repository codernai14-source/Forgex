package com.forgex.sys.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单DTO
 * 
 * @author Forgex Team
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
     * 是否可见：0=隐藏，1=显示
     */
    private Integer visible;
    
    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}
