package com.forgex.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.web.R;
import com.forgex.integration.service.IApiRouterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 集成服务统一对外接口控制器
 * <p>
 * 提供统一的对外接口调用入口，所有第三方系统调用都通过此接口进行路由
 * 支持动态路由到不同的处理器，实现灵活的集成方案
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see com.forgex.integration.service.IApiRouterService
 * @see com.forgex.integration.service.IApiCallLogService
 */
@Slf4j
@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
@Tag(name = "集成服务", description = "提供统一的对外接口调用入口")
public class IntegrationController {

    /**
     * JSON 对象映射器
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * API 路由服务
     */
    private final IApiRouterService apiRouterService;

    /**
     * 统一对外接口
     * <p>
     * 所有第三方系统调用都通过此接口进行路由
     * 根据 apiCode 参数将请求路由到对应的处理器
     * 支持 POST 和 PUT 方法，接收 JSON 格式的请求数据
     * </p>
     *
     * @param apiCode 接口编码（必填），用于识别路由到哪个处理器
     * @param request HTTP 请求对象，用于获取请求体和客户端 IP
     * @return 调用结果
     * @see com.forgex.integration.service.IApiRouterService#routeRequest
     */
    @PostMapping("/invoke")
    @Operation(summary = "统一对外接口", description = "所有第三方系统调用都通过此接口进行路由")
    public R<Object> invoke(
        @RequestParam String apiCode,
        HttpServletRequest request
    ) {
        try {
            // 1. 获取调用方 IP 地址
            String callerIp = getClientIp(request);
            
            // 2. 读取请求体 JSON 数据
            JsonNode requestData = objectMapper.readTree(request.getInputStream());
            
            // 3. 调用路由服务
            log.info("收到接口调用请求：apiCode={}, callerIp={}", apiCode, callerIp);
            return apiRouterService.routeRequest(apiCode, requestData, callerIp);
            
        } catch (Exception e) {
            log.error("接口调用异常：apiCode={}, error={}", apiCode, e.getMessage(), e);
            R<Object> result = R.fail();
            result.setMessage("接口调用失败：" + e.getMessage());
            return result;
        }
    }

    /**
     * 统一对外接口（GET 方式）
     * <p>
     * 支持 GET 方式的接口调用，参数通过查询字符串传递
     * </p>
     *
     * @param apiCode 接口编码
     * @param request HTTP 请求对象
     * @return 调用结果
     */
    @GetMapping("/invoke")
    @Operation(summary = "统一对外接口（GET）", description = "支持 GET 方式的接口调用")
    public R<Object> invokeGet(
        @RequestParam String apiCode,
        HttpServletRequest request
    ) {
        try {
            // 1. 获取调用方 IP 地址
            String callerIp = getClientIp(request);
            
            // 2. 将查询参数转换为 JSON 对象
            JsonNode requestData = objectMapper.valueToTree(request.getParameterMap());
            
            // 3. 调用路由服务
            log.info("收到 GET 接口调用请求：apiCode={}, callerIp={}", apiCode, callerIp);
            return apiRouterService.routeRequest(apiCode, requestData, callerIp);
            
        } catch (Exception e) {
            log.error("GET 接口调用异常：apiCode={}, error={}", apiCode, e.getMessage(), e);
            R<Object> result = R.fail();
            result.setMessage("接口调用失败：" + e.getMessage());
            return result;
        }
    }

    /**
     * 获取客户端真实 IP 地址
     * <p>
     * 考虑了反向代理、负载均衡等场景
     * </p>
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        // 从 X-Forwarded-For 头获取真实 IP（经过代理的情况）
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多个 IP（逗号分隔），取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
