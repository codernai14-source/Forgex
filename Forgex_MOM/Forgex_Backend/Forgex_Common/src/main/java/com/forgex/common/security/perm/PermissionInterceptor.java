package com.forgex.common.security.perm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 接口权限拦截器（按钮级）。
 * <p>
 * 对标注了 {@link RequirePerm} 的接口进行强制鉴权：\n
 * - 依赖 {@link UserContext}/{@link TenantContext} 获取当前用户与租户；\n
 * - 依赖 {@link PermKeyService} 计算 permKey；\n
 * - 校验不通过返回统一响应 {@link R}。\n
 * </p>
 *
 * <p>
 * 说明：Common 模块提供该拦截器，业务模块只需：\n
 * 1) 提供 {@link PermKeyService} Bean；\n
 * 2) 在本模块 WebMvc 配置中注册拦截路径（如 /sys/**）。\n
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see RequirePerm
 * @see PermKeyService
 */
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermKeyService permKeyService;
    private final ObjectMapper objectMapper;

    /**
     * 在 Controller 方法执行前进行权限校验。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return 允许继续处理返回 true；拒绝返回 false
     * @throws Exception 响应写出等异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        Set<String> required = resolveRequiredPerms(hm);
        if (required.isEmpty()) {
            return true;
        }

        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();
        if (userId == null || tenantId == null) {
            writeFail(response, 401, "未登录或租户未选择");
            return false;
        }

        boolean ok = permKeyService.hasAllPerms(userId, tenantId, required);
        if (!ok) {
            writeFail(response, 403, "无权限");
            return false;
        }

        return true;
    }

    /**
     * 解析 HandlerMethod 声明的权限键集合。
     *
     * @param hm HandlerMethod
     * @return 去重后的权限键集合
     */
    private Set<String> resolveRequiredPerms(HandlerMethod hm) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        RequirePerm classAnn = hm.getBeanType().getAnnotation(RequirePerm.class);
        if (classAnn != null && classAnn.value() != null) {
            set.addAll(Arrays.asList(classAnn.value()));
        }
        RequirePerm methodAnn = hm.getMethodAnnotation(RequirePerm.class);
        if (methodAnn != null && methodAnn.value() != null) {
            set.addAll(Arrays.asList(methodAnn.value()));
        }
        set.removeIf(s -> s == null || s.isBlank());
        return set;
    }

    /**
     * 输出统一失败响应。
     *
     * @param response 响应
     * @param code     业务码
     * @param msg      提示信息
     * @throws Exception 写出失败
     */
    private void writeFail(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(R.fail(code, msg)));
    }
}

