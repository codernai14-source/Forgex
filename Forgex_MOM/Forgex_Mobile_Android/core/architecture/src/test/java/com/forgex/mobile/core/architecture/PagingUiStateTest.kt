package com.forgex.mobile.core.architecture

import com.forgex.mobile.core.model.FxPageData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PagingUiStateTest {
    @Test
    fun appendMergesRecordsAndUpdatesContinuation() {
        val state = PagingUiState(list = listOf("a"), isLoadingMore = true)
        val result = state.append(FxPageData(records = listOf("a", "b"), total = 2, current = 2, pages = 2))

        assertEquals(listOf("a", "b"), result.list)
        assertEquals(2L, result.total)
        assertTrue(!result.hasMore)
        assertTrue(!result.isLoadingMore)
    }
}
