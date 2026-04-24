package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.ExcelFileService;
import com.forgex.common.web.R;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.service.SysI18nLanguageTypeService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 多语言类型配置 Controller
 * <p>
 * 提供多语言类型配置的 API 接口，用于管理系统支持的语言类型。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.sys.service.SysI18nLanguageTypeService
 * @see com.forgex.common.domain.entity.i18n.FxI18nLanguageType
 */
@RestController
@RequestMapping("/sys/i18n/languageType")
@RequiredArgsConstructor
public class I18nLanguageTypeController {

    private final SysI18nLanguageTypeService languageTypeService;
    private final ExcelConfigService excelConfigService;
    private final ExcelFileService excelFileService;

    /**
     * 获取所有启用的语言类型列表
     *
     * @return 启用的语言类型列表
     */
    @PostMapping("/listEnabled")
    public R<List<FxI18nLanguageType>> listEnabled() {
        return R.ok(languageTypeService.listEnabled());
    }

    /**
     * 获取所有语言类型列表
     *
     * @return 所有语言类型列表
     */
    @RequirePerm("sys:i18nLanguageType:view")
    @PostMapping("/listAll")
    public R<List<FxI18nLanguageType>> listAll() {
        return R.ok(languageTypeService.listAll());
    }

    /**
     * 根据语言代码获取语言类型
     *
     * @param body 请求参数，包含 langCode
     * @return 语言类型实体
     */
    @PostMapping("/getByLangCode")
    public R<FxI18nLanguageType> getByLangCode(@RequestBody Map<String, Object> body) {
        String langCode = (String) body.get("langCode");
        if (langCode == null || langCode.isEmpty()) {
            return R.fail(CommonPrompt.LANG_CODE_EMPTY);
        }
        FxI18nLanguageType languageType = languageTypeService.getByLangCode(langCode);
        if (languageType == null) {
            return R.fail(CommonPrompt.LANG_TYPE_NOT_FOUND);
        }
        return R.ok(languageType);
    }

    /**
     * 获取默认语言类型
     *
     * @return 默认语言类型实体
     */
    @PostMapping("/getDefault")
    public R<FxI18nLanguageType> getDefault() {
        FxI18nLanguageType languageType = languageTypeService.getDefault();
        if (languageType == null) {
            return R.fail(CommonPrompt.DEFAULT_LANG_NOT_FOUND);
        }
        return R.ok(languageType);
    }

    /**
     * 创建语言类型
     *
     * @param languageType 语言类型实体
     * @return 是否创建成功
     */
    @RequirePerm("sys:i18nLanguageType:add")
    @PostMapping("/create")
    public R<Boolean> create(@RequestBody FxI18nLanguageType languageType) {
        if (languageType.getLangCode() == null || languageType.getLangCode().isEmpty()) {
            return R.fail(CommonPrompt.LANG_CODE_EMPTY);
        }
        if (languageType.getLangName() == null || languageType.getLangName().isEmpty()) {
            return R.fail(CommonPrompt.LANG_NAME_EMPTY);
        }
        return R.ok(languageTypeService.create(languageType));
    }

    /**
     * 更新语言类型
     *
     * @param languageType 语言类型实体
     * @return 是否更新成功
     */
    @RequirePerm("sys:i18nLanguageType:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody FxI18nLanguageType languageType) {
        if (languageType.getId() == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        return R.ok(languageTypeService.update(languageType));
    }

    /**
     * 删除语言类型
     *
     * @param body 请求参数，包含 id
     * @return 是否删除成功
     */
    @RequirePerm("sys:i18nLanguageType:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        if (idObj == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        Long id = null;
        if (idObj instanceof Integer) {
            id = ((Integer) idObj).longValue();
        } else if (idObj instanceof Long) {
            id = (Long) idObj;
        } else if (idObj instanceof String) {
            id = Long.parseLong((String) idObj);
        }
        return R.ok(languageTypeService.delete(id));
    }

    /**
     * 设置默认语言
     *
     * @param body 请求参数，包含 id
     * @return 是否设置成功
     */
    @RequirePerm("sys:i18nLanguageType:setDefault")
    @PostMapping("/setDefault")
    public R<Boolean> setDefault(@RequestBody Map<String, Object> body) {
        Object idObj = body.get("id");
        if (idObj == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        Long id = null;
        if (idObj instanceof Integer) {
            id = ((Integer) idObj).longValue();
        } else if (idObj instanceof Long) {
            id = (Long) idObj;
        } else if (idObj instanceof String) {
            id = Long.parseLong((String) idObj);
        }
        return R.ok(languageTypeService.setDefault(id));
    }

    /**
     * 分页查询语言类型列表
     * <p>
     * 支持按语言代码、语言名称模糊查询，按排序号排序
     * </p>
     *
     * @param param 查询参数，包含 pageNum、pageSize、langCode、langName 等
     * @return 分页结果
     * @see FxI18nLanguageType
     */
    @RequirePerm("sys:i18nLanguageType:view")
    @PostMapping("/page")
    public R<IPage<FxI18nLanguageType>> page(@RequestBody(required = false) Map<String, Object> param) {
        int pageNum = param != null && param.get("pageNum") != null 
            ? Integer.parseInt(param.get("pageNum").toString()) : 1;
        int pageSize = param != null && param.get("pageSize") != null 
            ? Integer.parseInt(param.get("pageSize").toString()) : 10;
        String langCode = param != null ? (String) param.get("langCode") : null;
        String langName = param != null ? (String) param.get("langName") : null;
        Boolean enabled = param != null ? (Boolean) param.get("enabled") : null;
        return R.ok(languageTypeService.pageQuery(pageNum, pageSize, langCode, langName, enabled));
    }

    /**
     * 根据 ID 获取语言类型详情
     *
     * @param param 请求参数，包含 id
     * @return 语言类型实体
     * @see FxI18nLanguageType
     */
    @RequirePerm("sys:i18nLanguageType:view")
    @PostMapping("/detail")
    public R<FxI18nLanguageType> detail(@RequestBody IdParam param) {
        if (param.getId() == null) {
            return R.fail(CommonPrompt.ID_EMPTY);
        }
        FxI18nLanguageType languageType = languageTypeService.getById(param.getId());
        if (languageType == null) {
            return R.fail(CommonPrompt.LANG_TYPE_NOT_FOUND);
        }
        return R.ok(languageType);
    }

    /**
     * 导入语言类型
     * <p>
     * 支持 Excel 文件导入，自动解析并批量创建语言类型配置
     * </p>
     *
     * @param file Excel 文件
     * @return 导入结果，包含成功数量和失败数量
     * @throws Exception 文件读取失败时抛出
     */
    @RequirePerm("sys:i18nLanguageType:import")
    @PostMapping("/import")
    public R<Map<String, Object>> importLanguageType(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return R.fail(CommonPrompt.FILE_EMPTY);
        }
        Map<String, Object> result = languageTypeService.importExcel(file);
        return R.ok(result);
    }

    /**
     * 下载导入模板
     * <p>
     * 根据 I18nLanguageTypeTable 配置生成 Excel 导入模板
     * </p>
     *
     * @param response HTTP 响应
     */
    @RequirePerm("sys:i18nLanguageType:template:download")
    @PostMapping("/template/download")
    public void downloadTemplate(HttpServletResponse response) {
        // 获取导入配置
        FxExcelImportConfigDTO config = excelConfigService.getImportConfigByCode("I18nLanguageTypeTable");
        if (config == null) {
            return;
        }
        
        // 生成模板文件
        byte[] bytes = excelFileService.buildImportTemplateXlsx(config);
        
        // 设置响应头
        String filename = "import-template-I18nLanguageTypeTable.xlsx";
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        
        try {
            response.setContentType(contentType);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(filename, "UTF-8"));
            response.getOutputStream().write(bytes == null ? new byte[0] : bytes);
            response.flushBuffer();
        } catch (Exception ignored) {
        }
    }
}
