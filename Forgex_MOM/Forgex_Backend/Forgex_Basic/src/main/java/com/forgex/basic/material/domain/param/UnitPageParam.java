package com.forgex.basic.material.domain.param;

import lombok.Data;

/**
 * 计量单位分页查询参数
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@Data
public class UnitPageParam {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 单位编码
     */
    private String unitCode;
    
    /**
     * 单位名称
     */
    private String unitName;
    
    /**
     * 单位分类
     */
    private String unitCategory;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
