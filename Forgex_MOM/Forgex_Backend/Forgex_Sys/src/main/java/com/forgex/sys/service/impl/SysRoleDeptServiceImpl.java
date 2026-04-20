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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.sys.domain.dto.RoleGrantDTO;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysRoleDept;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.param.RoleGrantQueryDTO;
import com.forgex.sys.domain.vo.RoleGrantVO;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysRoleDeptMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.service.ISysRoleDeptService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色部门关联服务实现
 * <p>
 * 负责角色与部门的授权关系维护，并同步处理部门下用户的角色关联。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-06
 * @see ISysRoleDeptService
 * @see SysRoleDept
 * @see SysDepartmentMapper
 */
@Service
@RequiredArgsConstructor
public class SysRoleDeptServiceImpl extends ServiceImpl<SysRoleDeptMapper, SysRoleDept>
        implements ISysRoleDeptService {

    private final SysRoleDeptMapper roleDeptMapper;
    private final SysRoleMapper roleMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    /** 授权类型：部门 */
    private static final String GRANT_TYPE_DEPARTMENT = "DEPARTMENT";

    @Override
    public Page<RoleGrantVO> getGrantedDepartments(Page<SysRoleDept> page, RoleGrantQueryDTO query) {
        if (query == null || query.getRoleId() == null || query.getTenantId() == null) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        Long roleId = query.getRoleId();
        Long tenantId = query.getTenantId();
        String keyword = query.getKeyword();
        Integer pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        Integer pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new BusinessException(400, "角色不存在或不属于当前租户");
        }

        LambdaQueryWrapper<SysRoleDept> relQw = new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getTenantId, tenantId)
                .eq(SysRoleDept::getRoleId, roleId)
                .orderByDesc(SysRoleDept::getId);

        Page<SysRoleDept> relPage = new Page<>(pageNum, pageSize);
        Page<SysRoleDept> relResult = roleDeptMapper.selectPage(relPage, relQw);
        List<SysRoleDept> relRecords = relResult.getRecords() == null ? List.of() : relResult.getRecords();

        if (relRecords.isEmpty()) {
            Page<RoleGrantVO> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(relResult.getTotal());
            empty.setRecords(List.of());
            return empty;
        }

        List<Long> deptIds = relRecords.stream()
                .map(SysRoleDept::getDeptId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        List<SysDepartment> departments = deptIds.isEmpty() ? List.of() : departmentMapper.selectByIds(deptIds);
        Map<Long, SysDepartment> deptMap = departments == null ? Map.of() : departments.stream()
                .filter(d -> d != null && d.getId() != null)
                .collect(Collectors.toMap(SysDepartment::getId, d -> d, (a, b) -> a));

        List<RoleGrantVO> voList = new ArrayList<>();
        for (SysRoleDept rel : relRecords) {
            SysDepartment dept = deptMap.get(rel.getDeptId());
            if (dept == null) {
                continue;
            }

            RoleGrantVO vo = new RoleGrantVO();
            vo.setId(rel.getId());
            vo.setRoleId(rel.getRoleId());
            vo.setRoleName(role.getRoleName());
            vo.setGrantType(GRANT_TYPE_DEPARTMENT);
            vo.setGrantObjectId(dept.getId());
            vo.setGrantObject(dept.getDeptName());
            vo.setGrantObjectCode(dept.getDeptCode());
            vo.setCreateTime(rel.getCreateTime());
            vo.setCreateBy(rel.getCreateBy());
            voList.add(vo);
        }

        if (StringUtils.isNotBlank(keyword)) {
            String lowerKeyword = keyword.toLowerCase();
            voList = voList.stream()
                    .filter(vo -> {
                        boolean matchName = vo.getGrantObject() != null
                                && vo.getGrantObject().toLowerCase().contains(lowerKeyword);
                        boolean matchCode = vo.getGrantObjectCode() != null
                                && vo.getGrantObjectCode().toLowerCase().contains(lowerKeyword);
                        return matchName || matchCode;
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
    public void grantDepartments(RoleGrantDTO grantDTO) {
        if (grantDTO == null || grantDTO.getRoleId() == null || grantDTO.getTenantId() == null) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }
        if (CollectionUtils.isEmpty(grantDTO.getDepartmentIds())) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        Long roleId = grantDTO.getRoleId();
        Long tenantId = grantDTO.getTenantId();

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new BusinessException(400, "角色不存在或不属于当前租户");
        }

        List<SysDepartment> departments = departmentMapper.selectByIds(grantDTO.getDepartmentIds());
        if (departments == null || departments.size() != grantDTO.getDepartmentIds().size()) {
            throw new BusinessException(400, "部分部门不存在");
        }

        List<SysRoleDept> existsRels = roleDeptMapper.selectList(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getTenantId, tenantId)
                .eq(SysRoleDept::getRoleId, roleId)
                .in(SysRoleDept::getDeptId, grantDTO.getDepartmentIds()));
        Set<Long> existsDeptIds = existsRels == null ? Set.of() : existsRels.stream()
                .map(SysRoleDept::getDeptId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        for (SysDepartment dept : departments) {
            if (existsDeptIds.contains(dept.getId())) {
                continue;
            }

            List<SysUser> deptUsers = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getDepartmentId, dept.getId())
                    .eq(SysUser::getDeleted, false));

            if (CollectionUtils.isEmpty(deptUsers)) {
                continue;
            }

            List<Long> userIds = deptUsers.stream()
                    .map(SysUser::getId)
                    .filter(id -> id != null && id > 0)
                    .toList();

            List<SysUserRole> existsUserRels = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getTenantId, tenantId)
                    .eq(SysUserRole::getRoleId, roleId)
                    .in(SysUserRole::getUserId, userIds));
            Set<Long> existsUserIds = existsUserRels == null ? Set.of() : existsUserRels.stream()
                    .map(SysUserRole::getUserId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());

            for (SysUser user : deptUsers) {
                if (existsUserIds.contains(user.getId())) {
                    continue;
                }
                SysUserRole userRel = new SysUserRole();
                userRel.setTenantId(tenantId);
                userRel.setRoleId(roleId);
                userRel.setUserId(user.getId());
                userRoleMapper.insert(userRel);
            }

            SysRoleDept roleDept = new SysRoleDept();
            roleDept.setTenantId(tenantId);
            roleDept.setRoleId(roleId);
            roleDept.setDeptId(dept.getId());
            roleDeptMapper.insert(roleDept);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeDepartments(RoleGrantDTO revokeDTO) {
        if (revokeDTO == null || revokeDTO.getRoleId() == null || revokeDTO.getTenantId() == null) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }
        if (CollectionUtils.isEmpty(revokeDTO.getDepartmentIds())) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        Long roleId = revokeDTO.getRoleId();
        Long tenantId = revokeDTO.getTenantId();

        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new BusinessException(400, "角色不存在或不属于当前租户");
        }

        List<SysDepartment> departments = departmentMapper.selectByIds(revokeDTO.getDepartmentIds());
        if (departments == null || departments.size() != revokeDTO.getDepartmentIds().size()) {
            throw new BusinessException(400, "部分部门不存在");
        }

        for (SysDepartment dept : departments) {
            List<SysUser> deptUsers = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getDepartmentId, dept.getId())
                    .eq(SysUser::getDeleted, false));

            if (CollectionUtils.isEmpty(deptUsers)) {
                continue;
            }

            List<Long> userIds = deptUsers.stream()
                    .map(SysUser::getId)
                    .filter(id -> id != null && id > 0)
                    .toList();

            if (!userIds.isEmpty()) {
                userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getTenantId, tenantId)
                        .eq(SysUserRole::getRoleId, roleId)
                        .in(SysUserRole::getUserId, userIds));
            }

            roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                    .eq(SysRoleDept::getTenantId, tenantId)
                    .eq(SysRoleDept::getRoleId, roleId)
                    .eq(SysRoleDept::getDeptId, dept.getId()));
        }
    }
}
