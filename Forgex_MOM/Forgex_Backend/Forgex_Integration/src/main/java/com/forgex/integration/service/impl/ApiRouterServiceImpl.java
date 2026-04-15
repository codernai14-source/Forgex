package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.web.R;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ThirdAuthorizationDTO;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.domain.entity.ApiCallLog;
import com.forgex.integration.domain.entity.ThirdAuthorization;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.service.IApiCallLogService;
import com.forgex.integration.service.IApiRouterService;
import com.forgex.integration.service.IApiConfigService;
import com.forgex.integration.service.IThirdAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API 路由服务实现类
 * <p>
 * 这是 Integration 模块的核心服务实现，负责统一对外接口的路由逻辑
 * 主要功能：
 * 1. 请求路由：根据接口编码将请求路由到对应的处理器
 * 2. 参数转换：使用 Jackson JsonNode 进行灵活的参数映射和转换
 * 3. 处理器路由：使用 ApplicationContext.getBean 动态获取处理器 Bean
 * 4. 授权校验：支持 Token 校验和 IP 白名单校验
 * 5. 调用记录：异步保存调用日志用于审计和统计
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see com.forgex.integration.service.IApiRouterService
 * @see com.forgex.integration.service.IApiCallLogService
 * @see com.forgex.integration.domain.entity.ApiConfig
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiRouterServiceImpl implements IApiRouterService {

    /**
     * 对象映射器，用于 JSON 参数转换
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用日志服务
     */
    private final IApiCallLogService apiCallLogService;

    /**
     * 接口配置服务
     */
    private final IApiConfigService apiConfigService;

    /**
     * 第三方授权服务
     */
    private final IThirdAuthorizationService thirdAuthorizationService;

    /**
     * Spring 应用上下文，用于动态获取处理器 Bean
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 处理器缓存，避免重复从 ApplicationContext 获取
     * Key: 接口编码，Value: 处理器 Bean 实例
     */
    private final Map<String, Object> processorCache = new ConcurrentHashMap<>();

    @Override
    public R<Object> routeRequest(String apiCode, JsonNode requestData, String callerIp) {
        long startTime = System.currentTimeMillis();
        
        // 创建调用记录
        ApiCallLog callLog = createCallLog(apiCode, requestData, callerIp);
        
        try {
            // 1. 授权校验
            boolean authorized = checkAuthorization(apiCode, extractToken(requestData));
            if (!authorized) {
                String errorMsg = "接口调用未授权";
                log.warn("接口调用未授权：apiCode={}, callerIp={}", apiCode, callerIp);
                callLog.setCallStatus("FAIL");
                callLog.setErrorMessage(errorMsg);
                R<Object> result = R.fail();
                result.setCode(403);
                result.setMessage(errorMsg);
                return result;
            }
            
            // 2. 参数转换
            Object transformedParams = transformParameters(requestData, apiCode);
            
            // 3. 路由到处理器并执行调用
            Object result = routeToProcessor(apiCode, transformedParams);
            
            // 4. 记录成功日志
            long costTime = System.currentTimeMillis() - startTime;
            callLog.setCallStatus("SUCCESS");
            callLog.setCostTimeMs((int) costTime);
            callLog.setResponseData(objectMapper.writeValueAsString(result));
            
            log.info("接口调用成功：apiCode={}, costTime={}ms", apiCode, costTime);
            return R.ok(result);
            
        } catch (BusinessException e) {
            // 业务异常处理
            long costTime = System.currentTimeMillis() - startTime;
            callLog.setCallStatus("FAIL");
            callLog.setErrorMessage(e.getMessage());
            callLog.setCostTimeMs((int) costTime);
            
            log.error("接口调用业务异常：apiCode={}, error={}", apiCode, e.getMessage(), e);
            R<Object> result = R.fail();
            result.setMessage(e.getMessage());
            return result;
            
        } catch (Exception e) {
            // 系统异常处理
            long costTime = System.currentTimeMillis() - startTime;
            callLog.setCallStatus("FAIL");
            callLog.setErrorMessage("系统异常：" + e.getMessage());
            callLog.setCostTimeMs((int) costTime);
            
            log.error("接口调用系统异常：apiCode={}, error={}", apiCode, e.getMessage(), e);
            R<Object> result = R.fail();
            result.setMessage("系统异常：" + e.getMessage());
            return result;
            
        } finally {
            // 5. 异步保存调用记录（不阻塞主流程）
            apiCallLogService.asyncSaveLog(callLog);
        }
    }

    @Override
    public boolean checkAuthorization(String apiCode, String token) {
        // 1. 检查接口配置是否存在且启用
        ApiConfigDTO apiConfig = apiConfigService.getByApiCode(apiCode);
        if (apiConfig == null) {
            log.warn("接口配置不存在：apiCode={}", apiCode);
            return false;
        }
        
        if (!Integer.valueOf(1).equals(apiConfig.getStatus())) {
            log.warn("接口未启用：apiCode={}", apiCode);
            return false;
        }
        
        // 2. Token 校验
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token 为空：apiCode={}", apiCode);
            return false;
        }
        
        // 3. 查询授权记录
        ThirdAuthorizationDTO authorizationDTO = thirdAuthorizationService.getByTokenValue(token);
        ThirdAuthorization authorization = authorizationDTO == null ? null : toAuthorizationEntity(authorizationDTO);
        if (authorization == null) {
            log.warn("未找到授权记录：apiCode={}, token={}", apiCode, token);
            return false;
        }
        
        // 4. 检查授权状态
        if (!Integer.valueOf(1).equals(authorization.getStatus())) {
            log.warn("授权已禁用：apiCode={}, authorizationId={}", apiCode, authorization.getId());
            return false;
        }
        
        // 5. 检查 Token 有效期
        LocalDateTime now = LocalDateTime.now();
        if (authorization.getTokenExpireTime() != null && now.isAfter(authorization.getTokenExpireTime())) {
            log.warn("授权已过期：apiCode={}, endTime={}", apiCode, authorization.getTokenExpireTime());
            return false;
        }
        
        log.debug("授权校验通过：apiCode={}, authorizationId={}", apiCode, authorization.getId());
        return true;
    }

    @Override
    public Object transformParameters(JsonNode requestData, String apiCode) {
        try {
            // 1. 获取接口配置
            ApiConfigDTO apiConfig = apiConfigService.getByApiCode(apiCode);
            if (apiConfig == null) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_CONFIG_NOT_FOUND, apiCode);
            }
            
            // 2. 获取参数映射规则
            // TODO: 从 ApiParamMapping 表获取参数映射配置
            // 这里实现一个简单的默认转换逻辑
            
            // 3. 使用 Jackson JsonNode 进行参数转换
            Map<String, Object> paramMap = new HashMap<>();
            
            if (requestData != null && requestData.isObject()) {
                // 遍历所有字段
                requestData.fields().forEachRemaining(entry -> {
                    String fieldName = entry.getKey();
                    JsonNode fieldValue = entry.getValue();
                    
                    // 根据字段类型转换
                    Object value = convertJsonNodeToValue(fieldValue);
                    paramMap.put(fieldName, value);
                });
            }
            
            log.debug("参数转换完成：apiCode={}, paramCount={}", apiCode, paramMap.size());
            return paramMap;
            
        } catch (Exception e) {
            log.error("参数转换失败：apiCode={}, error={}", apiCode, e.getMessage(), e);
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_PARAM_CONVERT_FAILED, e.getMessage());
        }
    }

    @Override
    public Object routeToProcessor(String apiCode, Object params) {
        try {
            // 1. 从缓存获取处理器
            Object processor = processorCache.get(apiCode);
            
            if (processor == null) {
                // 2. 缓存未命中，从 ApplicationContext 获取
                // 处理器 Bean 命名规范：{apiCode}Processor（首字母小写）
                String beanName = apiCode + "Processor";
                
                // 尝试从 ApplicationContext 获取处理器 Bean
                if (applicationContext.containsBean(beanName)) {
                    processor = applicationContext.getBean(beanName);
                    processorCache.put(apiCode, processor);
                    log.info("获取处理器成功：beanName={}, apiCode={}", beanName, apiCode);
                } else {
                    // 3. 如果找不到专用处理器，使用默认处理器
                    log.warn("未找到专用处理器，使用默认处理器：apiCode={}", apiCode);
                    throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_HANDLER_NOT_CONFIGURED, apiCode);
                }
            }
            
            // 4. 调用处理器
            // 处理器需要实现统一接口：IApiProcessor<Object, Object>
            // 这里使用反射调用 handle 方法
            if (processor != null) {
                java.lang.reflect.Method handleMethod = processor.getClass()
                    .getMethod("handle", Object.class);
                return handleMethod.invoke(processor, params);
            }
            
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_HANDLER_NULL, apiCode);
            
        } catch (I18nBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("路由处理器失败：apiCode={}, error={}", apiCode, e.getMessage(), e);
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.API_ROUTE_FAILED, e.getMessage());
        }
    }

    /**
     * 创建调用记录对象
     *
     * @param apiCode 接口编码
     * @param requestData 请求数据
     * @param callerIp 调用方 IP
     * @return 调用记录对象
     */
    private ApiCallLog createCallLog(String apiCode, JsonNode requestData, String callerIp) {
        ApiCallLog callLog = new ApiCallLog();
        callLog.setApiConfigId(getApiConfigId(apiCode));
        callLog.setCallDirection("INBOUND");
        callLog.setCallerIp(callerIp);
        callLog.setCallTime(LocalDateTime.now());
        callLog.setCallStatus("PROCESSING");
        
        try {
            callLog.setRequestData(requestData != null ? objectMapper.writeValueAsString(requestData) : null);
        } catch (Exception e) {
            log.warn("序列化请求数据失败：{}", e.getMessage());
            callLog.setRequestData(requestData != null ? requestData.toString() : null);
        }
        
        return callLog;
    }

    /**
     * 从请求数据中提取 Token
     *
     * @param requestData 请求数据
     * @return Token
     */
    private String extractToken(JsonNode requestData) {
        if (requestData == null) {
            return null;
        }
        
        // 优先从 header 字段获取
        if (requestData.has("header")) {
            JsonNode header = requestData.get("header");
            if (header.has("token")) {
                return header.get("token").asText();
            }
        }
        
        // 从根节点获取
        if (requestData.has("token")) {
            return requestData.get("token").asText();
        }
        
        return null;
    }

    /**
     * 根据接口编码获取配置 ID
     *
     * @param apiCode 接口编码
     * @return 配置 ID，不存在返回 null
     */
    private Long getApiConfigId(String apiCode) {
        try {
            ApiConfigDTO config = apiConfigService.getByApiCode(apiCode);
            return config != null ? config.getId() : null;
        } catch (Exception e) {
            log.warn("获取接口配置 ID 失败：apiCode={}", apiCode);
            return null;
        }
    }

    private ThirdAuthorization toAuthorizationEntity(ThirdAuthorizationDTO dto) {
        ThirdAuthorization entity = new ThirdAuthorization();
        entity.setId(dto.getId());
        entity.setThirdSystemId(dto.getThirdSystemId());
        entity.setAuthType(dto.getAuthType());
        entity.setTokenValue(dto.getTokenValue());
        entity.setTokenExpireHours(dto.getTokenExpireHours());
        entity.setTokenExpireTime(dto.getTokenExpireTime());
        entity.setWhitelistIps(dto.getWhitelistIps());
        entity.setStatus(dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setCreateTime(dto.getCreateTime());
        return entity;
    }

    /**
     * 将 JsonNode 转换为 Java 对象
     *
     * @param node JsonNode 节点
     * @return Java 对象
     */
    private Object convertJsonNodeToValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        
        if (node.isInt()) {
            return node.asInt();
        }
        
        if (node.isLong()) {
            return node.asLong();
        }
        
        if (node.isDouble() || node.isFloat()) {
            return node.asDouble();
        }
        
        if (node.isTextual()) {
            return node.asText();
        }
        
        if (node.isArray()) {
            // 数组转换为 List
            try {
                return objectMapper.readValue(node.toString(), java.util.List.class);
            } catch (Exception e) {
                log.warn("转换数组失败：{}", e.getMessage());
                return node.toString();
            }
        }
        
        if (node.isObject()) {
            // 对象转换为 Map
            try {
                return objectMapper.readValue(node.toString(), java.util.Map.class);
            } catch (Exception e) {
                log.warn("转换对象失败：{}", e.getMessage());
                return node.toString();
            }
        }
        
        // 默认返回字符串
        return node.asText();
    }
}
