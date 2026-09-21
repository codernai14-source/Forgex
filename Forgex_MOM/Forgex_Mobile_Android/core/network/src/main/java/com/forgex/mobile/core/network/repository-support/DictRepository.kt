package com.forgex.mobile.core.network.repository_support

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.model.FxDictionaryOption
import com.forgex.mobile.core.network.api.DictApi
import com.forgex.mobile.core.network.model.dict.DictItemsByPathRequest
import com.forgex.mobile.core.network.model.dict.DictItemsRequest
import javax.inject.Inject

class DictRepository @Inject constructor(
    private val api: DictApi,
    private val executor: ApiExecutor
) {
    private val cache = mutableMapOf<String, List<FxDictionaryOption>>()

    suspend fun items(dictCode: String, forceRefresh: Boolean = false): AppResult<List<FxDictionaryOption>> =
        load("code:$dictCode", forceRefresh) { api.items(DictItemsRequest(dictCode)) }

    suspend fun itemsByPath(nodePath: String, forceRefresh: Boolean = false): AppResult<List<FxDictionaryOption>> =
        load("path:$nodePath", forceRefresh) { api.itemsByPath(DictItemsByPathRequest(nodePath)) }

    fun clearCache() = cache.clear()

    private suspend fun load(
        key: String,
        forceRefresh: Boolean,
        request: suspend () -> com.forgex.mobile.core.common.model.ApiResponse<List<com.forgex.mobile.core.network.model.dict.DictItemVO>>
    ): AppResult<List<FxDictionaryOption>> {
        if (!forceRefresh) cache[key]?.let { return AppResult.Success(it) }
        return when (val result = executor.request(request) { values -> values.orEmpty().mapNotNull { it.toOption() } }) {
            is AppResult.Success -> {
                cache[key] = result.data
                result
            }
            is AppResult.Error -> result
            AppResult.Loading -> AppResult.Error("字典请求进行中")
        }
    }
}
