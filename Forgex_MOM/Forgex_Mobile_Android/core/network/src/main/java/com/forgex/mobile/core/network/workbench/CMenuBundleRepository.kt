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

package com.forgex.mobile.core.network.workbench

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.datastore.SessionStore
import com.forgex.mobile.core.network.api.WorkbenchApi
import com.forgex.mobile.core.network.model.workbench.CMenuBundleRequest
import com.forgex.mobile.core.network.model.workbench.CMenuBundleVO
import com.forgex.mobile.core.network.model.workbench.DEVICE_TYPE_MOBILE
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * C 端菜单聚合包仓储：拉取聚合包并缓存到 DataStore（绑定租户），
 * 供选租户后预载与工作台缓存优先渲染共用。
 */
@Singleton
class CMenuBundleRepository @Inject constructor(
    private val workbenchApi: WorkbenchApi,
    private val sessionStore: SessionStore
) {

    private val gson = Gson()

    /**
     * 拉取 C 端菜单聚合包（授权模块整树 + 收藏）并缓存到 DataStore。
     *
     * @param tenantId 当前租户 ID（缓存归属校验用）
     * @param deviceType 设备类型，默认手机/PDA
     */
    suspend fun fetchBundle(
        tenantId: String,
        deviceType: String = DEVICE_TYPE_MOBILE
    ): AppResult<CMenuBundleVO> {
        return try {
            val response = workbenchApi.getCMenuBundle(CMenuBundleRequest(deviceType))
            if (response.isSuccess()) {
                val bundle = response.data ?: CMenuBundleVO()
                sessionStore.saveCMenuBundle(tenantId, gson.toJson(bundle))
                AppResult.Success(bundle)
            } else {
                AppResult.Error(response.errorMessage(), response.code)
            }
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Load C menu bundle failed")
        }
    }

    /**
     * 读取缓存的聚合包；无缓存或租户不匹配时返回 null。
     *
     * @param tenantId 当前租户 ID
     */
    suspend fun readCachedBundle(tenantId: String): CMenuBundleVO? {
        val cachedTenantId = sessionStore.cMenuBundleTenantId.first()
        val cachedJson = sessionStore.cMenuBundleJson.first()
        if (cachedTenantId.isNullOrBlank() || cachedJson.isNullOrBlank()) {
            return null
        }
        if (cachedTenantId != tenantId) {
            return null
        }
        return runCatching {
            gson.fromJson(cachedJson, CMenuBundleVO::class.java)
        }.getOrNull()
    }
}
