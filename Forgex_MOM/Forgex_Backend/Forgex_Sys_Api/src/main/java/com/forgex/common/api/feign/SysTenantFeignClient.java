package com.forgex.common.api.feign;

import com.forgex.common.api.dto.SysTenantCreateRequestDTO;
import com.forgex.common.api.dto.SysTenantSimpleDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 租户内部调用 Feign 客户端
 * <p>
 * 供基础数据等其它服务创建或查询租户信息。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-25
 */
@FeignClient(name = "forgex-sys", contextId = "sysTenantFeignClient", path = "/sys/tenant/internal")
public interface SysTenantFeignClient {

    /**
     * 内部创建租户
     *
     * @param param 租户创建参数
     * @return 租户 ID
     */
    @PostMapping("/create")
    R<Long> create(@RequestBody SysTenantCreateRequestDTO param);

    /**
     * 根据租户编码查询租户
     *
     * @param param 查询参数
     * @return 租户信息
     */
    @PostMapping("/get-by-code")
    R<SysTenantSimpleDTO> getByCode(@RequestBody Map<String, Object> param);
}
