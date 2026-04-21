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

package com.forgex.mobile.core.network.api

import com.forgex.mobile.core.common.model.ApiResponse
import com.forgex.mobile.core.network.model.workbench.CMenuVO
import com.forgex.mobile.core.network.model.workbench.ToggleFavoriteRequest
import com.forgex.mobile.core.network.model.workbench.WorkbenchMenusRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * C 端工作台/收藏 API 接口
 */
interface WorkbenchApi {

    /** 获取工作台模块列表 */
    @POST("app/c-menu/workbench/modules")
    suspend fun getWorkbenchModules(): ApiResponse<List<CMenuVO>>

    /** 获取指定模块下的菜单列表 */
    @POST("app/c-menu/workbench/menus")
    suspend fun getWorkbenchMenus(@Body request: WorkbenchMenusRequest): ApiResponse<List<CMenuVO>>

    /** 获取用户收藏菜单列表 */
    @POST("app/c-menu/favorites/list")
    suspend fun getFavorites(): ApiResponse<List<CMenuVO>>

    /** 切换收藏状态 */
    @POST("app/c-menu/favorites/toggle")
    suspend fun toggleFavorite(@Body request: ToggleFavoriteRequest): ApiResponse<Boolean>
}

