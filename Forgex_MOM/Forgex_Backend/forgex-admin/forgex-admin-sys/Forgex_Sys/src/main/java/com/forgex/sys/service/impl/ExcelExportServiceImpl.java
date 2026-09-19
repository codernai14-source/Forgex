/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.security.LoginFailureReasonResolver;
import com.forgex.common.service.excel.ExcelFileService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.TenantContextIgnore;
import com.forgex.sys.domain.dto.ExcelLoginLogExportDTO;
import com.forgex.sys.domain.dto.ExcelOperationLogExportDTO;
import com.forgex.sys.domain.dto.ExcelUserExportDTO;
import com.forgex.sys.domain.dto.LoginLogQueryDTO;
import com.forgex.sys.domain.dto.SysOperationLogQueryDTO;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.domain.entity.SysOperationLog;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.mapper.LoginLogMapper;
import com.forgex.sys.mapper.SysOperationLogMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.service.ExcelExportService;
import com.forgex.sys.service.ISysUserService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * System Excel export service.
 */
@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    private final ExcelFileService excelFileService;
    private final LoginLogMapper loginLogMapper;
    private final SysOperationLogMapper operationLogMapper;
    private final SysUserMapper userMapper;
    private final ISysUserService userService;

    public ExcelExportServiceImpl(ExcelFileService excelFileService,
                                  LoginLogMapper loginLogMapper,
                                  SysOperationLogMapper operationLogMapper,
                                  SysUserMapper userMapper,
                                  ISysUserService userService) {
        this.excelFileService = excelFileService;
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<InputStreamResource> exportLoginLog(ExcelLoginLogExportDTO body) {
        String tableCode = body == null ? null : body.getTableCode();
        LoginLogQueryDTO query = body == null ? null : body.getQuery();
        List<LoginLog> list = executeIgnoringTenant(() -> loginLogMapper.selectList(buildLoginLogWrapper(query)));
        list.forEach(log -> log.setReasonText(LoginFailureReasonResolver.resolve(log.getReason())));
        return excelFileService.buildExportResponse(tableCode, list, timestampFilename("login-log"));
    }

    @Override
    public ResponseEntity<InputStreamResource> exportUser(ExcelUserExportDTO body) {
        String tableCode = body == null ? null : body.getTableCode();
        SysUserQueryDTO query = body == null ? null : body.getQuery();
        List<SysUserDTO> list = userService.listUsers(query);
        return excelFileService.buildExportResponse(tableCode, list, timestampFilename("sys-user"));
    }

    @Override
    public ResponseEntity<InputStreamResource> exportOperationLog(ExcelOperationLogExportDTO body) {
        String tableCode = body == null ? null : body.getTableCode();
        SysOperationLogQueryDTO query = body == null ? null : body.getQuery();
        List<SysOperationLog> list = operationLogMapper.selectList(buildOperationLogWrapper(query));
        enrichOperationLogAccounts(list);
        list.forEach(log -> log.setOperationTypeText(resolveOperationTypeText(log.getOperationType())));
        return excelFileService.buildExportResponse(tableCode, list, timestampFilename("operation-log"));
    }

    private LambdaQueryWrapper<LoginLog> buildLoginLogWrapper(LoginLogQueryDTO query) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<LoginLog>()
            .like(query != null && StringUtils.hasText(query.getAccount()), LoginLog::getAccount, query == null ? null : query.getAccount())
            .eq(query != null && query.getStatus() != null, LoginLog::getStatus, query == null ? null : query.getStatus())
            .ge(query != null && query.getStartTime() != null, LoginLog::getLoginTime, query == null ? null : query.getStartTime())
            .le(query != null && query.getEndTime() != null, LoginLog::getLoginTime, query == null ? null : query.getEndTime());
        applyLoginLogTenantScope(wrapper);
        wrapper.orderByDesc(LoginLog::getLoginTime);
        return wrapper;
    }

    private void applyLoginLogTenantScope(LambdaQueryWrapper<LoginLog> wrapper) {
        Long tenantId = TenantContext.get();
        wrapper.and(w -> {
            if (tenantId != null) {
                w.eq(LoginLog::getTenantId, tenantId).or();
            }
            w.eq(LoginLog::getTenantId, 0L).or().isNull(LoginLog::getTenantId);
        });
    }

    private <T> T executeIgnoringTenant(Supplier<T> supplier) {
        boolean oldIgnore = TenantContextIgnore.isIgnore();
        TenantContextIgnore.setIgnore(true);
        try {
            return supplier.get();
        } finally {
            if (!oldIgnore) {
                TenantContextIgnore.clear();
            }
        }
    }

    private void enrichOperationLogAccounts(List<SysOperationLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        Set<Long> userIds = logs.stream()
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
        logs.forEach(log -> log.setAccount(resolveOperationLogAccount(log, accountMap)));
    }

    private String resolveOperationLogAccount(SysOperationLog log, Map<Long, String> accountMap) {
        if (log == null) {
            return "";
        }
        String account = accountMap.get(log.getUserId());
        if (StringUtils.hasText(account)) {
            return account;
        }
        if (StringUtils.hasText(log.getUsername())) {
            return log.getUsername();
        }
        return log.getUserId() == null ? "" : String.valueOf(log.getUserId());
    }

    private String resolveOperationTypeText(String operationType) {
        if (!StringUtils.hasText(operationType)) {
            return "";
        }
        return switch (operationType.toUpperCase()) {
            case "ADD", "CREATE" -> "新增";
            case "EDIT", "UPDATE" -> "修改";
            case "DELETE" -> "删除";
            case "QUERY", "SEARCH" -> "查询";
            case "EXPORT" -> "导出";
            case "IMPORT" -> "导入";
            case "LOGIN" -> "登录";
            case "LOGOUT" -> "退出";
            default -> operationType;
        };
    }

    private LambdaQueryWrapper<SysOperationLog> buildOperationLogWrapper(SysOperationLogQueryDTO query) {
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
        if (query != null && StringUtils.hasText(query.getUsername())) {
            qw.like(SysOperationLog::getUsername, query.getUsername());
        }
        if (query != null && query.getUserId() != null) {
            qw.eq(SysOperationLog::getUserId, query.getUserId());
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
        return qw;
    }

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

    private String timestampFilename(String prefix) {
        return prefix + "-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
    }
}
