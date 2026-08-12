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
package com.forgex.common.api.service;

import com.forgex.common.api.dto.UserInfoDTO;
import com.forgex.common.api.feign.SysUserFeignClient;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户信息服务
 * <p>封装 Feign 调用，提供便捷的用户信息查询方法</p>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoService {
    
    private final SysUserFeignClient sysUserFeignClient;
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息，查询失败返回 null
     */
    public UserInfoDTO getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            R<UserInfoDTO> response = sysUserFeignClient.getUserById(userId);
            if (response != null && response.getCode() == 200) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("查询用户信息失败, userId: {}", userId, e);
        }
        
        return null;
    }
    
    /**
     * 根据账号获取用户信息
     * 
     * @param account 账号
     * @return 用户信息，查询失败返回 null
     */
    public UserInfoDTO getUserByAccount(String account) {
        if (account == null || account.trim().isEmpty()) {
            return null;
        }
        
        try {
            R<UserInfoDTO> response = sysUserFeignClient.getUserByAccount(account);
            if (response != null && response.getCode() == 200) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("查询用户信息失败, account: {}", account, e);
        }
        
        return null;
    }
    
    /**
     * 根据用户ID获取用户名
     * 
     * @param userId 用户ID
     * @return 用户名，查询失败返回 null
     */
    public String getUsernameById(Long userId) {
        UserInfoDTO userInfo = getUserById(userId);
        return userInfo != null ? userInfo.getUsername() : null;
    }
    
    /**
     * 批量获取用户信息
     * 
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    public List<UserInfoDTO> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            R<List<UserInfoDTO>> response = sysUserFeignClient.getUsersByIds(userIds);
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("批量查询用户信息失败, userIds: {}", userIds, e);
        }
        
        return Collections.emptyList();
    }
    
    /**
     * 批量获取用户名映射
     * 
     * @param userIds 用户ID列表
     * @return 用户ID到用户名的映射
     */
    public Map<Long, String> getUsernameMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            R<Map<Long, String>> response = sysUserFeignClient.getUsernameMap(userIds);
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("批量查询用户名映射失败, userIds: {}", userIds, e);
        }
        
        return Collections.emptyMap();
    }
}



