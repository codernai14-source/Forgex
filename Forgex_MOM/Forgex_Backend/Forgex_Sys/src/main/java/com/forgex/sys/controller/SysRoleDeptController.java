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
package com.forgex.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.RoleGrantDTO;
import com.forgex.sys.domain.param.RoleGrantQueryDTO;
import com.forgex.sys.domain.vo.RoleGrantVO;
import com.forgex.sys.service.ISysRoleDeptService;
import com.forgex.sys.util.RoleGrantWebHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色部门授权 Controller。
 * <p>
 * 提供角色与部门绑定关系的查询、授权与取消授权接口，
 * 对应前端「角色管理 - 授权部门」功能。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>查询角色已授权的部门列表</li>
 *   <li>批量授予角色部门权限</li>
 *   <li>批量取消角色部门权限</li>
 * </ul>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-06
 * @see ISysRoleDeptService
 * @see SysRoleUserController
 */
@RestController
@RequestMapping("/sys/role/dept")
@RequiredArgsConstructor
public class SysRoleDeptController {

    /**
     * 角色-部门授权服务。
     */
    private final ISysRoleDeptService roleDeptService;

    /**
     * 查询角色已授权部门列表（分页）。
     * <p>
     * 分页查询角色已授权的部门信息，支持按部门名称/编码搜索。
     * </p>
     *
     * @param query 查询参数（角色 ID、租户 ID、关键字、分页）
     * @return 已授权部门分页结果，{@code data} 为 {@link Page}{@code <}{@link RoleGrantVO}{@code >}
     * @throws com.forgex.common.exception.I18nBusinessException 参数校验失败或角色不存在时抛出
     * @see RoleGrantQueryDTO
     * @see RoleGrantVO
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/granted/list")
    public R<Page<RoleGrantVO>> getGrantedList(@RequestBody RoleGrantQueryDTO query) {
        RoleGrantWebHelper.fillTenantId(query);
        RoleGrantWebHelper.normalizePage(query);

        return R.ok(RoleGrantWebHelper.toPage(
                roleDeptService.getGrantedDepartments(
                        new Page<>(query.getPageNum(), query.getPageSize()),
                        query)));
    }

    /**
     * 授予角色部门授权。
     * <p>
     * 批量为角色授予部门权限，系统会自动查询部门下的所有用户并建立角色与用户关联。
     * </p>
     *
     * @param grantDTO 授权参数（角色 ID、租户 ID、部门 ID 列表等）
     * @return 无业务数据，仅操作结果
     * @throws com.forgex.common.exception.I18nBusinessException 参数校验失败、角色不存在或部门不存在时抛出
     * @see RoleGrantDTO
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/grant")
    public R<Void> grant(@RequestBody RoleGrantDTO grantDTO) {
        RoleGrantWebHelper.fillTenantId(grantDTO);
        grantDTO.setGrantType("DEPARTMENT");

        roleDeptService.grantDepartments(grantDTO);

        return R.ok(CommonPrompt.AUTHORIZE_SUCCESS);
    }

    /**
     * 取消角色部门授权。
     * <p>
     * 批量取消角色的部门权限，系统会自动删除部门下所有用户的对应角色关联。
     * </p>
     *
     * @param revokeDTO 取消授权参数（角色 ID、租户 ID、部门 ID 列表等）
     * @return 无业务数据，仅操作结果
     * @throws com.forgex.common.exception.I18nBusinessException 参数校验失败或角色不存在时抛出
     * @see RoleGrantDTO
     */
    @RequirePerm("sys:role:authUser")
    @PostMapping("/revoke")
    public R<Void> revoke(@RequestBody RoleGrantDTO revokeDTO) {
        RoleGrantWebHelper.fillTenantId(revokeDTO);
        revokeDTO.setGrantType("DEPARTMENT");

        roleDeptService.revokeDepartments(revokeDTO);

        return R.ok(CommonPrompt.UNAUTHORIZE_SUCCESS);
    }
}
