package com.forgex.mobile.feature.auth

import com.forgex.mobile.core.common.i18n.AppText
import java.util.Locale

/**
 * 将认证事件中的统一文案转换为当前语言可展示的文本。
 */
object AuthEventMessageResolver {

    /**
     * 优先使用已缓存的动态语言包，离线时回退到 Android 资源或服务端提供的默认文本。
     */
    fun resolve(
        appText: AppText?,
        bundle: Map<String, String>,
        resourceResolver: (Int, List<Any>) -> String?
    ): String? {
        return when (appText) {
            null -> null
            is AppText.Raw -> appText.value
            is AppText.Resource -> resourceResolver(appText.resId, appText.args)
            is AppText.Dynamic -> {
                bundle[appText.key]
                    ?.takeIf { it.isNotBlank() }
                    ?.let { format(it, appText.args) }
                    ?: appText.fallbackResId
                        ?.let { resourceResolver(it, appText.args) }
                    ?: appText.fallbackRaw
                        ?.takeIf { it.isNotBlank() }
                        ?.let { format(it, appText.args) }
            }
        }
    }

    private fun format(template: String, args: List<Any>): String {
        if (args.isEmpty()) {
            return template
        }
        return runCatching {
            String.format(Locale.getDefault(), template, *args.toTypedArray())
        }.getOrElse {
            template
        }
    }
}
