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
package com.forgex.sys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;

/**
 * 系统管理服务启动类
 * 提供系统配置、用户角色等管理能力
 */
@SpringBootApplication(scanBasePackages = "com.forgex", exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableAsync
@Import(DynamicDataSourceAutoConfiguration.class)
@MapperScan({"com.forgex.sys.mapper","com.forgex.common.mapper"})
public class ForgexSysApplication {
    /**
     * 应用入口
     * @param args 启动参数
     * @see org.springframework.boot.SpringApplication#run(Class, String[])
     */
    public static void main(String[] args) {
        SpringApplication.run(ForgexSysApplication.class, args);
    }
}
