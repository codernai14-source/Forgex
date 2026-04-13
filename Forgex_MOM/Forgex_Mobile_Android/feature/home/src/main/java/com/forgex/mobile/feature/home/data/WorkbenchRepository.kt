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

package com.forgex.mobile.feature.home.data

import com.forgex.mobile.core.common.result.AppResult
import com.forgex.mobile.core.network.api.WorkbenchApi
import com.forgex.mobile.core.network.model.workbench.CMenuVO
import com.forgex.mobile.core.network.model.workbench.ToggleFavoriteRequest
import com.forgex.mobile.core.network.model.workbench.WorkbenchMenusRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkbenchRepository @Inject constructor(
    private val workbenchApi: WorkbenchApi
) {

    suspend fun loadModules(): AppResult<List<CMenuVO>> {
        return try {
            val response = workbenchApi.getWorkbenchModules()
            if (!response.isSuccess()) {
                return AppResult.Error(response.errorMessage(), response.code)
            }
            AppResult.Success(response.data.orEmpty())
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "加载工作台模块失败")
        }
    }

    suspend fun loadMenus(moduleId: Long?): AppResult<List<CMenuVO>> {
        return try {
            val response = workbenchApi.getWorkbenchMenus(WorkbenchMenusRequest(moduleId))
            if (!response.isSuccess()) {
                return AppResult.Error(response.errorMessage(), response.code)
            }
            AppResult.Success(response.data.orEmpty())
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "加载工作台菜单失败")
        }
    }

    suspend fun loadFavorites(): AppResult<List<CMenuVO>> {
        return try {
            val response = workbenchApi.getFavorites()
            if (!response.isSuccess()) {
                return AppResult.Error(response.errorMessage(), response.code)
            }
            AppResult.Success(response.data.orEmpty())
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "加载收藏列表失败")
        }
    }

    suspend fun toggleFavorite(cMenuId: Long): AppResult<Boolean> {
        return try {
            val response = workbenchApi.toggleFavorite(ToggleFavoriteRequest(cMenuId))
            if (!response.isSuccess()) {
                return AppResult.Error(response.errorMessage(), response.code)
            }
            AppResult.Success(response.data ?: false)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "收藏操作失败")
        }
    }
}

