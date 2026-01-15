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
 * <p>
 * 提供用户登录、租户选择、权限校验、密码重置、登出等认证相关功能
 * </p>
 * <p>业务返回统一使用 {@link R} 类型，Controller 直接返回 Service 的方法结果</p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #login(LoginParam)} - 用户登录，返回绑定的租户列表</li>
 *   <li>{@link #chooseTenant(TenantChoiceParam)} - 选择租户，设置当前租户上下文</li>
 *   <li>{@link #secureAdmin()} - 管理员权限校验</li>
 *   <li>{@link #resetPasswordById(Long)} - 重置用户密码</li>
 *   <li>{@link #logout()} - 用户登出</li>
 *   <li>{@link #updateTenantPreferences(String, List, Long)} - 更新租户偏好设置</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.auth.service.impl.AuthServiceImpl
 * @see com.forgex.common.web.R
 */
public interface AuthService {
    /**
     * 登录并返回租户列表
     * <p>
     * 逻辑：校验参数 -> 登录 -> 查询绑定租户 -> 转为VO
     * </p>
     * <p>详细流程：</p>
     * <ul>
     *   <li>校验账号和密码是否为空</li>
     *   <li>如果启用了SM2传输加密，解密密码</li>
     *   <li>查询用户信息</li>
     *   <li>根据密码策略验证密码</li>
     *   <li>根据配置校验验证码（图片验证码或滑块验证码）</li>
     *   <li>查询用户绑定的租户列表</li>
     *   <li>返回租户VO列表</li>
     * </ul>
     *
     * @param param 登录参数，包含账号、密码、验证码等信息
     * @return {@link R} 包含租户VO列表的统一返回结构
     * @see com.forgex.auth.domain.param.LoginParam
     * @see com.forgex.auth.domain.vo.TenantVO
     */
    R<List<TenantVO>> login(LoginParam param);

    /**
     * 选择租户，设置上下文
     * <p>
     * 逻辑：校验参数 -> 设置租户上下文
     * </p>
     * <p>详细流程：</p>
     * <ul>
     *   <li>校验租户ID是否为空</li>
     *   <li>检查用户与租户的绑定关系</li>
     *   <li>调用SaToken进行登录</li>
     *   <li>设置会话信息（用户ID、租户ID、账号）</li>
     *   <li>将登录上下文存入Redis</li>
     *   <li>设置租户上下文</li>
     *   <li>更新用户最后登录信息</li>
     *   <li>返回当前用户信息</li>
     * </ul>
     *
     * @param param 选择租户参数，包含账号和租户ID
     * @return {@link R} 包含当前登录用户信息（含头像、当前租户ID等）的统一返回结构
     * @see com.forgex.auth.domain.param.TenantChoiceParam
     * @see com.forgex.auth.domain.dto.SysUserDTO
     * @see com.forgex.common.tenant.TenantContext
     */
    R<SysUserDTO> chooseTenant(TenantChoiceParam param);

    /**
     * 管理员权限校验
     * <p>
     * 逻辑：检查是否拥有 admin 角色
     * </p>
     * <p>用途：</p>
     * <ul>
     *   <li>验证当前登录用户是否拥有管理员角色</li>
     *   <li>用于权限控制</li>
     * </ul>
     *
     * @return {@link R} 校验结果，true表示拥有管理员权限
     * @see cn.dev33.satoken.stp.StpUtil#checkRole(String)
     */
    R<Boolean> secureAdmin();

    /**
     * 根据用户ID重置密码
     * <p>
     * 将指定用户的密码重置为默认密码（123456）
     * </p>
     * <p>详细流程：</p>
     * <ul>
     *   <li>校验用户ID是否为空</li>
     *   <li>查询用户是否存在</li>
     *   <li>使用BCrypt算法对默认密码进行哈希</li>
     *   <li>更新用户密码</li>
     * </ul>
     *
     * @param userId 用户ID
     * @return {@link R} 重置是否成功
     */
    R<Boolean> resetPasswordById(Long userId);

    /**
     * 用户登出
     * <p>
     * 清除用户登录状态，删除Redis中的登录上下文信息
     * </p>
     * <p>详细流程：</p>
     * <ul>
     *   <li>获取当前登录用户ID和租户ID</li>
     *   <li>记录登出日志</li>
     *   <li>删除Redis中的登录上下文</li>
     *   <li>调用SaToken登出</li>
     *   <li>清除租户上下文</li>
     * </ul>
     *
     * @return {@link R} 登出是否成功
     * @see com.forgex.common.security.LogoutReason
     */
    R<Boolean> logout();

    /**
     * 更新租户偏好排序与默认租户
     * <p>
     * 用于保存用户对租户的排序偏好和默认租户设置
     * </p>
     * <p>详细流程：</p>
     * <ul>
     *   <li>校验账号和排序列表是否为空</li>
     *   <li>查询用户是否存在</li>
     *   <li>根据排序列表更新每个租户的偏好顺序</li>
     *   <li>设置默认租户标记</li>
     * </ul>
     *
     * @param account 用户账号
     * @param ordered  租户ID按喜好顺序排列（前端保存的顺序）
     * @param defaultTenantId 默认租户ID（可选）
     * @return {@link R} 更新是否成功
     */
    R<Boolean> updateTenantPreferences(String account, java.util.List<Long> ordered, Long defaultTenantId);
}
