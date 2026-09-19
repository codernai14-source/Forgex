package com.company.order.platform;

import com.forgex.common.api.feign.SysUserFeignClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** 平台模式只创建显式选择的用户客户端，不扫描其他平台 API。 */
@Configuration(proxyBeanMethods = false)
@Profile("platform")
@EnableFeignClients(clients = SysUserFeignClient.class)
public class PlatformUserConfiguration {
}
