package com.forgex.admin.client;

import com.forgex.common.api.feign.SysUserFeignClient;
import com.forgex.common.audit.OperationLogFeignClient;
import com.forgex.common.audit.OperationLogRecorder;
import com.forgex.common.audit.RemoteOperationLogRecorder;
import com.forgex.common.feign.client.EncodeRuleFeignClient;
import com.forgex.common.spi.EncodeRuleProvider;
import com.forgex.common.spi.UserDirectory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/** 显式引入 admin-client 后注册平台适配器；公共 Starter 不引入此配置。 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "forgex.admin.client", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableFeignClients(clients = {SysUserFeignClient.class, EncodeRuleFeignClient.class, OperationLogFeignClient.class})
public class AdminClientAutoConfiguration {
    /** @param client 用户客户端 @return 用户目录适配器 */
    @Bean
    @ConditionalOnMissingBean(UserDirectory.class)
    public UserDirectory userDirectory(SysUserFeignClient client) {
        return new FeignUserDirectory(client);
    }

    /** @param client 编码客户端 @return 编码提供者 */
    @Bean
    @ConditionalOnMissingBean(EncodeRuleProvider.class)
    public EncodeRuleProvider encodeRuleProvider(EncodeRuleFeignClient client) {
        return client::generateCode;
    }

    /** @param client 审计客户端 @return 远程审计写入器，本地实现优先 */
    @Bean
    @ConditionalOnMissingBean(OperationLogRecorder.class)
    public OperationLogRecorder remoteOperationLogRecorder(OperationLogFeignClient client) {
        return new RemoteOperationLogRecorder(client);
    }
}
