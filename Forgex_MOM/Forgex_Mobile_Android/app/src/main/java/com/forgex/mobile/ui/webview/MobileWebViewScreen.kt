package com.forgex.mobile.ui.webview

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.R
import com.forgex.mobile.ui.AppShellViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MobileWebViewScreen(
    title: String,
    url: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    shellViewModel: AppShellViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val shellState by shellViewModel.uiState.collectAsState()
    var canGoBack by remember { mutableStateOf(false) }

    val languageTag = shellState.languageState.currentLanguageTag
    val finalUrl = remember(url, languageTag) { appendLanguageIfNeeded(url, languageTag) }
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    canGoBack = view?.canGoBack() == true
                }
            }
        }
    }

    LaunchedEffect(finalUrl, languageTag) {
        if (finalUrl.isNotBlank() && webView.url != finalUrl) {
            webView.loadUrl(
                finalUrl,
                mapOf(
                    "X-Lang" to languageTag,
                    "Accept-Language" to languageTag
                )
            )
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    BackHandler(enabled = canGoBack) {
        webView.goBack()
        canGoBack = webView.canGoBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title.ifBlank { stringResource(R.string.webview_default_title) }
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = {
                            if (webView.canGoBack()) {
                                webView.goBack()
                                canGoBack = webView.canGoBack()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (finalUrl.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.webview_empty_url),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            AndroidView(
                factory = { webView },
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

private fun appendLanguageIfNeeded(rawUrl: String, languageTag: String): String {
    if (rawUrl.isBlank()) return rawUrl
    return runCatching {
        val uri = Uri.parse(rawUrl)
        val host = uri.host.orEmpty()
        if (host.isBlank() || !host.contains("forgex", ignoreCase = true)) {
            return rawUrl
        }
        if (!uri.getQueryParameter("lang").isNullOrBlank()) {
            return rawUrl
        }
        uri.buildUpon()
            .appendQueryParameter("lang", languageTag)
            .build()
            .toString()
    }.getOrElse { rawUrl }
}
