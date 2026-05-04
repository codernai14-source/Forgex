package com.forgex.integration.service.impl;

import cn.hutool.json.JSONUtil;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.OutboundRequestDefinition;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.service.IApiOutboundExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

/**
 * 出站接口执行器实现。
 * <p>
 * 根据出站请求定义渲染 URL、查询参数、请求头和请求体，并通过集成专用 RestTemplate 发起 HTTP 调用。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see IApiOutboundExecutor
 */
@Service
@RequiredArgsConstructor
public class ApiOutboundExecutorImpl implements IApiOutboundExecutor {

    /**
     * 集成平台出站请求 RestTemplate。
     */
    private final RestTemplate integrationRestTemplate;

    /**
     * 执行出站调用。
     *
     * @param snapshot          接口定义快照
     * @param context           执行上下文
     * @param requestDefinition 出站请求定义
     * @return 第三方响应体
     */
    @Override
    public Object execute(ApiDefinitionSnapshot snapshot, ApiExecutionContext context, OutboundRequestDefinition requestDefinition) {
        ApiConfigDTO config = snapshot.getApiConfig();
        String url = requestDefinition.getTargetUrl();
        if (!StringUtils.hasText(url)) {
            url = config.getTargetUrl();
        }
        if (!StringUtils.hasText(url)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                IntegrationPromptEnum.API_ROUTE_FAILED, "targetUrl is empty");
        }
        String renderedUrl = renderPath(url, requestDefinition.getPathVariables());
        if (!requestDefinition.getQuery().isEmpty()) {
            StringBuilder builder = new StringBuilder(renderedUrl);
            builder.append(renderedUrl.contains("?") ? "&" : "?");
            boolean first = true;
            for (Map.Entry<String, String> entry : requestDefinition.getQuery().entrySet()) {
                if (!first) {
                    builder.append("&");
                }
                builder.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            renderedUrl = builder.toString();
        }

        HttpHeaders headers = new HttpHeaders();
        requestDefinition.getHeaders().forEach(headers::add);
        headers.setContentType(resolveContentType(config.getContentType()));
        HttpEntity<String> entity = new HttpEntity<>(JSONUtil.toJsonStr(requestDefinition.getBody()), headers);
        ResponseEntity<String> response = integrationRestTemplate.exchange(
            URI.create(renderedUrl),
            resolveMethod(config.getHttpMethod()),
            entity,
            String.class
        );
        return response.getBody();
    }

    /**
     * 渲染路径变量。
     *
     * @param url           原始 URL
     * @param pathVariables 路径变量
     * @return 渲染后的 URL
     */
    private String renderPath(String url, Map<String, String> pathVariables) {
        String result = url;
        for (Map.Entry<String, String> entry : pathVariables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /**
     * 解析 HTTP 方法。
     *
     * @param method HTTP 方法字符串
     * @return HTTP 方法
     */
    private HttpMethod resolveMethod(String method) {
        if (!StringUtils.hasText(method)) {
            return HttpMethod.POST;
        }
        return HttpMethod.valueOf(method.toUpperCase());
    }

    /**
     * 解析请求内容类型。
     *
     * @param contentType 内容类型字符串
     * @return 媒体类型
     */
    private MediaType resolveContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_JSON;
        }
        return MediaType.parseMediaType(contentType);
    }
}
