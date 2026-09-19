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

package com.forgex.sys.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.vo.CMenuTreeVO;
import com.forgex.sys.service.ISysCMenuService;
import com.forgex.sys.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * C 端 App 工作台/收藏聚合接口 Controller
 * <p>
 * 提供安卓/Pad 首页所需的工作台模块、菜单、收藏功能。
 * </p>
 *
 * @author Forgex Team
 * @since 2026-04-11
 *
 * @version 1.0.0
 */
@RestController
@RequestMapping("/app/c-menu")
@RequiredArgsConstructor
public class AppCMenuController {

    private final ISysCMenuService cMenuService;
    private final ISysUserService sysUserService;

    /**
     * 获取工作台模块列表（顶级菜单/目录）
     */
    @PostMapping("/workbench/modules")
    public R<List<CMenuTreeVO>> workbenchModules() {
        Long userId = resolveCurrentUserId();
        Long tenantId = TenantContext.get();
        return R.ok(cMenuService.getWorkbenchModules(userId, tenantId));
    }

    /**
     * 获取指定模块下的菜单列表
     */
    @PostMapping("/workbench/menus")
    public R<List<CMenuTreeVO>> workbenchMenus(@RequestBody Map<String, Object> body) {
        Long userId = resolveCurrentUserId();
        Long tenantId = TenantContext.get();
        Long moduleId = parseLong(body.get("moduleId"));
        return R.ok(cMenuService.getWorkbenchMenus(userId, tenantId, moduleId));
    }

    /**
     * 获取用户收藏菜单列表
     */
    @PostMapping("/favorites/list")
    public R<List<CMenuTreeVO>> favoritesList() {
        Long userId = resolveCurrentUserId();
        Long tenantId = TenantContext.get();
        return R.ok(cMenuService.getUserFavorites(userId, tenantId));
    }

    /**
     * 切换收藏状态（收藏/取消收藏）
     *
     * @return true=已收藏, false=已取消
     */
    @PostMapping("/favorites/toggle")
    public R<Boolean> favoritesToggle(@RequestBody Map<String, Object> body) {
        Long userId = resolveCurrentUserId();
        Long tenantId = TenantContext.get();
        Long cMenuId = parseLong(body.get("cMenuId"));
        boolean result = cMenuService.toggleFavorite(userId, tenantId, cMenuId);
        return R.ok(result);
    }

    private Long resolveCurrentUserId() {
        return sysUserService.resolveUserIdByLoginId(StpUtil.getLoginIdDefaultNull());
    }

    private Long parseLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof String) {
            try { return Long.parseLong((String) obj); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }
}
