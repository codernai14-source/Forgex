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
