package com.forgex.mobile.feature.auth

import com.forgex.mobile.core.common.i18n.AppText
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthEventMessageResolverTest {

    @Test
    fun dynamicTextUsesCachedLanguageBundleBeforeOfflineFallback() {
        val message = AuthEventMessageResolver.resolve(
            appText = AppText.Dynamic(
                key = "network.error.moduleOffline",
                fallbackRaw = "Service unavailable, please try again later"
            ),
            bundle = mapOf("network.error.moduleOffline" to "Service indisponible, reessayez plus tard"),
            resourceResolver = { _, _ -> null }
        )

        assertEquals("Service indisponible, reessayez plus tard", message)
    }

    @Test
    fun dynamicTextUsesOfflineFallbackWhenBundleIsUnavailable() {
        val message = AuthEventMessageResolver.resolve(
            appText = AppText.Dynamic(
                key = "network.error.passwordExpired",
                fallbackRaw = "Password expired, please change it"
            ),
            bundle = emptyMap(),
            resourceResolver = { _, _ -> null }
        )

        assertEquals("Password expired, please change it", message)
    }
}
