package com.forgex.mobile.feature.message.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.network.api.MessageApi
import com.forgex.mobile.core.network.model.message.SysMessagePageRequest
import com.forgex.mobile.core.network.model.message.SysMessageReadRequest
import com.forgex.mobile.core.network.model.message.SysMessageVO
import com.forgex.mobile.feature.message.MessageEntryMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageApi: MessageApi
) {

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

    suspend fun markRead(messageId: Long): AppResult<Boolean> {
        return try {
            val response = messageApi.markRead(SysMessageReadRequest(id = messageId))
            if (response.isSuccess()) {
                AppResult.Success(response.data ?: true)
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "标记已读失败")
        }
    }

    private suspend fun loadUnread(limit: Int): AppResult<List<SysMessageVO>> {
        return try {
            val response = messageApi.listUnread(limit = limit)
            if (!response.isSuccess()) {
                return AppResult.Error(response.errorMessage(), response.code)
            }
            AppResult.Success(response.data.orEmpty())
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "加载未读消息失败")
        }
    }

    private suspend fun loadPage(pageNum: Int, pageSize: Int, status: Int): AppResult<List<SysMessageVO>> {
        return try {
            val response = messageApi.pageMessages(
                SysMessagePageRequest(
                    pageNum = pageNum,
                    pageSize = pageSize,
                    status = status
                )
            )
            if (!response.isSuccess()) {
                return AppResult.Error(response.errorMessage(), response.code)
            }
            AppResult.Success(response.data?.records.orEmpty())
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "加载消息分页失败")
        }
    }
}
