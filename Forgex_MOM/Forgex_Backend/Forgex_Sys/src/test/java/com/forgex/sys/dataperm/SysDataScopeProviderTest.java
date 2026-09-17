package com.forgex.sys.dataperm;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.forgex.common.dataperm.DataPermissionHelper;
import com.forgex.common.dataperm.DataScope;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysRoleDept;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysRoleDeptMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysDataScopeProvider} 范围加载测试。
 * <p>
 * 验证 SELF 不查 {@code sys_role_dept}，CUSTOM 才查关联表并把范围提升为自定义。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see SysDataScopeProvider
 */
class SysDataScopeProviderTest {

    private static final Long USER_ID = 88L;

    private SysUserRoleMapper userRoleMapper;
    private SysRoleMapper roleMapper;
    private SysRoleDeptMapper roleDeptMapper;
    private SysDataScopeProvider provider;

    /**
     * 初始化 Mock 依赖。
     */
    @BeforeEach
    void setUp() {
        userRoleMapper = mock(SysUserRoleMapper.class);
        roleMapper = mock(SysRoleMapper.class);
        roleDeptMapper = mock(SysRoleDeptMapper.class);
        provider = new SysDataScopeProvider(
                userRoleMapper,
                roleMapper,
                roleDeptMapper,
                mock(SysDepartmentMapper.class),
                mock(SysUserMapper.class));
    }

    /**
     * 清理数据权限缓存，避免用例互相污染。
     */
    @AfterEach
    void tearDown() {
        DataPermissionHelper.clearCache(USER_ID);
    }

    /**
     * SELF 角色不应查询角色部门关联表。
     */
    @Test
    void selfScopeDoesNotQueryRoleDept() {
        stubSingleRole("SELF");

        DataPermissionHelper.DataPermissionInfo info = provider.load(USER_ID);

        assertEquals(DataScope.SELF, info.getDataScope());
        verify(roleDeptMapper, never()).selectList(any(Wrapper.class));
    }

    /**
     * CUSTOM 角色应查询关联部门，并把范围从默认 SELF 提升为 CUSTOM。
     */
    @Test
    void customScopeQueriesRoleDept() {
        stubSingleRole("CUSTOM");
        SysRoleDept roleDept = new SysRoleDept();
        roleDept.setDeptId(100L);
        when(roleDeptMapper.selectList(any(Wrapper.class))).thenReturn(List.of(roleDept));

        DataPermissionHelper.DataPermissionInfo info = provider.load(USER_ID);

        assertEquals(DataScope.CUSTOM, info.getDataScope());
        assertTrue(info.getDeptIds().contains(100L));
        verify(roleDeptMapper).selectList(any(Wrapper.class));
    }

    /**
     * 构造单个用户角色绑定。
     *
     * @param dataScope 角色数据范围编码
     */
    private void stubSingleRole(String dataScope) {
        SysUserRole link = new SysUserRole();
        link.setUserId(USER_ID);
        link.setRoleId(10L);
        when(userRoleMapper.selectList(any(Wrapper.class))).thenReturn(List.of(link));
        SysRole role = new SysRole();
        role.setId(10L);
        role.setDataScope(dataScope);
        when(roleMapper.selectById(10L)).thenReturn(role);
    }
}
