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

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 个人信息 Controller
 * 
 * 职责：
 * - 接收 HTTP 请求
 * - 参数校验（调用 Validator）
 * - 调用 Service 层方法
 * - 返回响应结果
 * 
 * 提供当前登录用户的个人信息管理接口，包括查看个人信息、更新基本信息、修改密码等
 * 
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-10
 * @see ISysUserService
 * @see SysUserDTO
 */
@RestController
@RequestMapping("/sys/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final ISysUserService userService;
    
    /**
     * 获取当前用户信息
     * <p>
     * 接口路径：POST /sys/profile/get
     * 需要认证：是（从 UserContext 获取当前用户 ID）
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从 UserContext 获取当前登录用户 ID</li>
     *   <li>调用 Service 层获取用户详情</li>
     *   <li>返回用户详情对象</li>
     * </ol>
     *
     * @return {@link R} 包含用户详情的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 用户详情对象（SysUserDTO）
     *         - message: 提示信息
     * @see SysUserDTO
     */
    @PostMapping("/get")
    public R<SysUserDTO> get() {
        // 1. 从 UserContext 获取当前登录用户 ID
        Long userId = UserContext.get();
        // 2. 调用 Service 层获取用户详情
        SysUserDTO user = userService.getUserById(userId);
        // 3. 返回用户详情对象
        return R.ok(user);
    }
    
    /**
     * 更新基本信息
     * <p>
     * 接口路径：POST /sys/profile/updateBasic
     * 需要认证：是（从 UserContext 获取当前用户 ID）
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从 UserContext 获取当前登录用户 ID</li>
     *   <li>设置用户 ID 到 DTO 中</li>
     *   <li>清空账号字段（不允许修改）</li>
     *   <li>调用 Service 层更新用户信息</li>
     *   <li>返回更新成功提示</li>
     * </ol>
     *
     * @param userDTO 用户信息对象
     *                - username: 用户名（可选）
     *                - realName: 真实姓名（可选）
     *                - phone: 手机号（可选）
     *                - email: 邮箱（可选）
     *                - avatar: 头像 URL（可选）
     *                - gender: 性别（可选，0=未知，1=男，2=女）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（更新成功）
     * @throws BusinessException 用户不存在时抛出
     * @see SysUserDTO
     */
    @PostMapping("/updateBasic")
    public R<Void> updateBasic(@RequestBody SysUserDTO userDTO) {
        // 1. 从 UserContext 获取当前登录用户 ID
        Long userId = UserContext.get();
        // 2. 设置用户 ID 到 DTO 中
        userDTO.setId(userId);
        
        // 3. 清空账号字段（不允许修改账号）
        userDTO.setAccount(null);
        
        // 4. 调用 Service 层更新用户信息
        userService.updateUser(userDTO);
        // 5. 返回更新成功提示
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }
    
    /**
     * 修改密码
     * <p>
     * 接口路径：POST /sys/profile/changePassword
     * 需要认证：是（从 UserContext 获取当前用户 ID）
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收请求参数（旧密码、新密码）</li>
     *   <li>校验旧密码是否为空</li>
     *   <li>校验新密码是否为空</li>
     *   <li>校验新密码长度（至少 6 位）</li>
     *   <li>从 UserContext 获取当前登录用户 ID</li>
     *   <li>调用 Service 层修改密码</li>
     *   <li>返回修改结果</li>
     * </ol>
     *
     * @param body 请求体
     *             - oldPassword: 旧密码（必填）
     *             - newPassword: 新密码（必填，至少 6 位）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - message: 提示信息（更新成功或错误信息）
     * @throws BusinessException 密码校验失败或旧密码不正确时抛出
     * @see CommonPrompt#OLD_PASSWORD_CANNOT_BE_EMPTY
     * @see CommonPrompt#NEW_PASSWORD_CANNOT_BE_EMPTY
     * @see CommonPrompt#PASSWORD_TOO_SHORT
     * @see CommonPrompt#PASSWORD_INCORRECT
     */
    @PostMapping("/changePassword")
    public R<Void> changePassword(@RequestBody Map<String, String> body) {
        // 1. 从请求体中提取密码参数
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        
        // 2. 参数校验：检查旧密码是否为空
        if (oldPassword == null || oldPassword.isEmpty()) {
            return R.fail(CommonPrompt.OLD_PASSWORD_CANNOT_BE_EMPTY);
        }
        
        // 3. 参数校验：检查新密码是否为空
        if (newPassword == null || newPassword.isEmpty()) {
            return R.fail(CommonPrompt.NEW_PASSWORD_CANNOT_BE_EMPTY);
        }
        
        // 4. 参数校验：检查新密码长度（至少 6 位）
        if (newPassword.length() < 6) {
            return R.fail(CommonPrompt.PASSWORD_TOO_SHORT);
        }

        // 5. 从 UserContext 获取当前登录用户 ID
        Long userId = UserContext.get();

        // 6. 调用 Service 层修改密码
        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        
        // 7. 返回修改结果
        if (!success) {
            return R.fail(CommonPrompt.PASSWORD_INCORRECT);
        }
        
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }
}
