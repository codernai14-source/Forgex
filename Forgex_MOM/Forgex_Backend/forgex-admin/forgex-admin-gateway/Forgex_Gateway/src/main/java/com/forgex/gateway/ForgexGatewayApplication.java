/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.gateway;

import com.forgex.common.license.LicenseAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

/**
 * 网关服务启动类
 * <p>
 * 负责启动API网关微服务，作为系统的统一入口
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>路由转发：将请求转发到不同的微服务</li>
 *   <li>负载均衡：支持服务发现和负载均衡</li>
 *   <li>请求过滤：在请求到达后端服务前进行统一处理</li>
 *   <li>租户透传：将租户信息透传给后端服务</li>
 *   <li>IP透传：将客户端真实IP透传给后端服务</li>
 * </ul>
 * <p>过滤器说明：</p>
 * <ul>
 *   <li>{@link com.forgex.gateway.filter.TenantPropagationGlobalFilter} - 租户上下文透传过滤器</li>
 *   <li>{@link com.forgex.gateway.filter.ClientIpPropagationFilter} - 客户端IP透传过滤器</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see org.springframework.cloud.gateway.GatewayFilter
 * @see org.springframework.cloud.client.discovery.EnableDiscoveryClient
 */
@SpringBootApplication(scanBasePackages = "com.forgex.gateway")
@Import(LicenseAutoConfiguration.class)
@EnableDiscoveryClient
public class ForgexGatewayApplication {
    /**
     * 应用入口
     * <p>启动Spring Boot应用</p>
     *
     * @param args 启动参数
     * @see org.springframework.boot.SpringApplication#run(Class, String[])
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用
        SpringApplication.run(ForgexGatewayApplication.class, args);
    }
}
