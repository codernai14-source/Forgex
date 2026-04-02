package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.sys.domain.dto.ExcelOperationLogExportDTO;
import com.forgex.sys.domain.dto.SysOperationLogQueryDTO;
import com.forgex.sys.domain.entity.SysOperationLog;
import com.forgex.sys.mapper.SysOperationLogMapper;
import com.forgex.sys.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 操作日志控制器
 * <p>提供操作日志的分页查询功能。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/operationLog")
@RequiredArgsConstructor
public class SysOperationLogController {

    /**
     * 操作日志Mapper
     */
    private final SysOperationLogMapper logMapper;

    private final ExcelExportService excelExportService;

    /**
     * 分页查询操作日志
     * <p>根据查询条件分页查询操作日志。</p>
     * 
     * @param query 查询条件
     * @return 操作日志分页结果
     */
    @PostMapping("/page")
    @RequirePerm("sys:operation-log:view")
    public R<Page<SysOperationLog>> page(@RequestBody SysOperationLogQueryDTO query) {
        // 获取分页参数
        long current = query != null && query.getCurrent() != null ? query.getCurrent() : 1L;
        long size = query != null && query.getSize() != null ? query.getSize() : 20L;
        Page<SysOperationLog> page = new Page<>(current, size);

        // 构建查询条件（包含租户、模块、操作类型、时间范围等过滤）
        LambdaQueryWrapper<SysOperationLog> qw = buildQueryWrapper(query);

        return R.ok(logMapper.selectPage(page, qw));
    }

    @RequirePerm("sys:operation-log:export")
    @PostMapping("/export")
    public void export(@RequestBody ExcelOperationLogExportDTO body, HttpServletResponse response) {
        excelExportService.exportOperationLog(body, response);
    }

    /**
     * 构建操作日志查询条件。
     *
     * @param query 查询参数
     * @return 查询包装器
     */
    private LambdaQueryWrapper<SysOperationLog> buildQueryWrapper(SysOperationLogQueryDTO query) {
        // 1. 基础排序：按操作时间倒序
        LambdaQueryWrapper<SysOperationLog> qw = new LambdaQueryWrapper<SysOperationLog>()
                .orderByDesc(SysOperationLog::getOperationTime);

        // 2. 租户ID过滤：默认使用上下文租户
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            qw.eq(SysOperationLog::getTenantId, tenantId);
        }

        // 3. 模块过滤
        if (query != null && StringUtils.hasText(query.getModule())) {
            qw.eq(SysOperationLog::getModule, query.getModule());
        }

        // 4. 操作类型过滤
        if (query != null && StringUtils.hasText(query.getOperationType())) {
            qw.eq(SysOperationLog::getOperationType, query.getOperationType());
        }

        // 5. 时间范围过滤
        if (query != null && StringUtils.hasText(query.getStartTime())) {
            LocalDateTime start = parseToLocalDateTime(query.getStartTime());
            if (start != null) {
                qw.ge(SysOperationLog::getOperationTime, start);
            }
        }
        if (query != null && StringUtils.hasText(query.getEndTime())) {
            LocalDateTime end = parseToLocalDateTime(query.getEndTime());
            if (end != null) {
                qw.le(SysOperationLog::getOperationTime, end);
            }
        }

        return qw;
    }

    /**
     * 解析时间字符串为 {@link LocalDateTime}。
     *
     * @param raw 原始字符串
     * @return 解析后的时间，解析失败返回 null
     */
    private LocalDateTime parseToLocalDateTime(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }

        // 兼容常见格式：yyyy-MM-dd HH:mm:ss
        try {
            return LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
        }

        // ISO-8601（例如 2026-02-05T12:34:56）
        try {
            return LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
        }

        return null;
    }

}

