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
 * ?????? Controller
 * 
 * ???
 * - ?? HTTP ??
 * - ??????? Validator?
 * - ?? Service ???
 * - ??????
 * 
 * ???
 * - ?????????????????/?? ID ??
 * - ??????????????/????????????
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
     * ????????????????? ID ?????????
     *
     * @param body ????????? roleId
     * @return ?????? ID ??
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/list")
    public R<List<Long>> list(@RequestBody Map<String, Object> body) {
        // 1. ????
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();
        
        // 2. ????
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        
        // 3. ?? Service
        List<Long> menuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        
        // 4. ????
        return R.ok(menuIds);
    }

    /**
     * ???????????RESTful ???
     *
     * @param roleId ?? ID
     * @return ?????? ID ??
     */
    @RequirePerm("sys:role:authMenu")
    @GetMapping
    public R<List<Long>> getRoleMenus(@RequestParam Long roleId) {
        Long tenantId = TenantContext.get();
        // 1. ????
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        
        // 2. ?? Service
        List<Long> menuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        
        // 3. ????
        return R.ok(menuIds);
    }

    /**
     * ????????????????????????? ID?
     *
     * @param body ????????? roleId
     * @return ????????????? ID ???
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/authData")
    public R<Map<String, Object>> getAuthData(@RequestBody Map<String, Object> body) {
        // 1. ????
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();
        
        // 2. ????
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        
        // 3. ?? Service ???????? ID
        List<Long> grantedMenuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        
        // 4. ???????
        List<MenuTreeVO> allMenuTree = menuService.getMenuTree(tenantId, null);
        
        // 5. ?????????
        Set<Long> grantedSet = grantedMenuIds.stream().collect(Collectors.toSet());
        List<MenuTreeVO> ownedMenuTree = buildOwnedTree(allMenuTree, grantedSet);

        // 6. ????
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("grantedMenuIds", grantedMenuIds);
        result.put("allPermissions", allMenuTree);
        result.put("ownedPermissions", ownedMenuTree);
        
        return R.ok(result);
    }

    /**
     * ?????????
     * ????????????????
     */
    private List<MenuTreeVO> buildOwnedTree(List<MenuTreeVO> nodes, Set<Long> grantedSet) {
        List<MenuTreeVO> result = new ArrayList<>();
        if (nodes == null) {
            return result;
        }
        
        for (MenuTreeVO node : nodes) {
            boolean isGranted = grantedSet.contains(node.getId());
            List<MenuTreeVO> children = buildOwnedTree(node.getChildren(), grantedSet);
            
            // ??????????????????????????
            if (isGranted || !children.isEmpty()) {
                MenuTreeVO newNode = new MenuTreeVO();
                // ????
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
     * ???????????????
     *
     * @param body ???????? roleId?menuIds??? ID ???
     * @return ??????
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/grant")
    public R<Boolean> grant(@RequestBody Map<String, Object> body) {
        // 1. ????
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();
        
        // ?? menuIds ????? Integer ? Long ???
        List<Long> menuIds = parseLongList(body.get("menuIds"));
        
        // 2. ?? DTO
        RolePermissionDTO permissionDTO = new RolePermissionDTO();
        permissionDTO.setRoleId(roleId);
        permissionDTO.setTenantId(tenantId);
        permissionDTO.setMenuIds(menuIds);
        
        // 3. ????
        roleMenuValidator.validateGrantParams(permissionDTO);
        
        // 4. ?? Service
        roleMenuService.grantPermission(permissionDTO);
        
        // 5. ????
        return R.ok(CommonPrompt.AUTHORIZE_SUCCESS, true);
    }

    /**
     * ?????????RESTful ???
     *
     * @param permissionDTO ????
     * @return ????
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping
    public R<Void> grantPermission(@RequestBody RolePermissionDTO permissionDTO) {
        permissionDTO.setTenantId(TenantContext.get());
        // 1. ????
        roleMenuValidator.validateGrantParams(permissionDTO);
        
        // 2. ?? Service
        roleMenuService.grantPermission(permissionDTO);
        
        // 3. ????
        return R.ok(CommonPrompt.AUTHORIZE_SUCCESS);
    }
    
    /**
     * ???????????
     *
     * @param roleId ?? ID
     * @return ????
     */
    @RequirePerm("sys:role:authMenu")
    @DeleteMapping
    public R<Void> deletePermissions(@RequestParam Long roleId) {
        Long tenantId = TenantContext.get();
        // 1. ????
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        
        // 2. ?? Service
        roleMenuService.deleteRolePermissions(roleId, tenantId);
        
        // 3. ????
        return R.ok(CommonPrompt.UNAUTHORIZE_SUCCESS);
    }

    /**
     * ?? Long ????
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
     * ?? Long ??????? Integer?Long?Number ????
     *
     * @param obj ?????????? List<Integer>?List<Long> ???
     * @return Long ??????????? null
     */
    @SuppressWarnings("unchecked")
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
                        // ?????????
                    }
                }
            }
            return result;
        }
        return null;
    }

    /**
     * ?????????????????????
     * <p>
     * ????????????????????????????????????
     * </p>
     *
     * @param moduleId ?? ID????
     * @param body ????????? roleId
     * @return ???????????? checked ????????
     * @throws com.forgex.common.exception.BusinessException ?????????
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/authData/module/{moduleId}")
    public R<List<MenuTreeVO>> getModuleAuthData(
            @PathVariable Long moduleId,
            @RequestBody Map<String, Object> body) {
        // 1. ????
        Long roleId = parseLong(body.get("roleId"));
        Long tenantId = TenantContext.get();

        // 2. ????
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        if (moduleId == null || moduleId <= 0) {
            return R.fail(CommonPrompt.BAD_REQUEST, "?? ID ????");
        }

        // 3. ?? Service ??????????
        List<MenuTreeVO> menuTree = menuService.getMenuTree(tenantId, moduleId);

        // 4. ???????? ID
        List<Long> grantedMenuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        Set<Long> grantedSet = new HashSet<>(grantedMenuIds);

        // 5. ???????????
        for (MenuTreeVO node : menuTree) {
            markCheckedStatus(node, grantedSet);
        }

        // 6. ????
        return R.ok(menuTree);
    }

    /**
     * ?????????
     * <p>
     * ?????????????????
     * </p>
     *
     * @param body ??????????
     *             - roleId: ?? ID????
     *             - keyword: ?????????
     *             - moduleId: ?? ID???????????????
     * @return ???????????????? checked ????????
     * @throws com.forgex.common.exception.BusinessException ?????????
     */
    @RequirePerm("sys:role:authMenu")
    @PostMapping("/authData/search")
    public R<List<MenuTreeVO>> searchAuthData(@RequestBody Map<String, Object> body) {
        // 1. ????
        Long roleId = parseLong(body.get("roleId"));
        String keyword = (String) body.get("keyword");
        Long moduleId = parseLong(body.get("moduleId"));
        Long tenantId = TenantContext.get();

        // 2. ????
        roleMenuValidator.validateQueryParams(roleId, tenantId);
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.fail(CommonPrompt.BAD_REQUEST, "?????????");
        }

        // 3. ??????????????????
        List<MenuTreeVO> allMenuTree = menuService.getMenuTree(tenantId, moduleId);

        // 4. ???????? ID
        List<Long> grantedMenuIds = roleMenuService.getRoleMenuIds(roleId, tenantId);
        Set<Long> grantedSet = new HashSet<>(grantedMenuIds);

        // 5. ??????
        List<MenuTreeVO> filteredTree = filterTreeByKeyword(allMenuTree, keyword.trim().toLowerCase());

        // 6. ???????????
        for (MenuTreeVO node : filteredTree) {
            markCheckedStatus(node, grantedSet);
        }

        // 7. ????
        return R.ok(filteredTree);
    }

    /**
     * ????????????
     *
     * @param node ????
     * @param grantedSet ????? ID ??
     */
    private void markCheckedStatus(MenuTreeVO node, Set<Long> grantedSet) {
        if (node == null) {
            return;
        }

        // ??????? checked ??
        Boolean checked = grantedSet.contains(node.getId());
        node.setChecked(checked);

        // ???????
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (MenuTreeVO child : node.getChildren()) {
                markCheckedStatus(child, grantedSet);
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param nodes ?????
     * @param keyword ?????????
     * @return ?????????
     */
    private List<MenuTreeVO> filterTreeByKeyword(List<MenuTreeVO> nodes, String keyword) {
        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }

        List<MenuTreeVO> result = new ArrayList<>();
        for (MenuTreeVO node : nodes) {
            // ???????
            List<MenuTreeVO> filteredChildren = filterTreeByKeyword(node.getChildren(), keyword);

            // ??????????
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

            // ????????????????????????
            if (isMatch || !filteredChildren.isEmpty()) {
                MenuTreeVO newNode = new MenuTreeVO();
                // ????
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
