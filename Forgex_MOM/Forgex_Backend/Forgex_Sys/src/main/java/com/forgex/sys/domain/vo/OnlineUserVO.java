package com.forgex.sys.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线用户 VO。
 * <p>
 * 用于返回在线会话的展示字段：账号、租户、最后登录信息、会话 TTL 等。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Data
public class OnlineUserVO {

    /**
     * 会话 token（Sa-Token tokenValue）。
     */
    private String token;

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 租户ID。
     */
    private Long tenantId;

    /**
     * 登录账号。
     */
    private String account;

    private String loginTerminal;

    /**
     * 用户名称（来自 {@code sys_user.username}）。
     */
    private String username;

    /**
     * 租户名称（来自 {@code sys_tenant.tenant_name}）。
     */
    private String tenantName;

    /**
     * 用户名称（来自 {@code sys_user.username}）。
     */
    private String username;

    /**
     * 租户名称（来自 {@code sys_tenant.tenant_name}）。
     */
    private String tenantName;

    /**
     * 最后登录时间（来自 {@code sys_user.last_login_time}）。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP（来自 {@code sys_user.last_login_ip}）。
     */
    private String lastLoginIp;

    /**
     * 最后登录地区（来自 {@code sys_user.last_login_region}）。
     */
    private String lastLoginRegion;

    /**
     * 会话剩余时间（秒）。
     * <p>
     * -1 表示长期或无法获取。
     * </p>
     */
    private Long ttlSeconds;
}
