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
package com.forgex.common.util;

import com.forgex.common.service.EncodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 编码生成工具类
 * <p>
 * 提供静态方法供各业务模块快速调用编码生成服务。
 * 通过 Spring 注入 EncodeRuleService，内部使用 Feign Client 调用 Sys 模块。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>提供静态方法生成编码</li>
 *   <li>封装 EncodeRuleService 调用</li>
 *   <li>支持便捷的工具类调用方式</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 生成销售订单编码
 * String code = EncodeUtils.nextEncode("SALE_ORDER");
 * 
 * // 生成采购订单编码
 * String purchaseCode = EncodeUtils.nextEncode("PURCHASE_ORDER");
 * }</pre>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see EncodeRuleService 编码生成服务
 * @see com.forgex.common.feign.client.EncodeRuleFeignClient Feign Client
 */
@Component
public class EncodeUtils {

    /**
     * 编码生成服务（通过 Spring 注入）
     */
    private static EncodeRuleService encodeRuleService;

    /**
     * Spring 注入时设置静态实例
     *
     * @param service 编码生成服务
     */
    @Autowired
    public void setEncodeRuleService(EncodeRuleService service) {
        EncodeUtils.encodeRuleService = service;
    }

    /**
     * 根据规则代码生成编码
     * <p>
     * 静态方法，供各业务模块快速调用。
     * </p>
     * <p>使用示例：</p>
     * <pre>{@code
     * String code = EncodeUtils.nextEncode("SALE_ORDER");
     * }</pre>
     *
     * @param ruleCode 规则代码，必须在 fx_encode_rule 表中存在且已启用
     * @return 生成的业务编码
     * @throws IllegalArgumentException 当规则代码为空时抛出
     * @throws RuntimeException 当远程调用失败时抛出
     */
    public static String nextEncode(String ruleCode) {
        if (encodeRuleService == null) {
            throw new IllegalStateException("EncodeUtils 未初始化，请确保 Spring 容器已启动");
        }
        return encodeRuleService.generateCode(ruleCode);
    }
}
