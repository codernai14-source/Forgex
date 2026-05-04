package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.domain.dto.excel.FxExcelExportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.enums.ExcelPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.PermKeyService;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.ExcelFileService;
import com.forgex.common.service.excel.FxExcelImportExecuteService;
import com.forgex.common.service.i18n.I18nMessageService;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.ExcelLoginLogExportDTO;
import com.forgex.sys.domain.dto.ExcelUserExportDTO;
import com.forgex.sys.domain.param.ExcelExportConfigPageParam;
import com.forgex.sys.domain.param.ExcelImportConfigPageParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.param.TableCodeParam;
import com.forgex.sys.service.ExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Excel 导入导出配置与文件能力控制器。
 * <p>
 * 负责导入模板配置、导出字段配置、模板下载、系统日志/用户等数据导出能力。
 * 配置数据存放在 {@code forgex_common} 库，供系统管理页面和业务导入导出流程复用。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/excel")
@RequiredArgsConstructor
public class ExcelConfigController {

    /**
     * Excel 配置服务。
     */
    private final ExcelConfigService excelConfigService;

    /**
     * Excel 文件生成服务。
     */
    private final ExcelFileService excelFileService;

    /**
     * Excel 公共导入执行服务。
     */
    private final FxExcelImportExecuteService excelImportExecuteService;

    /**
     * 权限服务。
     */
    private final PermKeyService permKeyService;

    private final I18nMessageService i18nMessageService;

    /**
     * Excel 导出服务。
     */
    private final ExcelExportService excelExportService;

    /**
     * 分页查询导出配置。
     *
     * @param param 查询参数
     * @return 导出配置分页结果
     */
    @RequirePerm("sys:excel:exportConfig:list")
    @PostMapping("/exportConfig/page")
    public R<IPage<FxExcelExportConfigDTO>> pageExportConfig(@RequestBody ExcelExportConfigPageParam param) {
        Page<FxExcelExportConfigDTO> page = new Page<>(param.getPageNum(), param.getPageSize());
        return R.ok(excelConfigService.pageExportConfig(page, param.getTableName(), param.getTableCode(), param.getEnabled()));
    }

    /**
     * 获取导出配置详情。
     *
     * @param param ID 参数
     * @return 导出配置详情，包含子项字段配置
     */
    @RequirePerm("sys:excel:exportConfig:list")
    @PostMapping("/exportConfig/detail")
    public R<FxExcelExportConfigDTO> exportConfigDetail(@RequestBody IdParam param) {
        return R.ok(excelConfigService.getExportConfig(param.getId()));
    }

    /**
     * 保存导出配置。
     *
     * @param dto 导出配置数据
     * @return 主表 ID
     */
    @RequirePerm("sys:excel:exportConfig:edit")
    @PostMapping("/exportConfig/save")
    public R<Long> saveExportConfig(@RequestBody FxExcelExportConfigDTO dto) {
        return R.ok(CommonPrompt.SAVE_SUCCESS, excelConfigService.saveExportConfig(dto));
    }

    /**
     * 删除导出配置。
     *
     * @param param ID 参数
     * @return 删除结果
     */
    @RequirePerm("sys:excel:exportConfig:delete")
    @PostMapping("/exportConfig/delete")
    public R<Boolean> deleteExportConfig(@RequestBody IdParam param) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, excelConfigService.deleteExportConfig(param.getId()));
    }

    /**
     * 分页查询导入配置。
     *
     * @param param 查询参数
     * @return 导入配置分页结果
     */
    @RequirePerm("sys:excel:importConfig:list")
    @PostMapping("/importConfig/page")
    public R<IPage<FxExcelImportConfigDTO>> pageImportConfig(@RequestBody ExcelImportConfigPageParam param) {
        Page<FxExcelImportConfigDTO> page = new Page<>(param.getPageNum(), param.getPageSize());
        return R.ok(excelConfigService.pageImportConfig(page, param.getTableName(), param.getTableCode()));
    }

    /**
     * 获取导入配置详情。
     *
     * @param param ID 参数
     * @return 导入配置详情，包含导入字段配置
     */
    @RequirePerm("sys:excel:importConfig:list")
    @PostMapping("/importConfig/detail")
    public R<FxExcelImportConfigDTO> importConfigDetail(@RequestBody IdParam param) {
        return R.ok(excelConfigService.getImportConfig(param.getId()));
    }

    /**
     * 根据表编码获取导入配置详情。
     *
     * @param param 表编码参数
     * @return 导入配置详情
     */
    @PostMapping("/importConfig/detailByCode")
    public R<FxExcelImportConfigDTO> importConfigDetailByCode(@RequestBody TableCodeParam param) {
        String tableCode = param == null ? null : param.getTableCode();
        FxExcelImportConfigDTO config = excelConfigService.getImportConfigByCode(tableCode);
        if (config == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_TEMPLATE_CONFIG_MISSING);
        }
        requireImportPermission(config);
        return R.ok(config);
    }

    /**
     * 保存导入配置。
     *
     * @param dto 导入配置数据
     * @return 主表 ID
     */
    @RequirePerm("sys:excel:importConfig:edit")
    @PostMapping("/importConfig/save")
    public R<Long> saveImportConfig(@RequestBody FxExcelImportConfigDTO dto) {
        return R.ok(CommonPrompt.SAVE_SUCCESS, excelConfigService.saveImportConfig(dto));
    }

    /**
     * 删除导入配置。
     *
     * @param param ID 参数
     * @return 删除结果
     */
    @RequirePerm("sys:excel:importConfig:delete")
    @PostMapping("/importConfig/delete")
    public R<Boolean> deleteImportConfig(@RequestBody IdParam param) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, excelConfigService.deleteImportConfig(param.getId()));
    }

    /**
     * 下载导入模板。
     * <p>
     * 模板生成成功时返回 xlsx 二进制；生成失败时返回 JSON 错误，避免前端把错误内容当 Excel 保存。
     * </p>
     *
     * @param param    表编码参数
     * @param response HTTP 响应
     */
    @PostMapping("/template/download")
    public void downloadTemplate(@RequestBody TableCodeParam param, HttpServletResponse response) {
        try {
            String tableCode = param == null ? null : param.getTableCode();
            FxExcelImportConfigDTO cfg = excelConfigService.getImportConfigByCode(tableCode);
            if (cfg == null) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_TEMPLATE_CONFIG_MISSING);
            }
            requireImportPermission(cfg);
            byte[] bytes = excelFileService.buildImportTemplateXlsxOrThrow(cfg);
            String filename = "import-template-" + tableCode + ".xlsx";

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + excelExportService.getSafeFilename(filename));
            response.getOutputStream().write(bytes);
            response.flushBuffer();
        } catch (Exception ex) {
            writeJsonError(response, ex);
        }
    }

    /**
     * 执行公共导入。
     * <p>
     * 前端解析文件后提交结构化数据，后端按表编码读取导入配置、动态校验业务导入权限，
     * 再根据处理器 Bean 名称分发给业务处理器。
     * </p>
     *
     * @param param 导入参数
     * @return 导入统计结果
     */
    @PostMapping("/import/execute")
    public R<FxExcelImportResultDTO> executeImport(@RequestBody FxExcelImportExecuteParam param) {
        if (param == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        FxExcelImportConfigDTO config = excelConfigService.getImportConfigByCode(param == null ? null : param.getTableCode());
        if (config == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_TEMPLATE_CONFIG_MISSING);
        }
        requireImportPermission(config);
        param.setImportConfig(config);
        FxExcelImportResultDTO result = excelImportExecuteService.execute(param);
        return R.okWithArgsAndData(ExcelPromptEnum.EXCEL_IMPORT_EXECUTE_SUCCESS,
                result,
                result.getTotalCount(),
                result.getCreatedCount(),
                result.getUpdatedCount(),
                result.getSkippedCount(),
                result.getFailedCount());
    }

    /**
     * 导出登录日志。
     *
     * @param body     导出参数
     * @param response HTTP 响应
     */
    @RequirePerm("sys:excel:export:loginLog")
    @PostMapping("/export/loginLog")
    public void exportLoginLog(@RequestBody ExcelLoginLogExportDTO body, HttpServletResponse response) {
        excelExportService.exportLoginLog(body, response);
    }

    /**
     * 导出用户数据。
     *
     * @param body     导出参数
     * @param response HTTP 响应
     */
    @RequirePerm({"sys:user:export", "sys:excel:export:user"})
    @PostMapping("/export/user")
    public void exportUser(@RequestBody ExcelUserExportDTO body, HttpServletResponse response) {
        excelExportService.exportUser(body, response);
    }

    /**
     * 获取所有已注册的导入数据源 Provider 编码。
     *
     * @return Provider 编码列表
     */
    @RequirePerm("sys:excel:importConfig:list")
    @PostMapping("/provider/list")
    public R<java.util.List<String>> listProviders() {
        return R.ok(excelConfigService.listProviderCodes());
    }

    /**
     * 写入 JSON 格式错误响应。
     *
     * @param response HTTP 响应
     * @param ex       异常信息
     */
    private void writeJsonError(HttpServletResponse response, Exception ex) {
        try {
            response.reset();
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            String message = resolveErrorMessage(ex);
            String body = "{\"code\":500,\"message\":\"" + escapeJson(message) + "\",\"data\":null}";
            response.getWriter().write(body);
            response.flushBuffer();
        } catch (IOException ignored) {
        }
    }

    private String resolveErrorMessage(Exception ex) {
        if (ex instanceof I18nBusinessException i18nEx) {
            String resolved = i18nMessageService.resolve(i18nEx.getMsg(), i18nEx.getMsgArgs());
            if (resolved != null && !resolved.isBlank()) {
                return resolved;
            }
        }
        String reason = ex == null || ex.getMessage() == null ? "" : ex.getMessage();
        String fallback = i18nMessageService.resolve(ExcelPromptEnum.EXCEL_IMPORT_TEMPLATE_BUILD_FAILED, new Object[]{reason});
        return fallback == null || fallback.isBlank() ? "Failed to generate import template" : fallback;
    }

    /**
     * 校验导入权限。
     *
     * @param config 导入配置
     */
    private void requireImportPermission(FxExcelImportConfigDTO config) {
        String importPermission = config == null ? null : config.getImportPermission();
        if (importPermission == null || importPermission.isBlank()) {
            throw new I18nBusinessException(StatusCode.UNAUTHORIZED, ExcelPromptEnum.EXCEL_IMPORT_PERMISSION_NOT_CONFIGURED);
        }
        Long userId = CurrentUserUtils.getUserId();
        Long tenantId = CurrentUserUtils.getTenantId();
        if (userId == null || tenantId == null) {
            throw new I18nBusinessException(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
        }
        if (!permKeyService.hasAllPerms(userId, tenantId, Set.of(importPermission))) {
            throw new I18nBusinessException(StatusCode.UNAUTHORIZED, ExcelPromptEnum.EXCEL_IMPORT_PERMISSION_DENIED);
        }
    }

    /**
     * 转义 JSON 字符串内容。
     *
     * @param value 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
