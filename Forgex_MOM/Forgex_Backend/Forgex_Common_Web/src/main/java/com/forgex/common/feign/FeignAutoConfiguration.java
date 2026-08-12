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
package com.forgex.common.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 自动配置
 * <p>
 * 自动注册 Feign Token 拦截器
 * 所有引入 Common 模块的服务都会自动启用此配置
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2026-04-06
 */
@Configuration
public class FeignAutoConfiguration {

    /**
     * 配置 Feign Token 拦截器
     * <p>
     * 该拦截器会在所有 Feign 请求中自动添加当前请求的 Authorization 和 Tenant-Id Header
     * 使得服务间调用可以传递用户身份，无需重新登录
     * </p>
     *
     * @return FeignTokenInterceptor 实例
     */
    @Bean
    public FeignTokenInterceptor feignTokenInterceptor() {
        return new FeignTokenInterceptor();
    }
}
