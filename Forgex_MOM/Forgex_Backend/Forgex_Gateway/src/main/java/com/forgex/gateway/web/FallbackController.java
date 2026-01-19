package com.forgex.gateway.web;

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 网关降级控制器
 * <p>当后端服务不可用时，提供降级响应。</p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>拦截服务不可用请求</li>
 *   <li>返回友好的降级提示信息</li>
 * </ul>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
public class FallbackController {

    /**
     * 服务降级处理
     * <p>当指定服务不可用时，返回降级响应。</p>
     * 
     * @param service 服务名称
     * @return 降级响应，包含服务不可用提示
     */
    @RequestMapping("/fallback/{service}")
    public Mono<R<Object>> fallback(@PathVariable("service") String service) {
        String moduleName = service == null ? "目标" : service;
        return Mono.just(R.fail(StatusCode.MODULE_OFFLINE, CommonPrompt.MODULE_OFFLINE, moduleName));
    }
}

