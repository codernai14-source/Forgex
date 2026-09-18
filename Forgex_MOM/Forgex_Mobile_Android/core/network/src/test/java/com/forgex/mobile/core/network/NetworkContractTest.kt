package com.forgex.mobile.core.network

import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.network.exception.ErrorCodeMapper
import com.forgex.mobile.core.network.interceptor.LanguageInterceptor
import com.forgex.mobile.core.network.interceptor.TenantInterceptor
import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.network.repository_support.ApiExecutor
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class NetworkContractTest {

    @Test
    fun tenantInterceptorSendsCanonicalHeader() {
        val sessionStore = mockk<SessionStore>()
        every { sessionStore.tenantId } returns flowOf("tenant-42")
        val chain = mockk<okhttp3.Interceptor.Chain>()
        val original = Request.Builder().url("http://localhost/test").build()
        val captured = slot<Request>()
        every { chain.request() } returns original
        every { chain.proceed(capture(captured)) } returns response(original)

        TenantInterceptor(sessionStore).intercept(chain)

        assertEquals("tenant-42", captured.captured.header("X-Tenant-Id"))
        assertEquals(null, captured.captured.header("Tenant-Id"))
        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun errorCodeMapperProvidesActionableMessagesForGatewayCodes() {
        val mapper = ErrorCodeMapper()

        mapOf(
            601 to "无权限",
            603 to "服务不可用，请稍后重试",
            604 to "需要授权",
            605 to "授权无效",
            606 to "密码已过期，请修改密码"
        ).forEach { (code, expected) ->
            val result = mapper.fromCode(code, "")
            assertEquals(expected, result.message)
            assertEquals(code, result.code)
            assertEquals(false, result.appText == null)
        }
    }

    @Test
    fun languageInterceptorNormalizesBothLanguageHeaders() {
        val sessionStore = mockk<SessionStore>()
        every { sessionStore.lastResolvedLanguageTag } returns flowOf("zh_tw")
        val chain = mockk<okhttp3.Interceptor.Chain>()
        val original = Request.Builder().url("http://localhost/test").build()
        val captured = slot<Request>()
        every { chain.request() } returns original
        every { chain.proceed(capture(captured)) } returns response(original)

        LanguageInterceptor(sessionStore).intercept(chain)

        assertEquals("zh-TW", captured.captured.header("X-Lang"))
        assertEquals("zh-TW", captured.captured.header("Accept-Language"))
    }

    @Test
    fun apiExecutorSeparatesSuccessBusinessFailureAndTransportFailure() = runBlocking {
        val executor = ApiExecutor()

        val success = executor.request { ApiResponse(code = 200, data = "ok") }
        assertEquals("ok", (success as AppResult.Success).data)

        val businessFailure = executor.request<String> { ApiResponse(code = 603) }
        assertEquals(603, (businessFailure as AppResult.Error).code)

        val transportFailure = executor.request<String> { throw IOException("offline") }
        assertEquals("网络连接异常，请检查网络后重试", (transportFailure as AppResult.Error).message)
    }

    private fun response(request: Request): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .build()
}
