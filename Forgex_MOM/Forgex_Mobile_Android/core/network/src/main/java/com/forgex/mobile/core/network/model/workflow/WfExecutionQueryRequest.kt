package com.forgex.mobile.core.network.model.workflow

data class WfExecutionQueryRequest(
    val pageNum: Int = 1,
    val pageSize: Int = 20,
    val taskName: String? = null,
    val taskCode: String? = null,
    val status: Int? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val approveTimeBegin: String? = null,
    val approveTimeEnd: String? = null
)
