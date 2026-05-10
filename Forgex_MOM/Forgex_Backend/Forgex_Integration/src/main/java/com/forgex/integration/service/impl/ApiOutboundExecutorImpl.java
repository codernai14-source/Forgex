package com.forgex.integration.service.impl;

import cn.hutool.json.JSONUtil;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.dto.ApiOutboundTargetDTO;
import com.forgex.integration.domain.dto.ThirdSystemDTO;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.model.ApiExecutionContext;
import com.forgex.integration.domain.model.OutboundRequestDefinition;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.service.IApiOutboundExecutor;
import com.forgex.integration.service.IThirdSystemService;
import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * 出站接口执行器实现。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Service
@RequiredArgsConstructor
public class ApiOutboundExecutorImpl implements IApiOutboundExecutor {

    private final RestTemplate integrationRestTemplate;
    private final IThirdSystemService thirdSystemService;

    @Override
    public Object execute(ApiDefinitionSnapshot snapshot, ApiExecutionContext context, OutboundRequestDefinition requestDefinition) {
        ApiConfigDTO config = snapshot.getApiConfig();
        String rawTarget = requestDefinition.getTargetUrl();
        if (!StringUtils.hasText(rawTarget)) {
            rawTarget = config.getTargetUrl();
        }
        if (!StringUtils.hasText(rawTarget)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                IntegrationPromptEnum.API_ROUTE_FAILED, "targetUrl is empty");
        }
        String url = resolveTargetUrl(snapshot, context, rawTarget);
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

    private String resolveTargetUrl(ApiDefinitionSnapshot snapshot, ApiExecutionContext context, String rawTarget) {
        if (isAbsoluteUrl(rawTarget)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                IntegrationPromptEnum.API_ROUTE_FAILED, "targetUrl only supports route path");
        }
        ApiOutboundTargetDTO currentTarget = resolveCurrentTarget(snapshot, context);
        if (currentTarget == null || currentTarget.getThirdSystemId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                IntegrationPromptEnum.API_ROUTE_FAILED, "thirdSystemId is empty");
        }
        ThirdSystemDTO thirdSystem = thirdSystemService.getThirdSystemById(currentTarget.getThirdSystemId());
        if (thirdSystem == null || !StringUtils.hasText(thirdSystem.getIpAddress())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                IntegrationPromptEnum.API_ROUTE_FAILED, "third system ipAddress is empty");
        }
        return normalizeThirdSystemHost(thirdSystem.getIpAddress()) + normalizeTargetRoute(rawTarget);
    }

    private ApiOutboundTargetDTO resolveCurrentTarget(ApiDefinitionSnapshot snapshot, ApiExecutionContext context) {
        if (snapshot == null || snapshot.getOutboundTargets() == null || context == null || context.getOutboundTargetId() == null) {
            return null;
        }
        return snapshot.getOutboundTargets().stream()
            .filter(item -> item != null && context.getOutboundTargetId().equals(item.getId()))
            .findFirst()
            .orElse(null);
    }

    private boolean isAbsoluteUrl(String url) {
        String value = url == null ? "" : url.trim().toLowerCase();
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String normalizeThirdSystemHost(String ipAddress) {
        String host = ipAddress.trim();
        if (!host.startsWith("http://") && !host.startsWith("https://")) {
            host = "http://" + host;
        }
        while (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        return host;
    }

    private String normalizeTargetRoute(String route) {
        String normalized = route.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

    private String renderPath(String url, Map<String, String> pathVariables) {
        String result = url;
        for (Map.Entry<String, String> entry : pathVariables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private HttpMethod resolveMethod(String method) {
        if (!StringUtils.hasText(method)) {
            return HttpMethod.POST;
        }
        return HttpMethod.valueOf(method.toUpperCase());
    }

    private MediaType resolveContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_JSON;
        }
        return MediaType.parseMediaType(contentType);
    }
}
