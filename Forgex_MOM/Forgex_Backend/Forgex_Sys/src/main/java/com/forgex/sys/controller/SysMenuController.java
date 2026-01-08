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
    @GetMapping("/page")
    public R<IPage<SysMenuDTO>> page(Page<SysMenu> page, SysMenuQueryDTO query) {
        return R.ok(menuService.pageMenus(page, query));
    }
    
    /**
     * 查询菜单列表
     */
    @GetMapping("/list")
    public R<List<SysMenuDTO>> list(SysMenuQueryDTO query) {
        return R.ok(menuService.listMenus(query));
    }
    
    /**
     * 根据ID获取菜单详情
     */
    @GetMapping("/{id}")
    public R<SysMenuDTO> getById(@PathVariable Long id) {
        menuValidator.validateId(id);
        return R.ok(menuService.getMenuById(id));
    }
    
    /**
     * 新增菜单
     */
    @PostMapping
    public R<Void> add(@RequestBody @Validated SysMenuDTO menuDTO) {
        // 1. 数据校验
        menuValidator.validateForAdd(menuDTO);
        
        // 2. 调用Service
        menuService.addMenu(menuDTO);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 新增菜单（兼容旧接口）
     */
    @PostMapping("/create")
    public R<Boolean> create(@RequestBody SysMenuDTO menuDTO) {
        menuValidator.validateForAdd(menuDTO);
        menuService.addMenu(menuDTO);
        return R.ok(true);
    }
    
    /**
     * 更新菜单
     */
    @PutMapping
    public R<Void> update(@RequestBody @Validated SysMenuDTO menuDTO) {
        // 1. 数据校验
        menuValidator.validateForUpdate(menuDTO);
        
        // 2. 调用Service
        menuService.updateMenu(menuDTO);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 更新菜单（兼容旧接口）
     */
    @PostMapping("/update")
    public R<Boolean> updateOld(@RequestBody SysMenuDTO menuDTO) {
        menuValidator.validateForUpdate(menuDTO);
        menuService.updateMenu(menuDTO);
        return R.ok(true);
    }
    
    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 1. 数据校验
        menuValidator.validateForDelete(id);
        
        // 2. 调用Service
        menuService.deleteMenu(id);
        
        // 3. 返回结果
        return R.ok();
    }
    
    /**
     * 删除菜单（兼容旧接口）
     */
    @PostMapping("/delete")
    public R<Boolean> deleteOld(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        menuValidator.validateForDelete(id);
        menuService.deleteMenu(id);
        return R.ok(true);
    }
    
    /**
     * 批量删除菜单
     */
    @DeleteMapping("/batch")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        // 1. 校验每个ID
        for (Long id : ids) {
            menuValidator.validateForDelete(id);
        }
        
        // 2. 调用Service
        menuService.batchDeleteMenus(ids);
        
        // 3. 返回结果
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
