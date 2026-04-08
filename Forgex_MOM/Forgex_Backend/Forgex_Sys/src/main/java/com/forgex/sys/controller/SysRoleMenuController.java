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

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.RolePermissionDTO;
import com.forgex.sys.domain.vo.MenuTreeVO;
import com.forgex.sys.service.ISysMenuService;
import com.forgex.sys.service.ISysRoleMenuService;
import com.forgex.sys.validator.RoleMenuValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@RestController
@RequestMapping("/sys/role/menu")
@RequiredArgsConstructor
public class SysRoleMenuController {

    private final ISysRoleMenuService roleMenuService;
    private final ISysMenuService menuService;
    private final RoleMenuValidator roleMenuValidator;

    /**
     * 查询指定角色在某租户下已授权的菜单ID列表（兼容旧接口）
     *
     * @param body 请求体参数，需包含 roleId
     * @return 已绑定的菜单ID集合
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/list")
    public R<List<Long>> list(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();
        
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
     * @return 已绑定的菜单ID集合
     */
    @RequirePerm("sys:role:authMenu")
    @GetMapping
    public R<List<Long>> getRoleMenus(@RequestParam Long roleId) {
        Long tenantId = TenantContext.get();
        // 1. 参数校验
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        
        // 2. 调用Service
        List<Long> menuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        
        // 3. 返回结果
        return R.ok(menuIds);
    }

    /**
     * 获取角色菜单授权数据（包含所有菜单树和已授权的菜单ID）
     *
     * @param body 请求体参数，需包含 roleId
     * @return 包含所有菜单树和已授权菜单ID的数据
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/authData")
    public R<Map<String, Object>> getAuthData(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();
        
        // 2. 参数校验
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        
        // 3. 调用Service获取已授权的菜单ID
        List<Long> grantedMenuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        
        // 4. 获取所有菜单树
        List<MenuTreeVO> allMenuTree = menuService.getMenuTree(tenantId, null);
        
        // 5. 构建已拥有的权限树
        Set<Long> grantedSet = grantedMenuIds.stream().collect(Collectors.toSet());
        List<MenuTreeVO> ownedMenuTree = buildOwnedTree(allMenuTree, grantedSet);

        // 6. 返回结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("grantedMenuIds", grantedMenuIds);
        result.put("allPermissions", allMenuTree);
        result.put("ownedPermissions", ownedMenuTree);
        
        return R.ok(result);
    }

    /**
     * 构建已拥有的权限树
     * 递归过滤出包含已授权节点的树结构
     */
    private List<MenuTreeVO> buildOwnedTree(List<MenuTreeVO> nodes, Set<Long> grantedSet) {
        List<MenuTreeVO> result = new ArrayList<>();
        if (nodes == null) {
            return result;
        }
        
        for (MenuTreeVO node : nodes) {
            boolean isGranted = grantedSet.contains(node.getId());
            List<MenuTreeVO> children = buildOwnedTree(node.getChildren(), grantedSet);
            
            // 如果当前节点被授权，或者有子节点被授权，则保留该节点
            if (isGranted || !children.isEmpty()) {
                MenuTreeVO newNode = new MenuTreeVO();
                // 复制属性
                newNode.setId(node.getId());
                newNode.setParentId(node.getParentId());
                newNode.setName(node.getName());
                newNode.setPath(node.getPath());
                newNode.setComponentKey(node.getComponentKey());
                newNode.setIcon(node.getIcon());
                newNode.setOrderNum(node.getOrderNum());
                newNode.setType(node.getType());
                newNode.setPermKey(node.getPermKey());
                newNode.setStatus(node.getStatus());
                newNode.setChildren(children);
                result.add(newNode);
            }
        }
        return result;
    }

    /**
     * 授予角色菜单权限（兼容旧接口）
     *
     * @param body 请求体参数，包含 roleId、menuIds（菜单ID列表）
     * @return 是否授权成功
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/grant")
    public R<Boolean> grant(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();
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
        return R.ok(CommonPrompt.AUTHORIZE_SUCCESS, true);
    }

    /**
     * 授予角色菜单权限（RESTful风格）
     *
     * @param permissionDTO 权限信息
     * @return 操作结果
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping
    public R<Void> grantPermission(@RequestBody RolePermissionDTO permissionDTO) {
        permissionDTO.setTenantId(TenantContext.get());
        // 1. 参数校验
        roleMenuValidator.validateGrantParams(permissionDTO);
        
        // 2. 调用Service
        roleMenuService.grantPermission(permissionDTO);
        
        // 3. 返回结果
        return R.ok(CommonPrompt.AUTHORIZE_SUCCESS);
    }
    
    /**
     * 删除角色的所有菜单权限
     *
     * @param roleId 角色ID
     * @return 操作结果
     */
    @RequirePerm("sys:role:authMenu")
    @DeleteMapping
    public R<Void> deletePermissions(@RequestParam Long roleId) {
        Long tenantId = TenantContext.get();
        // 1. 参数校验
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        
        // 2. 调用Service
        roleMenuService.deleteRolePermissions(roleId, tenantId);
        
        // 3. 返回结果
        return R.ok(CommonPrompt.UNAUTHORIZE_SUCCESS);
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

    /**
     * 获取指定模块的菜单授权数据（带授权状态）。
     * <p>
     * 查询指定模块下的菜单树，并标记每个菜单节点的授权状态（是否已授予该角色）
     * </p>
     *
     * @param moduleId 模块 ID（必填）
     * @param body 请求体参数，需包含 roleId
     * @return 菜单树列表，每个节点包含 checked 属性标记授权状态
     * @throws com.forgex.common.exception.BusinessException 参数校验失败时抛出
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/authData/module/{moduleId}")
    public R<List<MenuTreeVO>> getModuleAuthData(
            @PathVariable Long moduleId,
            @RequestBody Map<String, Object> body) {
        // 1. 参数解析
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();

        // 2. 参数校验
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        if (moduleId == null || moduleId <= 0) {
            return R.fail(CommonPrompt.BAD_REQUEST, "模块 ID 不能为空");
        }

        // 3. 调用 Service 获取该模块下的菜单树
        List<MenuTreeVO> menuTree = menuService.getMenuTree(tenantId, moduleId);

        // 4. 查询已授权的菜单 ID
        List<Long> grantedMenuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        Set<Long> grantedSet = new HashSet<>(grantedMenuIds);

        // 5. 标记每个节点的授权状态
        for (MenuTreeVO node : menuTree) {
            markCheckedStatus(node, grantedSet);
        }

        // 6. 返回结果
        return R.ok(menuTree);
    }

    /**
     * 搜索菜单授权数据。
     * <p>
     * 根据关键字搜索菜单，并标记授权状态
     * </p>
     *
     * @param body 请求体参数，需包含：
     *             - roleId: 角色 ID（必填）
     *             - keyword: 搜索关键字（必填）
     *             - moduleId: 模块 ID（可选，限制在指定模块内搜索）
     * @return 搜索到的菜单树列表，每个节点包含 checked 属性标记授权状态
     * @throws com.forgex.common.exception.BusinessException 参数校验失败时抛出
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/authData/search")
    public R<List<MenuTreeVO>> searchAuthData(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        Long roleId = parseLong(body.get("roleId"));
        String keyword = (String) body.get("keyword");
        Long moduleId = parseLong(body.get("moduleId"));
        Long tenantId = TenantContext.get();

        // 2. 参数校验
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.fail(CommonPrompt.BAD_REQUEST, "搜索关键字不能为空");
        }

        // 3. 获取所有菜单树（或指定模块的菜单树）
        List<MenuTreeVO> allMenuTree = menuService.getMenuTree(tenantId, moduleId);

        // 4. 查询已授权的菜单 ID
        List<Long> grantedMenuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        Set<Long> grantedSet = new HashSet<>(grantedMenuIds);

        // 5. 前端过滤搜索
        List<MenuTreeVO> filteredTree = filterTreeByKeyword(allMenuTree, keyword.trim().toLowerCase());

        // 6. 标记每个节点的授权状态
        for (MenuTreeVO node : filteredTree) {
            markCheckedStatus(node, grantedSet);
        }

        // 7. 返回结果
        return R.ok(filteredTree);
    }

    /**
     * 递归标记菜单树的授权状态
     *
     * @param node 菜单节点
     * @param grantedSet 已授权菜单 ID 集合
     */
    private void markCheckedStatus(MenuTreeVO node, Set<Long> grantedSet) {
        if (node == null) {
            return;
        }

        // 设置当前节点的 checked 状态
        Boolean checked = grantedSet.contains(node.getId());
        node.setChecked(checked);

        // 递归处理子节点
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (MenuTreeVO child : node.getChildren()) {
                markCheckedStatus(child, grantedSet);
            }
        }
    }

    /**
     * 递归过滤菜单树，保留匹配关键字的节点
     *
     * @param nodes 菜单树列表
     * @param keyword 搜索关键字（小写）
     * @return 过滤后的菜单树列表
     */
    private List<MenuTreeVO> filterTreeByKeyword(List<MenuTreeVO> nodes, String keyword) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }

        List<MenuTreeVO> result = new ArrayList<>();
        for (MenuTreeVO node : nodes) {
            // 递归过滤子节点
            List<MenuTreeVO> filteredChildren = filterTreeByKeyword(node.getChildren(), keyword);

            // 判断当前节点是否匹配
            boolean isMatch = false;
            if (node.getName() != null && node.getName().toLowerCase().contains(keyword)) {
                isMatch = true;
            }
            if (node.getPermKey() != null && node.getPermKey().toLowerCase().contains(keyword)) {
                isMatch = true;
            }
            if (node.getPath() != null && node.getPath().toLowerCase().contains(keyword)) {
                isMatch = true;
            }

            // 如果当前节点匹配，或者有子节点匹配，则保留该节点
            if (isMatch || !filteredChildren.isEmpty()) {
                MenuTreeVO newNode = new MenuTreeVO();
                // 复制属性
                newNode.setId(node.getId());
                newNode.setParentId(node.getParentId());
                newNode.setName(node.getName());
                newNode.setPath(node.getPath());
                newNode.setComponentKey(node.getComponentKey());
                newNode.setIcon(node.getIcon());
                newNode.setOrderNum(node.getOrderNum());
                newNode.setType(node.getType());
                newNode.setPermKey(node.getPermKey());
                newNode.setStatus(node.getStatus());
                newNode.setMenuLevel(node.getMenuLevel());
                newNode.setMenuMode(node.getMenuMode());
                newNode.setVisible(node.getVisible());
                newNode.setChildren(filteredChildren);
                result.add(newNode);
            }
        }
        return result;
    }
}
