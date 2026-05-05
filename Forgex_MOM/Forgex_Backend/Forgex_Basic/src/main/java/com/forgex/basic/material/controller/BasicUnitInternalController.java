package com.forgex.basic.material.controller;

import cn.hutool.json.JSONObject;
import com.forgex.basic.material.service.IBasicUnitService;
import com.forgex.common.api.dto.UnitConvertRequestDTO;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 计量单位内部服务控制器。
 * <p>
 * 暴露给其它微服务调用的公共换算接口。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@RestController
@RequestMapping("/basic/unit/internal")
@RequiredArgsConstructor
public class BasicUnitInternalController {

    private final IBasicUnitService unitService;

    /**
     * 换算实体中的指定数字字段。
     *
     * @param request 换算请求
     * @return 换算结果
     */
    @PostMapping("/convert")
    public R<JSONObject> convert(@RequestBody UnitConvertRequestDTO request) {
        return R.ok(unitService.convertFields(request));
    }
}
