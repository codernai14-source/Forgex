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
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.mapper.SysMenuMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘Service实现类
 * 
 * @author coder_nai@163.com
 * @date 2025-01-08
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public Map<String, Object> getStatistics(Long tenantId) {
        Map<String, Object> result = new HashMap<>();
        
        // 统计用户数量
        Long userCount = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getDeleted, 0)
        );
        
        // 统计角色数量
        Long roleCount = roleMapper.selectCount(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, 0)
        );
        
        // 统计菜单数量
        Long menuCount = menuMapper.selectCount(
            new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getTenantId, tenantId)
                .eq(SysMenu::getDeleted, 0)
        );
        
        // 在线用户数（这里简化处理，实际应该从Redis或Session中获取）
        // TODO: 实现真实的在线用户统计
        Long onlineUsers = 0L;
        
        result.put("userCount", userCount);
        result.put("roleCount", roleCount);
        result.put("menuCount", menuCount);
        result.put("onlineUsers", onlineUsers);
        
        return result;
    }
}
