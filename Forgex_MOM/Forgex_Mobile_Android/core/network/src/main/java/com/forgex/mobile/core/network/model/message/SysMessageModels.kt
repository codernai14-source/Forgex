package com.forgex.mobile.core.network.model.message

data class SysMessagePageRequest(
    val pageNum: Int = 1,
    val pageSize: Int = 20,
    val messageType: String? = null,
    val platform: String? = null,
    val status: Int? = null,
    val title: String? = null
)

data class SysMessageReadRequest(
    val id: Long
)

data class SysMessageVO(
    val id: Long? = null,
    val senderTenantId: Long? = null,
    val senderUserId: Long? = null,
    val receiverTenantId: Long? = null,
    val receiverUserId: Long? = null,
    val scope: String? = null,
    val messageType: String? = null,
    val type: String? = null,
    val platform: String? = null,
    val senderName: String? = null,
    val title: String? = null,
    val content: String? = null,
    val linkUrl: String? = null,
    val bizType: String? = null,
    val status: Int? = null,
    val createTime: String? = null,
    val readTime: String? = null
)
