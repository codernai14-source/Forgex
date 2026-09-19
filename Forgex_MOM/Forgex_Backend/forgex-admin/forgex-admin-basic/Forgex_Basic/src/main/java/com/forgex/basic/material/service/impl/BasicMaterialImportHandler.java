package com.forgex.basic.material.service.impl;

import com.forgex.basic.material.service.IMaterialService;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.service.excel.FxExcelImportHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 物料公共导入处理器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-12
 */
@Service("basicMaterialImportHandler")
@RequiredArgsConstructor
public class BasicMaterialImportHandler implements FxExcelImportHandler {

    private final IMaterialService materialService;

    /**
     * 处理导入数据。
     *
     * @param param 请求参数
     * @return 处理结果
     */
    @Override
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        return materialService.executeCommonImport(param);
    }
}
