package com.forgex.basic.supplier.service.impl;

import com.forgex.basic.supplier.service.ISupplierService;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.service.excel.FxExcelImportHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 供应商公共导入处理器。
 * <p>
 * 以稳定 Bean 名称供公共导入配置引用，实际供应商写库逻辑委托给供应商服务。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
@Service("basicSupplierImportHandler")
@RequiredArgsConstructor
public class BasicSupplierImportHandler implements FxExcelImportHandler {

    private final ISupplierService supplierService;

    @Override
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        return supplierService.executeCommonImport(param);
    }
}
