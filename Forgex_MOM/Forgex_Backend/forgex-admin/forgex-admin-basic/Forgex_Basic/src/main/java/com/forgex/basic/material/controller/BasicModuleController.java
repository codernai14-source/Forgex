package com.forgex.basic.material.controller;

import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 基础信息模块控制器。
 * <p>
 * 提供基础可用性检查接口，用于验证模块能够正常启动与访问。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-09
 */
@Tag(name = "基础信息模块")
@RestController
@RequestMapping("/module")
public class BasicModuleController {

    /**
     * 健康检查接口。
     *
     * @return 模块状态信息
     */
    @Operation(summary = "基础信息模块健康检查")
    @GetMapping("/ping")
    public R<Map<String, Object>> ping() {
        Map<String, Object> data = Map.of(
                "moduleCode", "basic",
                "moduleName", "基础信息模块",
                "bizEnabled", false,
                "status", "UP",
                "serverTime", LocalDateTime.now().toString()
        );
        R<Map<String, Object>> result = R.ok(data);
        result.setMessage("基础信息模块启动成功");
        return result;
    }
}
