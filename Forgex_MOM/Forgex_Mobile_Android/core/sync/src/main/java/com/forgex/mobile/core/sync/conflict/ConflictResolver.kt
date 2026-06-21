package com.forgex.mobile.core.sync.conflict

import com.forgex.mobile.core.database.entity.SyncQueueEntity
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 冲突解决结果。
 *
 * @see ConflictResolver
 */
enum class ConflictResult {
    /** 接受服务端响应, 标记 SYNCED 并删除队列记录 */
    ACCEPT_SERVER,

    /** 需要重试, retryCount++ (若未超上限则标记 PENDING) */
    RETRY,

    /** 不可恢复的失败, 标记 FAILED */
    FAIL,

    /** 跳过 (目标已达成, 如 404), 标记 SYNCED 并删除 */
    SKIP
}

/**
 * 冲突解决器接口。
 *
 * 根据服务端 HTTP 响应决定同步队列记录的后续处理。
 */
interface ConflictResolver {

    /**
     * 解析服务端响应, 返回冲突解决结果。
     *
     * @param local 本地同步队列记录
     * @param response OkHttp 响应 (可能为 null, 表示网络异常)
     * @return 冲突解决结果
     */
    fun resolve(local: SyncQueueEntity, response: Response?): ConflictResult
}

/**
 * Last-Write-Wins (LWW) 冲突解决实现。
 *
 * 策略: 服务端权威, 以服务端响应码为准。
 *
 * | HTTP 响应 | ConflictResult | 动作 |
 * |-----------|----------------|------|
 * | 2xx (200/201/204) | ACCEPT_SERVER | SYNCED, 删除 |
 * | 404 | SKIP | SYNCED, 删除 (目标已达成) |
 * | 409 Conflict | FAIL | FAILED, 记录错误 |
 * | 401/403 | FAIL | FAILED, 认证问题需重新登录 |
 * | 400 | FAIL | FAILED, 请求格式错误 |
 * | 5xx / null | RETRY | retryCount++, PENDING |
 */
@Singleton
class LwwConflictResolver @Inject constructor() : ConflictResolver {

    override fun resolve(local: SyncQueueEntity, response: Response?): ConflictResult {
        // 网络异常 (无响应)
        if (response == null) {
            return ConflictResult.RETRY
        }

        val code = response.code

        return when {
            // 2xx: 成功
            code in 200..299 -> ConflictResult.ACCEPT_SERVER

            // 404: 目标已删除/不存在, 视为达成
            code == 404 -> ConflictResult.SKIP

            // 409: 冲突, 不可自动解决
            code == 409 -> ConflictResult.FAIL

            // 401/403: 认证问题, 需用户重新登录
            code == 401 || code == 403 -> ConflictResult.FAIL

            // 400: 请求格式错误, 重试无意义
            code == 400 -> ConflictResult.FAIL

            // 5xx: 服务端临时错误, 可重试
            code in 500..599 -> ConflictResult.RETRY

            // 其他: 默认重试
            else -> ConflictResult.RETRY
        }
    }
}
