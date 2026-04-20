package com.forgex.integration.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 第三方授权信息实体类
 * <p>
 * 对应数据库表：fx_third_authorization
 * 用于存储第三方系统的授权信息，支持白名单和 Token 两种授权方式
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fx_third_authorization")
public class ThirdAuthorization extends BaseEntity {

    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 第三方系统 ID（关联 fx_third_system 表）
     */
    private Long thirdSystemId;

    /**
     * 授权方式：WHITELIST-白名单，TOKEN-限时 token
     */
    private String authType;

    /**
     * Token 值（授权方式为 TOKEN 时必填）
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
     * 白名单 IP 列表（授权方式为 WHITELIST 时使用，多个 IP 用逗号分隔）
     */
    private String whitelistIps;

    /**
     * 状态：0-禁用，1-启用
     * @see com.forgex.common.constant.SystemConstants#STATUS_DISABLED
     * @see com.forgex.common.constant.SystemConstants#STATUS_NORMAL
     */
    private Integer status;

    /**
     * 备注信息
     */
    private String remark;
}
