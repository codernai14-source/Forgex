package com.forgex.mobile.core.network.model.workflow

data class WfExecutionDTO(
    val id: Long? = null,
    val taskConfigId: Long? = null,
    val taskCode: String? = null,
    val taskName: String? = null,
    val initiatorId: Long? = null,
    val initiatorName: String? = null,
    val currentNodeId: Long? = null,
    val currentNodeName: String? = null,
    val formContent: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val status: Int? = null,
    val tenantId: Long? = null,
    val createTime: String? = null,
    val updateTime: String? = null
)
