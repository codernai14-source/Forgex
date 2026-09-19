package com.forgex.sys.service.impl;

import com.forgex.common.api.feign.BasicCustomerImportFeignClient;
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
 * 客户导入代理处理器。
 * <p>
 * 公共导入入口运行在 Sys 模块，客户主数据写入逻辑归属 Basic 模块。
 * 该处理器只负责将导入参数转发给 Basic 内部接口。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Service("basicCustomerImportHandler")
@RequiredArgsConstructor
public class BasicCustomerImportProxyHandler implements FxExcelImportHandler {

    private final BasicCustomerImportFeignClient basicCustomerImportFeignClient;

    /**
     * 处理导入数据。
     *
     * @param param 导入参数
     * @return 导入结果
     */
    @Override
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        R<FxExcelImportResultDTO> response = basicCustomerImportFeignClient.execute(param);
        if (response == null || response.getCode() == null || response.getCode() != StatusCode.SUCCESS || response.getData() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.EXCEL_IMPORT_FAILED);
        }
        return response.getData();
    }
}
