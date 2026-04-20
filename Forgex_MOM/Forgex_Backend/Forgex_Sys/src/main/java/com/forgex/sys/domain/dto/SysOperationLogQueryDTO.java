package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 操作日志查询DTO
 * <p>封装操作日志的查询条件。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class SysOperationLogQueryDTO {
    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账号（用户 ID）
     */
    private Long userId;
}

