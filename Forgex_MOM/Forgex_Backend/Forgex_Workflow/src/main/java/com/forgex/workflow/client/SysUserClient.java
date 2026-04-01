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
package com.forgex.workflow.client;

import com.forgex.common.web.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 系统服务用户 Feign 客户端。
 * <p>
 * 用于工作流模块调用系统服务的用户相关接口。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@FeignClient(name = "forgex-sys", contextId = "sysUserClient")
public interface SysUserClient {
    
    /**
     * 根据部门 ID 列表获取用户 ID 列表
     * 
     * @param deptIds 部门 ID 列表
     * @return 用户 ID 列表
     */
    @PostMapping("/sys/user/internal/listUserIdsByDeptIds")
    R<List<Long>> listUserIdsByDeptIds(@RequestBody List<Long> deptIds);
    
    /**
     * 根据角色 ID 列表获取用户 ID 列表
     * 
     * @param roleIds 角色 ID 列表
     * @return 用户 ID 列表
     */
    @PostMapping("/sys/user/internal/listUserIdsByRoleIds")
    R<List<Long>> listUserIdsByRoleIds(@RequestBody List<Long> roleIds);
    
    /**
     * 根据职位 ID 列表获取用户 ID 列表
     * 
     * @param positionIds 职位 ID 列表
     * @return 用户 ID 列表
     */
    @PostMapping("/sys/user/internal/listUserIdsByPositionIds")
    R<List<Long>> listUserIdsByPositionIds(@RequestBody List<Long> positionIds);
}