package com.forgex.mobile.core.sync.model

/**
 * 同步操作类型枚举。
 *
 * 每个操作类型关联 HTTP 方法和请求路径模板, 用于构造 SyncQueueEntity。
 * 路径相对于 baseUrl (不含前导 /, 与 Retrofit 注解一致)。
 *
 * @property method HTTP 方法
 * @property path 请求路径 (相对于 baseUrl)
 */
enum class SyncOperationType(val method: HttpMethod, val path: String) {

    /** 标记单条消息已读: POST sys/message/read */
    MESSAGE_MARK_READ(HttpMethod.POST, "sys/message/read"),

    /** 标记全部消息已读: POST sys/message/read-all */
    MESSAGE_MARK_ALL_READ(HttpMethod.POST, "sys/message/read-all"),

    /** 工作流审批: POST wf/execution/approve */
    WORKFLOW_APPROVE(HttpMethod.POST, "wf/execution/approve")
}

/**
 * HTTP 方法枚举。
 */
enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE
}
