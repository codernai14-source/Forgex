package com.forgex.common.api.feign;

import cn.hutool.json.JSONObject;
import com.forgex.common.api.dto.UnitConvertRequestDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 计量单位换算 Feign 契约。
 * <p>
 * 供其它业务服务调用 Forgex_Basic 的计量单位公共换算能力。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@FeignClient(name = "forgex-basic", contextId = "basicUnitConversionFeignClient", path = "/basic/unit/internal")
public interface BasicUnitConversionFeignClient {

    /**
     * 换算实体中的指定数值字段。
     *
     * @param request 换算请求
     * @return 换算后的 JSON 对象
     */
    @PostMapping("/convert")
    R<JSONObject> convert(@RequestBody UnitConvertRequestDTO request);
}
