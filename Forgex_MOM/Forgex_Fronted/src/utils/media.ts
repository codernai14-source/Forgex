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

const backendMediaPrefixes = ['api/', 'files/', 'sys/', 'basic/', 'app/']

function hasBackendMediaPrefix(url: string): boolean {
  const normalized = url.replace(/^\/+/, '')
  return backendMediaPrefixes.some(prefix => normalized.startsWith(prefix))
}

export function normalizeMediaUrl(value?: string | null): string {
  const url = String(value || '').trim().replace(/\\/g, '/')
  if (!url) return ''

  if (
    url.startsWith('data:') ||
    url.startsWith('blob:') ||
    url.startsWith('http://') ||
    url.startsWith('https://') ||
    url.startsWith('//')
  ) {
    return url
  }

  if (url.startsWith('/api')) {
    return url
  }

  if (url.startsWith('/')) {
    return hasBackendMediaPrefix(url) ? `/api${url}` : url
  }

  if (hasBackendMediaPrefix(url)) {
    return `/api/${url.replace(/^\/+/, '')}`
  }

  return `/${url.replace(/^\/+/, '')}`
}

