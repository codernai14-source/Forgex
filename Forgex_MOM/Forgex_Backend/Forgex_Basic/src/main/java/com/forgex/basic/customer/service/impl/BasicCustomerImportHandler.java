package com.forgex.basic.customer.service.impl;

import com.forgex.basic.customer.service.ICustomerService;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.service.excel.FxExcelImportHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 客户公共导入处理器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Service("basicCustomerImportHandler")
@RequiredArgsConstructor
public class BasicCustomerImportHandler implements FxExcelImportHandler {

    private final ICustomerService customerService;

    /**
     * 处理导入数据。
     *
     * @param param 导入参数
     * @return 导入结果
     */
    @Override
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        return customerService.executeCommonImport(param);
    }
}
