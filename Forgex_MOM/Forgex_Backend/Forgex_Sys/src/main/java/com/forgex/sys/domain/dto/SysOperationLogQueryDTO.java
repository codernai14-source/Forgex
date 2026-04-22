package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 操作日志查询 DTO。
 * <p>
 * 封装操作日志分页查询、模块、操作类型、时间范围、用户名称、账号和用户 ID 等查询条件。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class SysOperationLogQueryDTO {
    /**
     * 当前页码。
     */
    private Long current;

    /**
     * 每页大小。
     */
    private Long size;

    /**
     * 模块名称。
     */
    private String module;

    /**
     * 操作类型。
     */
    private String operationType;

    /**
     * 开始时间，格式支持 yyyy-MM-dd HH:mm:ss 或 ISO-8601。
     */
    private String startTime;

    /**
     * 结束时间，格式支持 yyyy-MM-dd HH:mm:ss 或 ISO-8601。
     */
    private String endTime;

    /**
     * 用户名称。
     */
    private String username;

    /**
     * 登录账号。
     * <p>
     * 对应 {@code forgex_admin.sys_user.account}，用于替代历史页面误展示的人员 ID。
     * </p>
     */
    private String account;

    /**
     * 用户 ID。
     */
    private Long userId;
}
