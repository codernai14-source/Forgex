package com.forgex.mobile.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity 抽象基类。
 *
 * 所有缓存表与同步队列表均继承此类, 提供通用主键与审计字段。
 * 时间统一使用 Long (epoch millis) 存储, 避免时区问题。
 *
 * @property id 本地主键, 自增
 * @property createdAt 本地创建时间 (epoch millis)
 * @property updatedAt 本地更新时间 (epoch millis)
 * @property syncStatus 同步状态, 存储为 SyncStatus 枚举名 (String)
 */
@Entity(
    indices = [
        Index(value = ["syncStatus"])
    ]
)
abstract class BaseEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

    var createdAt: Long = System.currentTimeMillis()

    var updatedAt: Long = System.currentTimeMillis()

    var syncStatus: String = SyncStatus.PENDING.name
}

/**
 * 同步状态枚举, 定义于数据层以避免循环依赖。
 *
 * 状态流转:
 * - PENDING -> SYNCING: SyncWorker 取出记录
 * - SYNCING -> SYNCED: 服务端返回 2xx 或 404
 * - SYNCING -> PENDING: 服务端返回非 2xx 且 retryCount < maxRetry
 * - SYNCING -> FAILED: 409 Conflict 或 retryCount >= maxRetry
 */
enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED;

    /**
     * 判断当前状态是否可以重试。
     *
     * @param maxRetry 最大重试次数
     * @return true 表示可以重试
     */
    fun canRetry(maxRetry: Int): Boolean {
        return this == PENDING
    }
}
