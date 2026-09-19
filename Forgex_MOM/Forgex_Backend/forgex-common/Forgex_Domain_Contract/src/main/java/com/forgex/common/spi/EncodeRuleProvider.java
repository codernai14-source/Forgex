package com.forgex.common.spi;

import com.forgex.common.web.R;

/** 编码规则提供者，公共层无需了解平台服务和通信协议。 */
public interface EncodeRuleProvider {
    /**
     * 生成业务编码。
     * @param ruleCode 规则代码
     * @return 生成结果
     */
    R<String> generateCode(String ruleCode);
}
