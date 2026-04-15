package com.forgex.integration.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 第三方授权信息 VO
 * <p>
 * 用于前端展示和数据交互
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
public class ThirdAuthorizationVO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 第三方系统 ID
     */
    private Long thirdSystemId;

    /**
     * 第三方系统名称（关联查询）
     */
    private String systemName;

    /**
     * 授权方式：WHITELIST-白名单，TOKEN-限时 token
     */
    private String authType;

    /**
     * Token 值
     */
    private String tokenValue;

    /**
     * Token 有效期（小时）
     */
    private Integer tokenExpireHours;

    /**
     * Token 过期时间
     */
    private LocalDateTime tokenExpireTime;

    /**
     * 白名单 IP 列表
     */
    private String whitelistIps;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;
}
