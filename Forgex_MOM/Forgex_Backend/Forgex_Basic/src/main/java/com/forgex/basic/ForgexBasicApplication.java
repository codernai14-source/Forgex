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
package com.forgex.basic;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 基础信息模块启动类。
 * <p>
 * 当前仅提供模块基础骨架与健康检查接口，暂不包含业务功能。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-09
 */
@SpringBootApplication(scanBasePackages = {"com.forgex.basic", "com.forgex.common"},
        exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableAsync
@Import(DynamicDataSourceAutoConfiguration.class)
@MapperScan({"com.forgex.basic.mapper", "com.forgex.common.mapper"})
public class ForgexBasicApplication {

    /**
     * 模块启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ForgexBasicApplication.class, args);
    }
}
