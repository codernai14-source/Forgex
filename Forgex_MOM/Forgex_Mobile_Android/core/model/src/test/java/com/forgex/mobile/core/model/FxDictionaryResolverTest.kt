package com.forgex.mobile.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FxDictionaryResolverTest {
    private val resolver = FxDictionaryResolver(
        listOf(
            FxDictionaryOption("enabled", "启用"),
            FxDictionaryOption("disabled", "停用")
        )
    )

    @Test
    fun resolvesKnownValueAndFallsBackToRawValue() {
        assertEquals("启用", resolver.label("enabled"))
        assertEquals("unknown", resolver.label("unknown"))
        assertEquals("-", resolver.label(null))
    }

    @Test
    fun returnsOptionForKnownValueOnly() {
        assertEquals("disabled", resolver.find("disabled")?.value)
        assertNull(resolver.find("missing"))
    }
}
