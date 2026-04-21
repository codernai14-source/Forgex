package com.forgex.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.util.RedisHelper;
import com.forgex.integration.config.IntegrationProperties;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.param.ApiParamMappingParam;
import com.forgex.integration.service.IApiConfigService;
import com.forgex.integration.service.IApiDefinitionService;
import com.forgex.integration.service.IApiParamConfigService;
import com.forgex.integration.service.IApiParamMappingService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * 鎺ュ彛瀹氫箟蹇収鏈嶅姟
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiDefinitionServiceImpl implements IApiDefinitionService {

    private final IApiConfigService apiConfigService;
    private final IApiParamConfigService apiParamConfigService;
    private final IApiParamMappingService apiParamMappingService;
    private final RedisHelper redisHelper;
    private final ObjectMapper objectMapper;
    private final IntegrationProperties properties;

    private Cache<String, ApiDefinitionSnapshot> localCache;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.localCache = Caffeine.newBuilder()
            .maximumSize(properties.getCache().getMaxSize())
            .expireAfterWrite(Duration.ofSeconds(properties.getCache().getLocalExpireSeconds()))
            .build();
    }

    @Override
    public ApiDefinitionSnapshot getSnapshot(String apiCode, String direction) {
        String cacheKey = buildCacheKey(apiCode, direction);
        ApiDefinitionSnapshot local = localCache.getIfPresent(cacheKey);
        if (local != null) {
            return local;
        }

        ApiDefinitionSnapshot remote = redisHelper.getJson(cacheKey, ApiDefinitionSnapshot.class);
        if (remote != null) {
            localCache.put(cacheKey, remote);
            return remote;
        }

        ApiConfigDTO config = apiConfigService.getByApiCode(apiCode);
        if (config == null) {
            return null;
        }

        List<ApiParamConfigDTO> requestTree = safeTree(config.getId(), "REQUEST");
        List<ApiParamConfigDTO> responseTree = safeTree(config.getId(), "RESPONSE");
        List<ApiParamMappingDTO> inboundMappings = listMappings(config.getId(), "INBOUND");
        List<ApiParamMappingDTO> outboundMappings = listMappings(config.getId(), "OUTBOUND");

        ApiDefinitionSnapshot snapshot = ApiDefinitionSnapshot.builder()
            .tenantId(resolveTenantId())
            .cacheKey(cacheKey)
            .apiConfig(config)
            .requestParamTree(requestTree)
            .responseParamTree(responseTree)
            .inboundMappings(inboundMappings)
            .outboundMappings(outboundMappings)
            .build();

        localCache.put(cacheKey, snapshot);
        redisHelper.setJson(cacheKey, snapshot, Duration.ofSeconds(properties.getCache().getRedisExpireSeconds()));
        return snapshot;
    }

    @Override
    public void evict(String apiCode, String direction) {
        String cacheKey = buildCacheKey(apiCode, direction);
        localCache.invalidate(cacheKey);
        redisHelper.delete(cacheKey);
    }

    private List<ApiParamConfigDTO> safeTree(Long apiConfigId, String direction) {
        try {
            return apiParamConfigService.listParamTree(apiConfigId, direction)
                .stream()
                .map(this::toDto)
                .toList();
        } catch (Exception ex) {
            log.warn("load api param tree failed, apiConfigId={}, direction={}", apiConfigId, direction, ex);
            return Collections.emptyList();
        }
    }

    private ApiParamConfigDTO toDto(com.forgex.integration.domain.vo.ApiParamTreeVO item) {
        ApiParamConfigDTO dto = new ApiParamConfigDTO();
        dto.setId(item.getId());
        dto.setApiConfigId(item.getApiConfigId());
        dto.setParentId(item.getParentId());
        dto.setDirection(item.getDirection());
        dto.setNodeType(item.getNodeType());
        dto.setFieldName(item.getFieldName());
        dto.setFieldLabel(item.getFieldLabel());
        dto.setFieldType(item.getFieldType());
        dto.setFieldPath(item.getFieldPath());
        dto.setRequired(item.getRequired());
        dto.setDefaultValue(item.getDefaultValue());
        dto.setDictCode(item.getDictCode());
        dto.setOrderNum(item.getOrderNum());
        dto.setRemark(item.getRemark());
        if (item.getChildren() != null) {
            dto.setChildren(item.getChildren().stream().map(this::toDto).toList());
        }
        return dto;
    }

    private List<ApiParamMappingDTO> listMappings(Long apiConfigId, String direction) {
        ApiParamMappingParam param = new ApiParamMappingParam();
        param.setApiConfigId(apiConfigId);
        param.setDirection(direction);
        return apiParamMappingService.listMappings(param);
    }

    private String buildCacheKey(String apiCode, String direction) {
        return "integration:definition:" + resolveTenantId() + ":" + direction + ":" + apiCode;
    }

    private Long resolveTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId != null ? tenantId : 0L;
    }
}
