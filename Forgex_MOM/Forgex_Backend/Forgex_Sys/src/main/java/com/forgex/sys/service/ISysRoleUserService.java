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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.RoleGrantDTO;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.param.RoleGrantQueryDTO;
import com.forgex.sys.domain.vo.RoleGrantVO;

/**
 * 用户角色关联服务接口
 * <p>
 * 提供用户与角色绑定关系的管理功能，包括查询已授权列表、批量授权、批量取消授权等
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2026-04-06
 * @see com.forgex.sys.domain.entity.SysUserRole
 * @see com.forgex.sys.domain.dto.RoleGrantDTO
 * @see com.forgex.sys.domain.vo.RoleGrantVO
 */
public interface ISysRoleUserService extends IService<SysUserRole> {

    /**
     * 获取角色已授权列表（支持用户、部门、职位）
     * <p>
     * 根据查询条件分页返回已授权的用户、部门或职位信息
     * </p>
     *
     * @param query 查询参数，包含角色 ID、租户 ID、授权类型、搜索关键字等
     * @return 已授权列表分页结果
     * @throws com.forgex.common.exception.I18nBusinessException 当参数校验失败或角色不存在时抛出
     * @see RoleGrantQueryDTO
     * @see RoleGrantVO
     */
    Page<RoleGrantVO> getGrantedList(RoleGrantQueryDTO query);

    /**
     * 批量授权（支持用户、部门、职位）
     * <p>
     * 根据授权类型批量为角色授予用户、部门或职位权限
     * 支持避免重复授权的幂等性操作
     * </p>
     *
     * @param grantDTO 授权参数，包含角色 ID、租户 ID、授权类型、授权对象 ID 列表等
     * @throws com.forgex.common.exception.I18nBusinessException 当参数校验失败、角色不存在或授权对象不存在时抛出
     * @see RoleGrantDTO
     */
    void grantBatch(RoleGrantDTO grantDTO);

    /**
     * 批量取消授权（支持用户、部门、职位）
     * <p>
     * 根据授权类型批量取消角色的用户、部门或职位权限
     * </p>
     *
     * @param revokeDTO 取消授权参数，包含角色 ID、租户 ID、授权类型、授权对象 ID 列表等
     * @throws com.forgex.common.exception.I18nBusinessException 当参数校验失败或角色不存在时抛出
     * @see RoleGrantDTO
     */
    void revokeBatch(RoleGrantDTO revokeDTO);
}
