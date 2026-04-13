package com.forgex.mobile.feature.workflow.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.network.api.WorkflowApi
import com.forgex.mobile.core.network.model.workflow.WfExecutionDTO
import com.forgex.mobile.core.network.model.workflow.WfExecutionQueryRequest
import com.forgex.mobile.feature.workflow.WorkflowEntryMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkflowRepository @Inject constructor(
    private val workflowApi: WorkflowApi
) {

    suspend fun loadExecutions(
        entryMode: WorkflowEntryMode,
        pageNum: Int = 1,
        pageSize: Int = 20
    ): AppResult<List<WfExecutionDTO>> {
        val request = WfExecutionQueryRequest(pageNum = pageNum, pageSize = pageSize)

        return try {
            val response = when (entryMode) {
                WorkflowEntryMode.HOME,
                WorkflowEntryMode.PENDING -> workflowApi.pageMyPending(request)

                WorkflowEntryMode.APPROVED -> workflowApi.pageMyProcessed(request)
                WorkflowEntryMode.MINE -> workflowApi.pageMyInitiated(request)
            }

            if (!response.isSuccess()) {
                return AppResult.Error(response.errorMessage(), response.code)
            }

            AppResult.Success(response.data?.records.orEmpty())
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "加载审批列表失败")
        }
    }
}
