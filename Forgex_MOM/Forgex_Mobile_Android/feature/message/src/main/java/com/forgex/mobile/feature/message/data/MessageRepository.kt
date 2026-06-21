package com.forgex.mobile.feature.message.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.database.dao.MessageCacheDao
import com.forgex.mobile.core.database.entity.MessageCacheEntity
import com.forgex.mobile.core.network.api.MessageApi
import com.forgex.mobile.core.network.model.message.SysMessagePageRequest
import com.forgex.mobile.core.network.model.message.SysMessageVO
import com.forgex.mobile.core.sync.SyncManager
import com.forgex.mobile.core.sync.network.NetworkMonitor
import com.forgex.mobile.feature.message.MessageEntryMode
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 消息 Repository (离线增强版)。
 *
 * 在线时调用 API 获取数据并回写 Room 缓存; 离线时从 Room 缓存读取。
 * 离线标记已读时: 先更新本地缓存 -> 入队 SyncManager -> 立即返回 Success (乐观更新)。
 */
@Singleton
class MessageRepository @Inject constructor(
    private val messageApi: MessageApi,
    private val messageCacheDao: MessageCacheDao,
    private val syncManager: SyncManager,
    private val networkMonitor: NetworkMonitor
) {

    /**
     * 加载消息列表。
     *
     * 在线: 调用 API -> 全量替换缓存 -> 返回网络数据。
     * 离线: 从缓存读取 -> 返回缓存数据。
     *
     * @param entryMode 入口模式
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return AppResult, Success.data 包含 SysMessageVO 列表
     */
    suspend fun loadMessages(
        entryMode: MessageEntryMode,
        pageNum: Int = 1,
        pageSize: Int = 20
    ): AppResult<List<SysMessageVO>> {
        return when (entryMode) {
            MessageEntryMode.HOME -> loadUnread(limit = pageSize)
            MessageEntryMode.UNREAD -> loadPage(pageNum = pageNum, pageSize = pageSize, status = 0)
            MessageEntryMode.READ -> loadPage(pageNum = pageNum, pageSize = pageSize, status = 1)
        }
    }

    /**
     * 标记单条消息已读 (离线优先)。
     *
     * 无论在线离线都先更新本地缓存, 然后入队 SyncManager。
     * 在线时 SyncWorker 立即重放; 离线时联网后自动同步。
     *
     * @param messageId 消息 ID
     * @return AppResult.Success(true) 始终返回成功 (乐观更新)
     */
    suspend fun markRead(messageId: Long): AppResult<Boolean> {
        // 1. 更新本地缓存 (乐观更新)
        messageCacheDao.markRead(messageId)

        // 2. 入队同步 (在线时立即触发, 离线时等待联网)
        syncManager.enqueueReadReceipt(messageId)

        // 3. 立即返回成功
        return AppResult.Success(true)
    }

    /**
     * 标记全部消息已读 (离线优先)。
     *
     * @return AppResult.Success(true)
     */
    suspend fun markAllRead(): AppResult<Boolean> {
        messageCacheDao.markAllRead()
        syncManager.enqueueMarkAllRead()
        return AppResult.Success(true)
    }

    /**
     * 加载未读消息 (带离线缓存)。
     */
    private suspend fun loadUnread(limit: Int): AppResult<List<SysMessageVO>> {
        val category = "UNREAD"

        if (!networkMonitor.isCurrentlyOnline()) {
            val cached = messageCacheDao.findByCategory(category).first()
            return AppResult.Success(cached.map { it.toVO() })
        }

        return try {
            val response = messageApi.listUnread(limit = limit)
            if (!response.isSuccess()) {
                val cached = messageCacheDao.findByCategory(category).first()
                if (cached.isNotEmpty()) {
                    return AppResult.Success(cached.map { it.toVO() })
                }
                return AppResult.Error(response.errorMessage(), response.code)
            }

            val records = response.data.orEmpty()
            val entities = records.map { it.toCacheEntity(category) }
            messageCacheDao.replaceAll(category, entities)

            AppResult.Success(records)
        } catch (e: Exception) {
            val cached = messageCacheDao.findByCategory(category).first()
            if (cached.isNotEmpty()) {
                return AppResult.Success(cached.map { it.toVO() })
            }
            AppResult.Error(e.message ?: "加载未读消息失败")
        }
    }

    /**
     * 加载消息分页 (带离线缓存)。
     */
    private suspend fun loadPage(pageNum: Int, pageSize: Int, status: Int): AppResult<List<SysMessageVO>> {
        val category = if (status == 0) "UNREAD" else "READ"

        if (!networkMonitor.isCurrentlyOnline()) {
            val cached = messageCacheDao.findByCategory(category).first()
            return AppResult.Success(cached.map { it.toVO() })
        }

        return try {
            val response = messageApi.pageMessages(
                SysMessagePageRequest(
                    pageNum = pageNum,
                    pageSize = pageSize,
                    status = status
                )
            )
            if (!response.isSuccess()) {
                val cached = messageCacheDao.findByCategory(category).first()
                if (cached.isNotEmpty()) {
                    return AppResult.Success(cached.map { it.toVO() })
                }
                return AppResult.Error(response.errorMessage(), response.code)
            }

            val records = response.data?.records.orEmpty()
            val entities = records.map { it.toCacheEntity(category) }
            messageCacheDao.replaceAll(category, entities)

            AppResult.Success(records)
        } catch (e: Exception) {
            val cached = messageCacheDao.findByCategory(category).first()
            if (cached.isNotEmpty()) {
                return AppResult.Success(cached.map { it.toVO() })
            }
            AppResult.Error(e.message ?: "加载消息分页失败")
        }
    }

    /**
     * 将 VO 转换为缓存 Entity。
     */
    private fun SysMessageVO.toCacheEntity(category: String): MessageCacheEntity {
        return MessageCacheEntity(
            remoteId = this.id,
            senderName = this.senderName,
            messageType = this.messageType,
            title = this.title,
            content = this.content,
            linkUrl = this.linkUrl,
            msgStatus = this.status,
            createTime = this.createTime,
            readTime = this.readTime,
            category = category
        )
    }

    /**
     * 将缓存 Entity 转换回 VO。
     */
    private fun MessageCacheEntity.toVO(): SysMessageVO {
        return SysMessageVO(
            id = this.remoteId,
            senderName = this.senderName,
            messageType = this.messageType,
            title = this.title,
            content = this.content,
            linkUrl = this.linkUrl,
            status = this.msgStatus,
            createTime = this.createTime,
            readTime = this.readTime
        )
    }
}
