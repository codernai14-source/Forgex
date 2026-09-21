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

package com.forgex.mobile.core.network.model.workbench

/**
 * 工作台模块/菜单视图对象，对应后端菜单树节点。
 */
data class CMenuVO(
    val id: Long? = null,
    val parentId: Long? = null,
    val moduleId: Long? = null,
    val name: String? = null,
    val nameI18nJson: String? = null,
    val path: String? = null,
    val icon: String? = null,
    val componentKey: String? = null,
    val type: String? = null,
    val menuLevel: Int? = null,
    val orderNum: Int? = null,
    val menuMode: String? = null,
    val externalUrl: String? = null,
    val permKey: String? = null,
    val visible: Boolean? = null,
    val status: Boolean? = null,
    val deviceType: String? = null,
    val checked: Boolean? = null,
    val children: List<CMenuVO>? = null
)

/**
 * 工作台菜单查询请求。
 */
data class WorkbenchMenusRequest(
    val moduleId: Long? = null
)

/**
 * 收藏切换请求。
 */
data class ToggleFavoriteRequest(
    val cMenuId: Long
)

/**
 * C 端菜单聚合包查询请求（选租户后一次拉取）。
 */
data class CMenuBundleRequest(
    val deviceType: String = DEVICE_TYPE_MOBILE
)

/**
 * C 端菜单聚合包视图对象：授权模块整树 + 用户收藏。
 */
data class CMenuBundleVO(
    val modules: List<CMenuVO> = emptyList(),
    val favorites: List<CMenuVO> = emptyList()
)

/** 设备类型：手机/PDA */
const val DEVICE_TYPE_MOBILE = "MOBILE"

/** 设备类型：Pad */
const val DEVICE_TYPE_TABLET = "TABLET"
