package com.forgex.sys.dataperm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.dataperm.DataPermissionHelper;
import com.forgex.common.dataperm.DataScope;
import com.forgex.common.dataperm.DataScopeProvider;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysRoleDept;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysRoleDeptMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sys 模块数据权限范围实现。
 * <p>
 * 多角色取并集：任一角色为 ALL 则整体 ALL；否则合并部门范围，DEPT_AND_CHILD 会展开下级部门。
 * {@code sys_role_dept} 只在角色范围为 CUSTOM 时查询，避免 SELF/DEPT 误查关联表。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DataScopeProvider
 */
@Component
@RequiredArgsConstructor
public class SysDataScopeProvider implements DataScopeProvider {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleDeptMapper roleDeptMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysUserMapper userMapper;

    /**
     * 加载用户数据权限。
     *
     * @param userId 用户 ID
     * @return 数据权限信息
     */
    @Override
    public DataPermissionHelper.DataPermissionInfo load(Long userId) {
        List<SysUserRole> links = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        DataScope dataScope = DataScope.SELF;
        Set<Long> deptIds = new HashSet<>();
        for (SysUserRole link : links) {
            SysRole role = roleMapper.selectById(link.getRoleId());
            if (role == null) {
                continue;
            }
            DataScope candidate = DataScope.fromCode(role.getDataScope());
            if (candidate == DataScope.ALL) {
                dataScope = DataScope.ALL;
                break;
            }
            if (candidate.ordinal() < dataScope.ordinal()) {
                dataScope = candidate;
            }
            // CUSTOM 的 ordinal 比 SELF 大，上面的比较不会提升范围，这里单独落到自定义部门
            if (candidate == DataScope.CUSTOM) {
                if (dataScope == DataScope.SELF) {
                    dataScope = DataScope.CUSTOM;
                }
                List<SysRoleDept> roleDepts = roleDeptMapper.selectList(new LambdaQueryWrapper<SysRoleDept>()
                        .eq(SysRoleDept::getRoleId, role.getId()));
                for (SysRoleDept roleDept : roleDepts) {
                    if (roleDept.getDeptId() != null) {
                        deptIds.add(roleDept.getDeptId());
                    }
                }
            }
            if (candidate == DataScope.DEPT || candidate == DataScope.DEPT_AND_CHILD) {
                SysUser user = userMapper.selectById(userId);
                if (user != null && user.getDepartmentId() != null) {
                    deptIds.add(user.getDepartmentId());
                    if (candidate == DataScope.DEPT_AND_CHILD) {
                        collectChildren(user.getDepartmentId(), deptIds);
                    }
                }
            }
        }
        DataPermissionHelper.cachePermission(userId, dataScope, deptIds);
        return new DataPermissionHelper.DataPermissionInfo(dataScope, deptIds, userId);
    }

    /**
     * 递归收集下级部门。
     *
     * @param parentId 父部门 ID
     * @param collector 收集集合
     */
    private void collectChildren(Long parentId, Set<Long> collector) {
        List<SysDepartment> children = departmentMapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getParentId, parentId));
        for (SysDepartment child : children) {
            if (child.getId() != null && collector.add(child.getId())) {
                collectChildren(child.getId(), collector);
            }
        }
    }
}
