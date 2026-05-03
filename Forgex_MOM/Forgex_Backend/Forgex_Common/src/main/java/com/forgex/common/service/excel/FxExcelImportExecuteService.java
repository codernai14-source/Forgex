package com.forgex.common.service.excel;

import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;

/**
 * Excel 公共导入执行服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
public interface FxExcelImportExecuteService {

    /**
     * 执行公共导入。
     *
     * @param param 导入参数
     * @return 导入结果
     */
    FxExcelImportResultDTO execute(FxExcelImportExecuteParam param);
}
