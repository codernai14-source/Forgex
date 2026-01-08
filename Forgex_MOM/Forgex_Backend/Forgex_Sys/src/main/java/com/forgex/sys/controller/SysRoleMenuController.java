package com.forgex.sys.controller;

import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.RolePermissionDTO;
import com.forgex.sys.service.ISysRoleMenuService;
import com.forgex.sys.validator.RoleMenuValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色菜单权限Controller
 * 
 * 职责：
 * - 接收HTTP请求
 * - 参数校验（调用Validator）
 * - 调用Service层方法
 * - 返回响应结果
 * 
 * 功能：
 * - 查询指定角色在某租户下已授权的菜单/按钮ID列表
 * - 重新授予角色在某租户下的菜单/按钮权限（先清空再写入）
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@RestController
@RequestMapping("/sys/role/menu")
@RequiredArgsConstructor
public class SysRoleMenuController {

    private final ISysRoleMenuService roleMenuService;
    private final RoleMenuValidator roleMenuValidator;

    /**
     * 查询指定角色在某租户下已授权的菜单ID列表（兼容旧接口）
     *
     * @param body 请求体参数，需包含 roleId、tenantId
     * @return 已绑定的菜单ID集合
     */
    @PostMapping("/list")
    public R<List<Long>> list(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = parseLong(body.get("tenantId"));
        
        // 2. 参数校验
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        
        // 3. 调用Service
        List<Long> menuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        
        // 4. 返回结果
        return R.ok(menuIds);
    }

    /**
     * 查询角色菜单权限列表（RESTful风格）
     *
     * @param roleId 角色ID
     * @param tenantId 租户ID
     * @return 已绑定的菜单ID集合
     */
    @GetMapping
    public R<List<Long>> getRoleMenus(@RequestParam Long roleId, @RequestParam Long tenantId) {
        // 1. 参数校验
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        
        // 2. 调用Service
        List<Long> menuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        
        // 3. 返回结果
        return R.ok(menuIds);
    }

    /**
     * 授予角色菜单权限（兼容旧接口）
     *
     * @param body 请求体参数，包含 roleId、tenantId、menuIds（菜单ID列表）
     * @return 是否授权成功
     */
    @PostMapping("/grant")
    public R<Boolean> grant(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = parseLong(body.get("tenantId"));
        @SuppressWarnings("unchecked")
        List<Integer> menuIdsInt = (List<Integer>) body.get("menuIds");
        
        // 转换为Long类型
        List<Long> menuIds = null;
        if (menuIdsInt != null) {
            menuIds = menuIdsInt.stream()
                    .filter(id -> id != null)
                    .map(Integer::longValue)
                    .toList();
        }
        
        // 2. 构建DTO
        RolePermissionDTO permissionDTO = new RolePermissionDTO();
        permissionDTO.setRoleId(roleId);
        permissionDTO.setTenantId(tenantId);
        permissionDTO.setMenuIds(menuIds);
        
        // 3. 参数校验
        roleMenuValidator.validateGrantParams(permissionDTO);
        
        // 4. 调用Service
        roleMenuService.grantPermission(permissionDTO);
        
        // 5. 返回结果
        return R.ok(true);
    }

    /**
     * 授予角色菜单权限（RESTful风格）
     *
     * @param permissionDTO 权限信息
     * @return 操作结果
     */
    @PostMapping
    public R<Void> grantPermission(@RequestBody RolePermissionDTO permissionDTO) {
        // 1. 参数校验
        roleMenuValidator.validateGrantParams(permissionDTO);
        
        // 2. 调用Service
        roleMenuService.grantPermission(permissionDTO);
        
        // 3. 返回结果
        return R.ok();
    }

    /**
     * 删除角色的所有菜单权限
     *
     * @param roleId 角色ID
     * @param tenantId 租户ID
     * @return 操作结果
     */
    @DeleteMapping
    public R<Void> deletePermissions(@RequestParam Long roleId, @RequestParam Long tenantId) {
        // 1. 参数校验
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        
        // 2. 调用Service
        roleMenuService.deleteRolePermissions(roleId, tenantId);
        
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
