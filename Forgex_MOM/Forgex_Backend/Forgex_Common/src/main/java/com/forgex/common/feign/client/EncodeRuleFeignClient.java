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
package com.forgex.common.feign.client;

import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 编码规则 Feign Client
 * <p>
 * 用于 Common 模块和其他业务模块调用 Sys 模块的编码规则服务。
 * 通过 Feign 远程调用 Sys 模块的编码生成接口，避免循环依赖。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>根据规则代码生成编码</li>
 *   <li>支持跨服务调用</li>
 *   <li>避免 Common 模块直接依赖 Sys 模块</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * @Autowired
 * private EncodeRuleFeignClient encodeRuleFeignClient;
 * 
 * // 生成销售订单编码
 * R<String> result = encodeRuleFeignClient.generateCode("SALE_ORDER");
 * String code = result.getData();
 * }</pre>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see com.forgex.sys.feign.EncodeRuleFeignService Sys 模块的 Feign 服务实现
 */
@FeignClient(name = "forgex-sys", contextId = "encodeRuleFeignClient")
public interface EncodeRuleFeignClient {

    /**
     * 根据规则代码生成编码
     * <p>
     * 远程调用 Sys 模块的编码生成服务，生成唯一的业务编码。
     * </p>
     *
     * @param ruleCode 规则代码，必须在 sys_encode_rule 表中存在且已启用
     * @return 生成的业务编码，失败时返回错误信息
     * @see com.forgex.sys.feign.EncodeRuleFeignService
     */
    @PostMapping("/sys/encodeRule/feign/generate")
    R<String> generateCode(@RequestBody String ruleCode);
}
