package com.forgex.integration.domain.param;

import lombok.Data;

/**
 * 接口配置查询参数类
 * <p>
 * 用于接收前端传递的查询条件
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
public class ApiConfigParam {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 接口编码（模糊查询）
     */
    private String apiCode;

    /**
     * 接口名称（模糊查询）
     */
    private String apiName;

    /**
     * 操作方向：INBOUND-外对内，OUTBOUND-内调外
     */
    private String direction;

    /**
     * 所属模块编码
     */
    private String moduleCode;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
