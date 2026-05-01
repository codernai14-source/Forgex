package com.forgex.sys.service.impl;

import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.service.excel.FxExcelImportHandler;
import com.forgex.sys.service.SysI18nLanguageTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 语言类型导入代理处理器。
 * <p>
 * Sys 模块通过该 Bean 暴露公共导入处理器名称，实际写库逻辑委托给 Common 语言类型服务。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
@Service("i18nLanguageTypeImportHandler")
@RequiredArgsConstructor
public class I18nLanguageTypeImportProxyHandler implements FxExcelImportHandler {

    private final SysI18nLanguageTypeService languageTypeService;

    @Override
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        return languageTypeService.executeCommonImport(param);
    }
}
