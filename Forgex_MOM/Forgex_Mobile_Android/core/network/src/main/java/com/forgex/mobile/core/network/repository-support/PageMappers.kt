package com.forgex.mobile.core.network.repository_support

import com.forgex.mobile.core.model.FxPageData
import com.forgex.mobile.core.network.model.common.PageData

fun <T> PageData<T>?.toFxPageData(): FxPageData<T> {
    return FxPageData(
        records = this?.records.orEmpty(),
        total = this?.total ?: 0L,
        size = this?.size ?: 0L,
        current = this?.current ?: 1L,
        pages = this?.pages ?: 0L
    )
}
