package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社交登录实体类
 * <p>
 * 对应数据库表：sys_social_login，存储用户的社交登录绑定信息。
 * </p>
 *
 * <p>
 * 支持多种社交平台登录，如微信、钉钉等，通过 openId 和 unionId 唯一标识用户。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.service.SocialLoginService
 * @see com.forgex.auth.mapper.SysSocialLoginMapper
 */
@Data
@TableName("sys_social_login")
public class SysSocialLogin extends BaseEntity {

    /**
     * 用户 ID
     * <p>关联到用户表的主键，标识该社交账号绑定的系统用户</p>
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 社交平台
     * <p>
     * 可选值：
     * <ul>
     *     <li>wechat：微信</li>
     *     <li>dingtalk：钉钉</li>
     * </ul>
     * </p>
     */
    @TableField("platform")
    private String platform;

    /**
     * 开放平台用户 ID
     * <p>由社交平台分配的唯一用户标识符</p>
     */
    @TableField("open_id")
    private String openId;

    /**
     * 开放平台用户 UnionID
     * <p>
     * 同一用户在多个应用下的统一标识符（可选）<br>
     * 用于识别同一用户在同一个开放平台下的不同应用
     * </p>
     */
    @TableField("union_id")
    private String unionId;

    /**
     * 绑定时间
     * <p>用户将该社交账号绑定到系统的时间</p>
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("bind_time")
    private LocalDateTime bindTime;
}
