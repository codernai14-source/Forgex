package com.forgex.mobile.core.network.api

import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.network.model.common.PageData
import com.forgex.mobile.core.network.model.workflow.WfExecutionDTO
import com.forgex.mobile.core.network.model.workflow.WfExecutionQueryRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface WorkflowApi {

    @POST("wf/execution/my/initiated")
    suspend fun pageMyInitiated(@Body request: WfExecutionQueryRequest): ApiResponse<PageData<WfExecutionDTO>>

    @POST("wf/execution/my/pending")
    suspend fun pageMyPending(@Body request: WfExecutionQueryRequest): ApiResponse<PageData<WfExecutionDTO>>

    @POST("wf/execution/my/processed")
    suspend fun pageMyProcessed(@Body request: WfExecutionQueryRequest): ApiResponse<PageData<WfExecutionDTO>>
}
