package com.forgex.mobile.core.network.interceptor

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCookieJar @Inject constructor() : CookieJar {

    private val cookieStore: MutableMap<String, MutableList<Cookie>> = ConcurrentHashMap()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return

        val key = url.host
        val current = cookieStore[key].orEmpty().toMutableList()

        cookies.forEach { newCookie ->
            current.removeAll { it.name == newCookie.name }
            current.add(newCookie)
        }

        cookieStore[key] = current
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val key = url.host
        val stored = cookieStore[key].orEmpty()
        if (stored.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()
        val validCookies = stored.filter { it.expiresAt > now }

        cookieStore[key] = validCookies.toMutableList()
        return validCookies
    }
}
