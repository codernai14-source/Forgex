package com.forgex.integration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.forgex.common.web.R;

/**
 * API 路由服务接口
 * <p>
 * 提供统一对外接口的路由功能，包括请求路由、授权校验、参数转换等核心功能
 * 这是 Integration 模块的核心服务，负责将外部请求路由到内部处理器
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see com.forgex.integration.service.IApiCallLogService
 */
public interface IApiRouterService {

    /**
     * 路由请求
     * <p>
     * 核心方法：将外部请求路由到对应的内部处理器
     * 流程：授权校验 -> 参数转换 -> 处理器路由 -> 执行调用 -> 记录日志
     * </p>
     *
     * @param apiCode 接口编码（用于识别路由到哪个处理器）
     * @param requestData 请求数据（JSON 格式）
     * @param callerIp 调用方 IP 地址
     * @return 调用结果
     * @see #checkAuthorization(String, String)
     * @see #transformParameters(JsonNode, String)
     * @see #routeToProcessor(String, Object)
     */
    R<Object> routeRequest(String apiCode, JsonNode requestData, String callerIp);

    /**
     * 检查授权
     * <p>
     * 校验调用方是否有权限访问指定接口
     * 支持 Token 校验和 IP 白名单校验
     * </p>
     *
     * @param apiCode 接口编码
     * @param token 调用方 Token（可选）
     * @return 授权结果，true-通过，false-拒绝
     * @see com.forgex.integration.domain.entity.ThirdAuthorization
     */
    boolean checkAuthorization(String apiCode, String token);

    /**
     * 参数转换
     * <p>
     * 将外部请求参数转换为内部处理器所需的参数格式
     * 根据接口配置的参数映射规则进行转换
     * </p>
     *
     * @param requestData 外部请求数据
     * @param apiCode 接口编码
     * @return 转换后的参数对象
     * @see com.forgex.integration.domain.entity.ApiParamMapping
     */
    Object transformParameters(JsonNode requestData, String apiCode);

    /**
     * 路由到处理器
     * <p>
     * 根据接口编码找到对应的处理器并执行调用
     * 使用 Spring ApplicationContext 动态获取处理器 Bean
     * </p>
     *
     * @param apiCode 接口编码
     * @param params 转换后的参数对象
     * @return 处理器返回结果
     * @see com.forgex.integration.domain.entity.ApiConfig
     */
    Object routeToProcessor(String apiCode, Object params);
}
