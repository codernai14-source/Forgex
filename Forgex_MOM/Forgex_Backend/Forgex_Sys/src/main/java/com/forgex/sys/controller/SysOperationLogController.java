package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.ExcelOperationLogExportDTO;
import com.forgex.sys.domain.dto.SysOperationLogQueryDTO;
import com.forgex.sys.domain.entity.SysOperationLog;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.mapper.SysOperationLogMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.service.ExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 操作日志控制器。
 * <p>
 * 提供操作日志分页查询和导出能力。列表查询会根据日志中的用户 ID 关联系统用户账号，
 * 避免前端账号列直接展示人员 ID。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/operationLog")
@RequiredArgsConstructor
public class SysOperationLogController {

    /**
     * 操作日志 Mapper。
     */
    private final SysOperationLogMapper logMapper;

    /**
     * Excel 导出服务。
     */
    private final ExcelExportService excelExportService;

    /**
     * 系统用户 Mapper，用于按用户 ID 补充登录账号。
     */
    private final SysUserMapper userMapper;

    /**
     * 分页查询操作日志。
     * <p>
     * 支持模块、操作类型、用户名称、账号、用户 ID 与操作时间范围过滤。
     * </p>
     *
     * @param query 查询条件
     * @return 操作日志分页结果
     */
    @PostMapping("/page")
    @RequirePerm("sys:operation-log:view")
    public R<Page<SysOperationLog>> page(@RequestBody SysOperationLogQueryDTO query) {
        long current = query != null && query.getCurrent() != null ? query.getCurrent() : 1L;
        long size = query != null && query.getSize() != null ? query.getSize() : 20L;
        Page<SysOperationLog> page = new Page<>(current, size);
        Page<SysOperationLog> result = logMapper.selectPage(page, buildQueryWrapper(query));
        enrichOperationLogAccounts(result.getRecords());
        return R.ok(result);
    }

    /**
     * 导出操作日志。
     *
     * @param body     导出参数
     * @param response HTTP 响应
     */
    @RequirePerm("sys:operation-log:export")
    @PostMapping("/export")
    public void export(@RequestBody ExcelOperationLogExportDTO body, HttpServletResponse response) {
        excelExportService.exportOperationLog(body, response);
    }

    /**
     * 构建操作日志查询条件。
     *
     * @param query 查询参数
     * @return MyBatis-Plus 查询包装器
     */
    private LambdaQueryWrapper<SysOperationLog> buildQueryWrapper(SysOperationLogQueryDTO query) {
        LambdaQueryWrapper<SysOperationLog> qw = new LambdaQueryWrapper<SysOperationLog>()
            .orderByDesc(SysOperationLog::getOperationTime);

        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            qw.eq(SysOperationLog::getTenantId, tenantId);
        }

        if (query != null && StringUtils.hasText(query.getModule())) {
            qw.eq(SysOperationLog::getModule, query.getModule());
        }
        if (query != null && StringUtils.hasText(query.getOperationType())) {
            qw.eq(SysOperationLog::getOperationType, query.getOperationType());
        }
        if (query != null && StringUtils.hasText(query.getAccount())) {
            List<Long> matchedUserIds = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .select(SysUser::getId)
                    .like(SysUser::getAccount, query.getAccount()))
                .stream()
                .map(SysUser::getId)
                .collect(Collectors.toList());
            if (matchedUserIds.isEmpty()) {
                qw.eq(SysOperationLog::getUserId, -1L);
            } else {
                qw.in(SysOperationLog::getUserId, matchedUserIds);
            }
        }
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
        if (query != null && StringUtils.hasText(query.getUsername())) {
            qw.like(SysOperationLog::getUsername, query.getUsername());
        }
        if (query != null && query.getUserId() != null) {
            qw.eq(SysOperationLog::getUserId, query.getUserId());
        }

        return qw;
    }

    /**
     * 批量补充操作日志中的登录账号。
     *
     * @param records 操作日志记录
     */
    private void enrichOperationLogAccounts(List<SysOperationLog> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> userIds = records.stream()
            .map(SysOperationLog::getUserId)
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return;
        }
        Map<Long, String> accountMap = userMapper.selectBatchIds(userIds).stream()
            .map(user -> (SysUser) user)
            .filter(user -> user.getId() != null)
            .collect(Collectors.toMap(SysUser::getId, SysUser::getAccount, (left, right) -> left));
        records.forEach(record -> record.setAccount(accountMap.get(record.getUserId())));
    }

    /**
     * 将前端传入的时间字符串解析为 {@link LocalDateTime}。
     *
     * @param raw 原始时间字符串
     * @return 解析成功的时间，解析失败返回 null
     */
    private LocalDateTime parseToLocalDateTime(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
        }
        return null;
    }
}
