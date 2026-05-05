package com.forgex.integration.spi;

import com.forgex.integration.domain.model.ApiExecutionContext;

/**
 * API 入站解释器。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface ApiInboundInterpreter {

    /**
     * 执行入站解释器处理逻辑。
     *
     * @param context 执行上下文
     * @param payload 请求载荷
     * @return 处理结果
     */
    Object handle(ApiExecutionContext context, Object payload);
}
