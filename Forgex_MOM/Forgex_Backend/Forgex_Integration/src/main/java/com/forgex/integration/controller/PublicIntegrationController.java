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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对外公共接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/integration/public")
@RequiredArgsConstructor
@Tag(name = "集成公共接口", description = "对外统一入口与异步结果查询")
public class PublicIntegrationController {

    private final IApiGatewayService apiGatewayService;

    /**
     * 调用集成接口。
     *
     * @param request 请求参数
     * @param servletRequest servlet 请求
     * @return 统一响应结果
     */
    @PostMapping("/invoke")
    @Operation(summary = "对外公共调用")
    public R<IntegrationExecuteResult> invoke(@RequestBody PublicInvokeRequest request, HttpServletRequest servletRequest) {
        IntegrationExecuteResult result = apiGatewayService.invokeInbound(
            request.getInterfaceCode(),
            request.getPayload(),
            getClientIp(servletRequest)
        );
        return R.ok(result);
    }

    /**
     * 查询异步任务结果。
     *
     * @param taskId 任务 ID
     * @return 统一响应结果
     */
    @GetMapping("/task/{taskId}")
    @Operation(summary = "查询异步任务结果")
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
