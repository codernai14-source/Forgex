package com.forgex.mobile.core.network.api

import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.network.model.menu.RoutesRequest
import com.forgex.mobile.core.network.model.menu.UserRoutesVO
import retrofit2.http.Body
import retrofit2.http.POST

interface MenuApi {

    @POST("sys/menu/routes")
    suspend fun getRoutes(@Body request: RoutesRequest): ApiResponse<UserRoutesVO>
}
