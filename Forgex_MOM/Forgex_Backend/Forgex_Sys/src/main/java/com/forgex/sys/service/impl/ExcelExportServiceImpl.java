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

import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.ExcelFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.sys.domain.dto.ExcelLoginLogExportDTO;
import com.forgex.sys.domain.dto.ExcelOperationLogExportDTO;
import com.forgex.sys.domain.dto.ExcelUserExportDTO;
import com.forgex.sys.domain.dto.LoginLogQueryDTO;
import com.forgex.sys.domain.dto.SysOperationLogQueryDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.domain.entity.SysOperationLog;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.mapper.LoginLogMapper;
import com.forgex.sys.mapper.SysOperationLogMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.service.ExcelExportService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel导出服务实现类
 * <p>负责处理Excel导出相关的业务逻辑，包括数据查询、转换和文件生成等。</p>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-15
 */
@Service
public class ExcelExportServiceImpl implements ExcelExportService {
    
    /**
     * Excel配置服务
     */
    private final ExcelConfigService excelConfigService;
    
    /**
     * Excel文件服务
     */
    private final ExcelFileService excelFileService;
    
    /**
     * 登录日志Mapper
     */
    private final LoginLogMapper loginLogMapper;
    
    /**
     * 用户Mapper
     */
    private final SysUserMapper userMapper;

    private final SysOperationLogMapper operationLogMapper;
    
    /**
     * 构造函数
     * 
     * @param excelConfigService Excel配置服务
     * @param excelFileService   Excel文件服务
     * @param loginLogMapper     登录日志Mapper
     * @param userMapper         用户Mapper
     */
    public ExcelExportServiceImpl(ExcelConfigService excelConfigService,
                                  ExcelFileService excelFileService,
                                  LoginLogMapper loginLogMapper,
                                  SysUserMapper userMapper,
                                  SysOperationLogMapper operationLogMapper) {
        this.excelConfigService = excelConfigService;
        this.excelFileService = excelFileService;
        this.loginLogMapper = loginLogMapper;
        this.userMapper = userMapper;
        this.operationLogMapper = operationLogMapper;
    }
    
    /**
     * 导出登录日志
     * 
     * @param body     导出参数，包含tableCode和查询条件
     * @param response HTTP响应对象
     */
    @Override
    public void exportLoginLog(ExcelLoginLogExportDTO body, HttpServletResponse response) {
        String tableCode = body == null ? null : body.getTableCode();
        LoginLogQueryDTO query = body == null ? null : body.getQuery();
        FxExcelExportConfigDTO cfg = excelConfigService.getExportConfigByCode(tableCode);
        
        // 查询登录日志数据
        List<LoginLog> list = loginLogMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LoginLog>()
                .like(query != null && StringUtils.hasText(query.getAccount()), LoginLog::getAccount, query.getAccount())
                .eq(query != null && query.getStatus() != null, LoginLog::getStatus, query.getStatus())
                .ge(query != null && query.getStartTime() != null, LoginLog::getLoginTime, query.getStartTime())
                .le(query != null && query.getEndTime() != null, LoginLog::getLoginTime, query.getEndTime())
                .orderByDesc(LoginLog::getLoginTime));
        
        // 转换数据格式
        List<Map<String, Object>> rows = list.stream().map(this::toLoginLogMap).collect(Collectors.toList());
        
        // 生成Excel文件
        byte[] bytes = excelFileService.buildExportFile(cfg, rows);
        
        // 生成文件名
        String filename = "login-log-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now()) + getFileExtension(cfg);
        
        // 写入响应
        writeFileToResponse(response, bytes, getContentType(cfg), filename);
    }
    
    /**
     * 导出用户数据
     * 
     * @param body     导出参数，包含tableCode和查询条件
     * @param response HTTP响应对象
     */
    @Override
    public void exportUser(ExcelUserExportDTO body, HttpServletResponse response) {
        String tableCode = body == null ? null : body.getTableCode();
        SysUserQueryDTO query = body == null ? null : body.getQuery();
        FxExcelExportConfigDTO cfg = excelConfigService.getExportConfigByCode(tableCode);
        
        // 查询用户数据
        List<SysUser> list = userMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .like(query != null && StringUtils.hasText(query.getAccount()), SysUser::getAccount, query.getAccount())
                .like(query != null && StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .eq(query != null && query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .orderByDesc(SysUser::getId));
        
        // 转换数据格式
        List<Map<String, Object>> rows = list.stream().map(this::toUserMap).collect(Collectors.toList());
        
        // 生成Excel文件
        byte[] bytes = excelFileService.buildExportFile(cfg, rows);
        
        // 生成文件名
        String filename = "sys-user-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now()) + getFileExtension(cfg);
        
        // 写入响应
        writeFileToResponse(response, bytes, getContentType(cfg), filename);
    }

    @Override
    public void exportOperationLog(ExcelOperationLogExportDTO body, HttpServletResponse response) {
        String tableCode = body == null ? null : body.getTableCode();
        SysOperationLogQueryDTO query = body == null ? null : body.getQuery();
        FxExcelExportConfigDTO cfg = excelConfigService.getExportConfigByCode(tableCode);

        List<SysOperationLog> list = operationLogMapper.selectList(buildOperationLogWrapper(query));
        List<Map<String, Object>> rows = list.stream().map(this::toOperationLogMap).collect(Collectors.toList());
        byte[] bytes = excelFileService.buildExportFile(cfg, rows);
        String filename = "operation-log-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + getFileExtension(cfg);
        writeFileToResponse(response, bytes, getContentType(cfg), filename);
    }
    
    /**
     * 将登录日志对象转换为Map格式
     * 
     * @param log 登录日志对象
     * @return Map格式的登录日志数据
     */
    private Map<String, Object> toLoginLogMap(LoginLog log) {
        Map<String, Object> m = new HashMap<>();
        m.put("account", log.getAccount());
        m.put("loginIp", log.getLoginIp());
        m.put("loginRegion", log.getLoginRegion());
        m.put("userAgent", log.getUserAgent());
        m.put("loginTime", log.getLoginTime());
        m.put("logoutTime", log.getLogoutTime());
        m.put("logoutReason", log.getLogoutReason());
        m.put("status", log.getStatus());
        m.put("reason", log.getReason());
        return m;
    }
    
    /**
     * 将用户对象转换为Map格式
     * 
     * @param u 用户对象
     * @return Map格式的用户数据
     */
    private Map<String, Object> toUserMap(SysUser u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("account", u.getAccount());
        m.put("username", u.getUsername());
        m.put("phone", u.getPhone());
        m.put("email", u.getEmail());
        m.put("status", u.getStatus());
        m.put("gender", u.getGender());
        m.put("avatar", u.getAvatar());
        m.put("departmentId", u.getDepartmentId());
        m.put("positionId", u.getPositionId());
        m.put("lastLoginTime", u.getLastLoginTime());
        m.put("lastLoginIp", u.getLastLoginIp());
        m.put("lastLoginRegion", u.getLastLoginRegion());
        return m;
    }

    private Map<String, Object> toOperationLogMap(SysOperationLog log) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", log.getId());
        m.put("tenantId", log.getTenantId());
        m.put("module", log.getModule());
        m.put("operationType", log.getOperationType());
        m.put("username", log.getUsername());
        m.put("requestMethod", log.getRequestMethod());
        m.put("requestUrl", log.getRequestUrl());
        m.put("requestParams", log.getRequestParams());
        m.put("responseStatus", log.getResponseStatus());
        m.put("costTime", log.getCostTime());
        m.put("ip", log.getIp());
        m.put("operationTime", log.getOperationTime());
        m.put("menuPath", log.getMenuPath());
        m.put("detailText", log.getDetailText());
        return m;
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
    
    /**
     * 获取配置对应的内容类型
     * 
     * @param cfg Excel导出配置
     * @return 内容类型字符串
     */
    @Override
    public String getContentType(FxExcelExportConfigDTO cfg) {
        if (cfg != null && "csv".equalsIgnoreCase(cfg.getExportFormat())) {
            return "text/csv";
        }
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }
    
    /**
     * 获取配置对应的文件扩展名
     * 
     * @param cfg Excel导出配置
     * @return 文件扩展名
     */
    @Override
    public String getFileExtension(FxExcelExportConfigDTO cfg) {
        if (cfg != null && "csv".equalsIgnoreCase(cfg.getExportFormat())) {
            return ".csv";
        }
        return ".xlsx";
    }
    
    /**
     * 处理文件名，确保URL安全
     * 
     * @param filename 原始文件名
     * @return URL安全的文件名
     */
    @Override
    public String getSafeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        } catch (Exception e) {
            return filename;
        }
    }
    
    /**
     * 将字节数据写入HTTP响应
     * 
     * @param response     HTTP响应对象
     * @param bytes        文件字节数据
     * @param contentType  内容类型
     * @param filename     文件名
     */
    @Override
    public void writeFileToResponse(HttpServletResponse response, byte[] bytes, String contentType, String filename) {
        try {
            response.setContentType(contentType);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + getSafeFilename(filename));
            response.getOutputStream().write(bytes == null ? new byte[0] : bytes);
            response.flushBuffer();
        } catch (Exception ignored) {
            // 忽略异常，确保响应能够正确关闭
        }
    }
}
