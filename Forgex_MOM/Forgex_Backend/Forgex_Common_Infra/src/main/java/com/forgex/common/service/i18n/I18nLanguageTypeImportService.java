package com.forgex.common.service.i18n;

import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;

/**
 * 语言类型公共导入服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
public interface I18nLanguageTypeImportService {

    /**
     * 执行公共导入。
     *
     * @param param 公共导入参数
     * @return 导入结果
     */
    FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param);
}
