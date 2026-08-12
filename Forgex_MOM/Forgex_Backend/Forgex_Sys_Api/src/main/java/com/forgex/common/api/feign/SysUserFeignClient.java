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
package com.forgex.common.api.feign;

import com.forgex.common.api.dto.UserInfoDTO;
import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 系统用户服务 Feign 客户端
 * <p>通过 Feign 调用 Sys 模块的用户服务接口</p>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-27
 */
@FeignClient(name = "forgex-sys", contextId = "sysUserFeignClient", path = "/sys/user")
public interface SysUserFeignClient {
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/info/{userId}")
    R<UserInfoDTO> getUserById(@PathVariable("userId") Long userId);
    
    /**
     * 根据账号获取用户信息
     * 
     * @param account 账号
     * @return 用户信息
     */
    @GetMapping("/info/account/{account}")
    R<UserInfoDTO> getUserByAccount(@PathVariable("account") String account);
    
    /**
     * 批量获取用户信息
     * 
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    @PostMapping("/info/batch")
    R<List<UserInfoDTO>> getUsersByIds(@RequestBody List<Long> userIds);
    
    /**
     * 批量获取用户名映射（用户ID -> 用户名）
     * 
     * @param userIds 用户ID列表
     * @return 用户ID到用户名的映射
     */
    @PostMapping("/info/username-map")
    R<Map<Long, String>> getUsernameMap(@RequestBody List<Long> userIds);
}



