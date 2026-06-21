package com.forgex.mobile.core.sync.conflict

import com.forgex.mobile.core.database.entity.SyncQueueEntity
import io.mockk.every
import io.mockk.mockk
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * LwwConflictResolver 单元测试。
 *
 * 测试 HTTP 响应码到 ConflictResult 的映射逻辑：
 * - 2xx (200/201/204) → ACCEPT_SERVER
 * - 404 → SKIP
 * - 409/401/403/400 → FAIL
 * - 5xx (500/503) → RETRY
 * - null (网络异常) → RETRY
 * - 其他未匹配码 → RETRY (默认)
 *
 * 被测类: [LwwConflictResolver]
 * 被测方法: [LwwConflictResolver.resolve]
 */
class LwwConflictResolverTest {

    private lateinit var resolver: LwwConflictResolver
    private lateinit var localEntity: SyncQueueEntity

    @Before
    fun setUp() {
        resolver = LwwConflictResolver()
        localEntity = SyncQueueEntity(
            operationType = "READ_RECEIPT",
            method = "PUT",
            path = "sys/message/read",
            payload = "{\"messageId\":1}"
        )
    }

    /**
     * 辅助方法：创建指定 HTTP 状态码的 mock Response。
     * OkHttp 4.x 中 Response.code 是 Kotlin val 属性, 使用 mockk 的 every 桩。
     */
    private fun mockResponse(code: Int): Response {
        return mockk<Response>().apply {
            every { this@apply.code } returns code
        }
    }

    // ==================== 2xx → ACCEPT_SERVER ====================

    @Test
    fun `HTTP 200 should return ACCEPT_SERVER`() {
        val response = mockResponse(200)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.ACCEPT_SERVER, result)
    }

    @Test
    fun `HTTP 201 should return ACCEPT_SERVER`() {
        val response = mockResponse(201)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.ACCEPT_SERVER, result)
    }

    @Test
    fun `HTTP 204 should return ACCEPT_SERVER`() {
        val response = mockResponse(204)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.ACCEPT_SERVER, result)
    }

    // ==================== 404 → SKIP ====================

    @Test
    fun `HTTP 404 should return SKIP`() {
        val response = mockResponse(404)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.SKIP, result)
    }

    // ==================== 409/401/403/400 → FAIL ====================

    @Test
    fun `HTTP 409 should return FAIL`() {
        val response = mockResponse(409)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.FAIL, result)
    }

    @Test
    fun `HTTP 401 should return FAIL`() {
        val response = mockResponse(401)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.FAIL, result)
    }

    @Test
    fun `HTTP 403 should return FAIL`() {
        val response = mockResponse(403)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.FAIL, result)
    }

    @Test
    fun `HTTP 400 should return FAIL`() {
        val response = mockResponse(400)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.FAIL, result)
    }

    // ==================== 5xx → RETRY ====================

    @Test
    fun `HTTP 500 should return RETRY`() {
        val response = mockResponse(500)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.RETRY, result)
    }

    @Test
    fun `HTTP 503 should return RETRY`() {
        val response = mockResponse(503)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.RETRY, result)
    }

    // ==================== null response → RETRY ====================

    @Test
    fun `null response should return RETRY`() {
        val result = resolver.resolve(localEntity, null)
        assertEquals(ConflictResult.RETRY, result)
    }

    // ==================== Edge cases: 未匹配码 → RETRY (默认) ====================

    @Test
    fun `HTTP 301 should return RETRY as default for redirect codes`() {
        val response = mockResponse(301)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.RETRY, result)
    }

    @Test
    fun `HTTP 405 should return RETRY as default for unmatched 4xx codes`() {
        val response = mockResponse(405)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.RETRY, result)
    }

    @Test
    fun `HTTP 422 should return RETRY as default for unmatched 4xx codes`() {
        val response = mockResponse(422)
        val result = resolver.resolve(localEntity, response)
        assertEquals(ConflictResult.RETRY, result)
    }
}
