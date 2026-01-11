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

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.enums.TenantTypeEnum;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.entity.SysModule;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysRoleMenu;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.entity.SysTenant;
import com.forgex.sys.domain.entity.SysTenantMenuCopyRule;
import com.forgex.sys.mapper.SysMenuMapper;
import com.forgex.sys.mapper.SysModuleMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysRoleMenuMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.mapper.SysTenantMapper;
import com.forgex.sys.mapper.SysTenantMenuCopyRuleMapper;
import com.forgex.sys.service.ITenantInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * 租户初始化服务实现类
 * <p>
 * 提供租户初始化的业务逻辑实现，用于在新增租户时自动初始化模块、菜单、角色、用户等基础数据
 * </p>
 * <p>
 * <strong>业务规则：</strong>
 * <ul>
 *   <li>主租户：创建admin账号，不设置租户ID，可关联多个租户</li>
 *   <li>其它租户（客户租户、供应商租户）：创建administrator账号，设置租户ID为该租户，只能在该租户下使用</li>
 * </ul>
 * </p>
 * 
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.sys.service.ITenantInitService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantInitServiceImpl implements ITenantInitService {
    
    private final SysModuleMapper moduleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysTenantMapper tenantMapper;
    private final SysTenantMenuCopyRuleMapper tenantMenuCopyRuleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initTenant(Long tenantId, String tenantName, TenantTypeEnum tenantType) {
        log.info("开始初始化租户，租户ID：{}，租户名称：{}，租户类型：{}", tenantId, tenantName, tenantType);
        
        // 1. 复制系统模块到新租户
        Long moduleId = copyModuleToTenant(tenantId);
        
        // 2. 复制系统模块下的菜单到新租户（按租户类型与配置过滤）
        List<Long> menuIds = copyMenusToTenant(tenantId, moduleId, tenantType);
        
        // 3. 创建系统管理员角色
        Long roleId = createAdminRole(tenantId);
        
        // 4. 根据租户类型创建管理员账号
        Long userId;
        if (TenantTypeEnum.MAIN_TENANT.equals(tenantType)) {
            // 主租户：创建admin账号，不设置租户ID（可关联多个租户）
            userId = createAdminUser(null, tenantName, "admin");
        } else {
            // 其它租户（客户租户、供应商租户）：创建administrator账号，设置租户ID
            userId = createAdministratorUser(tenantId, tenantName);
        }
        
        // 5. 将管理员账号关联到系统管理员角色
        bindUserToRole(userId, roleId, tenantId);
        
        // 6. 将系统管理员角色关联到所有菜单
        bindRoleToMenus(roleId, menuIds, tenantId);
        
        log.info("租户初始化完成，租户ID：{}", tenantId);
    }
    
    /**
     * 从模板租户复制系统模块到新租户
     * <p>
     * 业务模型：每个租户拥有完全隔离的模块与菜单数据，但可以基于“模板租户”（通常为主租户）
     * 复制一份作为初始数据。这里通过主租户作为模板来源。
     * </p>
     *
     * @param tenantId 新租户ID
     * @return 为新租户创建的系统模块ID；若未找到模板则返回null
     */
    private Long copyModuleToTenant(Long tenantId) {
        Long templateTenantId = findTemplateTenantId(tenantId);
        if (templateTenantId == null) {
            log.warn("复制系统模块失败：未找到可作为模板的主租户，租户ID：{}", tenantId);
            return null;
        }

        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysModule::getTenantId, templateTenantId)
               .eq(SysModule::getCode, "sys")
               .eq(SysModule::getDeleted, false)
               .last("LIMIT 1");
        
        SysModule sysModule = moduleMapper.selectOne(wrapper);
        if (sysModule == null) {
            log.warn("复制系统模块失败：模板租户下未找到sys模块，模板租户ID：{}", templateTenantId);
            return null;
        }
        
        SysModule newModule = new SysModule();
        BeanUtils.copyProperties(sysModule, newModule);
        newModule.setId(null);
        newModule.setTenantId(tenantId);
        moduleMapper.insert(newModule);
        
        log.info("复制系统模块到租户，模板租户ID：{}，新模块ID：{}，新租户ID：{}", 
                templateTenantId, newModule.getId(), tenantId);
        
        return newModule.getId();
    }
    
    /**
     * 复制系统模块下的菜单到新租户
     * <p>
     * 从模板租户（主租户）对应的系统模块下读取菜单，完整复制为新租户自己的菜单数据，
     * 并返回“新菜单记录”的ID列表，用于后续角色权限绑定。
     * </p>
     *
     * @param tenantId 新租户ID
     * @param moduleId 新租户的系统模块ID
     * @param tenantType 租户类型
     * @return 新创建的菜单ID列表（属于新租户）
     */
    private List<Long> copyMenusToTenant(Long tenantId, Long moduleId, TenantTypeEnum tenantType) {
        if (moduleId == null) {
            log.warn("复制系统菜单失败：模块ID为空，租户ID：{}", tenantId);
            return List.of();
        }

        Long templateTenantId = findTemplateTenantId(tenantId);
        if (templateTenantId == null) {
            log.warn("复制系统菜单失败：未找到可作为模板的主租户，租户ID：{}", tenantId);
            return List.of();
        }

        // 查找模板租户的 sys 模块
        LambdaQueryWrapper<SysModule> moduleWrapper = new LambdaQueryWrapper<>();
        moduleWrapper.eq(SysModule::getTenantId, templateTenantId)
                     .eq(SysModule::getCode, "sys")
                     .eq(SysModule::getDeleted, false)
                     .last("LIMIT 1");
        SysModule templateModule = moduleMapper.selectOne(moduleWrapper);
        if (templateModule == null) {
            log.warn("复制系统菜单失败：模板租户下未找到sys模块，模板租户ID：{}", templateTenantId);
            return List.of();
        }

        // 查找模板租户该模块下的所有菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getTenantId, templateTenantId)
               .eq(SysMenu::getModuleId, templateModule.getId())
               .eq(SysMenu::getDeleted, false);
        
        List<SysMenu> templateMenus = menuMapper.selectList(wrapper);
        
        if (templateMenus.isEmpty()) {
            log.warn("复制系统菜单失败：模板租户下未找到系统菜单，模板租户ID：{}，模块ID：{}", 
                    templateTenantId, templateModule.getId());
            return List.of();
        }

        List<String> excludedPrefixes = getExcludedPermPrefixes(tenantType);

        // 第一遍：复制菜单，暂时保留父ID为模板菜单的父ID，记录ID映射关系
        Map<Long, Long> idMap = new HashMap<>();
        for (SysMenu templateMenu : templateMenus) {
            if (isMenuExcluded(templateMenu, excludedPrefixes)) {
                continue;
            }
            Long templateId = templateMenu.getId();
            SysMenu newMenu = new SysMenu();
            BeanUtils.copyProperties(templateMenu, newMenu);
            newMenu.setId(null);
            newMenu.setTenantId(tenantId);
            newMenu.setModuleId(moduleId);
            menuMapper.insert(newMenu);
            idMap.put(templateId, newMenu.getId());
        }

        // 第二遍：修正新菜单的父ID，使其指向同一批新菜单中的父节点
        for (SysMenu templateMenu : templateMenus) {
            if (isMenuExcluded(templateMenu, excludedPrefixes)) {
                continue;
            }
            Long templateId = templateMenu.getId();
            Long templateParentId = templateMenu.getParentId();
            if (templateParentId == null || templateParentId == 0L) {
                continue;
            }
            Long newMenuId = idMap.get(templateId);
            Long newParentId = idMap.get(templateParentId);
            if (newMenuId != null && newParentId != null) {
                SysMenu update = new SysMenu();
                update.setId(newMenuId);
                update.setParentId(newParentId);
                menuMapper.updateById(update);
            }
        }

        List<Long> newMenuIds = new ArrayList<>(idMap.values());
        log.info("复制系统菜单到租户成功，模板租户ID：{}，新租户ID：{}，菜单数量：{}", 
                templateTenantId, tenantId, newMenuIds.size());
        
        return newMenuIds;
    }

    private List<String> getExcludedPermPrefixes(TenantTypeEnum tenantType) {
        if (tenantType == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysTenantMenuCopyRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenantMenuCopyRule::getTenantType, tenantType.getCode())
               .eq(SysTenantMenuCopyRule::getEnabled, true);
        List<SysTenantMenuCopyRule> rules = tenantMenuCopyRuleMapper.selectList(wrapper);
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (SysTenantMenuCopyRule rule : rules) {
            String prefix = rule.getPermPrefix();
            if (prefix == null) {
                continue;
            }
            String trimmed = prefix.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private boolean isMenuExcluded(SysMenu menu, List<String> excludedPrefixes) {
        if (menu == null || excludedPrefixes == null || excludedPrefixes.isEmpty()) {
            return false;
        }
        String permKey = menu.getPermKey();
        if (permKey == null || permKey.isBlank()) {
            return false;
        }
        for (String prefix : excludedPrefixes) {
            if (permKey.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找用于复制模块与菜单数据的“模板租户ID”
     * <p>
     * 当前策略：选择系统中的主租户（TenantTypeEnum.MAIN_TENANT）作为模板来源，
     * 且要求模板租户与当前新建租户不是同一条记录。
     * </p>
     *
     * @param currentTenantId 当前正在初始化的租户ID
     * @return 模板租户ID；若不存在合适模板则返回null
     */
    private Long findTemplateTenantId(Long currentTenantId) {
        LambdaQueryWrapper<SysTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysTenant::getTenantType, TenantTypeEnum.MAIN_TENANT)
               .eq(SysTenant::getDeleted, false)
               .last("LIMIT 1");
        SysTenant mainTenant = tenantMapper.selectOne(wrapper);
        if (mainTenant == null) {
            log.warn("未找到主租户，无法作为菜单/模块模板");
            return null;
        }
        if (currentTenantId != null && currentTenantId.equals(mainTenant.getId())) {
            // 当前租户本身就是主租户（通常是通过初始化SQL创建），不作为模板来源
            log.info("当前租户即为主租户，不使用自身作为模板，tenantId={}", currentTenantId);
            return null;
        }
        return mainTenant.getId();
    }
    
    /**
     * 创建系统管理员角色
     * <p>
     * 在新租户下创建系统管理员角色
     * </p>
     * 
     * @param tenantId 租户ID
     * @return 新创建的角色ID
     */
    private Long createAdminRole(Long tenantId) {
        SysRole role = new SysRole();
        role.setRoleName("系统管理员");
        role.setRoleKey("admin");
        role.setDescription("系统管理员，拥有所有权限");
        role.setStatus(true);
        role.setTenantId(tenantId);
        roleMapper.insert(role);
        
        log.info("创建系统管理员角色，角色ID：{}，租户ID：{}", role.getId(), tenantId);
        
        return role.getId();
    }
    
    /**
     * 创建admin用户（主租户专用）
     * <p>
     * 在主租户下创建admin用户，设置租户ID为主租户ID，可关联多个租户
     * </p>
     * 
     * @param tenantId 租户ID
     * @param tenantName 租户名称
     * @param account 账号
     * @return 新创建的用户ID
     */
    private Long createAdminUser(Long tenantId, String tenantName, String account) {
        SysUser user = new SysUser();
        user.setAccount(account);
        user.setUsername("系统管理员");
        user.setPassword(BCrypt.hashpw("admin123"));
        user.setEmail(account + "@" + tenantName + ".com");
        user.setStatus(true);
        user.setTenantId(tenantId);
        userMapper.insert(user);
        
        log.info("创建{}用户，用户ID：{}，租户ID：{}", account, user.getId(), tenantId);
        
        return user.getId();
    }
    
    /**
     * 创建administrator用户（其它租户专用）
     * <p>
     * 在客户租户或供应商租户下创建administrator用户，设置租户ID，只能在该租户下使用
     * </p>
     * 
     * @param tenantId 租户ID
     * @param tenantName 租户名称
     * @return 新创建的用户ID
     */
    private Long createAdministratorUser(Long tenantId, String tenantName) {
        SysUser user = new SysUser();
        user.setAccount("administrator");
        user.setUsername("系统管理员");
        user.setPassword(BCrypt.hashpw("admin123"));
        user.setEmail("administrator@" + tenantName + ".com");
        user.setStatus(true);
        user.setTenantId(tenantId);
        userMapper.insert(user);
        
        log.info("创建administrator用户，用户ID：{}，租户ID：{}", user.getId(), tenantId);
        
        return user.getId();
    }
    
    /**
     * 将用户关联到角色
     * <p>
     * 创建用户-角色关联记录
     * </p>
     * 
     * @param userId  用户ID
     * @param roleId  角色ID
     * @param tenantId 租户ID
     */
    private void bindUserToRole(Long userId, Long roleId, Long tenantId) {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setTenantId(tenantId);
        userRoleMapper.insert(userRole);
        
        log.info("绑定用户到角色，用户ID：{}，角色ID：{}，租户ID：{}", userId, roleId, tenantId);
    }
    
    /**
     * 将角色关联到菜单
     * <p>
     * 批量创建角色-菜单关联记录
     * </p>
     * 
     * @param roleId   角色ID
     * @param menuIds  菜单ID列表
     * @param tenantId 租户ID
     */
    private void bindRoleToMenus(Long roleId, List<Long> menuIds, Long tenantId) {
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenu.setTenantId(tenantId);
            roleMenuMapper.insert(roleMenu);
        }
        
        log.info("绑定角色到菜单，角色ID：{}，菜单数量：{}，租户ID：{}", roleId, menuIds.size(), tenantId);
    }
}
