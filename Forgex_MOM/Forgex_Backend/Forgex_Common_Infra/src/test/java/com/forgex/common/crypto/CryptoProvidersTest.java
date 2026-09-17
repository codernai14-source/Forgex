package com.forgex.common.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CryptoProvidersTest {

    @Test
    void passwordResolverRejectsReversibleAlgorithms() {
        assertThrows(IllegalArgumentException.class, () -> CryptoProviders.resolvePassword("sm2", null));
        assertThrows(IllegalArgumentException.class, () -> CryptoProviders.resolvePassword("rsa", null));
        assertThrows(IllegalArgumentException.class, () -> CryptoProviders.resolvePassword("aes", null));
        assertThrows(IllegalArgumentException.class, () -> CryptoProviders.resolvePassword("sm4", null));
    }

    @Test
    void passwordResolverAllowsOneWayAlgorithms() {
        assertDoesNotThrow(() -> CryptoProviders.resolvePassword("bcrypt", null));
        assertDoesNotThrow(() -> CryptoProviders.resolvePassword("pbkdf2", null));
    }
}
