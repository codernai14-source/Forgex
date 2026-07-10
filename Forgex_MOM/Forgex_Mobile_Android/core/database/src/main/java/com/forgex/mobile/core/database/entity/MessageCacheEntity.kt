package com.forgex.mobile.core.database.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * 消息缓存表 Entity。
 *
 * 映射 SysMessageVO 的关键字段, 用于断网时展示历史消息列表。
 * 在线刷新时按 category 全量替换; 离线标记已读时更新 msgStatus。
 *
 * @property remoteId 服务端原始消息 ID
 * @property senderName 发送人姓名
 * @property messageType 消息类型
 * @property title 消息标题
 * @property content 消息内容
 * @property linkUrl 关联链接
 * @property msgStatus 消息状态 (0=未读, 1=已读)
 * @property createTime 创建时间 (服务端原始字符串)
 * @property readTime 阅读时间 (服务端原始字符串)
 * @property category 缓存分类 (UNREAD/READ)
 * @property cacheTime 缓存写入时间 (epoch millis)
 */
@Entity(
    tableName = "message_cache",
    indices = [
        Index(value = ["category"]),
        Index(value = ["remoteId"]),
        Index(value = ["msgStatus"])
    ]
)
class MessageCacheEntity(
    val remoteId: Long? = null,
    val senderName: String? = null,
    val messageType: String? = null,
    val title: String? = null,
    val content: String? = null,
    val linkUrl: String? = null,
    var msgStatus: Int? = null,
    val createTime: String? = null,
    var readTime: String? = null,
    val category: String,
    val cacheTime: Long = System.currentTimeMillis()
) : BaseEntity()
