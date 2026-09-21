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

package com.forgex.sys.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * C 端菜单聚合包 VO（选租户后一次拉取）
 * <p>
 * 供安卓/Pad 端在选定租户后一次性获取全部授权模块（含完整菜单子树）与收藏，
 * 替代"模块列表 + 按模块逐个查菜单"的二级加载。
 * </p>
 *
 * @author Forgex Team
 * @since 2026-09-19
 *
 * @version 1.0.0
 */
@Data
public class CMenuBundleVO {

    /**
     * 授权模块列表（顶级菜单，每个节点携带完整 children 菜单树）。
     */
    private List<CMenuTreeVO> modules;

    /**
     * 用户收藏菜单列表。
     */
    private List<CMenuTreeVO> favorites;
}
