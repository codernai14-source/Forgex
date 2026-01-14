package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.ExcelFileService;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.ExcelLoginLogExportDTO;
import com.forgex.sys.domain.dto.ExcelUserExportDTO;
import com.forgex.sys.domain.dto.LoginLogQueryDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.mapper.LoginLogMapper;
import com.forgex.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel 导入导出配置与文件能力 Controller。
 * <p>
 * - 系统管理：配置导入模板/导出字段与样式（存放 forgex_common）；\n
 * - 业务页面：下载导入模板、导出数据文件。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/excel")
@RequiredArgsConstructor
public class ExcelConfigController {

    private final ExcelConfigService excelConfigService;
    private final ExcelFileService excelFileService;
    private final LoginLogMapper loginLogMapper;
    private final SysUserMapper userMapper;

    /**
     * 分页查询导出配置。
     *
     * @param body 入参：current/size/tableName/tableCode
     * @return 分页结果
     */
    @RequirePerm("sys:excel:exportConfig:list")
    @PostMapping("/exportConfig/page")
    public R<IPage<FxExcelExportConfigDTO>> pageExportConfig(@RequestBody Map<String, Object> body) {
        long current = body != null && body.get("current") instanceof Number ? ((Number) body.get("current")).longValue() : 1L;
        long size = body != null && body.get("size") instanceof Number ? ((Number) body.get("size")).longValue() : 20L;
        String tableName = body == null ? null : (String) body.get("tableName");
        String tableCode = body == null ? null : (String) body.get("tableCode");
        Page<FxExcelExportConfigDTO> page = new Page<>(current, size);
        return R.ok(excelConfigService.pageExportConfig(page, tableName, tableCode));
    }

    /**
     * 获取导出配置详情（含子项）。
     *
     * @param body 入参：id
     * @return 配置
     */
    @RequirePerm("sys:excel:exportConfig:list")
    @PostMapping("/exportConfig/detail")
    public R<FxExcelExportConfigDTO> exportConfigDetail(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body == null ? null : body.get("id"));
        return R.ok(excelConfigService.getExportConfig(id));
    }

    /**
     * 保存导出配置（主+子）。
     *
     * @param dto 导出配置
     * @return 主表ID
     */
    @RequirePerm("sys:excel:exportConfig:edit")
    @PostMapping("/exportConfig/save")
    public R<Long> saveExportConfig(@RequestBody FxExcelExportConfigDTO dto) {
        return R.ok(excelConfigService.saveExportConfig(dto));
    }

    /**
     * 删除导出配置（主+子）。
     *
     * @param body 入参：id
     * @return 是否成功
     */
    @RequirePerm("sys:excel:exportConfig:delete")
    @PostMapping("/exportConfig/delete")
    public R<Boolean> deleteExportConfig(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body == null ? null : body.get("id"));
        return R.ok(excelConfigService.deleteExportConfig(id));
    }

    /**
     * 分页查询导入配置。
     *
     * @param body 入参：current/size/tableName/tableCode
     * @return 分页结果
     */
    @RequirePerm("sys:excel:importConfig:list")
    @PostMapping("/importConfig/page")
    public R<IPage<FxExcelImportConfigDTO>> pageImportConfig(@RequestBody Map<String, Object> body) {
        long current = body != null && body.get("current") instanceof Number ? ((Number) body.get("current")).longValue() : 1L;
        long size = body != null && body.get("size") instanceof Number ? ((Number) body.get("size")).longValue() : 20L;
        String tableName = body == null ? null : (String) body.get("tableName");
        String tableCode = body == null ? null : (String) body.get("tableCode");
        Page<FxExcelImportConfigDTO> page = new Page<>(current, size);
        return R.ok(excelConfigService.pageImportConfig(page, tableName, tableCode));
    }

    /**
     * 获取导入配置详情（含子项）。
     *
     * @param body 入参：id
     * @return 配置
     */
    @RequirePerm("sys:excel:importConfig:list")
    @PostMapping("/importConfig/detail")
    public R<FxExcelImportConfigDTO> importConfigDetail(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body == null ? null : body.get("id"));
        return R.ok(excelConfigService.getImportConfig(id));
    }

    /**
     * 保存导入配置（主+子）。
     *
     * @param dto 导入配置
     * @return 主表ID
     */
    @RequirePerm("sys:excel:importConfig:edit")
    @PostMapping("/importConfig/save")
    public R<Long> saveImportConfig(@RequestBody FxExcelImportConfigDTO dto) {
        return R.ok(excelConfigService.saveImportConfig(dto));
    }

    /**
     * 删除导入配置（主+子）。
     *
     * @param body 入参：id
     * @return 是否成功
     */
    @RequirePerm("sys:excel:importConfig:delete")
    @PostMapping("/importConfig/delete")
    public R<Boolean> deleteImportConfig(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body == null ? null : body.get("id"));
        return R.ok(excelConfigService.deleteImportConfig(id));
    }

    /**
     * 下载导入模板（按 tableCode 配置生成）。
     *
     * @param body     入参：tableCode
     * @param response 响应
     */
    @RequirePerm("sys:excel:template:download")
    @PostMapping("/template/download")
    public void downloadTemplate(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        String tableCode = body == null ? null : (String) body.get("tableCode");
        FxExcelImportConfigDTO cfg = excelConfigService.getImportConfigByCode(tableCode);
        byte[] bytes = excelFileService.buildImportTemplateXlsx(cfg);
        writeFile(response, bytes, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                safeName("import-template-" + tableCode + ".xlsx"));
    }

    /**
     * 导出登录日志（按 tableCode 配置生成）。
     *
     * @param body     入参：tableCode + query(LoginLogQueryDTO)
     * @param response 响应
     */
    @RequirePerm("sys:excel:export:loginLog")
    @PostMapping("/export/loginLog")
    public void exportLoginLog(@RequestBody ExcelLoginLogExportDTO body, HttpServletResponse response) {
        String tableCode = body == null ? null : body.getTableCode();
        LoginLogQueryDTO query = body == null ? null : body.getQuery();
        FxExcelExportConfigDTO cfg = excelConfigService.getExportConfigByCode(tableCode);
        Long tenantId = TenantContext.get();
        List<LoginLog> list = loginLogMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LoginLog>()
                .like(query != null && StringUtils.hasText(query.getAccount()), LoginLog::getAccount, query.getAccount())
                .eq(query != null && query.getStatus() != null, LoginLog::getStatus, query.getStatus())
                .ge(query != null && query.getStartTime() != null, LoginLog::getLoginTime, query.getStartTime())
                .le(query != null && query.getEndTime() != null, LoginLog::getLoginTime, query.getEndTime())
                .eq(tenantId != null, LoginLog::getTenantId, tenantId)
                .eq(tenantId == null && query != null && query.getTenantId() != null, LoginLog::getTenantId, query.getTenantId())
                .orderByDesc(LoginLog::getLoginTime));

        List<Map<String, Object>> rows = list.stream().map(this::toLoginLogMap).collect(Collectors.toList());
        byte[] bytes = excelFileService.buildExportFile(cfg, rows);
        String filename = "login-log-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now()) + ext(cfg);
        writeFile(response, bytes, contentType(cfg), safeName(filename));
    }

    /**
     * 导出用户（按 tableCode 配置生成）。
     *
     * @param body     入参：tableCode + query(SysUserQueryDTO)
     * @param response 响应
     */
    @RequirePerm({"sys:user:export", "sys:excel:export:user"})
    @PostMapping("/export/user")
    public void exportUser(@RequestBody ExcelUserExportDTO body, HttpServletResponse response) {
        String tableCode = body == null ? null : body.getTableCode();
        SysUserQueryDTO query = body == null ? null : body.getQuery();
        FxExcelExportConfigDTO cfg = excelConfigService.getExportConfigByCode(tableCode);
        Long tenantId = TenantContext.get();
        List<SysUser> list = userMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .like(query != null && StringUtils.hasText(query.getAccount()), SysUser::getAccount, query.getAccount())
                .like(query != null && StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .eq(query != null && query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(tenantId != null, SysUser::getTenantId, tenantId)
                .eq(tenantId == null && query != null && query.getTenantId() != null, SysUser::getTenantId, query.getTenantId())
                .orderByDesc(SysUser::getId));

        List<Map<String, Object>> rows = list.stream().map(this::toUserMap).collect(Collectors.toList());
        byte[] bytes = excelFileService.buildExportFile(cfg, rows);
        String filename = "sys-user-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now()) + ext(cfg);
        writeFile(response, bytes, contentType(cfg), safeName(filename));
    }

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

    private String contentType(FxExcelExportConfigDTO cfg) {
        if (cfg != null && "csv".equalsIgnoreCase(cfg.getExportFormat())) {
            return "text/csv";
        }
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    private String ext(FxExcelExportConfigDTO cfg) {
        if (cfg != null && "csv".equalsIgnoreCase(cfg.getExportFormat())) {
            return ".csv";
        }
        return ".xlsx";
    }

    private String safeName(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        } catch (Exception e) {
            return filename;
        }
    }

    private void writeFile(HttpServletResponse response, byte[] bytes, String contentType, String filename) {
        try {
            response.setContentType(contentType);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
            response.getOutputStream().write(bytes == null ? new byte[0] : bytes);
            response.flushBuffer();
        } catch (Exception ignored) {
        }
    }

    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.valueOf((String) obj);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
