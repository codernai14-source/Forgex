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
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.RolePermissionDTO;
import com.forgex.sys.domain.vo.MenuTreeVO;
import com.forgex.sys.service.ISysMenuService;
import com.forgex.sys.service.ISysRoleMenuService;
import com.forgex.sys.validator.RoleMenuValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色菜单授权控制器。
 * <p>
 * 负责角色与菜单权限关系的查询、授权、取消授权，以及按模块或关键字加载授权树。
 * 参数校验委托 {@link RoleMenuValidator}，授权关系维护委托 {@link ISysRoleMenuService}。
 * </p>
 *
 * @author coder_nai@163.com
 * @date 2025-01-07
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/role/menu")
@RequiredArgsConstructor
public class SysRoleMenuController {

    private final ISysRoleMenuService roleMenuService;
    private final ISysMenuService menuService;
    private final RoleMenuValidator roleMenuValidator;

    /**
     * 查询角色已授权菜单 ID 列表。
     *
     * @param body 请求体，包含 roleId
     * @return 菜单 ID 列表
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/list")
    public R<List<Long>> list(@RequestBody Map<String, Object> body) {
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();

        roleMenuValidator.validateQueryParams(roleId, tenantId);
        List<Long> menuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        return R.ok(menuIds);
    }

    /**
     * 查询角色已授权菜单 ID 列表。
     *
     * @param roleId 角色 ID
     * @return 菜单 ID 列表
     */
    @RequirePerm("sys:role:authMenu")
    @GetMapping
    public R<List<Long>> getRoleMenus(@RequestParam Long roleId) {
        Long tenantId = TenantContext.get();
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        List<Long> menuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        return R.ok(menuIds);
    }

    /**
     * 获取角色菜单授权数据。
     *
     * @param body 请求体，包含 roleId
     * @return 已授权菜单、全部权限树和已拥有权限树
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/authData")
    public R<Map<String, Object>> getAuthData(@RequestBody Map<String, Object> body) {
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();

        roleMenuValidator.validateQueryParams(roleId, tenantId);

        List<Long> grantedMenuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        List<MenuTreeVO> allMenuTree = menuService.getMenuTree(tenantId, null);

        Set<Long> grantedSet = grantedMenuIds.stream().collect(Collectors.toSet());
        List<MenuTreeVO> ownedMenuTree = buildOwnedTree(allMenuTree, grantedSet);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("grantedMenuIds", grantedMenuIds);
        result.put("allPermissions", allMenuTree);
        result.put("ownedPermissions", ownedMenuTree);

        return R.ok(result);
    }

    /**
     * 构造仅包含已授权节点及其父级链路的菜单树。
     */
    private List<MenuTreeVO> buildOwnedTree(List<MenuTreeVO> nodes, Set<Long> grantedSet) {
        List<MenuTreeVO> result = new ArrayList<>();
        if (nodes == null) {
            return result;
        }

        for (MenuTreeVO node : nodes) {
            boolean isGranted = grantedSet.contains(node.getId());
            List<MenuTreeVO> children = buildOwnedTree(node.getChildren(), grantedSet);

            if (isGranted || !children.isEmpty()) {
                MenuTreeVO newNode = copyMenuNode(node);
                newNode.setChildren(children);
                result.add(newNode);
            }
        }
        return result;
    }

    /**
     * 授权角色菜单。
     *
     * @param body 请求体，包含 roleId 和 menuIds
     * @return 授权结果
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/grant")
    public R<Boolean> grant(@RequestBody Map<String, Object> body) {
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();
        List<Long> menuIds = parseLongList(body.get("menuIds"));

        RolePermissionDTO permissionDTO = new RolePermissionDTO();
        permissionDTO.setRoleId(roleId);
        permissionDTO.setTenantId(tenantId);
        permissionDTO.setMenuIds(menuIds);

        roleMenuValidator.validateGrantParams(permissionDTO);
        roleMenuService.grantPermission(permissionDTO);
        return R.ok(CommonPrompt.AUTHORIZE_SUCCESS, true);
    }

    /**
     * 授权角色权限。
     *
     * @param permissionDTO 角色权限授权参数
     * @return 授权结果
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping
    public R<Void> grantPermission(@RequestBody RolePermissionDTO permissionDTO) {
        permissionDTO.setTenantId(TenantContext.get());
        roleMenuValidator.validateGrantParams(permissionDTO);
        roleMenuService.grantPermission(permissionDTO);
        return R.ok(CommonPrompt.AUTHORIZE_SUCCESS);
    }

    /**
     * 删除角色菜单授权。
     *
     * @param roleId 角色 ID
     * @return 删除结果
     */
    @RequirePerm("sys:role:authMenu")
    @DeleteMapping
    public R<Void> deletePermissions(@RequestParam Long roleId) {
        Long tenantId = TenantContext.get();
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        roleMenuService.deleteRolePermissions(roleId, tenantId);
        return R.ok(CommonPrompt.UNAUTHORIZE_SUCCESS);
    }

    /**
     * 转换为 Long。
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
     * 将列表参数转换为 Long 列表。
     */
    private List<Long> parseLongList(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof List) {
            List<?> rawList = (List<?>) obj;
            List<Long> result = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof Number) {
                    result.add(((Number) item).longValue());
                } else if (item instanceof String) {
                    try {
                        result.add(Long.parseLong((String) item));
                    } catch (NumberFormatException e) {
                        // 忽略非法 ID。
                    }
                }
            }
            return result;
        }
        return null;
    }

    /**
     * 按模块获取菜单授权树。
     *
     * @param moduleId 模块 ID
     * @param body 请求体，包含 roleId
     * @return 标记 checked 状态的菜单树
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/authData/module/{moduleId}")
    public R<List<MenuTreeVO>> getModuleAuthData(
            @PathVariable Long moduleId,
            @RequestBody Map<String, Object> body) {
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();

        roleMenuValidator.validateQueryParams(roleId, tenantId);
        if (moduleId == null || moduleId <= 0) {
            return R.fail(CommonPrompt.BAD_REQUEST, "模块 ID 不合法");
        }

        List<MenuTreeVO> menuTree = menuService.getMenuTree(tenantId, moduleId);

        List<Long> grantedMenuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        Set<Long> grantedSet = new HashSet<>(grantedMenuIds);

        for (MenuTreeVO node : menuTree) {
            markCheckedStatus(node, grantedSet);
        }

        return R.ok(menuTree);
    }

    /**
     * 搜索角色菜单授权数据。
     *
     * @param body 请求体，包含 roleId、keyword 和可选 moduleId
     * @return 匹配关键字且标记 checked 状态的菜单树
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/authData/search")
    public R<List<MenuTreeVO>> searchAuthData(@RequestBody Map<String, Object> body) {
        Long roleId = parseLong(body.get("roleId"));
        String keyword = (String) body.get("keyword");
        Long moduleId = parseLong(body.get("moduleId"));
        Long tenantId = TenantContext.get();

        roleMenuValidator.validateQueryParams(roleId, tenantId);
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.fail(CommonPrompt.BAD_REQUEST, "搜索关键字不能为空");
        }

        List<MenuTreeVO> allMenuTree = menuService.getMenuTree(tenantId, moduleId);

        List<Long> grantedMenuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        Set<Long> grantedSet = new HashSet<>(grantedMenuIds);

        List<MenuTreeVO> filteredTree = filterTreeByKeyword(allMenuTree, keyword.trim().toLowerCase());

        for (MenuTreeVO node : filteredTree) {
            markCheckedStatus(node, grantedSet);
        }

        return R.ok(filteredTree);
    }

    /**
     * 递归标记菜单节点是否已授权。
     */
    private void markCheckedStatus(MenuTreeVO node, Set<Long> grantedSet) {
        if (node == null) {
            return;
        }

        Boolean checked = grantedSet.contains(node.getId());
        node.setChecked(checked);

        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (MenuTreeVO child : node.getChildren()) {
                markCheckedStatus(child, grantedSet);
            }
        }
    }

    /**
     * 按关键字过滤菜单树。
     *
     * @param nodes 菜单节点
     * @param keyword 小写关键字
     * @return 过滤后的菜单树
     */
    private List<MenuTreeVO> filterTreeByKeyword(List<MenuTreeVO> nodes, String keyword) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }

        List<MenuTreeVO> result = new ArrayList<>();
        for (MenuTreeVO node : nodes) {
            List<MenuTreeVO> filteredChildren = filterTreeByKeyword(node.getChildren(), keyword);

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

            if (isMatch || !filteredChildren.isEmpty()) {
                MenuTreeVO newNode = copyMenuNode(node);
                newNode.setMenuLevel(node.getMenuLevel());
                newNode.setMenuMode(node.getMenuMode());
                newNode.setVisible(node.getVisible());
                newNode.setChildren(filteredChildren);
                result.add(newNode);
            }
        }
        return result;
    }

    private MenuTreeVO copyMenuNode(MenuTreeVO node) {
        MenuTreeVO newNode = new MenuTreeVO();
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
        return newNode;
    }
}
