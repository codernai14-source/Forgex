package com.forgex.mobile.core.network.model.dict

import com.forgex.mobile.core.model.FxDictionaryOption

data class DictItemsRequest(val dictCode: String)
data class DictItemsByPathRequest(val nodePath: String)

data class DictItemVO(
    val label: String? = null,
    val value: String? = null,
    val tagStyle: String? = null,
    val disabled: Boolean? = null
) {
    fun toOption(): FxDictionaryOption? {
        val itemValue = value?.takeIf { it.isNotBlank() } ?: return null
        return FxDictionaryOption(
            value = itemValue,
            label = label?.takeIf { it.isNotBlank() } ?: itemValue,
            tagColor = tagStyle,
            disabled = disabled == true
        )
    }
}
