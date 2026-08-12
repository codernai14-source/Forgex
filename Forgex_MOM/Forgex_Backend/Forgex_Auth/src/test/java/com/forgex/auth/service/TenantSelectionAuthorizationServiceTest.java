package com.forgex.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class TenantSelectionAuthorizationServiceTest {

    private LoginInteractionCodeService interactionCodeService;
    private TenantSelectionAuthorizationService service;

    @BeforeEach
    void setUp() {
        interactionCodeService = mock(LoginInteractionCodeService.class);
        service = new TenantSelectionAuthorizationService(interactionCodeService);
    }

    @Test
    void unauthenticatedRequestRequiresConsumableInteractionCode() {
        when(interactionCodeService.consume("valid", 7L, "demo", "B"))
                .thenReturn(com.forgex.auth.domain.dto.LoginInteractionContext.of(7L, "demo", "B"));
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(false);

            assertTrue(service.authorize(7L, "demo", "B", "valid"));
            assertFalse(service.authorize(7L, "demo", "B", null));
        }
    }

    @Test
    void authenticatedRequestAllowsOnlySameAccount() {
        try (MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            stp.when(StpUtil::isLogin).thenReturn(true);
            stp.when(StpUtil::getLoginIdAsString).thenReturn("demo");

            assertTrue(service.authorize(7L, "demo", "B", null));
            assertFalse(service.authorize(8L, "other", "B", null));
        }
    }
}
