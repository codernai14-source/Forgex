/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import type { LanguageType } from '@/api/system/i18n'

export const LANG_SWITCH_ICON_SRC = '/diqiu.gif'

const FALLBACK_LANGUAGE_LABELS: Record<string, string> = {
  'zh-CN': '简体中文',
  'en-US': 'English',
  'zh-TW': '繁體中文',
  'ja-JP': '日本語',
  'ko-KR': '한국어',
}

const GARBLED_TEXT_PATTERN = /�|\?{2,}|馃|寰|鏉|鍒|璇|閰|缂|鐎|闄|琛|锛|鎴|閫|甯|妗/

export function isPotentiallyGarbledText(value?: string | null): boolean {
  const text = String(value || '').trim()
  if (!text) {
    return false
  }
  return GARBLED_TEXT_PATTERN.test(text)
}

export function getLanguageDisplayName(language?: Partial<LanguageType> | null): string {
  const langCode = String(language?.langCode || '').trim()
  const langName = String(language?.langName || '').trim()
  if (langName && !isPotentiallyGarbledText(langName)) {
    return langName
  }

  const fallbackLabel = FALLBACK_LANGUAGE_LABELS[langCode]
  if (fallbackLabel) {
    return fallbackLabel
  }

  const langNameEn = String(language?.langNameEn || '').trim()
  if (langNameEn && !isPotentiallyGarbledText(langNameEn)) {
    return langNameEn
  }

  return langCode || langName || langNameEn || 'Language'
}

