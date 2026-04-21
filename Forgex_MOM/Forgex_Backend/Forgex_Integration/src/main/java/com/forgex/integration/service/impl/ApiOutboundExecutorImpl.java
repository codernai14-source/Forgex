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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

/**
 * HTTP 鍑虹珯鎵ц鍣?
 */
@Service
@RequiredArgsConstructor
public class ApiOutboundExecutorImpl implements IApiOutboundExecutor {

    private final RestTemplate integrationRestTemplate;

    @Override
    public Object execute(ApiDefinitionSnapshot snapshot, ApiExecutionContext context, OutboundRequestDefinition requestDefinition) {
        ApiConfigDTO config = snapshot.getApiConfig();
        if (!StringUtils.hasText(config.getTargetUrl())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                IntegrationPromptEnum.API_ROUTE_FAILED, "targetUrl is empty");
        }
        String url = renderPath(config.getTargetUrl(), requestDefinition.getPathVariables());
        if (!requestDefinition.getQuery().isEmpty()) {
            StringBuilder builder = new StringBuilder(url);
            builder.append(url.contains("?") ? "&" : "?");
            boolean first = true;
            for (Map.Entry<String, String> entry : requestDefinition.getQuery().entrySet()) {
                if (!first) {
                    builder.append("&");
                }
                builder.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            url = builder.toString();
        }

        HttpHeaders headers = new HttpHeaders();
        requestDefinition.getHeaders().forEach(headers::add);
        headers.setContentType(resolveContentType(config.getContentType()));
        HttpEntity<String> entity = new HttpEntity<>(JSONUtil.toJsonStr(requestDefinition.getBody()), headers);
        ResponseEntity<String> response = integrationRestTemplate.exchange(
            URI.create(url),
            resolveMethod(config.getHttpMethod()),
            entity,
            String.class
        );
        return response.getBody();
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
