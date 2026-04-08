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
import com.forgex.common.exception.BusinessException;
import com.forgex.common.i18n.CommonPrompt;
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
 * 鐢ㄦ埛瑙掕壊鍏宠仈鏈嶅姟瀹炵幇绫?
 * <p>
 * 瀹炵幇鐢ㄦ埛涓庤鑹茬粦瀹氬叧绯荤殑绠＄悊鍔熻兘锛屽寘鎷煡璇㈠凡鎺堟潈鍒楄〃銆佹壒閲忔巿鏉冦€佹壒閲忓彇娑堟巿鏉冪瓑
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

    /**
     * 鎺堟潈绫诲瀷甯搁噺锛歎SER=鐢ㄦ埛
     */
    private static final String GRANT_TYPE_USER = "USER";
    
    /**
     * 鎺堟潈绫诲瀷甯搁噺锛欴EPARTMENT=閮ㄩ棬
     */
    private static final String GRANT_TYPE_DEPARTMENT = "DEPARTMENT";
    
    /**
     * 鎺堟潈绫诲瀷甯搁噺锛歅OSITION=鑱屼綅
     */
    private static final String GRANT_TYPE_POSITION = "POSITION";

    @Override
    public Page<RoleGrantVO> getGrantedList(RoleGrantQueryDTO query) {
        // 1. 鍙傛暟鏍￠獙
        if (query == null || query.getRoleId() == null || query.getTenantId() == null) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        Long roleId = query.getRoleId();
        Long tenantId = query.getTenantId();
        String grantType = query.getGrantType();
        String keyword = query.getKeyword();
        Integer pageNum = query.getPageNum() == null ? 1 : query.getPageNum();
        Integer pageSize = query.getPageSize() == null ? 20 : query.getPageSize();

        // 2. 鏍￠獙瑙掕壊瀛樺湪涓斿睘浜庡綋鍓嶇鎴?
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new BusinessException(400, "角色不存在或不属于当前租户");
        }

        // 3. 鍒嗛〉鏌ヨ缁戝畾鍏崇郴
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

        // 4. 鏌ヨ鐢ㄦ埛淇℃伅骞剁粍瑁?VO
        List<Long> userIds = relRecords.stream()
                .map(SysUserRole::getUserId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        List<SysUser> users = userIds.isEmpty() ? List.of() : userMapper.selectByIds(userIds);
        Map<Long, SysUser> userMap = users == null ? Map.of() : users.stream()
                .filter(u -> u != null && u.getId() != null)
                .collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));

        // 5. 鎵归噺鏌ヨ閮ㄩ棬/鑱屼綅鍚嶇О
        Map<Long, String> deptNameMap = buildDepartmentNameMap(userMap.values());
        Map<Long, String> positionNameMap = buildPositionNameMap(userMap.values());

        // 6. 缁勮 VO 鍒楄〃
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
            // SysUserRole 表没有 create_time 字段\n            // vo.setCreateTime(rel.getCreateTime());
            // SysUserRole 表没有 create_by 字段\n            // vo.setCreateBy(rel.getCreateBy());

            // 琛ュ厖閮ㄩ棬/鑱屼綅淇℃伅
            if (user.getDepartmentId() != null) {
                vo.setGrantObject(deptNameMap.getOrDefault(user.getDepartmentId(), user.getUsername()));
            }
            if (user.getPositionId() != null) {
                vo.setGrantObject(positionNameMap.getOrDefault(user.getPositionId(), user.getUsername()));
            }

            voList.add(vo);
        }

        // 7. 鍓嶇杩囨护锛堟寜鎺堟潈绫诲瀷鍜屽叧閿瓧锛?
        if (StringUtils.isNotBlank(grantType) || StringUtils.isNotBlank(keyword)) {
            voList = voList.stream()
                    .filter(vo -> {
                        if (StringUtils.isNotBlank(grantType) && !grantType.equals(vo.getGrantType())) {
                            return false;
                        }
                        if (StringUtils.isNotBlank(keyword)) {
                            String lowerKeyword = keyword.toLowerCase();
                            boolean matchName = vo.getGrantObject() != null && 
                                    vo.getGrantObject().toLowerCase().contains(lowerKeyword);
                            boolean matchCode = vo.getGrantObjectCode() != null && 
                                    vo.getGrantObjectCode().toLowerCase().contains(lowerKeyword);
                            return matchName || matchCode;
                        }
                        return true;
                    })
                    .toList();
        }

        // 8. 鎵嬪姩鍒嗛〉锛堝洜涓哄墠绔繃婊ゅ悗鏁版嵁閲忓彉鍖栵級
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
        // 1. 鍙傛暟鏍￠獙
        if (grantDTO == null || grantDTO.getRoleId() == null || grantDTO.getTenantId() == null) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }
        if (StringUtils.isBlank(grantDTO.getGrantType())) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        Long roleId = grantDTO.getRoleId();
        Long tenantId = grantDTO.getTenantId();
        String grantType = grantDTO.getGrantType();

        // 2. 鏍￠獙瑙掕壊瀛樺湪涓斿睘浜庡綋鍓嶇鎴?
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new BusinessException(400, "角色不存在或不属于当前租户");
        }

        // 3. 鏍规嵁鎺堟潈绫诲瀷澶勭悊
        if (GRANT_TYPE_USER.equals(grantType)) {
            grantUsers(grantDTO);
        } else if (GRANT_TYPE_DEPARTMENT.equals(grantType)) {
            grantDepartments(grantDTO);
        } else if (GRANT_TYPE_POSITION.equals(grantType)) {
            grantPositions(grantDTO);
        } else {
            throw new BusinessException(400, "不支持的授权类型：" + grantType);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeBatch(RoleGrantDTO revokeDTO) {
        // 1. 鍙傛暟鏍￠獙
        if (revokeDTO == null || revokeDTO.getRoleId() == null || revokeDTO.getTenantId() == null) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }
        if (StringUtils.isBlank(revokeDTO.getGrantType())) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        Long roleId = revokeDTO.getRoleId();
        Long tenantId = revokeDTO.getTenantId();
        String grantType = revokeDTO.getGrantType();

        // 2. 鏍￠獙瑙掕壊瀛樺湪涓斿睘浜庡綋鍓嶇鎴?
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false));
        if (role == null) {
            throw new BusinessException(400, "角色不存在或不属于当前租户");
        }

        // 3. 鏍规嵁鎺堟潈绫诲瀷澶勭悊
        if (GRANT_TYPE_USER.equals(grantType)) {
            revokeUsers(revokeDTO);
        } else if (GRANT_TYPE_DEPARTMENT.equals(grantType)) {
            revokeDepartments(revokeDTO);
        } else if (GRANT_TYPE_POSITION.equals(grantType)) {
            revokePositions(revokeDTO);
        } else {
            throw new BusinessException(400, "不支持的授权类型：" + grantType);
        }
    }

    /**
     * 鎺堜簣鐢ㄦ埛鏉冮檺
     *
     * @param grantDTO 鎺堟潈鍙傛暟
     */
    private void grantUsers(RoleGrantDTO grantDTO) {
        if (CollectionUtils.isEmpty(grantDTO.getUserIds())) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        // 鏍￠獙鐢ㄦ埛瀛樺湪
        List<SysUser> users = userMapper.selectByIds(grantDTO.getUserIds());
        if (users == null || users.size() != grantDTO.getUserIds().size()) {
            throw new BusinessException(400, "部分用户不存在");
        }

        // 鏌ヨ宸插瓨鍦ㄧ殑缁戝畾鍏崇郴锛岄伩鍏嶉噸澶嶆彃鍏?
        List<SysUserRole> exists = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, grantDTO.getTenantId())
                .eq(SysUserRole::getRoleId, grantDTO.getRoleId())
                .in(SysUserRole::getUserId, grantDTO.getUserIds()));
        Set<Long> existsUserIds = exists == null ? Set.of() : exists.stream()
                .map(SysUserRole::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // 鎵归噺鎻掑叆缂哄け鐨勭粦瀹氬叧绯?
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
     * 鎺堜簣閮ㄩ棬鏉冮檺
     *
     * @param grantDTO 鎺堟潈鍙傛暟
     */
    private void grantDepartments(RoleGrantDTO grantDTO) {
        // 濮旀墭缁?SysRoleDeptService 澶勭悊
        roleDeptService.grantDepartments(grantDTO);
    }

    /**
     * 鎺堜簣鑱屼綅鏉冮檺
     *
     * @param grantDTO 鎺堟潈鍙傛暟
     */
    private void grantPositions(RoleGrantDTO grantDTO) {
        // 濮旀墭缁?SysRolePositionService 澶勭悊
        rolePositionService.grantPositions(grantDTO);
    }

    /**
     * 鍙栨秷鐢ㄦ埛鎺堟潈
     *
     * @param revokeDTO 鍙栨秷鎺堟潈鍙傛暟
     */
    private void revokeUsers(RoleGrantDTO revokeDTO) {
        if (CollectionUtils.isEmpty(revokeDTO.getUserIds())) {
            throw new BusinessException(400, CommonPrompt.BAD_REQUEST.getDefaultTemplate().replace("{0}", ""));
        }

        // 鍒犻櫎缁戝畾鍏崇郴
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getTenantId, revokeDTO.getTenantId())
                .eq(SysUserRole::getRoleId, revokeDTO.getRoleId())
                .in(SysUserRole::getUserId, revokeDTO.getUserIds()));
    }

    /**
     * 鍙栨秷閮ㄩ棬鎺堟潈
     *
     * @param revokeDTO 鍙栨秷鎺堟潈鍙傛暟
     */
    private void revokeDepartments(RoleGrantDTO revokeDTO) {
        // 濮旀墭缁?SysRoleDeptService 澶勭悊
        roleDeptService.revokeDepartments(revokeDTO);
    }

    /**
     * 鍙栨秷鑱屼綅鎺堟潈
     *
     * @param revokeDTO 鍙栨秷鎺堟潈鍙傛暟
     */
    private void revokePositions(RoleGrantDTO revokeDTO) {
        // 濮旀墭缁?SysRolePositionService 澶勭悊
        rolePositionService.revokePositions(revokeDTO);
    }

    /**
     * 鎵归噺鏋勫缓閮ㄩ棬 ID 鍒伴儴闂ㄥ悕绉扮殑鏄犲皠
     *
     * @param users 鐢ㄦ埛闆嗗悎
     * @return 閮ㄩ棬鍚嶇О鏄犲皠
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
     * 鎵归噺鏋勫缓鑱屼綅 ID 鍒拌亴浣嶅悕绉扮殑鏄犲皠
     *
     * @param users 鐢ㄦ埛闆嗗悎
     * @return 鑱屼綅鍚嶇О鏄犲皠
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


