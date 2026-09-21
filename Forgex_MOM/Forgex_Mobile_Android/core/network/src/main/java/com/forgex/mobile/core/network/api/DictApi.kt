package com.forgex.mobile.core.network.api

import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.network.model.dict.DictItemVO
import com.forgex.mobile.core.network.model.dict.DictItemsByPathRequest
import com.forgex.mobile.core.network.model.dict.DictItemsRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface DictApi {
    @POST("sys/dict/items")
    suspend fun items(@Body request: DictItemsRequest): ApiResponse<List<DictItemVO>>

    @POST("sys/dict/itemsByPath")
    suspend fun itemsByPath(@Body request: DictItemsByPathRequest): ApiResponse<List<DictItemVO>>
}
