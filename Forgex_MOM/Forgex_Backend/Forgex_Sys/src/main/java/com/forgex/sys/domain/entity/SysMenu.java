package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    private Long moduleId;
    private Long parentId;
    private String type;
    private Integer menuLevel;  // 菜单层级：1=一级菜单(目录), 2=二级菜单, 3=三级菜单
    private String path;
    private String name;
    private String icon;
    private String componentKey;
    private String permKey;
    private String menuMode;    // 菜单模式：embedded=内嵌，external=外联
    private String externalUrl; // 外联URL
    private Integer orderNum;
    private Integer visible;
    private Integer status;
}
