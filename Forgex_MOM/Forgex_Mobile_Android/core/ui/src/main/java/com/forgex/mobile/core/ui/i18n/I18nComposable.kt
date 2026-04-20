package com.forgex.mobile.core.ui.i18n

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import com.forgex.mobile.core.common.i18n.AppText
import java.util.Locale

val LocalI18nBundle = staticCompositionLocalOf<Map<String, String>> { emptyMap() }

@Composable
fun ProvideI18nBundle(
    bundle: Map<String, String>,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalI18nBundle provides bundle, content = content)
}

@Composable
@ReadOnlyComposable
fun i18nString(
    key: String,
    @StringRes fallbackResId: Int,
    vararg args: Any
): String {
    val dynamic = LocalI18nBundle.current[key].orEmpty()
    return if (dynamic.isNotBlank()) {
        formatI18n(dynamic, args.toList())
    } else {
        stringResource(fallbackResId, *args)
    }
}

@Composable
@ReadOnlyComposable
fun resolveAppText(appText: AppText?): String? {
    return when (appText) {
        null -> null
        is AppText.Raw -> appText.value
        is AppText.Resource -> stringResource(appText.resId, *appText.args.toTypedArray())
        is AppText.Dynamic -> {
            val dynamic = LocalI18nBundle.current[appText.key].orEmpty()
            val fallbackResId = appText.fallbackResId
            val fallbackRaw = appText.fallbackRaw
            when {
                dynamic.isNotBlank() -> formatI18n(dynamic, appText.args)
                fallbackResId != null -> stringResource(
                    fallbackResId,
                    *appText.args.toTypedArray()
                )
                !fallbackRaw.isNullOrBlank() -> formatI18n(fallbackRaw, appText.args)
                else -> null
            }
        }
    }
}

private fun formatI18n(
    template: String,
    args: List<Any>
): String {
    if (args.isEmpty()) {
        return template
    }
    return runCatching {
        String.format(Locale.getDefault(), template, *args.toTypedArray())
    }.getOrElse {
        template
    }
}
