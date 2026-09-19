package com.forgex.gateway.filter;

import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

/**
 * 网关登录鉴权路径规则。
 * <p>
 * 登录页 Logo、登录背景、用户头像等通过浏览器原生 {@code <img>} / {@code <video>} 加载，
 * 不会携带 Axios 的 Authorization 头；网关登录态又只从 Cookie 读取 Token。
 * 未登录时没有 Cookie，跨域绝对地址时也不会带上当前站点 Cookie。
 * 因此 {@code GET/HEAD /api/sys/files/**} 必须允许匿名读取。
 * 上传、删除仍走 {@code POST /api/sys/file/**} 等需登录接口。
 * 本地文件名使用 UUID，枚举成本高；敏感附件不要走这条公开读取路径。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see GatewayAuthGlobalFilter
 */
public final class GatewayAuthPathRules {

    private GatewayAuthPathRules() {
    }

    /**
     * 判断指定路径是否需要登录后才能访问。
     *
     * @param path   网关看到的原始路径，例如 {@code /api/sys/files/xxx.png}
     * @param method HTTP 方法，可能为 {@code null}
     * @return {@code true} 表示必须先有有效登录会话
     */
    public static boolean needAuth(String path, HttpMethod method) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        if (path.startsWith("/api/auth/")) {
            return false;
        }
        if (path.equals("/api/sys/config/system-basic") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/config/login-captcha") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/i18n/languageType/listEnabled") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/i18n/languageType/getDefault") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/i18n/message/mobileBundle") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/init/status") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/init/apply") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/license/status") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/license/request-info") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/license/logs") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/sys/license/refresh") && HttpMethod.POST.equals(method)) {
            return false;
        }
        if (path.equals("/api/basic/module/ping") && HttpMethod.GET.equals(method)) {
            return false;
        }
        if (path.equals("/api/integration/invoke")) {
            return false;
        }
        if (path.equals("/api/integration/third-authorization/validate-token")) {
            return false;
        }
        if (path.startsWith("/api/integration/third-authorization/check-ip-whitelist/")) {
            return false;
        }
        // 登录页品牌资源和头像预览依赖匿名 GET，上传接口仍受下方 /api/sys/ 保护。
        if (isPublicFileRead(path, method)) {
            return false;
        }
        if (path.startsWith("/api/integration/api-config/")
                || path.startsWith("/api/integration/call-log/")
                || path.startsWith("/api/integration/param-config/")
                || path.startsWith("/api/integration/param-mapping/")
                || path.startsWith("/api/integration/third-system/")
                || path.startsWith("/api/integration/third-authorization/")) {
            return true;
        }
        return path.startsWith("/api/sys/")
                || path.startsWith("/api/basic/")
                || path.startsWith("/api/sys/app/")
                || path.startsWith("/api/wf/")
                || path.startsWith("/api/integration/")
                || path.startsWith("/api/job/")
                || path.startsWith("/api/report/");
    }

    /**
     * 判断是否为公开文件读取请求。
     *
     * @param path   请求路径
     * @param method HTTP 方法
     * @return {@code true} 表示允许匿名 GET/HEAD 本地公开文件
     */
    public static boolean isPublicFileRead(String path, HttpMethod method) {
        if (!StringUtils.hasText(path) || !path.startsWith("/api/sys/files/")) {
            return false;
        }
        return HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method);
    }
}
