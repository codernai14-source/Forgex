package com.forgex.job.core.executor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.job.config.JobProperties;
import com.forgex.job.domain.entity.SysJobTask;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

/**
 * HTTP 任务调用器。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Component
public class HttpJobInvoker {

    private final JobProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public HttpJobInvoker(JobProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public JobResult invoke(SysJobTask task, JobExecutionContext context) throws Exception {
        if (!StringUtils.hasText(task.getHttpUrl())) {
            throw new IllegalArgumentException("httpUrl required");
        }
        URI uri = URI.create(task.getHttpUrl());
        boolean allowed = properties.getSecurity().getHttpWhitelist().stream()
            .anyMatch(prefix -> StringUtils.hasText(prefix) && task.getHttpUrl().startsWith(prefix));
        if (!allowed) {
            throw new IllegalArgumentException("HTTP target is not in whitelist");
        }
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(task.getHttpHeaders())) {
            Map<String, String> map = objectMapper.readValue(task.getHttpHeaders(), new TypeReference<>() {});
            map.forEach(headers::add);
        }
        HttpMethod method = StringUtils.hasText(task.getHttpMethod())
            ? HttpMethod.valueOf(task.getHttpMethod().toUpperCase()) : HttpMethod.POST;
        ResponseEntity<String> response = restTemplate.exchange(uri, method, new HttpEntity<>(context.getParams(), headers), String.class);
        return response.getStatusCode().is2xxSuccessful()
            ? JobResult.success(response.getBody())
            : JobResult.failure(response.getStatusCode() + ":" + response.getBody());
    }
}
