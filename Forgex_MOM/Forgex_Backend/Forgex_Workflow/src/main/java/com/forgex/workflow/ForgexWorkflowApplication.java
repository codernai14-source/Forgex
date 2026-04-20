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
package com.forgex.workflow;

import com.forgex.common.feign.client.EncodeRuleFeignClient;
import com.forgex.workflow.client.AuthPermClient;
import com.forgex.workflow.client.SysMessageClient;
import com.forgex.workflow.client.SysUserClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 工作流模块启动类
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@SpringBootApplication(scanBasePackages = {"com.forgex.workflow", "com.forgex.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackageClasses = {
        EncodeRuleFeignClient.class,
        AuthPermClient.class,
        SysMessageClient.class,
        SysUserClient.class
})
@EnableAsync
@MapperScan({"com.forgex.workflow.mapper", "com.forgex.common.mapper"})
public class ForgexWorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForgexWorkflowApplication.class, args);
    }
}
