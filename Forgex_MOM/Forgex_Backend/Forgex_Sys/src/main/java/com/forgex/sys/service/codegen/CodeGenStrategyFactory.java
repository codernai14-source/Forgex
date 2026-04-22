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
package com.forgex.sys.service.codegen;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.enums.SysPromptEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 代码生成策略工厂
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@Component
public class CodeGenStrategyFactory {

    private final List<CodeGenStrategy> strategies;

    public CodeGenStrategyFactory(List<CodeGenStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * 根据页面类型选择策略
     *
     * @param pageType 页面类型
     * @return 对应策略
     */
    public CodeGenStrategy getStrategy(String pageType) {
        return strategies.stream()
            .filter(strategy -> strategy.supports(pageType))
            .findFirst()
            .orElseThrow(() -> new I18nBusinessException(
                StatusCode.BUSINESS_ERROR,
                SysPromptEnum.CODEGEN_PAGE_TYPE_INVALID,
                pageType
            ));
    }
}
