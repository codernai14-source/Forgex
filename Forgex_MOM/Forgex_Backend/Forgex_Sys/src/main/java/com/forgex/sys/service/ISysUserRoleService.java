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
package com.forgex.sys.service;

import java.util.List;

/**
 * 用户-角色分配服务接口。
 * <p>
 * 用于运行期对 {@code sys_user_role} 进行查询与保存（增删绑定），以支持：
 * <ul>
 *   <li>用户管理页：查看某用户在当前租户下已分配角色</li>
 *   <li>用户管理页：保存某用户在当前租户下的角色分配结果</li>
 * </ul>
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0
 * @see com.forgex.sys.domain.entity.SysUserRole
 * @see com.forgex.sys.mapper.SysUserRoleMapper
 */
public interface ISysUserRoleService {

    /**
     * 查询用户在指定租户下已分配的角色ID列表。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 已分配角色ID列表（空列表表示未分配任何角色）
     * @throws com.forgex.common.exception.BusinessException 参数非法或用户不存在时抛出
     */
    List<Long> listAssignedRoleIds(Long userId, Long tenantId);

    /**
     * 保存用户在指定租户下的角色分配结果。
     * <p>实现建议：先删除该用户在该租户下的全部绑定，再插入本次提交的绑定。</p>
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param roleIds  角色ID列表（为空则视为清空绑定）
     * @throws com.forgex.common.exception.BusinessException 用户不存在、角色不存在或跨租户时抛出
     */
    void saveUserRoles(Long userId, Long tenantId, List<Long> roleIds);
}
