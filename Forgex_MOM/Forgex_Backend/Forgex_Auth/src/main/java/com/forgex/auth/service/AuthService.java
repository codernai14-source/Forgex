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
package com.forgex.auth.service;

import com.forgex.auth.domain.param.LoginParam;
import com.forgex.auth.domain.param.TenantChoiceParam;
import com.forgex.auth.domain.dto.SysUserDTO;
import com.forgex.auth.domain.vo.TenantVO;
import com.forgex.common.web.R;

import java.util.List;

/**
 * 认证服务接口
 * 业务返回统一使用 R 类型，Controller 直接返回 Service 的方法结果
 */
public interface AuthService {
    /**
     * 登录并返回租户列表
     * 逻辑：校验参数 -> 登录 -> 查询绑定租户 -> 转为VO
     * @param param 登录参数
     * @return 租户VO列表
     */
    R<List<TenantVO>> login(LoginParam param);

    /**
     * 选择租户，设置上下文
     * 逻辑：校验参数 -> 设置租户上下文
     * @param param 选择租户参数
     * @return 当前登录用户信息（含头像、当前租户ID 等）
     */
    R<SysUserDTO> chooseTenant(TenantChoiceParam param);

    /**
     * 管理员权限校验
     * 逻辑：检查是否拥有 admin 角色
     * @return 校验结果
     */
    R<Boolean> secureAdmin();

    R<Boolean> resetPasswordById(Long userId);

    R<Boolean> logout();

    /**
     * 更新租户偏好排序与默认租户
     * @param account 账号
     * @param ordered  租户ID按喜好顺序排列（前端保存的顺序）
     * @param defaultTenantId 默认租户ID（可选）
     * @return 是否成功
     */
    R<Boolean> updateTenantPreferences(String account, java.util.List<Long> ordered, Long defaultTenantId);
}
