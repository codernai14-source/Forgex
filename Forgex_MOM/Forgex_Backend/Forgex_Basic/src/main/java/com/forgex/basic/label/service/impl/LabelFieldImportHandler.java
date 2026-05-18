package com.forgex.basic.label.service.impl;

import com.forgex.basic.label.service.LabelFieldService;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.service.excel.FxExcelImportHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 标签字段公共导入处理器。
 */
@Service("labelFieldImportHandler")
@RequiredArgsConstructor
public class LabelFieldImportHandler implements FxExcelImportHandler {

    private final LabelFieldService labelFieldService;

    @Override
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        return labelFieldService.executeCommonImport(param);
    }
}
