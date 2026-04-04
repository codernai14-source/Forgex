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
import com.forgex.common.config.ConfigService;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.crypto.CryptoProviders;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.enums.TenantTypeEnum;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.entity.SysModule;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysRoleMenu;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.entity.SysTenant;
import com.forgex.sys.domain.entity.SysTenantInitTask;
import com.forgex.sys.domain.entity.SysTenantMenuCopyRule;
import com.forgex.sys.mapper.SysMenuMapper;
import com.forgex.sys.mapper.SysModuleMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysRoleMenuMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.mapper.SysTenantMapper;
import com.forgex.sys.mapper.SysTenantMenuCopyRuleMapper;
import com.forgex.sys.mapper.SysTenantInitTaskMapper;
import com.forgex.sys.service.ITenantInitService;
import com.forgex.sys.service.SsePushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.Executor;

/**
 * 租户初始化服务实现类（异步版本）
 * <p>
 * 提供租户异步初始化的业务逻辑实现，用于在新增租户时自动初始化模块、菜单、角色、用户等基础数据。
 * 支持 SSE 实时进度推送，前端可查看初始化进度。
 * </p>
 * <p>
 * <strong>业务规则：</strong>
 * <ul>
 *   <li>主租户：创建 admin 账号，默认密码 Aa123456</li>
 *   <li>其它租户：根据租户 ID 和编码动态生成唯一账号（admin_{tenantCode}_{后 4 位}），默认密码 Aa123456</li>
 * </ul>
 * </p>
 * 
 * @author Forgex Team
 * @version 2.0.0
 * @see com.forgex.sys.service.ITenantInitService
 * @see SysTenantInitTask
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantInitServiceImpl implements ITenantInitService {
    private static final String KEY_SECURITY_PASSWORD_POLICY = "security.password.policy";

    private final SysModuleMapper moduleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysTenantMapper tenantMapper;
    private final SysTenantMenuCopyRuleMapper tenantMenuCopyRuleMapper;
    private final SysTenantInitTaskMapper tenantInitTaskMapper;
    private final SsePushService ssePushService;
    private final Executor tenantInitExecutor;
    private final ConfigService configService;

    @Override
    @Async("tenantInitExecutor")
    public void initTenant(Long tenantId, String tenantName, TenantTypeEnum tenantType) {
        log.info("开始异步初始化租户，租户 ID：{}，租户名称：{}，租户类型：{}", tenantId, tenantName, tenantType);
        
        // 创建初始化任务
        SysTenantInitTask task = new SysTenantInitTask();
        task.setTenantId(tenantId);
        task.setTenantName(tenantName);
        task.setTenantType(tenantType.getCode());
        task.setStatus("PENDING");
        task.setProgress(0);
        task.setCurrentStep("等待初始化开始");
        tenantInitTaskMapper.insert(task);
        
        String taskId = task.getId().toString();
        
        try {
            // 更新状态为运行中
            task.setStatus("RUNNING");
            task.setStartTime(LocalDateTime.now());
            tenantInitTaskMapper.updateById(task);
            
            // 1. 复制系统模块（进度 10%）
            updateTaskProgress(task, 10, "正在复制系统模块...");
            Long moduleId = copyModuleToTenant(tenantId);
            
            // 2. 复制菜单（进度 30%）
            updateTaskProgress(task, 30, "正在复制菜单权限...");
            List<Long> menuIds = copyMenusToTenant(tenantId, moduleId, tenantType);
            
            // 3. 创建管理员角色（进度 40%）
            updateTaskProgress(task, 40, "正在创建管理员角色...");
            Long roleId = createAdminRole(tenantId);
            
            // 4. 创建管理员账号（进度 50%）
            updateTaskProgress(task, 50, "正在创建管理员账号...");
            Long userId;
            if (TenantTypeEnum.MAIN_TENANT.equals(tenantType)) {
                userId = createAdminUser(null, tenantName, "admin");
            } else {
                // 根据租户 ID 和编码生成唯一账号
                String dynamicAccount = generateDynamicAccount(tenantId, tenantName);
                userId = createAdministratorUser(tenantId, tenantName, dynamicAccount);
            }
            
            // 5. 绑定用户角色（进度 60%）
            updateTaskProgress(task, 60, "正在绑定用户角色...");
            bindUserToRole(userId, roleId, tenantId);
            
            // 6. 绑定角色菜单（进度 70%）
            updateTaskProgress(task, 70, "正在绑定角色菜单...");
            bindRoleToMenus(roleId, menuIds, tenantId);
            
            // 7. 完成（进度 100%）
            updateTaskProgress(task, 100, "初始化完成");
            task.setStatus("SUCCESS");
            task.setEndTime(LocalDateTime.now());
            tenantInitTaskMapper.updateById(task);
            
            // 推送成功消息
            ssePushService.pushComplete(taskId, true, "租户初始化成功");
            
            log.info("租户初始化成功，租户 ID：{}，任务 ID：{}", tenantId, taskId);
            
        } catch (Exception e) {
            log.error("租户初始化失败，租户 ID：{}，任务 ID：{}", tenantId, taskId, e);
            task.setStatus("FAILED");
            task.setErrorMessage(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            tenantInitTaskMapper.updateById(task);
            
            // 推送失败消息
            ssePushService.pushComplete(taskId, false, "初始化失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新任务进度
     */
    private void updateTaskProgress(SysTenantInitTask task, int progress, String currentStep) {
        task.setProgress(progress);
        task.setCurrentStep(currentStep);
        tenantInitTaskMapper.updateById(task);
        
        // 推送进度到前端
        ssePushService.pushProgress(task.getId().toString(), progress, currentStep);
    }
    
    /**
     * 生成动态管理员账号
     * 规则：admin_{tenantCode}_{tenantId 后 4 位}
     */
    private String generateDynamicAccount(Long tenantId, String tenantName) {
        String tenantCode = tenantName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String suffix = String.valueOf(tenantId).substring(Math.max(0, String.valueOf(tenantId).length() - 4));
        return "admin_" + tenantCode + "_" + suffix;
    }
    
    /**
     * 从模板租户复制系统模块到新租户
     */
    private Long copyModuleToTenant(Long tenantId) {
        Long templateTenantId = findTemplateTenantId(tenantId);
        if (templateTenantId == null) {
            log.warn("复制系统模块失败：未找到可作为模板的主租户，租户 ID：{}", tenantId);
            return null;
        }

        LambdaQueryWrapper<SysModule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysModule::getTenantId, templateTenantId)
               .eq(SysModule::getCode, "sys")
               .eq(SysModule::getDeleted, false)
               .last("LIMIT 1");
        
        SysModule sysModule = moduleMapper.selectOne(wrapper);
        if (sysModule == null) {
            log.warn("复制系统模块失败：模板租户下未找到 sys 模块，模板租户 ID：{}", templateTenantId);
            return null;
        }
        
        SysModule newModule = new SysModule();
        BeanUtils.copyProperties(sysModule, newModule);
        newModule.setId(null);
        newModule.setTenantId(tenantId);
        moduleMapper.insert(newModule);
        
        log.info("复制系统模块到租户，模板租户 ID：{}，新模块 ID：{}，新租户 ID：{}", 
                templateTenantId, newModule.getId(), tenantId);
        
        return newModule.getId();
    }
    
    /**
     * 复制系统模块下的菜单到新租户
     */
    private List<Long> copyMenusToTenant(Long tenantId, Long moduleId, TenantTypeEnum tenantType) {
        if (moduleId == null) {
            log.warn("复制系统菜单失败：模块 ID 为空，租户 ID：{}", tenantId);
            return List.of();
        }

        Long templateTenantId = findTemplateTenantId(tenantId);
        if (templateTenantId == null) {
            log.warn("复制系统菜单失败：未找到可作为模板的主租户，租户 ID：{}", tenantId);
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
            log.warn("复制系统菜单失败：模板租户下未找到 sys 模块，模板租户 ID：{}", templateTenantId);
            return List.of();
        }

        // 查找模板租户该模块下的所有菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getTenantId, templateTenantId)
               .eq(SysMenu::getModuleId, templateModule.getId())
               .eq(SysMenu::getDeleted, false);
        
        List<SysMenu> templateMenus = menuMapper.selectList(wrapper);
        
        if (templateMenus.isEmpty()) {
            log.warn("复制系统菜单失败：模板租户下未找到系统菜单，模板租户 ID：{}，模块 ID：{}", 
                    templateTenantId, templateModule.getId());
            return List.of();
        }

        List<String> excludedPermPrefixes = getExcludedPermPrefixes(tenantType);

        // 第一遍：复制菜单，记录 ID 映射关系
        Map<Long, Long> idMap = new HashMap<>();
        for (SysMenu templateMenu : templateMenus) {
            // 排除规则 1：根据 perm_prefix 排除（现有逻辑）
            if (isMenuExcluded(templateMenu, excludedPermPrefixes)) {
                continue;
            }
            
            // 排除规则 2：根据租户类型过滤（新增逻辑）
            if (!matchesTenantType(templateMenu, tenantType)) {
                log.debug("菜单租户类型不匹配，跳过复制，菜单 ID：{}，菜单 permKey：{}，菜单 tenantType：{}", 
                        templateMenu.getId(), templateMenu.getPermKey(), templateMenu.getTenantType());
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

        // 第二遍：修正新菜单的父 ID
        for (SysMenu templateMenu : templateMenus) {
            if (isMenuExcluded(templateMenu, excludedPermPrefixes)) {
                continue;
            }
            
            // 跳过租户类型不匹配的菜单
            if (!matchesTenantType(templateMenu, tenantType)) {
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
        log.info("复制系统菜单到租户成功，模板租户 ID：{}，新租户 ID：{}，菜单数量：{}", 
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
     * 判断菜单是否匹配租户类型
     * <p>
     * 菜单的 tenantType 为 "PUBLIC" 或 null 表示适用于所有租户类型，直接返回 true。
     * 否则需要精确匹配租户类型。
     * </p>
     *
     * @param menu 菜单实体
     * @param tenantType 目标租户类型
     * @return true=匹配，false=不匹配
     */
    private boolean matchesTenantType(SysMenu menu, TenantTypeEnum tenantType) {
        if (menu == null) {
            return false;
        }
        
        // 菜单的 tenantType 为 "PUBLIC" 或 null，表示适用于所有租户类型
        String menuTenantType = menu.getTenantType();
        if (menuTenantType == null || menuTenantType.trim().isEmpty() || "PUBLIC".equals(menuTenantType)) {
            return true;
        }
        
        // 精确匹配租户类型
        if (tenantType == null) {
            return false;
        }
        
        return menuTenantType.equals(tenantType.name());
    }

    /**
     * 查找模板租户 ID
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
            log.info("当前租户即为主租户，不使用自身作为模板，tenantId={}", currentTenantId);
            return null;
        }
        return mainTenant.getId();
    }
    
    /**
     * 创建系统管理员角色
     */
    private Long createAdminRole(Long tenantId) {
        SysRole role = new SysRole();
        role.setRoleName("系统管理员");
        role.setRoleKey("admin");
        role.setDescription("系统管理员，拥有所有权限");
        role.setStatus(true);
        role.setTenantId(tenantId);
        roleMapper.insert(role);
        
        log.info("创建系统管理员角色，角色 ID：{}，租户 ID：{}", role.getId(), tenantId);
        
        return role.getId();
    }
    
    /**
     * 创建 admin 用户（主租户专用）
     */
    private Long createAdminUser(Long tenantId, String tenantName, String account) {
        SysUser user = new SysUser();
        user.setAccount(account);
        user.setUsername("系统管理员");
        user.setPassword(encryptDefaultPassword());
        user.setEmail(account + "@" + tenantName + ".com");
        user.setStatus(true);
        user.setTenantId(tenantId);
        userMapper.insert(user);
        
        log.info("创建{}用户，用户 ID：{}，租户 ID：{}", account, user.getId(), tenantId);
        
        return user.getId();
    }
    
    /**
     * 创建 administrator 用户（其它租户专用）
     */
    private Long createAdministratorUser(Long tenantId, String tenantName, String account) {
        SysUser user = new SysUser();
        user.setAccount(account);
        user.setUsername("系统管理员");
        user.setPassword(encryptDefaultPassword());
        user.setEmail(account + "@" + tenantName + ".com");
        user.setStatus(true);
        user.setTenantId(tenantId);
        userMapper.insert(user);
        
        log.info("创建 administrator 用户，用户 ID：{}，租户 ID：{}，账号：{}", user.getId(), tenantId, account);
        
        return user.getId();
    }
    
    /**
     * 将用户关联到角色
     */
    private void bindUserToRole(Long userId, Long roleId, Long tenantId) {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setTenantId(tenantId);
        userRoleMapper.insert(userRole);
        
        log.info("绑定用户到角色，用户 ID：{}，角色 ID：{}，租户 ID：{}", userId, roleId, tenantId);
    }
    
    /**
     * 将角色关联到菜单
     */
    private void bindRoleToMenus(Long roleId, List<Long> menuIds, Long tenantId) {
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenu.setTenantId(tenantId);
            roleMenuMapper.insert(roleMenu);
        }
        
        log.info("绑定角色到菜单，角色 ID：{}，菜单数量：{}，租户 ID：{}", roleId, menuIds.size(), tenantId);
    }

    private String encryptDefaultPassword() {
        PasswordPolicyConfig policy = configService.getJson(KEY_SECURITY_PASSWORD_POLICY, PasswordPolicyConfig.class, null);
        String store = policy == null || !org.springframework.util.StringUtils.hasText(policy.getStore()) ? "bcrypt" : policy.getStore();
        String defaultPassword = policy == null || !org.springframework.util.StringUtils.hasText(policy.getDefaultPassword())
            ? "Aa123456"
            : policy.getDefaultPassword();
        CryptoPasswordProvider provider = CryptoProviders.resolve(store, configService);
        return provider.encrypt(defaultPassword);
    }
}
