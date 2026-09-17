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

/**
 * 把历史绝对文件地址改写为当前前端同源路径。
 *
 * 后端常把 `publicBaseUrl` 写成局域网 IP。页面在 localhost 打开时，
 * `<img>` / `<video>` 会跨域请求该 IP，既带不上当前站点登录 Cookie，
 * 在未登录的登录页更没有任何会话。改写为 `/api/sys/files/**` 后走 Vite/Nginx 代理即可显示。
 *
 * @param url 原始媒体地址
 * @returns 可给 `<img src>` / `<video src>` 使用的同源或原样外部地址
 */
function toSameOriginBackendFileUrl(url: string): string {
  const value = url.trim()
  if (!value) {
    return ''
  }

  let absolute = value
  if (absolute.startsWith('//')) {
    const protocol = typeof window !== 'undefined' && window.location?.protocol
      ? window.location.protocol
      : 'http:'
    absolute = `${protocol}${absolute}`
  }

  try {
    const parsed = new URL(absolute)
    const path = parsed.pathname || ''
    const suffix = `${parsed.search || ''}${parsed.hash || ''}`
    if (path.startsWith('/api/sys/files/')) {
      return `${path}${suffix}`
    }
    if (path.startsWith('/sys/files/')) {
      return `/api${path}${suffix}`
    }
    if (path.startsWith('/api/files/')) {
      return `/api/sys${path.slice('/api'.length)}${suffix}`
    }
    if (path.startsWith('/files/')) {
      return `/api/sys${path}${suffix}`
    }
  } catch {
    return url
  }
  return url
}

/**
 * 统一媒体地址，供登录页 Logo/背景、头像和配置预览使用。
 *
 * @param value 后端返回的相对路径、网关绝对地址或外部 URL
 * @returns 浏览器可直接加载的地址；空值返回空串
 */
export function normalizeMediaUrl(value?: string | null): string {
  const url = String(value || '').trim().replace(/\\/g, '/')
  if (!url) return ''

  if (url.startsWith('data:') || url.startsWith('blob:')) {
    return url
  }

  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('//')) {
    return toSameOriginBackendFileUrl(url)
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
