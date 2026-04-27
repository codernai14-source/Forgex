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
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.RoleGrantDTO;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.param.RoleGrantQueryDTO;
import com.forgex.sys.domain.param.RoleUserIdsParam;
import com.forgex.sys.domain.param.RoleUserListParam;
import com.forgex.sys.domain.vo.RoleGrantVO;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.service.ISysRoleUserService;
import com.forgex.sys.util.RoleGrantWebHelper;
import com.forgex.sys.validator.RoleValidator;
import com.forgex.sys.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
 * 对应前端「角色管理 - 授权人员」功能。
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
     * 角色-用户关联 Service。
     */
    private final ISysRoleUserService roleUserService;

    /**
     * 查询角色已授权用户列表（分页）。
     *
     * @param param 查询参数（角色 ID、租户 ID、分页）
     * @return 已授权用户分页结果，{@code data} 为 {@link Page}{@code <}{@link SysUserDTO}{@code >}
     * @throws com.forgex.common.exception.I18nBusinessException 参数校验失败时抛出
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/list")
    public R<Page<SysUserDTO>> list(@RequestBody RoleUserListParam param) {
        RoleGrantWebHelper.fillTenantId(param);

        Long roleId = param.getRoleId();
        Long tenantId = param.getTenantId();
        long pageNum = param.getPageNum() == null ? 1L : param.getPageNum();
        long pageSize = param.getPageSize() == null ? 20L : param.getPageSize();

        // 参数校验
        roleValidator.validateId(roleId);
        if (tenantId == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }
        if (pageNum <= 0 || pageSize <= 0) {
            return R.fail(CommonPrompt.BAD_REQUEST, "分页参数不正确");
        }

        // 校验角色存在且属于当前租户
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            return R.fail(CommonPrompt.BAD_REQUEST, "角色不存在或不属于当前租户");
        }

        // 先分页查询绑定关系，再批量查询用户
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
     * @param param 请求参数（角色 ID、租户 ID、用户 ID 列表）
     * @return 操作结果
     * @throws com.forgex.common.exception.I18nBusinessException 参数校验失败时抛出
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/grant")
    public R<Void> grant(@RequestBody RoleUserIdsParam param) {
        RoleGrantWebHelper.fillTenantId(param);

        Long roleId = param.getRoleId();
        Long tenantId = param.getTenantId();
        List<Long> userIds = param.getUserIds();

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

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            return R.fail(CommonPrompt.BAD_REQUEST, "角色不存在或不属于当前租户");
        }

        List<SysUserRole> exists = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, tenantId)
                .eq(SysUserRole::getRoleId, roleId)
                .in(SysUserRole::getUserId, userIds));
        Set<Long> existsUserIds = exists == null ? Set.of() : exists.stream()
                .map(SysUserRole::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

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
     * @param param 请求参数（角色 ID、租户 ID、用户 ID 列表）
     * @return 操作结果
     * @throws com.forgex.common.exception.I18nBusinessException 参数校验失败时抛出
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/revoke")
    public R<Void> revoke(@RequestBody RoleUserIdsParam param) {
        RoleGrantWebHelper.fillTenantId(param);

        Long roleId = param.getRoleId();
        Long tenantId = param.getTenantId();
        List<Long> userIds = param.getUserIds();

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

        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, tenantId)
                .eq(SysUserRole::getRoleId, roleId)
                .in(SysUserRole::getUserId, userIds));

        return R.ok(CommonPrompt.UNAUTHORIZE_SUCCESS);
    }

    /**
     * 批量构建部门 ID 到部门名称的映射。
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
     * 批量构建职位 ID 到职位名称的映射。
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

    /**
     * 获取角色已授权列表（支持用户、部门、职位）。
     * <p>
     * 分页查询角色已授权的用户、部门或职位信息。
     * </p>
     *
     * @param query 查询参数（角色、租户、授权类型、关键字、分页）
     * @return 已授权列表分页结果，{@code data} 为 {@link Page}{@code <}{@link RoleGrantVO}{@code >}
     * @throws com.forgex.common.exception.I18nBusinessException 参数校验失败或角色不存在时抛出
     * @see RoleGrantQueryDTO
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/granted/list")
    public R<Page<RoleGrantVO>> getGrantedList(@RequestBody RoleGrantQueryDTO query) {
        RoleGrantWebHelper.fillTenantId(query);
        RoleGrantWebHelper.normalizePage(query);

        Page<RoleGrantVO> result = roleUserService.getGrantedList(query);

        return R.ok(result);
    }

    /**
     * 批量授权（支持用户、部门、职位）。
     * <p>
     * 根据授权类型批量为角色授予用户、部门或职位权限（幂等）。
     * </p>
     *
     * @param grantDTO 授权参数
     * @return 操作结果
     * @throws com.forgex.common.exception.I18nBusinessException 参数校验失败、角色不存在或授权对象不存在时抛出
     * @see RoleGrantDTO
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/grant/batch")
    public R<Void> grantBatch(@RequestBody RoleGrantDTO grantDTO) {
        RoleGrantWebHelper.fillTenantId(grantDTO);
        if (grantDTO.getTenantId() == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }

        roleUserService.grantBatch(grantDTO);

        return R.ok(CommonPrompt.AUTHORIZE_SUCCESS);
    }

    /**
     * 批量取消授权（支持用户、部门、职位）。
     * <p>
     * 根据授权类型批量取消角色的用户、部门或职位权限。
     * </p>
     *
     * @param revokeDTO 取消授权参数
     * @return 操作结果
     * @throws com.forgex.common.exception.I18nBusinessException 参数校验失败或角色不存在时抛出
     * @see RoleGrantDTO
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/revoke/batch")
    public R<Void> revokeBatch(@RequestBody RoleGrantDTO revokeDTO) {
        RoleGrantWebHelper.fillTenantId(revokeDTO);
        if (revokeDTO.getTenantId() == null) {
            return R.fail(CommonPrompt.TENANT_ID_EMPTY);
        }

        roleUserService.revokeBatch(revokeDTO);

        return R.ok(CommonPrompt.UNAUTHORIZE_SUCCESS);
    }
}
