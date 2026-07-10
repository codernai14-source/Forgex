package com.forgex.mobile.core.database.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * 同步队列表 Entity。
 *
 * 记录待上传到服务端的写操作 (如消息已读回执), 联网后由 SyncWorker 逐条重放。
 * 索引: (syncStatus, retryCount) 用于高效查询待处理记录。
 *
 * @property operationType 操作类型 (SyncOperationType 枚举名)
 * @property method HTTP 方法 (GET/POST/PUT/DELETE)
 * @property path 请求路径 (相对于 baseUrl, 如 "sys/message/read")
 * @property payload 请求体 JSON (Gson 序列化)
 * @property retryCount 已重试次数
 * @property maxRetry 最大重试次数 (默认 5)
 * @property lastError 最近一次错误信息
 * @property entityId 关联业务实体 ID (如消息 ID), 用于回溯
 * @property queuedAt 入队时间 (epoch millis)
 */
@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["syncStatus", "retryCount"]),
        Index(value = ["operationType"])
    ]
)
class SyncQueueEntity(
    val operationType: String,
    val method: String,
    val path: String,
    val payload: String,
    var retryCount: Int = 0,
    var maxRetry: Int = 5,
    var lastError: String? = null,
    val entityId: Long? = null,
    val queuedAt: Long = System.currentTimeMillis()
) : BaseEntity()
