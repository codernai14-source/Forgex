package com.forgex.integration.controller;

import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ApiTaskResultDTO;
import com.forgex.integration.domain.model.IntegrationExecuteResult;
import com.forgex.integration.domain.param.PublicInvokeRequest;
import com.forgex.integration.service.IApiGatewayService;
import com.forgex.integration.service.impl.ApiGatewayServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 瀵瑰鍏叡鎺ュ彛
 */
@RestController
@RequestMapping("/api/integration/public")
@RequiredArgsConstructor
@Tag(name = "闆嗘垚鍏叡鎺ュ彛", description = "瀵瑰缁熶竴鍏ュ彛涓庡紓姝ョ粨鏋滄煡璇?")
public class PublicIntegrationController {

    private final IApiGatewayService apiGatewayService;

    @PostMapping("/invoke")
    @Operation(summary = "瀵瑰鍏叡璋冪敤")
    public R<IntegrationExecuteResult> invoke(@RequestBody PublicInvokeRequest request, HttpServletRequest servletRequest) {
        IntegrationExecuteResult result = apiGatewayService.invokeInbound(
            request.getInterfaceCode(),
            request.getPayload(),
            getClientIp(servletRequest)
        );
        return R.ok(result);
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "鏌ヨ寮傛浠诲姟缁撴灉")
    public R<ApiTaskResultDTO> queryTask(@PathVariable String taskId) {
        ApiTaskResultDTO result = ((ApiGatewayServiceImpl) apiGatewayService).getTaskResult(taskId);
        if (result == null) {
            return R.fail();
        }
        return R.ok(result);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }
        return ip;
    }
}
