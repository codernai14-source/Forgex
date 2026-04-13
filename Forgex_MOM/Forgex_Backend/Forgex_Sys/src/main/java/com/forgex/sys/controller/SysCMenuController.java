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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysCMenuDTO;
import com.forgex.sys.domain.dto.SysCMenuQueryDTO;
import com.forgex.sys.domain.entity.SysCMenu;
import com.forgex.sys.domain.vo.CMenuTreeVO;
import com.forgex.sys.service.ISysCMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C 端菜单管理 Controller
 * <p>
 * 提供 C 端菜单的 CRUD 接口，供 PC 管理端使用。
 * </p>
 *
 * @author Forgex Team
 * @since 2026-04-11
 */
@RestController
@RequestMapping("/sys/c-menu")
@RequiredArgsConstructor
public class SysCMenuController {

    private final ISysCMenuService cMenuService;

    /**
     * 分页查询 C 端菜单
     */
    @PostMapping("/page")
    public R<IPage<SysCMenuDTO>> page(@RequestBody SysCMenuQueryDTO query) {
        Page<SysCMenu> page = new Page<>(query.getPageNum(), query.getPageSize());
        return R.ok(cMenuService.pageMenus(page, query));
    }

    /**
     * 获取 C 端菜单树
     */
    @PostMapping("/tree")
    public R<List<CMenuTreeVO>> tree(@RequestBody Map<String, Object> body) {
        Long tenantId = TenantContext.get();
        Long moduleId = parseLong(body.get("moduleId"));
        return R.ok(cMenuService.getMenuTree(tenantId, moduleId));
    }

    /**
     * C 端菜单详情
     */
    @PostMapping("/detail")
    public R<SysCMenuDTO> detail(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        return R.ok(cMenuService.getMenuById(id));
    }

    /**
     * 新增 C 端菜单
     */
    @RequirePerm("sys:c-menu:add")
    @PostMapping("/create")
    public R<Void> create(@RequestBody @Validated SysCMenuDTO dto) {
        cMenuService.addMenu(dto);
        return R.ok(CommonPrompt.CREATE_SUCCESS);
    }

    /**
     * 更新 C 端菜单
     */
    @RequirePerm("sys:c-menu:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated SysCMenuDTO dto) {
        cMenuService.updateMenu(dto);
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    /**
     * 删除 C 端菜单
     */
    @RequirePerm("sys:c-menu:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        cMenuService.deleteMenu(id);
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    /**
     * 批量删除 C 端菜单
     */
    @RequirePerm("sys:c-menu:delete")
    @PostMapping("/batchDelete")
    @SuppressWarnings("unchecked")
    public R<Void> batchDelete(@RequestBody Map<String, Object> body) {
        List<Long> ids = (List<Long>) body.get("ids");
        cMenuService.batchDeleteMenus(ids);
        return R.ok(CommonPrompt.DELETE_SUCCESS);
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

