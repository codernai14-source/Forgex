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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.validator.RoleValidator;
import com.forgex.sys.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色授权用户 Controller。
 * <p>
 * 提供角色与用户绑定关系的查询、授权与取消授权接口，
 * 对应前端“角色管理-授权人员”功能。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see com.forgex.sys.controller.SysRoleController
 * @see com.forgex.sys.controller.UserController
 */
@RestController
@RequestMapping("/sys/role/user")
@RequiredArgsConstructor
public class SysRoleUserController {

    /**
     * 用户-角色绑定 Mapper。
     */
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 角色 Mapper。
     */
    private final SysRoleMapper roleMapper;

    /**
     * 用户 Mapper。
     */
    private final SysUserMapper userMapper;

    /**
     * 部门 Mapper。
     */
    private final SysDepartmentMapper departmentMapper;

    /**
     * 职位 Mapper。
     */
    private final SysPositionMapper positionMapper;

    /**
     * 角色校验器。
     */
    private final RoleValidator roleValidator;

    /**
     * 用户校验器。
     */
    private final UserValidator userValidator;

    /**
     * 查询角色已授权用户列表（分页）。
     *
     * @param body 请求体（roleId/tenantId/pageNum/pageSize）
     * @return 已授权用户分页结果
     * @throws com.forgex.common.exception.BusinessException 参数校验失败时抛出
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/list")
    public R<Page<SysUserDTO>> list(@RequestBody Map<String, Object> body) {
        // 1. 解析参数
        Long roleId = parseLong(body == null ? null : body.get("roleId"));
        Long tenantId = getTenantId(body);
        long pageNum = parseLong(body == null ? null : body.get("pageNum")) == null ? 1L : parseLong(body.get("pageNum"));
        long pageSize = parseLong(body == null ? null : body.get("pageSize")) == null ? 20L : parseLong(body.get("pageSize"));

        // 2. 参数校验
        roleValidator.validateId(roleId);
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        if (pageNum <= 0 || pageSize <= 0) {
            return R.fail(CommonPrompt.BAD_REQUEST, "分页参数不正确");
        }

        // 3. 校验角色存在且属于当前租户
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            return R.fail(CommonPrompt.BAD_REQUEST, "角色不存在或不属于当前租户");
        }

        // 4. 先分页查询绑定关系，再批量查询用户
        Page<SysUserRole> relPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUserRole> relQw = new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, tenantId)
                .eq(SysUserRole::getRoleId, roleId)
                .orderByDesc(SysUserRole::getId);
        Page<SysUserRole> relResult = userRoleMapper.selectPage(relPage, relQw);

        List<SysUserRole> relRecords = relResult.getRecords() == null ? List.of() : relResult.getRecords();
        if (relRecords.isEmpty()) {
            Page<SysUserDTO> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(relResult.getTotal());
            empty.setRecords(List.of());
            return R.ok(empty);
        }

        List<Long> userIds = relRecords.stream()
                .map(SysUserRole::getUserId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        if (userIds.isEmpty()) {
            Page<SysUserDTO> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(relResult.getTotal());
            empty.setRecords(List.of());
            return R.ok(empty);
        }

        List<SysUser> users = userMapper.selectByIds(userIds);
        Map<Long, SysUser> userMap = users == null ? Map.of() : users.stream()
                .filter(u -> u != null && u.getId() != null)
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        // 5. 批量查询部门/职位名称并组装DTO
        Map<Long, String> deptNameMap = buildDepartmentNameMap(userMap.values());
        Map<Long, String> positionNameMap = buildPositionNameMap(userMap.values());

        List<SysUserDTO> dtoList = new ArrayList<>();
        for (SysUserRole rel : relRecords) {
            SysUser user = userMap.get(rel.getUserId());
            if (user == null) {
                continue;
            }

            SysUserDTO dto = new SysUserDTO();
            dto.setId(user.getId());
            dto.setAccount(user.getAccount());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setAvatar(user.getAvatar());
            dto.setDepartmentId(user.getDepartmentId());
            dto.setPositionId(user.getPositionId());
            dto.setStatus(user.getStatus());
            dto.setTenantId(user.getTenantId());

            if (user.getDepartmentId() != null) {
                dto.setDepartmentName(deptNameMap.get(user.getDepartmentId()));
            }
            if (user.getPositionId() != null) {
                dto.setPositionName(positionNameMap.get(user.getPositionId()));
            }

            dtoList.add(dto);
        }

        Page<SysUserDTO> out = new Page<>(pageNum, pageSize);
        out.setTotal(relResult.getTotal());
        out.setRecords(dtoList);
        return R.ok(out);
    }

    /**
     * 授予角色用户授权。
     *
     * @param body 请求体（roleId/tenantId/userIds）
     * @return 操作结果
     * @throws com.forgex.common.exception.BusinessException 参数校验失败时抛出
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/grant")
    public R<Void> grant(@RequestBody Map<String, Object> body) {
        // 1. 解析参数
        Long roleId = parseLong(body == null ? null : body.get("roleId"));
        Long tenantId = getTenantId(body);
        List<Long> userIds = parseLongList(body == null ? null : body.get("userIds"));

        // 2. 参数校验
        roleValidator.validateId(roleId);
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        if (userIds == null || userIds.isEmpty()) {
            return R.fail(CommonPrompt.BAD_REQUEST, "用户列表不能为空");
        }
        for (Long uid : userIds) {
            userValidator.validateId(uid);
        }

        // 3. 校验角色存在且属于当前租户
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            return R.fail(CommonPrompt.BAD_REQUEST, "角色不存在或不属于当前租户");
        }

        // 4. 查询已存在的绑定关系，避免重复插入
        List<SysUserRole> exists = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, tenantId)
                .eq(SysUserRole::getRoleId, roleId)
                .in(SysUserRole::getUserId, userIds));
        Set<Long> existsUserIds = exists == null ? Set.of() : exists.stream()
                .map(SysUserRole::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 5. 批量插入缺失的绑定关系
        for (Long uid : userIds) {
            if (existsUserIds.contains(uid)) {
                continue;
            }
            SysUserRole rel = new SysUserRole();
            rel.setTenantId(tenantId);
            rel.setRoleId(roleId);
            rel.setUserId(uid);
            userRoleMapper.insert(rel);
        }

        return R.ok(CommonPrompt.AUTHORIZE_SUCCESS);
    }

    /**
     * 取消角色用户授权。
     *
     * @param body 请求体（roleId/tenantId/userIds）
     * @return 操作结果
     * @throws com.forgex.common.exception.BusinessException 参数校验失败时抛出
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/revoke")
    public R<Void> revoke(@RequestBody Map<String, Object> body) {
        // 1. 解析参数
        Long roleId = parseLong(body == null ? null : body.get("roleId"));
        Long tenantId = getTenantId(body);
        List<Long> userIds = parseLongList(body == null ? null : body.get("userIds"));

        // 2. 参数校验
        roleValidator.validateId(roleId);
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        if (userIds == null || userIds.isEmpty()) {
            return R.fail(CommonPrompt.BAD_REQUEST, "用户列表不能为空");
        }
        for (Long uid : userIds) {
            userValidator.validateId(uid);
        }

        // 3. 删除绑定关系
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, tenantId)
                .eq(SysUserRole::getRoleId, roleId)
                .in(SysUserRole::getUserId, userIds));

        return R.ok(CommonPrompt.UNAUTHORIZE_SUCCESS);
    }

    /**
     * 解析 Long 类型参数。
     *
     * @param obj 入参对象
     * @return Long 值，解析失败返回 null
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
                String s = (String) obj;
                if (!StringUtils.hasText(s)) {
                    return null;
                }
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 解析 Long 列表参数。
     *
     * @param obj 入参对象（List/数组等）
     * @return Long 列表，无法解析返回空列表
     */
    @SuppressWarnings("unchecked")
    private List<Long> parseLongList(Object obj) {
        if (obj == null) {
            return List.of();
        }
        if (obj instanceof List) {
            List<Object> raw = (List<Object>) obj;
            return raw.stream()
                    .map(this::parseLong)
                    .filter(v -> v != null && v > 0)
                    .distinct()
                    .toList();
        }
        return List.of();
    }

    /**
     * 获取租户ID（优先线程上下文，其次请求参数，其次会话）。
     *
     * @param body 请求体
     * @return 租户ID，获取失败返回 null
     */
    private Long getTenantId(Map<String, Object> body) {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            return tenantId;
        }
        tenantId = parseLong(body == null ? null : body.get("tenantId"));
        if (tenantId != null) {
            return tenantId;
        }
        return CurrentUserUtils.getTenantId();
    }

    /**
     * 批量构建部门ID到部门名称的映射。
     *
     * @param users 用户集合
     * @return 部门名称映射
     */
    private Map<Long, String> buildDepartmentNameMap(Iterable<SysUser> users) {
        Set<Long> deptIds = new HashSet<>();
        for (SysUser user : users) {
            if (user != null && user.getDepartmentId() != null) {
                deptIds.add(user.getDepartmentId());
            }
        }
        if (deptIds.isEmpty()) {
            return Map.of();
        }

        List<SysDepartment> departments = departmentMapper.selectByIds(deptIds);
        if (departments == null || departments.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> map = new HashMap<>();
        for (SysDepartment dept : departments) {
            if (dept != null && dept.getId() != null) {
                map.put(dept.getId(), dept.getDeptName());
            }
        }
        return map;
    }

    /**
     * 批量构建职位ID到职位名称的映射。
     *
     * @param users 用户集合
     * @return 职位名称映射
     */
    private Map<Long, String> buildPositionNameMap(Iterable<SysUser> users) {
        Set<Long> positionIds = new HashSet<>();
        for (SysUser user : users) {
            if (user != null && user.getPositionId() != null) {
                positionIds.add(user.getPositionId());
            }
        }
        if (positionIds.isEmpty()) {
            return Map.of();
        }

        List<SysPosition> positions = positionMapper.selectByIds(positionIds);
        if (positions == null || positions.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> map = new HashMap<>();
        for (SysPosition position : positions) {
            if (position != null && position.getId() != null) {
                map.put(position.getId(), position.getPositionName());
            }
        }
        return map;
    }
}
