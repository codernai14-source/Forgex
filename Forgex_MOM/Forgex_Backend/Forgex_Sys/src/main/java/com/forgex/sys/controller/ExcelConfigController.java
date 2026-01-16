package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.ExcelFileService;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.sys.service.ExcelExportService;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.ExcelLoginLogExportDTO;
import com.forgex.sys.domain.dto.ExcelUserExportDTO;
import com.forgex.sys.domain.dto.LoginLogQueryDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.param.ExcelExportConfigPageParam;
import com.forgex.sys.domain.param.ExcelImportConfigPageParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.param.TableCodeParam;
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
    private final ExcelExportService excelExportService;

    /**
     * 分页查询导出配置。
     *
     * @param param 入参：current/size/tableName/tableCode
     * @return 分页结果
     */
    @RequirePerm("sys:excel:exportConfig:list")
    @PostMapping("/exportConfig/page")
    public R<IPage<FxExcelExportConfigDTO>> pageExportConfig(@RequestBody ExcelExportConfigPageParam param) {
        Page<FxExcelExportConfigDTO> page = new Page<>(param.getPageNum(), param.getPageSize());
        return R.ok(excelConfigService.pageExportConfig(page, param.getTableName(), param.getTableCode()));
    }

    /**
     * 获取导出配置详情（含子项）。
     *
     * @param param 入参：id
     * @return 配置
     */
    @RequirePerm("sys:excel:exportConfig:list")
    @PostMapping("/exportConfig/detail")
    public R<FxExcelExportConfigDTO> exportConfigDetail(@RequestBody IdParam param) {
        return R.ok(excelConfigService.getExportConfig(param.getId()));
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
     * @param param 入参：id
     * @return 结果
     */
    @RequirePerm("sys:excel:exportConfig:delete")
    @PostMapping("/exportConfig/delete")
    public R<Boolean> deleteExportConfig(@RequestBody IdParam param) {
        return R.ok(excelConfigService.deleteExportConfig(param.getId()));
    }

    /**
     * 分页查询导入配置。
     *
     * @param param 入参：current/size/tableName/tableCode
     * @return 分页结果
     */
    @RequirePerm("sys:excel:importConfig:list")
    @PostMapping("/importConfig/page")
    public R<IPage<FxExcelImportConfigDTO>> pageImportConfig(@RequestBody ExcelImportConfigPageParam param) {
        Page<FxExcelImportConfigDTO> page = new Page<>(param.getPageNum(), param.getPageSize());
        return R.ok(excelConfigService.pageImportConfig(page, param.getTableName(), param.getTableCode()));
    }

    /**
     * 获取导入配置详情（含子项）。
     *
     * @param param 入参：id
     * @return 配置
     */
    @RequirePerm("sys:excel:importConfig:list")
    @PostMapping("/importConfig/detail")
    public R<FxExcelImportConfigDTO> importConfigDetail(@RequestBody IdParam param) {
        return R.ok(excelConfigService.getImportConfig(param.getId()));
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
    public R<Boolean> deleteImportConfig(@RequestBody IdParam param) {
        return R.ok(excelConfigService.deleteImportConfig(param.getId()));
    }

    /**
     * 下载导入模板（按 tableCode 配置生成）。
     *
     * @param param     入参：tableCode
     * @param response 响应
     */
    @RequirePerm("sys:excel:template:download")
    @PostMapping("/template/download")
    public void downloadTemplate(@RequestBody TableCodeParam param, HttpServletResponse response) {
        String tableCode = param.getTableCode();
        FxExcelImportConfigDTO cfg = excelConfigService.getImportConfigByCode(tableCode);
        byte[] bytes = excelFileService.buildImportTemplateXlsx(cfg);
        
        // 使用ExcelExportService的方法处理文件下载
        String filename = "import-template-" + tableCode + ".xlsx";
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String safeFilename = excelExportService.getSafeFilename(filename);
        
        try {
            response.setContentType(contentType);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + safeFilename);
            response.getOutputStream().write(bytes == null ? new byte[0] : bytes);
            response.flushBuffer();
        } catch (Exception ignored) {
        }
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
        // 调用Excel导出服务处理登录日志导出
        excelExportService.exportLoginLog(body, response);
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
        // 调用Excel导出服务处理用户数据导出
        excelExportService.exportUser(body, response);
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
