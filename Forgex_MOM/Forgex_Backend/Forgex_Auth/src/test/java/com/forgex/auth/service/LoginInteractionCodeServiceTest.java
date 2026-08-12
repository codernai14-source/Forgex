package com.forgex.auth.service;

import com.forgex.auth.domain.dto.LoginInteractionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginInteractionCodeServiceTest {

    private RedissonClient redissonClient;
    private RBucket<String> bucket;
    private LoginInteractionCodeService service;

    @BeforeEach
    void setUp() {
        redissonClient = mock(RedissonClient.class);
        bucket = mock(RBucket.class);
        when(redissonClient.<String>getBucket(anyString())).thenReturn(bucket);
        service = new LoginInteractionCodeService(redissonClient);
    }

    @Test
    void issueStoresBoundContextForFiveMinutes() {
        String code = service.issue(7L, "demo", "B");

        assertNotNull(code);
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(bucket).set(payload.capture(), eq(300L), eq(TimeUnit.SECONDS));
        LoginInteractionContext context = LoginInteractionContext.fromJson(payload.getValue());
        assertEquals(7L, context.userId());
        assertEquals("demo", context.account());
        assertEquals("B", context.loginTerminal());
    }

    @Test
    void consumeAcceptsMatchingContextOnlyOnce() {
        String payload = LoginInteractionContext.of(7L, "demo", "B").toJson();
        when(bucket.get()).thenReturn(payload);
        when(bucket.compareAndSet(payload, null)).thenReturn(true, false);

        assertEquals(LoginInteractionContext.of(7L, "demo", "B"),
                service.consume("code", 7L, "demo", "B"));
        assertNull(service.consume("code", 7L, "demo", "B"));
    }

    @Test
    void consumeRejectsMismatchedContextWithoutDeletingCode() {
        String payload = LoginInteractionContext.of(7L, "demo", "B").toJson();
        when(bucket.get()).thenReturn(payload);

        assertNull(service.consume("code", 8L, "demo", "B"));
    }
}
