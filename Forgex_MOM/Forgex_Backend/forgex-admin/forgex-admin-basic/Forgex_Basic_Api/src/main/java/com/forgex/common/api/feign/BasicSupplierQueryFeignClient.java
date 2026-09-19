package com.forgex.common.api.feign;

import com.forgex.common.api.dto.SupplierAggregateDTO;
import com.forgex.common.api.dto.SupplierOptionDTO;
import com.forgex.common.api.dto.SupplierQueryRequestDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 供应商内部查询 Feign 客户端
 * <p>
 * 供其它服务以统一契约查询供应商聚合和供应商下拉数据。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@FeignClient(name = "forgex-basic", contextId = "basicSupplierQueryFeignClient", path = "/basic/supplier/internal")
public interface BasicSupplierQueryFeignClient {

    /**
     * 按编码查询单个供应商聚合
     *
     * @param request 查询请求
     * @return 供应商聚合信息
     */
    @PostMapping("/get-by-code")
    R<SupplierAggregateDTO> getByCode(@RequestBody SupplierQueryRequestDTO request);

    /**
     * 按编码列表批量查询供应商聚合
     *
     * @param request 查询请求
     * @return 供应商聚合列表
     */
    @PostMapping("/list-by-codes")
    R<List<SupplierAggregateDTO>> listByCodes(@RequestBody SupplierQueryRequestDTO request);

    /**
     * 查询供应商下拉选项
     *
     * @param request 查询请求
     * @return 供应商下拉选项
     */
    @PostMapping("/options")
    R<List<SupplierOptionDTO>> listOptions(@RequestBody SupplierQueryRequestDTO request);
}
