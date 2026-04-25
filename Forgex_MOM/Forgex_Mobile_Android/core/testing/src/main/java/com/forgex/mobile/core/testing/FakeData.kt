package com.forgex.mobile.core.testing

import com.forgex.mobile.core.model.FxPageData

object FakeData {
    fun <T> page(records: List<T>): FxPageData<T> {
        return FxPageData(
            records = records,
            total = records.size.toLong(),
            size = records.size.toLong(),
            current = 1L,
            pages = 1L
        )
    }
}
