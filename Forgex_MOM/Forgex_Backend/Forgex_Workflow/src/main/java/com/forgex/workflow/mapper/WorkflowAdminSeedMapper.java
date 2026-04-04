package com.forgex.workflow.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.workflow.domain.dto.WorkflowAdminSeedUserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作流演示数据初始化所需的管理端用户查询 Mapper。
 */
@Mapper
@DS("admin")
public interface WorkflowAdminSeedMapper {

    /**
     * 查询各租户启用中的管理员账号。
     *
     * @return 管理员用户列表
     */
    @Select("""
            SELECT ur.tenant_id AS tenantId,
                   u.id AS userId,
                   u.account AS account,
                   u.username AS username
            FROM sys_user_role ur
            INNER JOIN sys_role r
                    ON r.id = ur.role_id
                   AND r.deleted = 0
                   AND r.role_key = 'admin'
                   AND r.status = 1
            INNER JOIN sys_user u
                    ON u.id = ur.user_id
                   AND u.deleted = 0
                   AND u.status = 1
            """)
    List<WorkflowAdminSeedUserDTO> listActiveAdminUsers();
}
