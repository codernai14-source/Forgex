package com.forgex.auth.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.auth.domain.entity.SysMenuPerm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper（仅用于权限计算）。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Mapper
@DS("admin")
public interface SysMenuPermMapper extends BaseMapper<SysMenuPerm> {

    /**
     * 直接按真实授权关系联表查询 permKey，避免多次 ORM 查询在运行态出现空结果。
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @return 去重后的权限键列表
     */
    @Select("""
            SELECT DISTINCT m.perm_key
            FROM sys_user_role ur
            INNER JOIN sys_role_menu rm
                ON rm.role_id = ur.role_id
               AND rm.tenant_id = ur.tenant_id
            INNER JOIN sys_menu m
                ON m.id = rm.menu_id
               AND m.tenant_id = rm.tenant_id
            WHERE ur.user_id = #{userId}
              AND ur.tenant_id = #{tenantId}
              AND IFNULL(m.deleted, 0) = 0
              AND m.perm_key IS NOT NULL
              AND m.perm_key <> ''
              AND LOWER(m.type) IN ('button', 'menu', 'catalog')
            ORDER BY m.perm_key
            """)
    List<String> selectPermKeysByUserIdAndTenantId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);
}
