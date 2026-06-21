package com.forgex.mobile.core.database.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * 工作流待办缓存表 Entity。
 *
 * 映射 WfExecutionDTO 的关键字段, 用于断网时展示历史待办列表。
 * 在线刷新时按 category 全量替换该分类缓存。
 *
 * @property remoteId 服务端原始 ID
 * @property taskConfigId 任务配置 ID
 * @property taskCode 任务编码
 * @property taskName 任务名称
 * @property initiatorName 发起人姓名
 * @property currentNodeName 当前节点名称
 * @property status 审批状态
 * @property startTime 开始时间 (服务端原始字符串)
 * @property category 缓存分类 (PENDING/APPROVED/MINE)
 * @property cacheTime 缓存写入时间 (epoch millis)
 */
@Entity(
    tableName = "workflow_cache",
    indices = [
        Index(value = ["category"]),
        Index(value = ["remoteId"])
    ]
)
class WorkflowCacheEntity(
    val remoteId: Long? = null,
    val taskConfigId: Long? = null,
    val taskCode: String? = null,
    val taskName: String? = null,
    val initiatorName: String? = null,
    val currentNodeName: String? = null,
    val status: Int? = null,
    val startTime: String? = null,
    val category: String,
    val cacheTime: Long = System.currentTimeMillis()
) : BaseEntity()
