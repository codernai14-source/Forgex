package com.forgex.mobile.feature.workflow.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.database.dao.WorkflowCacheDao
import com.forgex.mobile.core.database.entity.WorkflowCacheEntity
import com.forgex.mobile.core.network.api.WorkflowApi
import com.forgex.mobile.core.network.model.workflow.WfExecutionDTO
import com.forgex.mobile.core.network.model.workflow.WfExecutionQueryRequest
import com.forgex.mobile.core.sync.network.NetworkMonitor
import com.forgex.mobile.feature.workflow.WorkflowEntryMode
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 工作流 Repository (离线增强版)。
 *
 * 在线时调用 API 获取数据并回写 Room 缓存; 离线时从 Room 缓存读取历史数据。
 * 实现只读缓存优先策略, 断网时可查看已缓存的待办列表。
 */
@Singleton
class WorkflowRepository @Inject constructor(
    private val workflowApi: WorkflowApi,
    private val workflowCacheDao: WorkflowCacheDao,
    private val networkMonitor: NetworkMonitor
) {

    /**
     * 加载工作流执行列表。
     *
     * 在线: 调用 API -> 全量替换缓存 -> 返回网络数据。
     * 离线: 从缓存读取 -> 返回缓存数据 (isFromCache = true)。
     *
     * @param entryMode 入口模式
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return AppResult, Success.data 包含 WfExecutionDTO 列表
     */
    suspend fun loadExecutions(
        entryMode: WorkflowEntryMode,
        pageNum: Int = 1,
        pageSize: Int = 20
    ): AppResult<List<WfExecutionDTO>> {
        val category = entryMode.toCategory()

        // 离线: 从缓存读取
        if (!networkMonitor.isCurrentlyOnline()) {
            val cached = workflowCacheDao.findByCategory(category).first()
            return AppResult.Success(cached.map { it.toDto() })
        }

        // 在线: 调用 API
        val request = WfExecutionQueryRequest(pageNum = pageNum, pageSize = pageSize)

        return try {
            val response = when (entryMode) {
                WorkflowEntryMode.HOME,
                WorkflowEntryMode.PENDING -> workflowApi.pageMyPending(request)

                WorkflowEntryMode.APPROVED -> workflowApi.pageMyProcessed(request)
                WorkflowEntryMode.MINE -> workflowApi.pageMyInitiated(request)
            }

            if (!response.isSuccess()) {
                // API 失败时回退到缓存
                val cached = workflowCacheDao.findByCategory(category).first()
                if (cached.isNotEmpty()) {
                    return AppResult.Success(cached.map { it.toDto() })
                }
                return AppResult.Error(response.errorMessage(), response.code)
            }

            val records = response.data?.records.orEmpty()

            // 回写缓存
            val entities = records.map { it.toCacheEntity(category) }
            workflowCacheDao.replaceAll(category, entities)

            AppResult.Success(records)
        } catch (e: Exception) {
            // 网络异常时回退到缓存
            val cached = workflowCacheDao.findByCategory(category).first()
            if (cached.isNotEmpty()) {
                return AppResult.Success(cached.map { it.toDto() })
            }
            AppResult.Error(e.message ?: "加载审批列表失败")
        }
    }

    /**
     * 将 WorkflowEntryMode 转换为缓存分类键。
     */
    private fun WorkflowEntryMode.toCategory(): String {
        return when (this) {
            WorkflowEntryMode.HOME, WorkflowEntryMode.PENDING -> "PENDING"
            WorkflowEntryMode.APPROVED -> "APPROVED"
            WorkflowEntryMode.MINE -> "MINE"
        }
    }

    /**
     * 将 DTO 转换为缓存 Entity。
     */
    private fun WfExecutionDTO.toCacheEntity(category: String): WorkflowCacheEntity {
        return WorkflowCacheEntity(
            remoteId = this.id,
            taskConfigId = this.taskConfigId,
            taskCode = this.taskCode,
            taskName = this.taskName,
            initiatorName = this.initiatorName,
            currentNodeName = this.currentNodeName,
            status = this.status,
            startTime = this.startTime,
            category = category
        )
    }

    /**
     * 将缓存 Entity 转换回 DTO。
     */
    private fun WorkflowCacheEntity.toDto(): WfExecutionDTO {
        return WfExecutionDTO(
            id = this.remoteId,
            taskConfigId = this.taskConfigId,
            taskCode = this.taskCode,
            taskName = this.taskName,
            initiatorName = this.initiatorName,
            currentNodeName = this.currentNodeName,
            status = this.status,
            startTime = this.startTime
        )
    }
}
