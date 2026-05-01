package com.forgex.sys.service.impl;

import com.forgex.common.api.feign.BasicSupplierImportFeignClient;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.enums.ExcelPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.service.excel.FxExcelImportHandler;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 供应商导入代理处理器。
 * <p>
 * 公共导入入口运行在 Sys 模块，供应商业务逻辑归属 Basic 模块。
 * 该处理器只负责把导入参数转发给 Basic 模块内部接口，具体写库逻辑仍由 Basic 模块处理。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
@Service("basicSupplierImportHandler")
@RequiredArgsConstructor
public class BasicSupplierImportProxyHandler implements FxExcelImportHandler {

    private final BasicSupplierImportFeignClient basicSupplierImportFeignClient;

    @Override
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        R<FxExcelImportResultDTO> response = basicSupplierImportFeignClient.execute(param);
        if (response == null || response.getCode() == null || response.getCode() != StatusCode.SUCCESS || response.getData() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_FAILED);
        }
        return response.getData();
    }
}
