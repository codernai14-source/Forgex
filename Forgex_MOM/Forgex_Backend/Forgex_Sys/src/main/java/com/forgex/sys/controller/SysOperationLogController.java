package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysOperationLogQueryDTO;
import com.forgex.sys.domain.entity.SysOperationLog;
import com.forgex.sys.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 分页查询操作日志
     * <p>根据查询条件分页查询操作日志。</p>
     * 
     * @param query 查询条件
     * @return 操作日志分页结果
     */
    @PostMapping("/page")
    public R<Page<SysOperationLog>> page(@RequestBody SysOperationLogQueryDTO query) {
        // 获取分页参数
        long current = query != null && query.getCurrent() != null ? query.getCurrent() : 1L;
        long size = query != null && query.getSize() != null ? query.getSize() : 20L;
        Page<SysOperationLog> page = new Page<>(current, size);

        // 构建查询条件
        LambdaQueryWrapper<SysOperationLog> qw = new LambdaQueryWrapper<SysOperationLog>()
                .orderByDesc(SysOperationLog::getOperationTime);

        // 租户ID过滤
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            qw.eq(SysOperationLog::getTenantId, tenantId);
        }
        
        // 模块过滤
        if (query != null && StringUtils.hasText(query.getModule())) {
            qw.eq(SysOperationLog::getModule, query.getModule());
        }
        
        // 操作类型过滤
        if (query != null && StringUtils.hasText(query.getOperationType())) {
            qw.eq(SysOperationLog::getOperationType, query.getOperationType());
        }

        return R.ok(logMapper.selectPage(page, qw));
    }
}

