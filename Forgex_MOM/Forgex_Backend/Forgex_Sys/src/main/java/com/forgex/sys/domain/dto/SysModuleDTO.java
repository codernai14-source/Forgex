package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 模块DTO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
public class SysModuleDTO {
    
    /** 主键ID */
    private Long id;
    
    /** 模块编码 */
    private String code;
    
    /** 模块名称 */
    private String name;
    
    /** 图标 */
    private String icon;
    
    /** 排序号 */
    private Integer orderNum;
    
    /** 是否可见：0=隐藏，1=显示 */
    private Integer visible;
    
    /** 状态：0=禁用，1=启用 */
    private Integer status;
    
    /** 租户ID */
    private Long tenantId;
    
    /** 创建时间 */
    private String createTime;
    
    /** 更新时间 */
    private String updateTime;
}
