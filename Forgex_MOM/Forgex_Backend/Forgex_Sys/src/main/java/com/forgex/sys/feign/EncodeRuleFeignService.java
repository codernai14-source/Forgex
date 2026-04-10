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
package com.forgex.sys.feign;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.common.tenant.TenantContextIgnore;
import com.forgex.common.web.R;
import com.forgex.sys.service.ISysEncodeRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 编码规则 Feign 服务实现
 * <p>
 * 提供给 Common 模块和其他业务模块调用的 Feign 接口。
 * 通过 Feign 远程调用方式，避免 Common 模块直接依赖 Sys 模块，解决循环依赖问题。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>提供编码生成的 Feign 接口</li>
 *   <li>支持跨服务调用</li>
 *   <li>忽略租户隔离，支持公共编码规则调用</li>
 * </ul>
 * <p>调用链路：</p>
 * <pre>{@code
 * 业务模块
 *   ↓
 * EncodeUtils.nextEncode("ORDER_CODE")
 *   ↓
 * EncodeRuleService.generateCode()  [Common 模块]
 *   ↓
 * EncodeRuleFeignClient.generateCode()  [Feign Client]
 *   ↓
 * EncodeRuleFeignService.generateCode()  [本类，Sys 模块]
 *   ↓
 * ISysEncodeRuleService.generateCode()  [Sys 模块 Service]
 * }</pre>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see ISysEncodeRuleService 编码规则 Service
 * @see com.forgex.common.feign.client.EncodeRuleFeignClient Feign Client
 */
@RestController
@RequestMapping("/sys/encodeRule/feign")
@Slf4j
@RequiredArgsConstructor
public class EncodeRuleFeignService {

    /**
     * 编码规则 Service
     */
    private final ISysEncodeRuleService encodeRuleService;

    /**
     * 根据规则代码生成编码（Feign 接口）
     * <p>
     * 提供给 Common 模块和其他业务模块调用的编码生成接口。
     * 使用 TenantContextIgnore 忽略租户隔离，支持公共编码规则调用。
     * </p>
     *
     * @param ruleCode 规则代码，必须在 sys_encode_rule 表中存在且已启用
     * @return 生成的业务编码，失败时返回错误信息
     * @throws IllegalArgumentException 当规则代码为空、不存在或已禁用时抛出
     */
    @PostMapping("/generate")
    public R<String> generateCode(@RequestBody String ruleCode) {
        try {
            log.info("Feign 生成编码，规则代码：{}", ruleCode);
            String code = encodeRuleService.generateCode(ruleCode);
            return R.ok(code);
        } catch (IllegalArgumentException e) {
            log.error("生成编码失败，规则代码：{}, 错误：{}", ruleCode, e.getMessage());
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("生成编码异常，规则代码：{}", ruleCode, e);
            return R.fail("生成编码失败：" + e.getMessage());
        }
    }
}
