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
package com.forgex.common.service;

import com.forgex.common.feign.client.EncodeRuleFeignClient;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 公共编码生成服务
 * <p>
 * 统一封装编码规则的远程调用能力，避免各业务模块直接依赖 Sys 模块实现。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see EncodeRuleFeignClient
 * @see com.forgex.sys.feign.EncodeRuleFeignService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EncodeRuleService {

    /**
     * 延迟获取 Feign 客户端，避免未启用相关客户端时阻塞模块启动。
     */
    private final ObjectProvider<EncodeRuleFeignClient> encodeRuleFeignClientProvider;

    /**
     * 根据规则编码生成业务编码
     *
     * @param ruleCode 规则编码
     * @return 生成后的业务编码
     */
    public String generateCode(String ruleCode) {
        if (!StringUtils.hasText(ruleCode)) {
            throw new IllegalArgumentException("规则编码不能为空");
        }

        String normalizedRuleCode = ruleCode.trim();
        EncodeRuleFeignClient encodeRuleFeignClient = encodeRuleFeignClientProvider.getIfAvailable();
        if (encodeRuleFeignClient == null) {
            log.error("编码规则 Feign Client 未注入，无法生成编码：ruleCode={}", normalizedRuleCode);
            throw new IllegalStateException("编码规则远程服务未启用，请确认当前模块已正确配置 Feign 客户端");
        }

        log.info("生成编码，规则代码：{}", normalizedRuleCode);
        R<String> result = encodeRuleFeignClient.generateCode(normalizedRuleCode);
        if (result == null) {
            log.error("生成编码失败，规则代码：{}，远程返回为空", normalizedRuleCode);
            throw new RuntimeException("生成编码失败：远程服务未返回结果");
        }
        if (result.getCode() == null || result.getCode() != StatusCode.SUCCESS) {
            log.error("生成编码失败，规则代码：{}，错误信息：{}", normalizedRuleCode, result.getMessage());
            throw new RuntimeException("生成编码失败：" + result.getMessage());
        }

        return result.getData();
    }
}
