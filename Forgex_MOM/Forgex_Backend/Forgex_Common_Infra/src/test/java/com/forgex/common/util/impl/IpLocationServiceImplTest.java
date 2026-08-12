package com.forgex.common.util.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * IP 归属地解析服务测试。
 *
 * @author Forgex Team
 * @since 2026-05-05
 */
class IpLocationServiceImplTest {

    @Test
    void shouldResolvePublicIpWithLocalXdb() {
        IpLocationServiceImpl service = new IpLocationServiceImpl();
        assertNotEquals("未知", service.getLocationByIp("111.37.243.116"));
        assertNotEquals("未知", service.getLocationByIp("223.81.191.101"));
    }

    @Test
    void shouldReturnLocalForLoopbackIp() {
        IpLocationServiceImpl service = new IpLocationServiceImpl();
        assertEquals("本地", service.getLocationByIp("127.0.0.1"));
    }

    @Test
    void shouldReturnUnknownForBlankIp() {
        IpLocationServiceImpl service = new IpLocationServiceImpl();
        assertEquals("未知", service.getLocationByIp(" "));
    }
}
