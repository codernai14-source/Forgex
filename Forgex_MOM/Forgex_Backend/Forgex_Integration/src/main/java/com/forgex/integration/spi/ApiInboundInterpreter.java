package com.forgex.integration.spi;

import com.forgex.integration.domain.model.ApiExecutionContext;

/**
 * 鍏ユ牳瑙ｉ噴鍣ㄦ爣鍑嗘帴鍙?
 */
public interface ApiInboundInterpreter {

    Object handle(ApiExecutionContext context, Object payload);
}
