package com.forgex.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 网关鉴权白名单规则测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see GatewayAuthPathRules
 */
class GatewayAuthPathRulesTest {

    /**
     * 登录页 Logo、背景和头像依赖匿名 GET 公开文件。
     */
    @Test
    void publicFileGetDoesNotNeedAuth() {
        assertFalse(GatewayAuthPathRules.needAuth("/api/sys/files/logo.png", HttpMethod.GET));
        assertFalse(GatewayAuthPathRules.needAuth("/api/sys/files/bg.mp4", HttpMethod.HEAD));
        assertTrue(GatewayAuthPathRules.isPublicFileRead("/api/sys/files/logo.png", HttpMethod.GET));
    }

    /**
     * 文件上传、删除仍必须登录。
     */
    @Test
    void fileWriteStillNeedsAuth() {
        assertTrue(GatewayAuthPathRules.needAuth("/api/sys/file/upload", HttpMethod.POST));
        assertTrue(GatewayAuthPathRules.needAuth("/api/sys/files/logo.png", HttpMethod.POST));
        assertTrue(GatewayAuthPathRules.needAuth("/api/sys/files/logo.png", HttpMethod.DELETE));
    }

    /**
     * 业务查询接口默认仍需登录。
     */
    @Test
    void protectedSysApiStillNeedsAuth() {
        assertTrue(GatewayAuthPathRules.needAuth("/api/sys/user/page", HttpMethod.POST));
        assertFalse(GatewayAuthPathRules.needAuth("/api/sys/config/system-basic", HttpMethod.GET));
        assertFalse(GatewayAuthPathRules.needAuth("/api/auth/login", HttpMethod.POST));
    }
}
