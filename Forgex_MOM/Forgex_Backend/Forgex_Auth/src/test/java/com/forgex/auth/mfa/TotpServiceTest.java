package com.forgex.auth.mfa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TotpServiceTest {
    @Test void generatedCodeVerifies() {
        TotpService service = new TotpService();
        String secret = service.generateSecret();
        long counter = java.time.Instant.now().getEpochSecond() / 30;
        assertTrue(service.verify(secret, service.generateCode(secret, counter)));
        assertFalse(service.verify(secret, "000000"));
    }
}
