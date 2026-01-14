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
import com.forgex.common.exception.BusinessException;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.entity.SysUserTenant;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.mapper.SysUserTenantMapper;
import com.forgex.sys.service.ISysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户-角色分配服务实现。
 * <p>
 * 说明：
 * <ul>
 *   <li>用户与角色绑定表：{@code sys_user_role}（含租户维度 tenant_id）</li>
 *   <li>本实现仅操作当前租户范围内的绑定关系</li>
 * </ul>
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0
 * @see com.forgex.sys.domain.entity.SysUserRole
 * @see com.forgex.sys.mapper.SysUserRoleMapper
 */
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl implements ISysUserRoleService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserTenantMapper userTenantMapper;

    /**
     * 查询用户在指定租户下的已分配角色ID列表。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 角色ID列表
     * @throws BusinessException 参数非法、用户不存在或用户未绑定该租户时抛出
     */
    @Override
    public List<Long> listAssignedRoleIds(Long userId, Long tenantId) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(tenantId, "租户ID不能为空");
        validateUserAndTenant(userId, tenantId);

        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getTenantId, tenantId);
        List<SysUserRole> list = userRoleMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    /**
     * 保存用户在指定租户下的角色分配结果。
     * <p>采用“先删后插”的方式保证结果与提交一致。</p>
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param roleIds  角色ID列表（为空表示清空）
     * @throws BusinessException 用户不存在、用户未绑定租户、角色不存在或跨租户时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserRoles(Long userId, Long tenantId, List<Long> roleIds) {
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(tenantId, "租户ID不能为空");
        validateUserAndTenant(userId, tenantId);

        Set<Long> distinctRoleIds = toDistinctRoleIds(roleIds);
        validateRolesInTenant(distinctRoleIds, tenantId);

        LambdaQueryWrapper<SysUserRole> del = new LambdaQueryWrapper<>();
        del.eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getTenantId, tenantId);
        userRoleMapper.delete(del);
        if (distinctRoleIds.isEmpty()) {
            return;
        }

        for (Long roleId : distinctRoleIds) {
            SysUserRole bind = new SysUserRole();
            bind.setUserId(userId);
            bind.setRoleId(roleId);
            bind.setTenantId(tenantId);
            userRoleMapper.insert(bind);
        }
    }

    /**
     * 校验用户是否存在，以及用户是否已绑定到指定租户。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @throws BusinessException 不满足条件时抛出
     */
    private void validateUserAndTenant(Long userId, Long tenantId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        LambdaQueryWrapper<SysUserTenant> tenantBindWrapper = new LambdaQueryWrapper<>();
        tenantBindWrapper.eq(SysUserTenant::getUserId, userId)
                .eq(SysUserTenant::getTenantId, tenantId)
                .last("limit 1");
        SysUserTenant bind = userTenantMapper.selectOne(tenantBindWrapper);
        if (bind == null) {
            throw new BusinessException("用户未绑定该租户，无法分配角色");
        }
    }

    /**
     * 将角色ID列表去重并保持原顺序。
     *
     * @param roleIds 角色ID列表
     * @return 去重后的角色ID集合
     */
    private Set<Long> toDistinctRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new LinkedHashSet<>();
        }

        Set<Long> distinct = new LinkedHashSet<>();
        for (Long roleId : roleIds) {
            if (roleId == null) {
                continue;
            }
            if (roleId <= 0) {
                throw new BusinessException("角色ID格式不正确");
            }
            distinct.add(roleId);
        }
        return distinct;
    }

    /**
     * 校验角色是否存在且属于指定租户。
     *
     * @param roleIds  角色ID集合
     * @param tenantId 租户ID
     * @throws BusinessException 存在无效/跨租户角色时抛出
     */
    private void validateRolesInTenant(Set<Long> roleIds, Long tenantId) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.in(SysRole::getId, roleIds)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false);

        Long validCount = roleMapper.selectCount(roleWrapper);
        if (validCount == null || validCount.intValue() != roleIds.size()) {
            throw new BusinessException("存在无效角色或跨租户角色");
        }
    }
}
