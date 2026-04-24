package com.forgex.integration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.util.RedisHelper;
import com.forgex.integration.config.IntegrationProperties;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.dto.ApiOutboundTargetDTO;
import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import com.forgex.integration.domain.model.ApiDefinitionSnapshot;
import com.forgex.integration.domain.param.ApiParamConfigParam;
import com.forgex.integration.domain.param.ApiParamMappingParam;
import com.forgex.integration.service.IApiConfigService;
import com.forgex.integration.service.IApiDefinitionService;
import com.forgex.integration.service.IApiOutboundTargetService;
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
 * 接口定义快照服务实现。
 * <p>
 * 负责按接口编码和方向加载接口配置、参数树、字段映射和出站目标，并将结果缓存到本地 Caffeine 和 Redis。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see IApiDefinitionService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiDefinitionServiceImpl implements IApiDefinitionService {

    /**
     * 接口配置服务。
     */
    private final IApiConfigService apiConfigService;

    /**
     * 参数配置服务。
     */
    private final IApiParamConfigService apiParamConfigService;

    /**
     * 字段映射服务。
     */
    private final IApiParamMappingService apiParamMappingService;

    /**
     * 出站目标服务。
     */
    private final IApiOutboundTargetService apiOutboundTargetService;

    /**
     * Redis 工具。
     */
    private final RedisHelper redisHelper;

    /**
     * JSON 序列化器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 集成平台配置。
     */
    private final IntegrationProperties properties;

    /**
     * 本地接口定义快照缓存。
     */
    private Cache<String, ApiDefinitionSnapshot> localCache;

    /**
     * 初始化本地缓存。
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        this.localCache = Caffeine.newBuilder()
            .maximumSize(properties.getCache().getMaxSize())
            .expireAfterWrite(Duration.ofSeconds(properties.getCache().getLocalExpireSeconds()))
            .build();
    }

    /**
     * 获取接口定义快照。
     *
     * @param apiCode   接口编码
     * @param direction 接口方向
     * @return 接口定义快照，不存在时返回 null
     */
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

        List<ApiOutboundTargetDTO> targets = apiOutboundTargetService.listEnabledByApiConfigId(config.getId());
        List<ApiParamConfigDTO> requestTree = safeTree(config.getId(), null, "REQUEST");
        List<ApiParamConfigDTO> responseTree = safeTree(config.getId(), null, "RESPONSE");
        List<ApiParamMappingDTO> inboundMappings = listMappings(config.getId(), null, "INBOUND");
        List<ApiParamMappingDTO> outboundMappings = loadOutboundMappings(config.getId(), targets);

        ApiDefinitionSnapshot snapshot = ApiDefinitionSnapshot.builder()
            .tenantId(resolveTenantId())
            .cacheKey(cacheKey)
            .apiConfig(config)
            .requestParamTree(requestTree)
            .responseParamTree(responseTree)
            .inboundMappings(inboundMappings)
            .outboundMappings(outboundMappings)
            .outboundTargets(targets)
            .build();

        localCache.put(cacheKey, snapshot);
        redisHelper.setJson(cacheKey, snapshot, Duration.ofSeconds(properties.getCache().getRedisExpireSeconds()));
        return snapshot;
    }

    /**
     * 清理接口定义快照缓存。
     *
     * @param apiCode   接口编码
     * @param direction 接口方向
     */
    @Override
    public void evict(String apiCode, String direction) {
        String cacheKey = buildCacheKey(apiCode, direction);
        localCache.invalidate(cacheKey);
        redisHelper.delete(cacheKey);
    }

    private List<ApiParamConfigDTO> safeTree(Long apiConfigId, Long outboundTargetId, String direction) {
        try {
            ApiParamConfigParam param = new ApiParamConfigParam();
            param.setApiConfigId(apiConfigId);
            param.setOutboundTargetId(outboundTargetId);
            param.setDirection(direction);
            return apiParamConfigService.listParamTree(apiConfigId, outboundTargetId, direction)
                .stream()
                .map(this::toDto)
                .toList();
        } catch (Exception ex) {
            log.warn("load api param tree failed, apiConfigId={}, outboundTargetId={}, direction={}",
                apiConfigId, outboundTargetId, direction, ex);
            return Collections.emptyList();
        }
    }

    private ApiParamConfigDTO toDto(com.forgex.integration.domain.vo.ApiParamTreeVO item) {
        ApiParamConfigDTO dto = new ApiParamConfigDTO();
        dto.setId(item.getId());
        dto.setApiConfigId(item.getApiConfigId());
        dto.setOutboundTargetId(item.getOutboundTargetId());
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

    private List<ApiParamMappingDTO> listMappings(Long apiConfigId, Long outboundTargetId, String direction) {
        ApiParamMappingParam param = new ApiParamMappingParam();
        param.setApiConfigId(apiConfigId);
        param.setOutboundTargetId(outboundTargetId);
        param.setDirection(direction);
        return apiParamMappingService.listMappings(param);
    }

    private List<ApiParamMappingDTO> loadOutboundMappings(Long apiConfigId, List<ApiOutboundTargetDTO> targets) {
        List<ApiParamMappingDTO> mappings = new java.util.ArrayList<>(listMappings(apiConfigId, null, "OUTBOUND"));
        if (targets != null) {
            for (ApiOutboundTargetDTO target : targets) {
                if (target != null && target.getId() != null) {
                    mappings.addAll(listMappings(apiConfigId, target.getId(), "OUTBOUND"));
                }
            }
        }
        return mappings;
    }

    private String buildCacheKey(String apiCode, String direction) {
        return "integration:definition:" + resolveTenantId() + ":" + direction + ":" + apiCode;
    }

    private Long resolveTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId != null ? tenantId : 0L;
    }
}
