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
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysRolePosition;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.param.RoleGrantQueryDTO;
import com.forgex.sys.domain.vo.RoleGrantVO;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysRolePositionMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.service.ISysRolePositionService;
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
 * 角色职位关联服务实现类
 * <p>
 * 实现角色与职位授权关系的管理功能，包括查询已授权职位列表、批量授权职位、批量取消授权职位等。
 * 职位授权通过查询职位下的所有用户，自动为这些用户建立角色 - 用户关联关系。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-06
 * @see ISysRolePositionService
 * @see SysRolePosition
 * @see SysPositionMapper
 */
@Service
@RequiredArgsConstructor
public class SysRolePositionServiceImpl extends ServiceImpl<SysRolePositionMapper, SysRolePosition>
    implements ISysRolePositionService {

    private final SysRolePositionMapper rolePositionMapper;
    private final SysRoleMapper roleMapper;
    private final SysPositionMapper positionMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 授权类型常量：POSITION=职位
     */
    private static final String GRANT_TYPE_POSITION = "POSITION";

    @Override
    public Page<RoleGrantVO> getGrantedPositions(Page<SysRolePosition> page, RoleGrantQueryDTO query) {
        // 1. 参数校验
        if (query == null || query.getRoleId() == null || query.getTenantId() == null) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        Long roleId = query.getRoleId();
        Long tenantId = query.getTenantId();
        String keyword = query.getKeyword();
        Integer pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        Integer pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        // 2. 校验角色存在且属于当前租户
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        // 3. 分页查询角色 - 职位关联关系
        LambdaQueryWrapper<SysRolePosition> relQw = new LambdaQueryWrapper<SysRolePosition>()
                .eq(SysRolePosition::getTenantId, tenantId)
                .eq(SysRolePosition::getRoleId, roleId)
                .orderByDesc(SysRolePosition::getId);

        Page<SysRolePosition> relPage = new Page<>(pageNum, pageSize);
        Page<SysRolePosition> relResult = rolePositionMapper.selectPage(relPage, relQw);
        List<SysRolePosition> relRecords = relResult.getRecords() == null ? List.of() : relResult.getRecords();

        if (relRecords.isEmpty()) {
            Page<RoleGrantVO> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(relResult.getTotal());
            empty.setRecords(List.of());
            return empty;
        }

        // 4. 查询职位信息并组装 VO
        List<Long> positionIds = relRecords.stream()
                .map(SysRolePosition::getPositionId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        List<SysPosition> positions = positionIds.isEmpty() ? List.of() : positionMapper.selectByIds(positionIds);
        Map<Long, SysPosition> positionMap = positions == null ? Map.of() : positions.stream()
                .filter(p -> p != null && p.getId() != null)
                .collect(Collectors.toMap(SysPosition::getId, p -> p, (a, b) -> a));

        // 5. 组装 VO 列表
        List<RoleGrantVO> voList = new ArrayList<>();
        for (SysRolePosition rel : relRecords) {
            SysPosition position = positionMap.get(rel.getPositionId());
            if (position == null) {
                continue;
            }

            RoleGrantVO vo = new RoleGrantVO();
            vo.setId(rel.getId());
            vo.setRoleId(rel.getRoleId());
            vo.setRoleName(role.getRoleName());
            vo.setGrantType(GRANT_TYPE_POSITION);
            vo.setGrantObjectId(position.getId());
            vo.setGrantObject(position.getPositionName());
            vo.setGrantObjectCode(position.getPositionCode());
            vo.setCreateTime(rel.getCreateTime());
            vo.setCreateBy(rel.getCreateBy());

            voList.add(vo);
        }

        // 6. 关键字过滤
        if (StringUtils.isNotBlank(keyword)) {
            String lowerKeyword = keyword.toLowerCase();
            voList = voList.stream()
                    .filter(vo -> {
                        boolean matchName = vo.getGrantObject() != null &&
                                vo.getGrantObject().toLowerCase().contains(lowerKeyword);
                        boolean matchCode = vo.getGrantObjectCode() != null &&
                                vo.getGrantObjectCode().toLowerCase().contains(lowerKeyword);
                        return matchName || matchCode;
                    })
                    .toList();
        }

        // 7. 手动分页（因为关键字过滤后数据量变化）
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
    public void grantPositions(RoleGrantDTO grantDTO) {
        // 1. 参数校验
        if (grantDTO == null || grantDTO.getRoleId() == null || grantDTO.getTenantId() == null) {
            throw new BusinessException(400, CommonPrompt.PARAM_EMPTY.getDefaultTemplate());
        }
        if (CollectionUtils.isEmpty(grantDTO.getPositionIds())) {
            throw new BusinessException(400, CommonPrompt.PARAM_EMPTY.getDefaultTemplate());
        }

        Long roleId = grantDTO.getRoleId();
        Long tenantId = grantDTO.getTenantId();

        // 2. 校验角色存在且属于当前租户
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        // 3. 校验职位存在
        List<SysPosition> positions = positionMapper.selectByIds(grantDTO.getPositionIds());
        if (positions == null || positions.size() != grantDTO.getPositionIds().size()) {
            throw new BusinessException("部分职位不存在");
        }

        // 4. 查询已存在的角色 - 职位关联，避免重复插入
        List<SysRolePosition> existsRels = rolePositionMapper.selectList(new LambdaQueryWrapper<SysRolePosition>()
                .eq(SysRolePosition::getTenantId, tenantId)
                .eq(SysRolePosition::getRoleId, roleId)
                .in(SysRolePosition::getPositionId, grantDTO.getPositionIds()));
        Set<Long> existsPositionIds = existsRels == null ? Set.of() : existsRels.stream()
                .map(SysRolePosition::getPositionId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 5. 遍历职位，为每个职位下的用户授权
        for (SysPosition position : positions) {
            if (existsPositionIds.contains(position.getId())) {
                continue;
            }

            // 5.1 查询职位下的所有用户
            List<SysUser> positionUsers = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPositionId, position.getId())
                    .eq(SysUser::getDeleted, false));

            if (CollectionUtils.isEmpty(positionUsers)) {
                continue;
            }

            // 5.2 查询已存在的角色 - 用户关联
            List<Long> userIds = positionUsers.stream()
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

            // 5.3 批量插入缺失的角色 - 用户关联
            for (SysUser user : positionUsers) {
                if (existsUserIds.contains(user.getId())) {
                    continue;
                }
                SysUserRole userRel = new SysUserRole();
                userRel.setTenantId(tenantId);
                userRel.setRoleId(roleId);
                userRel.setUserId(user.getId());
                userRoleMapper.insert(userRel);
            }

            // 5.4 插入角色 - 职位关联
            SysRolePosition rolePosition = new SysRolePosition();
            rolePosition.setTenantId(tenantId);
            rolePosition.setRoleId(roleId);
            rolePosition.setPositionId(position.getId());
            rolePositionMapper.insert(rolePosition);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokePositions(RoleGrantDTO revokeDTO) {
        // 1. 参数校验
        if (revokeDTO == null || revokeDTO.getRoleId() == null || revokeDTO.getTenantId() == null) {
            throw new BusinessException(400, CommonPrompt.PARAM_EMPTY.getDefaultTemplate());
        }
        if (CollectionUtils.isEmpty(revokeDTO.getPositionIds())) {
            throw new BusinessException(400, CommonPrompt.PARAM_EMPTY.getDefaultTemplate());
        }

        Long roleId = revokeDTO.getRoleId();
        Long tenantId = revokeDTO.getTenantId();

        // 2. 校验角色存在且属于当前租户
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        // 3. 校验职位存在
        List<SysPosition> positions = positionMapper.selectByIds(revokeDTO.getPositionIds());
        if (positions == null || positions.size() != revokeDTO.getPositionIds().size()) {
            throw new BusinessException("部分职位不存在");
        }

        // 4. 遍历职位，删除每个职位下用户的角色关联
        for (SysPosition position : positions) {
            // 4.1 查询职位下的所有用户
            List<SysUser> positionUsers = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getPositionId, position.getId())
                    .eq(SysUser::getDeleted, false));

            if (CollectionUtils.isEmpty(positionUsers)) {
                continue;
            }

            // 4.2 批量删除角色 - 用户关联
            List<Long> userIds = positionUsers.stream()
                    .map(SysUser::getId)
                    .filter(id -> id != null && id > 0)
                    .toList();

            if (!userIds.isEmpty()) {
                userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getTenantId, tenantId)
                        .eq(SysUserRole::getRoleId, roleId)
                        .in(SysUserRole::getUserId, userIds));
            }

            // 4.3 删除角色 - 职位关联
            rolePositionMapper.delete(new LambdaQueryWrapper<SysRolePosition>()
                    .eq(SysRolePosition::getTenantId, tenantId)
                    .eq(SysRolePosition::getRoleId, roleId)
                    .eq(SysRolePosition::getPositionId, position.getId()));
        }
    }
}
