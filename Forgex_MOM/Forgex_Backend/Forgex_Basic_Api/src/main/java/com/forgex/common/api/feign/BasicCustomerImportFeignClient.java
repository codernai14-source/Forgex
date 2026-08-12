package com.forgex.common.api.feign;

import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 客户公共导入内部 Feign 客户端。
 * <p>
 * Sys 模块公共导入入口通过该客户端把客户结构化导入数据转发给 Basic 模块，
 * 客户具体导入逻辑仍由 Basic 模块处理。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@FeignClient(name = "forgex-basic", contextId = "basicCustomerImportFeignClient", path = "/basic/customer/internal/import")
public interface BasicCustomerImportFeignClient {

    /**
     * 执行客户公共导入。
     *
     * @param param 导入参数
     * @return 导入结果
     */
    @PostMapping("/execute")
    R<FxExcelImportResultDTO> execute(@RequestBody FxExcelImportExecuteParam param);
}
