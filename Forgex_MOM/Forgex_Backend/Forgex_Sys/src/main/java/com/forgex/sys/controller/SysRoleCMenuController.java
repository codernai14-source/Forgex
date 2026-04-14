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

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.sys.domain.vo.CMenuTreeVO;
import com.forgex.sys.service.ISysCMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C 端角色菜单授权 Controller
 * <p>
 * 提供 C 端角色菜单的查询和授权接口，供 PC 管理端使用。
 * </p>
 *
 * @author Forgex Team
 * @since 2026-04-11
 */
@RestController
@RequestMapping("/sys/role/c-menu")
@RequiredArgsConstructor
public class SysRoleCMenuController {

    private final ISysCMenuService cMenuService;

    /**
     * 查询角色已授权的 C 端菜单 ID 列表
     */
    @PostMapping("/list")
    public R<List<Long>> list(@RequestBody Map<String, Object> body) {
        Long roleId = parseLong(body.get("roleId"));
        return R.ok(cMenuService.getRoleCMenuIds(roleId));
    }

    /**
     * 获取模块下菜单树（含角色勾选状态）
     */
    @PostMapping("/authData/module/{moduleId}")
    public R<List<CMenuTreeVO>> authData(@PathVariable Long moduleId, @RequestBody Map<String, Object> body) {
        Long roleId = parseLong(body.get("roleId"));
        return R.ok(cMenuService.getAuthMenuTree(moduleId, roleId));
    }

    /**
     * 角色授权 C 端菜单
     * <p>
     * 将指定菜单授权给角色。
     * </p>
     * 
     * @param body 请求体，包含 roleId 和 menuIds
     * @return 授权结果
     */
    @PostMapping("/grant")
    @SuppressWarnings("unchecked")
    public R<Void> grant(@RequestBody Map<String, Object> body) {
        Long roleId = parseLong(body.get("roleId"));
        List<Long> menuIds = (List<Long>) body.get("menuIds");
        cMenuService.grantRoleCMenus(roleId, menuIds);
        return R.ok(CommonPrompt.AUTHORIZE_SUCCESS);
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

