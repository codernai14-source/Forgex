package com.forgex.integration.domain.param;

import lombok.Data;

/**
 * 第三方系统查询参数类
 * <p>
 * 用于接收前端传递的查询条件
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
public class ThirdSystemParam {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 系统编码（模糊查询）
     */
    private String systemCode;

    /**
     * 系统名称（模糊查询）
     */
    private String systemName;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
