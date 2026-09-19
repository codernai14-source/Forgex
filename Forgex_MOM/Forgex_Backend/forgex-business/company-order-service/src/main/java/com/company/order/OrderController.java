package com.company.order;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.I18nPrompt;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/** 无数据库的订单示例，用于说明统一返回、上下文和 JSON 契约。 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    /**
     * 查询演示订单，不访问任何平台或中间件。
     * @param request 查询参数
     * @return 订单与当前请求上下文
     */
    @PostMapping("/preview")
    public R<OrderPreview> preview(@RequestBody OrderRequest request) {
        if (request.id() == null || request.id() <= 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, OrderPrompt.INVALID_ID, request.id());
        }
        return R.ok(new OrderPreview(request.id(), TenantContext.get(), UserContext.get(), LangContext.get(),
                LocalDateTime.of(2026, 9, 19, 10, 30), request.enabled()));
    }

    /**
     * 订单查询参数。
     * @param id 订单编号
     * @param enabled 演示兼容布尔值输入
     */
    public record OrderRequest(Long id, Boolean enabled) {
    }

    /**
     * 订单预览与请求上下文。
     * @param id 订单编号
     * @param tenantId 当前租户
     * @param userId 当前用户
     * @param lang 当前语言
     * @param createdAt 演示时间
     * @param enabled 启用状态
     */
    public record OrderPreview(Long id, Long tenantId, Long userId, String lang,
                               LocalDateTime createdAt, Boolean enabled) {
    }

    /** 示例业务提示；独立应用默认使用枚举模板，不读取平台国际化表。 */
    enum OrderPrompt implements I18nPrompt {
        INVALID_ID;

        /** @return 业务模块标识 */
        public String getModule() { return "company-order"; }

        /** @return 提示代码 */
        public String getPromptCode() { return name(); }

        /** @return 默认错误模板 */
        public String getDefaultTemplate() { return "订单编号 {0} 无效"; }
    }
}
