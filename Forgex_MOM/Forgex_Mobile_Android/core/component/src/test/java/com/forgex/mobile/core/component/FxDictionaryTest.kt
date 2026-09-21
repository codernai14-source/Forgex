package com.forgex.mobile.core.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FxDictionaryTest {

    private val options = listOf(
        FxDictOption(value = "enabled", label = "启用"),
        FxDictOption(value = "disabled", label = "停用", enabled = false)
    )

    @Test
    fun resolverReturnsMatchingOptionAndLabel() {
        val resolver = FxDictionaryResolver(options)

        assertEquals("启用", resolver.labelOf("enabled"))
        assertEquals(options[1], resolver.resolve("disabled"))
    }

    @Test
    fun resolverReturnsNullAndFallbackForUnknownValue() {
        val resolver = FxDictionaryResolver(options)

        assertNull(resolver.resolve("missing"))
        assertEquals("missing", resolver.labelOf("missing"))
    }

    @Test
    fun resolverUsesCustomFallbackLabel() {
        val resolver = FxDictionaryResolver(options, fallbackLabel = "未知")

        assertEquals("未知", resolver.labelOf("missing"))
    }
}
