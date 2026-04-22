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
import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.ExcelFileService;
import com.forgex.common.tenant.TenantContext;
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
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Excel 导出服务实现类。
 * <p>
 * 负责系统内置导出场景的数据查询、数据扁平化和文件响应写出，包括登录日志、用户列表、
 * 操作日志等导出能力。导出列、文件格式和样式仍由公共 Excel 配置驱动。
 * </p>
 *
 * @author coder_nai@163.com
 * @date 2026-01-15
 * @see ExcelExportService
 */
@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    /**
     * Excel 配置服务，用于读取导出配置。
     */
    private final ExcelConfigService excelConfigService;

    /**
     * Excel 文件服务，用于根据配置生成导出文件。
     */
    private final ExcelFileService excelFileService;

    /**
     * 登录日志 Mapper。
     */
    private final LoginLogMapper loginLogMapper;

    /**
     * 操作日志 Mapper。
     */
    private final SysOperationLogMapper operationLogMapper;

    /**
     * 用户 Mapper，用于补充导出中的账号信息。
     */
    private final SysUserMapper userMapper;

    /**
     * 用户服务，用于复用用户列表查询和角色等扩展信息组装逻辑。
     */
    private final ISysUserService userService;

    /**
     * 构造函数。
     *
     * @param excelConfigService Excel 配置服务
     * @param excelFileService   Excel 文件服务
     * @param loginLogMapper     登录日志 Mapper
     * @param operationLogMapper 操作日志 Mapper
     * @param userMapper         用户 Mapper
     * @param userService        用户服务
     */
    public ExcelExportServiceImpl(ExcelConfigService excelConfigService,
                                  ExcelFileService excelFileService,
                                  LoginLogMapper loginLogMapper,
                                  SysOperationLogMapper operationLogMapper,
                                  SysUserMapper userMapper,
                                  ISysUserService userService) {
        this.excelConfigService = excelConfigService;
        this.excelFileService = excelFileService;
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    /**
     * 导出登录日志。
     *
     * @param body     导出参数，包含表格编码和查询条件
     * @param response HTTP 响应对象
     */
    @Override
    public void exportLoginLog(ExcelLoginLogExportDTO body, HttpServletResponse response) {
        String tableCode = body == null ? null : body.getTableCode();
        LoginLogQueryDTO query = body == null ? null : body.getQuery();
        FxExcelExportConfigDTO cfg = excelConfigService.getExportConfigByCode(tableCode);

        // 按页面筛选条件查询登录日志，确保导出结果与列表筛选一致。
        List<LoginLog> list = loginLogMapper.selectList(new LambdaQueryWrapper<LoginLog>()
            .like(query != null && StringUtils.hasText(query.getAccount()), LoginLog::getAccount, query.getAccount())
            .eq(query != null && query.getStatus() != null, LoginLog::getStatus, query.getStatus())
            .ge(query != null && query.getStartTime() != null, LoginLog::getLoginTime, query.getStartTime())
            .le(query != null && query.getEndTime() != null, LoginLog::getLoginTime, query.getEndTime())
            .orderByDesc(LoginLog::getLoginTime));

        // 转为通用 Map 后交给公共 Excel 服务按动态列配置生成文件。
        List<Map<String, Object>> rows = list.stream().map(this::toLoginLogMap).collect(Collectors.toList());
        byte[] bytes = excelFileService.buildExportFile(cfg, rows);
        String filename = "login-log-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + getFileExtension(cfg);
        writeFileToResponse(response, bytes, getContentType(cfg), filename);
    }

    /**
     * 导出用户数据。
     *
     * @param body     导出参数，包含表格编码和查询条件
     * @param response HTTP 响应对象
     */
    @Override
    public void exportUser(ExcelUserExportDTO body, HttpServletResponse response) {
        String tableCode = body == null ? null : body.getTableCode();
        SysUserQueryDTO query = body == null ? null : body.getQuery();
        FxExcelExportConfigDTO cfg = excelConfigService.getExportConfigByCode(tableCode);

        // 复用用户服务查询，避免导出与列表在租户、角色、扩展字段上的口径不一致。
        List<SysUserDTO> list = userService.listUsers(query);
        List<Map<String, Object>> rows = list.stream().map(this::toUserMap).collect(Collectors.toList());
        byte[] bytes = excelFileService.buildExportFile(cfg, rows);
        String filename = "sys-user-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + getFileExtension(cfg);
        writeFileToResponse(response, bytes, getContentType(cfg), filename);
    }

    /**
     * 导出操作日志。
     *
     * @param body     导出参数，包含表格编码和查询条件
     * @param response HTTP 响应对象
     */
    @Override
    public void exportOperationLog(ExcelOperationLogExportDTO body, HttpServletResponse response) {
        String tableCode = body == null ? null : body.getTableCode();
        SysOperationLogQueryDTO query = body == null ? null : body.getQuery();
        FxExcelExportConfigDTO cfg = excelConfigService.getExportConfigByCode(tableCode);

        List<SysOperationLog> list = operationLogMapper.selectList(buildOperationLogWrapper(query));
        // 操作日志表只存 userId/username，导出前补齐登录账号用于页面与文件展示一致。
        enrichOperationLogAccounts(list);
        List<Map<String, Object>> rows = list.stream().map(this::toOperationLogMap).collect(Collectors.toList());
        byte[] bytes = excelFileService.buildExportFile(cfg, rows);
        String filename = "operation-log-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + getFileExtension(cfg);
        writeFileToResponse(response, bytes, getContentType(cfg), filename);
    }

    /**
     * 将登录日志实体转换为导出行。
     *
     * @param log 登录日志实体
     * @return 导出行数据
     */
    private Map<String, Object> toLoginLogMap(LoginLog log) {
        Map<String, Object> m = new HashMap<>();
        m.put("account", log.getAccount());
        m.put("loginIp", log.getLoginIp());
        m.put("loginRegion", log.getLoginRegion());
        m.put("userAgent", log.getUserAgent());
        m.put("loginTime", log.getLoginTime());
        m.put("status", log.getStatus());
        m.put("reason", log.getReason());
        return m;
    }

    /**
     * 将用户 DTO 转换为导出行。
     *
     * @param u 用户 DTO
     * @return 导出行数据
     */
    private Map<String, Object> toUserMap(SysUserDTO u) {
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
        m.put("employeeId", u.getEmployeeId());
        m.put("userSource", u.getUserSource());
        m.put("lastLoginTime", u.getLastLoginTime());
        m.put("lastLoginIp", u.getLastLoginIp());
        m.put("lastLoginRegion", u.getLastLoginRegion());
        m.put("createTime", u.getCreateTime());
        m.put("updateTime", u.getUpdateTime());
        return m;
    }

    /**
     * 将操作日志实体转换为导出行。
     *
     * @param log 操作日志实体
     * @return 导出行数据
     */
    private Map<String, Object> toOperationLogMap(SysOperationLog log) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", log.getId());
        m.put("tenantId", log.getTenantId());
        m.put("module", log.getModule());
        m.put("operationType", resolveOperationTypeText(log.getOperationType()));
        m.put("username", log.getUsername());
        m.put("account", resolveOperationLogAccount(log));
        m.put("userId", resolveOperationLogAccount(log));
        m.put("requestMethod", log.getRequestMethod());
        m.put("requestUrl", log.getRequestUrl());
        m.put("requestParams", log.getRequestParams());
        m.put("responseResult", log.getResponseResult());
        m.put("responseStatus", log.getResponseStatus());
        m.put("costTime", log.getCostTime());
        m.put("ip", log.getIp());
        m.put("operationTime", log.getOperationTime());
        m.put("menuPath", log.getMenuPath());
        m.put("detailText", log.getDetailText());
        return m;
    }

    /**
     * 批量补充操作日志账号。
     * <p>
     * 通过 userId 一次性查询用户账号，避免导出时逐行查询数据库。
     * </p>
     *
     * @param logs 操作日志列表
     */
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
        logs.forEach(log -> log.setAccount(accountMap.get(log.getUserId())));
    }

    /**
     * 解析操作日志展示账号。
     * <p>
     * 优先显示 sys_user.account，历史日志或用户已删除时依次回退到 username、userId。
     * </p>
     *
     * @param log 操作日志实体
     * @return 展示账号
     */
    private String resolveOperationLogAccount(SysOperationLog log) {
        if (log == null) {
            return "";
        }
        if (StringUtils.hasText(log.getAccount())) {
            return log.getAccount();
        }
        if (StringUtils.hasText(log.getUsername())) {
            return log.getUsername();
        }
        return log.getUserId() == null ? "" : String.valueOf(log.getUserId());
    }

    /**
     * 解析操作类型展示文案。
     *
     * @param operationType 操作类型枚举值或历史字符串
     * @return 中文展示文案
     */
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

    /**
     * 构建操作日志导出查询条件。
     *
     * @param query 查询条件
     * @return MyBatis-Plus 查询包装器
     */
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
            // 账号不是审计表字段，需要先按账号匹配用户，再用 userId 过滤日志。
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

    /**
     * 将前端时间字符串解析为本地时间。
     *
     * @param raw 时间字符串
     * @return 解析成功的时间，解析失败时返回 null
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

    /**
     * 获取导出文件内容类型。
     *
     * @param cfg Excel 导出配置
     * @return HTTP Content-Type
     */
    @Override
    public String getContentType(FxExcelExportConfigDTO cfg) {
        if (cfg != null && "csv".equalsIgnoreCase(cfg.getExportFormat())) {
            return "text/csv";
        }
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    /**
     * 获取导出文件扩展名。
     *
     * @param cfg Excel 导出配置
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
     * 转义文件名，确保 Content-Disposition 可被浏览器正确识别。
     *
     * @param filename 原始文件名
     * @return URL 安全的文件名
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
     * 将导出文件写入 HTTP 响应。
     *
     * @param response    HTTP 响应对象
     * @param bytes       文件字节数组
     * @param contentType 内容类型
     * @param filename    文件名
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
            // 导出响应已开始写出时不再二次抛出，避免破坏 servlet 容器的响应关闭流程。
        }
    }
}
