package com.forgex.mobile.core.network.model.common

data class PageData<T>(
    val records: List<T> = emptyList(),
    val total: Long = 0L,
    val size: Long? = null,
    val current: Long? = null,
    val pages: Long? = null
)
