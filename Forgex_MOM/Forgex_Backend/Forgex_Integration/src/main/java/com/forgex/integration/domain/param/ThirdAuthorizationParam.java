package com.forgex.integration.domain.param;

import lombok.Data;

/**
 * 第三方授权查询参数类
 * <p>
 * 用于接收前端传递的查询条件
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
public class ThirdAuthorizationParam {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 第三方系统 ID
     */
    private Long thirdSystemId;

    /**
     * 授权方式：WHITELIST-白名单，TOKEN-限时 token
     */
    private String authType;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}
