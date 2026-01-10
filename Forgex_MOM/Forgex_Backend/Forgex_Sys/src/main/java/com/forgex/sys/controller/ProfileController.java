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

import cn.dev33.satoken.stp.StpUtil;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 个人信息Controller
 * 
 * 提供当前登录用户的个人信息管理接口
 * 
 * @author coder_nai@163.com
 * @date 2025-01-10
 */
@RestController
@RequestMapping("/sys/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final ISysUserService userService;
    
    /**
     * 获取当前用户信息
     */
    @PostMapping("/get")
    public R<SysUserDTO> get() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUserDTO user = userService.getUserById(userId);
        return R.ok(user);
    }
    
    /**
     * 更新基本信息
     */
    @PostMapping("/updateBasic")
    public R<Void> updateBasic(@RequestBody SysUserDTO userDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        userDTO.setId(userId);
        
        // 不允许修改账号
        userDTO.setAccount(null);
        
        userService.updateUser(userDTO);
        return R.ok();
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public R<Void> changePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            return R.fail("旧密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return R.fail("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return R.fail("新密码长度不能少于6位");
        }
        
        Long userId = StpUtil.getLoginIdAsLong();
        
        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        if (!success) {
            return R.fail("旧密码不正确");
        }
        
        return R.ok();
    }
}
