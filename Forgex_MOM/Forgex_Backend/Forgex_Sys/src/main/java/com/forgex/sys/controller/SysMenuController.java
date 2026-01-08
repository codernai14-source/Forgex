/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysMenuDTO;
import com.forgex.sys.domain.dto.SysMenuQueryDTO;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.vo.MenuTreeVO;
import com.forgex.sys.domain.vo.UserRoutesVO;
import com.forgex.sys.service.ISysMenuService;
import com.forgex.sys.validator.MenuValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单管理Controller
 * 
 * 职责：
 * - 接收HTTP请求
 * - 参数校验（调用Validator）
 * - 调用Service层方法
 * - 返回响应结果
 * 
 * 接口规范：
 * - 所有接口统一使用 POST 方法
 * - 参数统一封装为对象
 * - 分页查询使用 BaseGetParam（pageNum/pageSize）
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {
    
    private final ISysMenuService menuService;
    private final MenuValidator menuValidator;
    
    /**
     * 获取用户路由（包含模块、菜单、按钮权限）
     */
    @PostMapping("/routes")
    public R<UserRoutesVO> routes(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        String account = (String) body.get("account");
        Long tenantId = parseLong(body.get("tenantId"));
        
        // 2. 参数校验
        menuValidator.validateRoutesParams(account, tenantId);
        
        // 3. 调用Service
        UserRoutesVO routes = menuService.getUserRoutes(account, tenantId);
        
        // 4. 返回结果
        return R.ok(routes);
    }
    
    /**
     * 获取菜单树
     */
    @PostMapping("/tree")
    public R<List<MenuTreeVO>> tree(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        Long tenantId = parseLong(body.get("tenantId"));
        Long moduleId = parseLong(body.get("moduleId"));
        
        // 2. 调用Service
        List<MenuTreeVO> tree = menuService.getMenuTree(tenantId, moduleId);
        
        // 3. 返回结果
        return R.ok(tree);
    }
    
    /**
     * 分页查询菜单列表
     */
    @PostMapping("/page")
    public R<IPage<SysMenuDTO>> page(@RequestBody SysMenuQueryDTO query) {
        // 使用 BaseGetParam 中的 pageNum 和 pageSize
        Page<SysMenu> page = new Page<>(query.getPageNum(), query.getPageSize());
        return R.ok(menuService.pageMenus(page, query));
    }
    
    /**
     * 查询菜单列表（不分页）
     */
    @PostMapping("/list")
    public R<List<SysMenuDTO>> list(@RequestBody SysMenuQueryDTO query) {
        return R.ok(menuService.listMenus(query));
    }
    
    /**
     * 根据ID获取菜单详情
     */
    @PostMapping("/detail")
    public R<SysMenuDTO> detail(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        menuValidator.validateId(id);
        return R.ok(menuService.getMenuById(id));
    }
    
    /**
     * 新增菜单
     */
    @PostMapping("/create")
    public R<Void> create(@RequestBody @Validated SysMenuDTO menuDTO) {
        // 1. 数据校验
        menuValidator.validateForAdd(menuDTO);
        
        // 2. 调用Service
        menuService.addMenu(menuDTO);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 更新菜单
     */
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated SysMenuDTO menuDTO) {
        // 1. 数据校验
        menuValidator.validateForUpdate(menuDTO);
        
        // 2. 调用Service
        menuService.updateMenu(menuDTO);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 删除菜单
     */
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, Object> body) {
        // 1. 解析参数
        Long id = parseLong(body.get("id"));
        
        // 2. 数据校验
        menuValidator.validateForDelete(id);
        
        // 3. 调用Service
        menuService.deleteMenu(id);
        
        // 4. 返回结果
        return R.ok();
    }
    
    /**
     * 批量删除菜单
     */
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody Map<String, Object> body) {
        // 1. 解析参数
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        
        // 2. 校验每个ID
        for (Long id : ids) {
            menuValidator.validateForDelete(id);
        }
        
        // 3. 调用Service
        menuService.batchDeleteMenus(ids);
        
        // 4. 返回结果
        return R.ok();
    }
    
    /**
     * 解析Long类型参数
     */
    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
