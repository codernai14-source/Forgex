package com.forgex.mobile.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgex.mobile.core.common.i18n.AppLanguage
import com.forgex.mobile.core.common.i18n.LanguageMode
import com.forgex.mobile.core.ui.R

const val PROFILE_ROUTE = "profile"

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_title),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Text(
                text = stringResource(R.string.profile_subtitle),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Text(
                text = stringResource(R.string.profile_language_title),
                style = MaterialTheme.typography.titleMedium
            )
        }
        item {
            LanguageRow(
                selected = uiState.mode == LanguageMode.FOLLOW_SYSTEM,
                title = stringResource(R.string.profile_language_follow_system),
                onClick = viewModel::followSystem
            )
        }
        items(uiState.languages, key = { it.langCode }) { language ->
            val displayName = if (language.langName.isNotBlank()) {
                language.langName
            } else if (language.langCode == "zh-CN") {
                stringResource(R.string.profile_language_zh_cn)
            } else if (language.langCode == "en-US") {
                stringResource(R.string.profile_language_en_us)
            } else {
                AppLanguage.displayName(language.langCode, uiState.currentLanguageTag)
            }
            LanguageRow(
                selected = uiState.mode == LanguageMode.MANUAL && uiState.currentLanguageTag == language.langCode,
                title = displayName,
                onClick = { viewModel.selectLanguage(language.langCode) }
            )
        }
        item {
            Text(
                text = stringResource(R.string.profile_language_current, uiState.currentLanguageTag),
                style = MaterialTheme.typography.bodySmall
            )
        }
        val noticeMessageRes = uiState.noticeMessageRes
        if (noticeMessageRes != null) {
            item {
                Text(
                    text = stringResource(noticeMessageRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        item {
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.common_back))
            }
        }
    }
}

@Composable
private fun LanguageRow(
    selected: Boolean,
    title: String,
    onClick: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        RadioButton(selected = selected, onClick = onClick)
    }
}
