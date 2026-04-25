package com.forgex.mobile.core.network.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface FileApi {
    @GET
    suspend fun download(@Url url: String): ResponseBody
}
