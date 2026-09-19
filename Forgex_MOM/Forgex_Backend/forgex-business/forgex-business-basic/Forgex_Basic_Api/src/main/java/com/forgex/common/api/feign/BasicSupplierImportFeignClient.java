package com.forgex.common.api.feign;

import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 供应商公共导入内部 Feign 客户端。
 * <p>
 * Sys 模块公共导入入口通过该客户端把供应商结构化导入数据转发给 Basic 模块，
 * 供应商具体导入逻辑仍由 Basic 模块处理。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
@FeignClient(name = "forgex-basic", contextId = "basicSupplierImportFeignClient", path = "/basic/supplier/internal/import")
public interface BasicSupplierImportFeignClient {

    /**
     * 执行供应商公共导入。
     *
     * @param param 导入参数
     * @return 导入结果
     */
    @PostMapping("/execute")
    R<FxExcelImportResultDTO> execute(@RequestBody FxExcelImportExecuteParam param);
}
