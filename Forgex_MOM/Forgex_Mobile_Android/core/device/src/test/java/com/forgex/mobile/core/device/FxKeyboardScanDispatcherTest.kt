package com.forgex.mobile.core.device

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class FxKeyboardScanDispatcherTest {

    private class ScriptedClock(var now: Long = 0L) {
        fun advance(millis: Long) {
            now += millis
        }
    }

    private fun type(dispatcher: FxKeyboardScanDispatcher, text: String, perKeyDelay: Long = 10L, clock: ScriptedClock) {
        text.forEach { char ->
            // keyCode 对组帧无意义，统一传 KEYCODE_UNKNOWN 即可。
            dispatcher.offer(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_UNKNOWN, char.code)
            clock.advance(perKeyDelay)
        }
    }

    @Test
    fun submitsFastTypedPayloadOnEnter() {
        val clock = ScriptedClock()
        val submitted = mutableListOf<String>()
        val dispatcher = FxKeyboardScanDispatcher(
            onSubmit = { submitted.add(it) },
            clock = { clock.now }
        )

        type(dispatcher, "MLOT-20260921-001", clock = clock)
        dispatcher.offer(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0)

        assertEquals(listOf("MLOT-20260921-001"), submitted)
    }

    @Test
    fun resetsFrameWhenTypingSlowerThanFramingWindow() {
        val clock = ScriptedClock()
        val submitted = mutableListOf<String>()
        val dispatcher = FxKeyboardScanDispatcher(
            onSubmit = { submitted.add(it) },
            clock = { clock.now }
        )

        type(dispatcher, "AB", perKeyDelay = 10L, clock = clock)
        clock.advance(2000L)
        type(dispatcher, "CD", perKeyDelay = 10L, clock = clock)
        dispatcher.offer(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0)

        assertEquals(listOf("CD"), submitted)
    }

    @Test
    fun ignoresEnterWithoutEnoughCharacters() {
        val clock = ScriptedClock()
        val submitted = mutableListOf<String>()
        val dispatcher = FxKeyboardScanDispatcher(
            onSubmit = { submitted.add(it) },
            clock = { clock.now }
        )

        dispatcher.offer(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A, 'A'.code)
        dispatcher.offer(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0)

        assertEquals(emptyList<String>(), submitted)
    }

    @Test
    fun navigationKeyInterruptsFrame() {
        val clock = ScriptedClock()
        val submitted = mutableListOf<String>()
        val dispatcher = FxKeyboardScanDispatcher(
            onSubmit = { submitted.add(it) },
            clock = { clock.now }
        )

        type(dispatcher, "ABC123", clock = clock)
        dispatcher.offer(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, 0)
        type(dispatcher, "XY", clock = clock)
        dispatcher.offer(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0)

        assertEquals(listOf("XY"), submitted)
    }

    @Test
    fun actionUpEventsAreIgnored() {
        val clock = ScriptedClock()
        val submitted = mutableListOf<String>()
        val dispatcher = FxKeyboardScanDispatcher(
            onSubmit = { submitted.add(it) },
            clock = { clock.now }
        )

        dispatcher.offer(KeyEvent.ACTION_UP, 'A'.code, 'A'.code)
        dispatcher.offer(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0)

        assertEquals(emptyList<String>(), submitted)
    }
}
