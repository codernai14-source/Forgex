package com.forgex.mobile.core.sync

import com.forgex.mobile.core.sync.coordinator.SyncCoordinator
import com.forgex.mobile.core.sync.model.SyncOperationType
import com.forgex.mobile.core.sync.model.SyncState
import com.forgex.mobile.core.sync.network.NetworkMonitor
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 同步管理器 (对外门面 / Facade)。
 *
 * 提供 feature 模块使用的同步 API, 内部委托 SyncCoordinator 处理实际调度。
 * 同时管理 SyncState (整体同步状态) 并监听网络恢复事件。
 *
 * 使用方式:
 * ```kotlin
 * @Inject lateinit var syncManager: SyncManager
 *
 * // 离线标记已读 -> 入队
 * syncManager.enqueueReadReceipt(messageId = 42L)
 *
 * // 手动触发同步
 * syncManager.triggerSync()
 *
 * // 观察同步状态
 * syncManager.observeSyncState().collect { state -> ... }
 * ```
 */
@Singleton
class SyncManager @Inject constructor(
    private val coordinator: SyncCoordinator,
    private val networkMonitor: NetworkMonitor
) {

    private val gson = Gson()
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _syncState = MutableStateFlow(SyncState.IDLE)

    /** 同步状态 (StateFlow), UI 可观察展示同步进度与待处理数 */
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        // 监听网络恢复 -> 触发即时同步
        managerScope.launch {
            networkMonitor.isOnline
                .onEach { isOnline ->
                    if (isOnline) {
                        coordinator.onNetworkRestored()
                    }
                }
                .launchIn(managerScope)
        }

        // 监听待处理数 -> 更新 SyncState
        coordinator.observePendingCount()
            .onEach { pendingCount ->
                _syncState.value = _syncState.value.copy(pendingCount = pendingCount)
            }
            .launchIn(managerScope)
    }

    /**
     * 入队单条消息已读回执。
     *
     * @param messageId 消息 ID
     * @return 队列记录 ID
     */
    suspend fun enqueueReadReceipt(messageId: Long): Long {
        val payload = gson.toJson(mapOf("id" to messageId))
        return coordinator.enqueue(
            operation = SyncOperationType.MESSAGE_MARK_READ,
            payload = payload,
            entityId = messageId
        )
    }

    /**
     * 入队全部已读操作。
     *
     * @return 队列记录 ID
     */
    suspend fun enqueueMarkAllRead(): Long {
        return coordinator.enqueue(
            operation = SyncOperationType.MESSAGE_MARK_ALL_READ,
            payload = "{}"
        )
    }

    /**
     * 入队工作流审批操作。
     *
     * @param executionId 流程实例 ID
     * @param payload 审批数据 JSON
     * @return 队列记录 ID
     */
    suspend fun enqueueWorkflowApprove(executionId: Long, payload: String): Long {
        return coordinator.enqueue(
            operation = SyncOperationType.WORKFLOW_APPROVE,
            payload = payload,
            entityId = executionId
        )
    }

    /**
     * 手动触发即时同步。
     */
    fun triggerSync() {
        coordinator.triggerImmediateSync()
    }

    /**
     * 注册周期性同步任务 (在 Application onCreate 中调用)。
     */
    fun schedulePeriodicSync() {
        coordinator.schedulePeriodicSync()
    }

    /**
     * 注册网络监听 (在 Application onCreate 中调用)。
     */
    fun registerNetworkMonitor() {
        networkMonitor.register()
    }

    /**
     * 注销网络监听 (在 Application onTerminate 或登出时调用)。
     */
    fun unregisterNetworkMonitor() {
        networkMonitor.unregister()
    }

    /**
     * 检查当前是否在线。
     *
     * @return true 表示有可用网络
     */
    fun isOnline(): Boolean {
        return networkMonitor.isCurrentlyOnline()
    }

    /**
     * 观察同步状态。
     *
     * @return SyncState StateFlow
     */
    fun observeSyncState(): StateFlow<SyncState> {
        return syncState
    }
}
