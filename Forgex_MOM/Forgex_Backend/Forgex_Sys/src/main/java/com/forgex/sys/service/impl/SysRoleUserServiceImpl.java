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
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.RoleGrantDTO;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.param.RoleGrantQueryDTO;
import com.forgex.sys.domain.vo.RoleGrantVO;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.service.ISysRoleUserService;
import com.forgex.sys.enums.SysPromptEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户角色关联服务实现
 * <p>
 * 负责查询角色已授权用户，以及按用户、部门、岗位维度批量授权和批量取消授权。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2026-04-06
 * @see ISysRoleUserService
 * @see SysUserRole
 */
@Service
@RequiredArgsConstructor
public class SysRoleUserServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
        implements ISysRoleUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysPositionMapper positionMapper;
    private final com.forgex.sys.service.ISysRoleDeptService roleDeptService;
    private final com.forgex.sys.service.ISysRolePositionService rolePositionService;

    /** 授权类型：用户 */
    private static final String GRANT_TYPE_USER = "USER";

    /** 授权类型：部门 */
    private static final String GRANT_TYPE_DEPARTMENT = "DEPARTMENT";

    /** 授权类型：岗位 */
    private static final String GRANT_TYPE_POSITION = "POSITION";

    @Override
    public Page<RoleGrantVO> getGrantedList(RoleGrantQueryDTO query) {
        if (query == null || query.getRoleId() == null || query.getTenantId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "");
        }

        Long roleId = query.getRoleId();
        Long tenantId = query.getTenantId();
        String grantType = query.getGrantType();
        String keyword = query.getKeyword();
        Integer pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        Integer pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_INVALID_OR_CROSS_TENANT_SINGLE);
        }

        Page<SysUserRole> relPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUserRole> relQw = new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, tenantId)
                .eq(SysUserRole::getRoleId, roleId)
                .orderByDesc(SysUserRole::getId);

        Page<SysUserRole> relResult = userRoleMapper.selectPage(relPage, relQw);
        List<SysUserRole> relRecords = relResult.getRecords() == null ? List.of() : relResult.getRecords();

        if (relRecords.isEmpty()) {
            Page<RoleGrantVO> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(relResult.getTotal());
            empty.setRecords(List.of());
            return empty;
        }

        List<Long> userIds = relRecords.stream()
                .map(SysUserRole::getUserId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        List<SysUser> users = userIds.isEmpty() ? List.of() : userMapper.selectByIds(userIds);
        Map<Long, SysUser> userMap = users == null ? Map.of() : users.stream()
                .filter(u -> u != null && u.getId() != null)
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        Map<Long, String> deptNameMap = buildDepartmentNameMap(userMap.values());
        Map<Long, String> positionNameMap = buildPositionNameMap(userMap.values());

        List<RoleGrantVO> voList = new ArrayList<>();
        for (SysUserRole rel : relRecords) {
            SysUser user = userMap.get(rel.getUserId());
            if (user == null) {
                continue;
            }

            RoleGrantVO vo = new RoleGrantVO();
            vo.setId(rel.getId());
            vo.setRoleId(rel.getRoleId());
            vo.setRoleName(role.getRoleName());
            vo.setGrantType(GRANT_TYPE_USER);
            vo.setGrantObjectId(user.getId());
            vo.setGrantObject(user.getUsername());
            vo.setGrantObjectCode(user.getAccount());

            if (user.getDepartmentId() != null) {
                vo.setGrantObject(deptNameMap.getOrDefault(user.getDepartmentId(), user.getUsername()));
            }
            if (user.getPositionId() != null) {
                vo.setGrantObject(positionNameMap.getOrDefault(user.getPositionId(), user.getUsername()));
            }

            voList.add(vo);
        }

        if (StringUtils.isNotBlank(grantType) || StringUtils.isNotBlank(keyword)) {
            voList = voList.stream()
                    .filter(vo -> {
                        if (StringUtils.isNotBlank(grantType) && !grantType.equals(vo.getGrantType())) {
                            return false;
                        }
                        if (StringUtils.isNotBlank(keyword)) {
                            String lowerKeyword = keyword.toLowerCase();
                            boolean matchName = vo.getGrantObject() != null
                                    && vo.getGrantObject().toLowerCase().contains(lowerKeyword);
                            boolean matchCode = vo.getGrantObjectCode() != null
                                    && vo.getGrantObjectCode().toLowerCase().contains(lowerKeyword);
                            return matchName || matchCode;
                        }
                        return true;
                    })
                    .toList();
        }

        long total = voList.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, voList.size());
        if (fromIndex >= voList.size()) {
            Page<RoleGrantVO> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(total);
            empty.setRecords(List.of());
            return empty;
        }
        List<RoleGrantVO> pagedList = voList.subList(fromIndex, toIndex);

        Page<RoleGrantVO> result = new Page<>(pageNum, pageSize);
        result.setTotal(total);
        result.setRecords(pagedList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantBatch(RoleGrantDTO grantDTO) {
        if (grantDTO == null || grantDTO.getRoleId() == null || grantDTO.getTenantId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "");
        }
        if (StringUtils.isBlank(grantDTO.getGrantType())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "");
        }

        Long roleId = grantDTO.getRoleId();
        Long tenantId = grantDTO.getTenantId();
        String grantType = grantDTO.getGrantType();

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_INVALID_OR_CROSS_TENANT_SINGLE);
        }

        if (GRANT_TYPE_USER.equals(grantType)) {
            grantUsers(grantDTO);
        } else if (GRANT_TYPE_DEPARTMENT.equals(grantType)) {
            grantDepartments(grantDTO);
        } else if (GRANT_TYPE_POSITION.equals(grantType)) {
            grantPositions(grantDTO);
        } else {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_GRANT_TYPE_NOT_SUPPORTED, grantType);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeBatch(RoleGrantDTO revokeDTO) {
        if (revokeDTO == null || revokeDTO.getRoleId() == null || revokeDTO.getTenantId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "");
        }
        if (StringUtils.isBlank(revokeDTO.getGrantType())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "");
        }

        Long roleId = revokeDTO.getRoleId();
        Long tenantId = revokeDTO.getTenantId();
        String grantType = revokeDTO.getGrantType();

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_INVALID_OR_CROSS_TENANT_SINGLE);
        }

        if (GRANT_TYPE_USER.equals(grantType)) {
            revokeUsers(revokeDTO);
        } else if (GRANT_TYPE_DEPARTMENT.equals(grantType)) {
            revokeDepartments(revokeDTO);
        } else if (GRANT_TYPE_POSITION.equals(grantType)) {
            revokePositions(revokeDTO);
        } else {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_GRANT_TYPE_NOT_SUPPORTED, grantType);
        }
    }

    /**
     * 授予用户权限
     *
     * @param grantDTO 授权参数
     */
    private void grantUsers(RoleGrantDTO grantDTO) {
        if (CollectionUtils.isEmpty(grantDTO.getUserIds())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "");
        }

        List<SysUser> users = userMapper.selectByIds(grantDTO.getUserIds());
        if (users == null || users.size() != grantDTO.getUserIds().size()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.ROLE_GRANT_PART_USERS_NOT_FOUND);
        }

        List<SysUserRole> exists = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, grantDTO.getTenantId())
                .eq(SysUserRole::getRoleId, grantDTO.getRoleId())
                .in(SysUserRole::getUserId, grantDTO.getUserIds()));
        Set<Long> existsUserIds = exists == null ? Set.of() : exists.stream()
                .map(SysUserRole::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        for (Long userId : grantDTO.getUserIds()) {
            if (existsUserIds.contains(userId)) {
                continue;
            }
            SysUserRole rel = new SysUserRole();
            rel.setTenantId(grantDTO.getTenantId());
            rel.setRoleId(grantDTO.getRoleId());
            rel.setUserId(userId);
            userRoleMapper.insert(rel);
        }
    }

    /**
     * 授予部门权限。
     *
     * @param grantDTO 授权参数
     */
    private void grantDepartments(RoleGrantDTO grantDTO) {
        roleDeptService.grantDepartments(grantDTO);
    }

    /**
     * 授予岗位权限。
     *
     * @param grantDTO 授权参数
     */
    private void grantPositions(RoleGrantDTO grantDTO) {
        rolePositionService.grantPositions(grantDTO);
    }

    /**
     * 取消用户授权
     *
     * @param revokeDTO 取消授权参数
     */
    private void revokeUsers(RoleGrantDTO revokeDTO) {
        if (CollectionUtils.isEmpty(revokeDTO.getUserIds())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "");
        }

        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, revokeDTO.getTenantId())
                .eq(SysUserRole::getRoleId, revokeDTO.getRoleId())
                .in(SysUserRole::getUserId, revokeDTO.getUserIds()));
    }

    /**
     * 取消部门授权。
     *
     * @param revokeDTO 取消授权参数
     */
    private void revokeDepartments(RoleGrantDTO revokeDTO) {
        roleDeptService.revokeDepartments(revokeDTO);
    }

    /**
     * 取消岗位授权。
     *
     * @param revokeDTO 取消授权参数
     */
    private void revokePositions(RoleGrantDTO revokeDTO) {
        rolePositionService.revokePositions(revokeDTO);
    }

    /**
     * 批量构建部门ID到名称的映射。
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
     * 批量构建岗位ID到名称的映射。
     *
     * @param users 用户集合
     * @return 岗位名称映射
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
