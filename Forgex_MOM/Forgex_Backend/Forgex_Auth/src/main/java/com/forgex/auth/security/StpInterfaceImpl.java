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
package com.forgex.auth.security;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限与角色数据提供实现
 * 通过登录ID动态返回权限与角色列表
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    /**
     * 返回权限列表
     * @param loginId 登录ID
     * @param loginType 登录类型
     * @return 权限标识列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        list.add("sys:read");
        return list;
    }

    /**
     * 返回角色列表
     * @param loginId 登录ID
     * @param loginType 登录类型
     * @return 角色标识列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> list = new ArrayList<>();
        String uid = String.valueOf(loginId);
        list.add("user");
        if ("admin".equals(uid)) {
            list.add("admin");
        }
        return list;
    }
}
