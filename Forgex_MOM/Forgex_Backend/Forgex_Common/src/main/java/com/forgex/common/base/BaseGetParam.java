package com.forgex.common.base;

import lombok.Data;

/**
 * 通用查询传参实体（分页查询基类）
 * @author lyq
 * @date 2025年11月01日 11:22
 * @description: 所有分页查询参数都应继承此类
 * @version: 1.0
 */
@Data
public class BaseGetParam {

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;
    
    /**
     * 每页条数
     */
    private Integer pageSize = 10;
    
    /**
     * 排序字段
     */
    private String orderBy;
    
    /**
     * 排序方式：asc/desc
     */
    private String orderDirection;
    
    /**
     * 开始时间
     */
    private String startDate;
    
    /**
     * 结束时间
     */
    private String endDate;

}
