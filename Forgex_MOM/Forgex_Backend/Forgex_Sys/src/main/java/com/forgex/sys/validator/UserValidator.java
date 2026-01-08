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
package com.forgex.sys.validator;

import com.forgex.common.exception.BusinessException;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 用户数据校验器
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Component
@RequiredArgsConstructor
public class UserValidator {
    
    private final ISysUserService userService;
    
    /**
     * 新增用户校验
     * 
     * @param userDTO 用户信息
     */
    public void validateForAdd(SysUserDTO userDTO) {
        // 1. 必填项校验
        Assert.hasText(userDTO.getAccount(), "账号不能为空");
        Assert.hasText(userDTO.getUsername(), "用户名不能为空");
        
        // 2. 账号唯一性校验
        if (userService.existsByAccount(userDTO.getAccount())) {
            throw new BusinessException("账号已存在");
        }
        
        // 3. 邮箱格式校验
        if (StringUtils.hasText(userDTO.getEmail()) && !isValidEmail(userDTO.getEmail())) {
            throw new BusinessException("邮箱格式不正确");
        }
        
        // 4. 手机号格式校验
        if (StringUtils.hasText(userDTO.getPhone()) && !isValidPhone(userDTO.getPhone())) {
            throw new BusinessException("手机号格式不正确");
        }
    }
    
    /**
     * 更新用户校验
     * 
     * @param userDTO 用户信息
     */
    public void validateForUpdate(SysUserDTO userDTO) {
        // 1. ID校验
        Assert.notNull(userDTO.getId(), "用户ID不能为空");
        
        // 2. 存在性校验
        if (!userService.existsById(userDTO.getId())) {
            throw new BusinessException("用户不存在");
        }
        
        // 3. 账号唯一性校验（排除自己）
        if (StringUtils.hasText(userDTO.getAccount()) 
            && userService.existsByAccountExcludeId(userDTO.getAccount(), userDTO.getId())) {
            throw new BusinessException("账号已被其他用户使用");
        }
        
        // 4. 邮箱格式校验
        if (StringUtils.hasText(userDTO.getEmail()) && !isValidEmail(userDTO.getEmail())) {
            throw new BusinessException("邮箱格式不正确");
        }
        
        // 5. 手机号格式校验
        if (StringUtils.hasText(userDTO.getPhone()) && !isValidPhone(userDTO.getPhone())) {
            throw new BusinessException("手机号格式不正确");
        }
    }
    
    /**
     * 删除用户校验
     * 
     * @param id 用户ID
     */
    public void validateForDelete(Long id) {
        // 1. ID校验
        validateId(id);
        
        // 2. 存在性校验
        if (!userService.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
    }
    
    /**
     * ID校验
     * 
     * @param id 用户ID
     */
    public void validateId(Long id) {
        Assert.notNull(id, "用户ID不能为空");
        if (id <= 0) {
            throw new BusinessException("用户ID格式不正确");
        }
    }
    
    /**
     * 校验邮箱格式
     */
    private boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return email.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    }
    
    /**
     * 校验手机号格式
     */
    private boolean isValidPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }
        return phone.matches("^1[3-9]\\d{9}$");
    }
}
