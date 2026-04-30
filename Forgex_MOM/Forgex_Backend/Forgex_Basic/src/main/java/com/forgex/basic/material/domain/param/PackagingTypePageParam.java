
package com.forgex.basic.material.domain.param;

import lombok.Data;

/**
 * 包装方式分页查询参数
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@Data
public class PackagingTypePageParam {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 包装方式编码
     */
    private String packagingCode;
    
    /**
     * 包装方式名称
     */
    private String packagingName;
    
    /**
     * 包装材料
     */
    private String packagingMaterial;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
