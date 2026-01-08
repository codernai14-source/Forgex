package com.forgex.sys.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 菜单树VO
 * 
 * @author Forgex Team
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
     * 子菜单列表
     */
    private List<MenuTreeVO> children;
}
