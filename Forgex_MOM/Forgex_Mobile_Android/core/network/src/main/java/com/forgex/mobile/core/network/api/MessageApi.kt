package com.forgex.mobile.core.network.api

import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.network.model.common.PageData
import com.forgex.mobile.core.network.model.message.SysMessagePageRequest
import com.forgex.mobile.core.network.model.message.SysMessageReadRequest
import com.forgex.mobile.core.network.model.message.SysMessageVO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MessageApi {

    @GET("sys/message/unread")
    suspend fun listUnread(@Query("limit") limit: Int? = null): ApiResponse<List<SysMessageVO>>

    @POST("sys/message/page")
    suspend fun pageMessages(@Body request: SysMessagePageRequest): ApiResponse<PageData<SysMessageVO>>

    @POST("sys/message/read")
    suspend fun markRead(@Body request: SysMessageReadRequest): ApiResponse<Boolean>

    @POST("sys/message/read-all")
    suspend fun markAllRead(): ApiResponse<Boolean>
}
