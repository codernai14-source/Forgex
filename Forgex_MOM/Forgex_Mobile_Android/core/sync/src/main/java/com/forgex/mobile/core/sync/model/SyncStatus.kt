package com.forgex.mobile.core.sync.model

import com.forgex.mobile.core.database.entity.SyncStatus

/**
 * 同步状态 (数据层枚举的重导出, 供 core/sync 业务层统一引用)。
 *
 * @see com.forgex.mobile.core.database.entity.SyncStatus
 */
typealias SyncRecordStatus = SyncStatus

/**
 * 同步管理器整体状态, 用于 UI 展示同步进度。
 *
 * @property isSyncing 是否正在同步
 * @property pendingCount 待处理记录数
 * @property lastError 最近一次同步错误, null 表示无错误
 * @property lastSyncTime 最近一次成功同步时间 (epoch millis), 0 表示从未同步
 */
data class SyncState(
    val isSyncing: Boolean = false,
    val pendingCount: Int = 0,
    val lastError: String? = null,
    val lastSyncTime: Long = 0L
) {
    companion object {
        /** 空闲状态 (未同步, 无待处理) */
        val IDLE = SyncState()
    }
}
