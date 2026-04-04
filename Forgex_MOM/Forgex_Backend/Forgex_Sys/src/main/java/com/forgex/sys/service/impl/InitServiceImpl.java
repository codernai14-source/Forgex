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

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.forgex.common.config.ConfigService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.sys.domain.entity.*;
import com.forgex.sys.domain.param.InitApplyParam;
import com.forgex.sys.domain.vo.InitStatusVO;
import com.forgex.sys.mapper.*;
import com.forgex.sys.service.InitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.forgex.common.tenant.TenantContextIgnore;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.time.Duration;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.core.util.HexUtil;
import com.forgex.common.domain.config.CaptchaConfig;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.domain.config.CryptoTransportConfig;
import com.forgex.common.domain.config.InitStatusConfig;
import com.forgex.common.domain.config.LoginSecurityConfig;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.crypto.CryptoProviders;

/**
 * 系统初始化服务实现。
 * <p>
 * 负责处理一次性系统初始化：写入安全配置（验证码、密码策略、传输密钥）、
 * 清理并重建 admin 库中的租户/用户/角色及其绑定关系，保证流程幂等。
 * <p>
 * 使用：通过 {@link com.forgex.sys.service.InitService#apply(com.forgex.sys.domain.param.InitApplyParam)}
 * 接收前端初始化参数并执行；初始化密码会按策略校验，不符合要求将被拦截。
 * <p>
 * 可扩展性：
 * - 安全策略可在 {@link #writeSecurityConfigsToCommon(InitApplyParam)} 中扩展配置项；
 * - 数据种子在 {@link #seedAdminData(InitApplyParam)} 中扩展角色/租户/用户构造；
 * - 校验规则在 {@link #validatePassword(String, com.forgex.common.domain.config.PasswordPolicyConfig)} 中扩展。
 */
@Service
public class InitServiceImpl implements InitService {
    @Autowired
    private ConfigService configService;
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redis;

    // admin 数据源 DAO
    @Autowired private SysUserMapper userMapper;
    @Autowired private SysTenantMapper tenantMapper;
    @Autowired private SysUserTenantMapper userTenantMapper;
    @Autowired private SysRoleMapper roleMapper;
    @Autowired private SysUserRoleMapper userRoleMapper;
    @Autowired private com.forgex.sys.mapper.SysModuleMapper moduleMapper;
    @Autowired private com.forgex.sys.mapper.SysMenuMapper menuMapper;
    @Autowired private com.forgex.sys.mapper.SysRoleMenuMapper roleMenuMapper;

    /**
     * 查询初始化状态
     * <p>读取合并后的初始化状态配置 {@code sys.init.status} 并计算是否首次使用。</p>
     * <p>返回结果包含使用次数和是否首次使用标志。</p>
     * @return 初始化状态信息，包含使用次数和是否首次使用标志
     * @see InitStatusVO
     */
    @Override
    public R<InitStatusVO> status() {
        InitStatusConfig cfg =
                configService.getJson("sys.init.status", InitStatusConfig.class, null); // 读取初始化状态配置实体
        Integer count = (cfg == null || cfg.getUsageCount() == null) ? 0 : cfg.getUsageCount(); // 提取使用次数（默认0）
        Boolean completed = (cfg == null || cfg.getInitCompleted() == null) ? false : cfg.getInitCompleted(); // 提取完成标志（默认false）
        InitStatusVO vo = new InitStatusVO(); // 创建返回对象
        vo.setUsageCount(count); // 设置使用次数
        vo.setFirstUse(count <= 0 || !completed); // 设置是否首次使用（使用次数<=0或未完成）
        return R.ok(vo); // 返回成功结果
    }

    /**
     * 执行初始化：
     * - 加分布式锁防并发
     * - 写入安全配置（验证码、密码策略、传输加密密钥）到 common
     * - 清空 admin 相关表并重建数据
     * - 标记初始化完成
     */
    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public R<Boolean> apply(InitApplyParam param) {
        String lockKey = "sys:init:lock"; // 分布式锁键（避免并发初始化）
        Boolean ok = redis.opsForValue().setIfAbsent(lockKey, "1", Duration.ofMinutes(2));
        if (Boolean.FALSE.equals(ok)) {
            return R.fail(CommonPrompt.INITIALIZING);
        }
        try {
            writeSecurityConfigsToCommon(param);
            PasswordPolicyConfig policyCheck = configService.getJson("security.password.policy", PasswordPolicyConfig.class, null);
            String initPwd = (param == null || param.getInitialPassword() == null || param.getInitialPassword().isEmpty()) ? "Aa123456" : param.getInitialPassword();
            if (!validatePassword(initPwd, policyCheck)) { return R.fail(CommonPrompt.INIT_PASSWORD_INVALID); }
            clearAdminTables(); // 清空 admin 相关业务表（忽略租户过滤，保证幂等）
            seedAdminData(param); // 重建租户、用户、角色以及绑定关系
            InitStatusConfig status = new InitStatusConfig(); // 创建初始化状态实体
            status.setUsageCount(1); // 设置使用次数为1
            status.setInitCompleted(true); // 设置初始化完成标志
            configService.setJson("sys.init.status", status); // 合并写入到配置库
            return R.ok(true); // 返回成功
        } finally {
            redis.delete(lockKey); // 释放分布式锁
        }
    }

    /**
     * 将安全配置写入 common 配置库（强制选中不可更改的默认集）。
     * 包含验证码模式、密码策略（强/弱、存储算法）以及传输加密密钥生成与存储。
     */
    private void writeSecurityConfigsToCommon(InitApplyParam p) {
        CaptchaConfig captcha = CaptchaConfig.defaults();
        String mode = (p != null && Boolean.TRUE.equals(p.getCaptchaEnabled())) ? p.getCaptchaMode() : "none";
        captcha.setMode(mode);
        configService.setJson("login.captcha", captcha);

        PasswordPolicyConfig pwdPolicy = new PasswordPolicyConfig();
        String strength = p == null ? null : p.getPwdStrength();
        if ("high".equalsIgnoreCase(strength)) {
            pwdPolicy.setMinLength(8);
            pwdPolicy.setRequireNumbers(true);
            pwdPolicy.setRequireUppercase(true);
            pwdPolicy.setRequireLowercase(true);
            pwdPolicy.setRequireSymbols(true);
        } else if ("normal".equalsIgnoreCase(strength)) {
            pwdPolicy.setMinLength(8);
            pwdPolicy.setRequireNumbers(true);
            pwdPolicy.setRequireUppercase(true);
            pwdPolicy.setRequireLowercase(true);
            pwdPolicy.setRequireSymbols(false);
        } else {
            pwdPolicy.setMinLength(6);
            pwdPolicy.setRequireNumbers(true);
            pwdPolicy.setRequireUppercase(false);
            pwdPolicy.setRequireLowercase(false);
            pwdPolicy.setRequireSymbols(false);
        }
        String initPwd = (p == null || !StringUtils.hasText(p.getInitialPassword())) ? "Aa123456" : p.getInitialPassword();
        pwdPolicy.setDefaultPassword(initPwd);
        String store = p == null ? null : p.getPasswordStore();
        String storeLower = store == null ? "sm2" : store.toLowerCase();
        pwdPolicy.setStore(storeLower);
        configService.setJson("security.password.policy", pwdPolicy);
        configService.setJson("security.login.failure", LoginSecurityConfig.defaults());

        if ("sm4".equalsIgnoreCase(storeLower)) {
            byte[] k = new byte[16]; new SecureRandom().nextBytes(k);
            String keyHex = HexUtil.encodeHexStr(k);
            Map<String, String> sm4Cfg = new HashMap<>();
            sm4Cfg.put("keyHex", keyHex);
            configService.setJson("security.crypto.sm4", sm4Cfg);
        }

        SM2 sm2 = new SM2();
        String pub = sm2.getPublicKeyBase64();
        String pri = sm2.getPrivateKeyBase64();
        CryptoTransportConfig crypto = new CryptoTransportConfig();
        crypto.setAlgorithm("SM2");
        crypto.setPublicKey(pub);
        crypto.setPrivateKey(pri);
        crypto.setCipher("BCD");
        configService.setJson("security.crypto.transport", crypto);
    }

    /**
     * 清空 admin 相关数据表（先子表后父表）。
     * 保证初始化幂等，临时忽略租户过滤。
     */
    private void clearAdminTables() {
        TenantContextIgnore.setIgnore(true); // 临时忽略租户过滤
        try {
            roleMenuMapper.delete(new LambdaQueryWrapper<>()); // 清空角色-菜单绑定
            userRoleMapper.delete(new LambdaQueryWrapper<>()); // 清空用户-角色绑定
            userTenantMapper.delete(new LambdaQueryWrapper<>()); // 清空用户-租户绑定
            menuMapper.delete(new LambdaQueryWrapper<>()); // 清空菜单
            moduleMapper.delete(new LambdaQueryWrapper<>()); // 清空模块
            roleMapper.delete(new LambdaQueryWrapper<>()); // 清空角色表
            tenantMapper.delete(new LambdaQueryWrapper<>()); // 清空租户表
            userMapper.delete(new LambdaQueryWrapper<>()); // 清空用户表
        } finally {
            TenantContextIgnore.clear(); // 恢复租户过滤
        }
    }

    /**
     * 按密码策略校验初始化密码是否合规。
     * @param pwd    明文密码
     * @param policy 策略配置（最小长度、是否要求数字/大小写/符号）
     * @return 是否合规
     */
    private boolean validatePassword(String pwd, PasswordPolicyConfig policy) {
        if (pwd == null) return false;
        int len = pwd.length();
        Integer min = policy == null ? null : policy.getMinLength();
        if (min != null && len < min) return false;
        Boolean reqNum = policy == null ? null : policy.getRequireNumbers();
        if (Boolean.TRUE.equals(reqNum) && !pwd.matches(".*\\d.*")) return false;
        Boolean reqUp = policy == null ? null : policy.getRequireUppercase();
        if (Boolean.TRUE.equals(reqUp) && !pwd.matches(".*[A-Z].*")) return false;
        Boolean reqLow = policy == null ? null : policy.getRequireLowercase();
        if (Boolean.TRUE.equals(reqLow) && !pwd.matches(".*[a-z].*")) return false;
        Boolean reqSym = policy == null ? null : policy.getRequireSymbols();
        if (Boolean.TRUE.equals(reqSym) && !pwd.matches(".*[^A-Za-z0-9].*")) return false;
        return true;
    }

    /**
     * 插入用户、租户、角色以及绑定关系。
     * 从初始化参数解析租户集合并创建，随后创建用户与角色，再建立绑定关系。
     */
    private void seedAdminData(InitApplyParam p) {
        List<String> names = new ArrayList<>();
        String defName = (p == null || p.getDefaultTenantName() == null || p.getDefaultTenantName().isEmpty()) ? "default" : p.getDefaultTenantName();
        names.add(defName);
        if (p != null && p.getTenantNames() != null) {
            for (String n : p.getTenantNames()) { if (n != null && n.trim().length() > 0 && !names.contains(n)) names.add(n.trim()); }
        }
        if (p != null && Boolean.TRUE.equals(p.getAddForgexTenant())) {
            if (!names.contains("Forgex")) names.add("Forgex");
        }
        String cust = null;
        if (p != null && Boolean.TRUE.equals(p.getAddCustomerTenant())) {
            cust = (p.getCustomerTenantName() == null || p.getCustomerTenantName().isEmpty()) ? "customer" : p.getCustomerTenantName();
            if (!names.contains(cust)) names.add(cust);
        }
        List<SysTenant> tenants = new ArrayList<>();
        for (String n : names) {
            SysTenant t = new SysTenant();
            t.setTenantName(n);
            String intro = null;
            if (n.equals(defName)) intro = p == null ? null : p.getDefaultTenantIntro();
            if (n.equals("Forgex")) intro = p == null ? null : p.getForgexTenantIntro();
            if (n.equals(defName) && (intro == null || intro.isEmpty())) intro = "默认租户";
            t.setDescription(intro);
            if (n.equals(defName)) {
                t.setTenantCode(p == null ? null : p.getDefaultTenantCode());
                t.setLogo(p == null ? null : p.getDefaultTenantLogo());
            } else if (n.equals("Forgex")) {
                t.setTenantCode("forgex");
                t.setLogo(null);
            } else if (cust != null && n.equals(cust)) {
                t.setTenantCode(p == null ? null : p.getCustomerTenantCode());
                t.setLogo(p == null ? null : p.getCustomerTenantLogo());
            }
            tenantMapper.insert(t);
            tenants.add(t);
        }
        Long defaultTenantId = tenants.get(0).getId();

        PasswordPolicyConfig policy = configService.getJson("security.password.policy", PasswordPolicyConfig.class, null);
        String store = policy == null ? "bcrypt" : policy.getStore();
        String rawInitPwd = (p == null || p.getInitialPassword() == null || p.getInitialPassword().isEmpty()) ? "Aa123456" : p.getInitialPassword();

        List<SysUser> users = new ArrayList<>();
        users.add(newUser("admin", rawInitPwd, store));
        if (p != null && p.isAddTest()) users.add(newUser("test", rawInitPwd, store));
        if (p != null && p.isAddDev()) users.add(newUser("dev", rawInitPwd, store));
        if (p != null && p.isAddCustom()) {
            String cname = StringUtils.hasText(p.getCustomUsername()) ? p.getCustomUsername() : "custom";
            users.add(newUser(cname, rawInitPwd, store));
        }
        for (SysUser u : users) userMapper.insert(u);

        if (p != null && p.getUserTenantBinds() != null && !p.getUserTenantBinds().isEmpty()) {
            for (InitApplyParam.UserTenantBind b : p.getUserTenantBinds()) {
                if (b == null || b.account == null || b.tenants == null) continue;
                SysUser u = users.stream().filter(x -> b.account.equalsIgnoreCase(x.getAccount())).findFirst().orElse(null);
                if (u == null) continue;
                for (String tn : b.tenants) {
                    SysTenant tt = tenants.stream().filter(x -> tn.equalsIgnoreCase(x.getTenantName())).findFirst().orElse(null);
                    if (tt == null) continue;
                    SysUserTenant bind = new SysUserTenant();
                    bind.setUserId(u.getId());
                    bind.setTenantId(tt.getId());
                    userTenantMapper.insert(bind);
                }
            }
        } else {
            for (SysUser u : users) {
                SysUserTenant bind = new SysUserTenant();
                bind.setUserId(u.getId());
                bind.setTenantId(defaultTenantId);
                userTenantMapper.insert(bind);
            }
        }

        List<SysRole> roles = new ArrayList<>();
        roles.add(newRole("平台管理员", "admin", defaultTenantId));
        if (p != null && p.isRoleTester()) roles.add(newRole("测试员", "tester", defaultTenantId));
        if (p != null && p.isRoleDeveloper()) roles.add(newRole("开发人员", "developer", defaultTenantId));
        if (p != null && p.isRoleCustomer()) roles.add(newRole("客户", "customer", defaultTenantId));
        for (SysRole r : roles) roleMapper.insert(r);

        Map<String, Long> roleIdByKey = new HashMap<>();
        for (SysRole r : roles) roleIdByKey.put(r.getRoleKey(), r.getId());
        if (p != null && p.getUserRoleBinds() != null && !p.getUserRoleBinds().isEmpty()) {
            for (InitApplyParam.UserRoleBind b : p.getUserRoleBinds()) {
                if (b == null || b.account == null || b.tenant == null || b.roleKey == null) continue;
                SysUser u = users.stream().filter(x -> b.account.equalsIgnoreCase(x.getAccount())).findFirst().orElse(null);
                SysTenant tt = tenants.stream().filter(x -> b.tenant.equalsIgnoreCase(x.getTenantName())).findFirst().orElse(null);
                Long rid = roleIdByKey.get(b.roleKey);
                if (u == null || tt == null || rid == null) continue;
                SysUserRole ur = new SysUserRole();
                ur.setUserId(u.getId());
                ur.setRoleId(rid);
                ur.setTenantId(tt.getId());
                userRoleMapper.insert(ur);
            }
        } else {
            for (SysUser u : users) {
                String key = "admin";
                if ("test".equalsIgnoreCase(u.getAccount()) && roleIdByKey.containsKey("tester")) key = "tester";
                if ("dev".equalsIgnoreCase(u.getAccount()) && roleIdByKey.containsKey("developer")) key = "developer";
                if (!"admin".equalsIgnoreCase(u.getAccount()) && !"test".equalsIgnoreCase(u.getAccount()) && !"dev".equalsIgnoreCase(u.getAccount()) && roleIdByKey.containsKey("customer")) key = "customer";
                Long rid = roleIdByKey.get(key);
                if (rid != null) {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(u.getId());
                    ur.setRoleId(rid);
                    ur.setTenantId(defaultTenantId);
                    userRoleMapper.insert(ur);
                }
            }
        }

        Long adminRoleId = roleIdByKey.get("admin");
        seedDefaultPermissionData(defaultTenantId, adminRoleId);
    }

    private void seedDefaultPermissionData(Long tenantId, Long adminRoleId) {
        List<Long> grantedMenuIds = new ArrayList<>();

        SysModule sysModule = insertModule(tenantId, "sys", "系统管理", "System Management", "SettingOutlined", 10);
        seedSystemModuleMenus(tenantId, sysModule.getId(), grantedMenuIds);

        SysModule workflowModule = insertModule(tenantId, "approval", "审批管理", "Approval", "AuditOutlined", 50);
        seedWorkflowModuleMenus(tenantId, workflowModule.getId(), grantedMenuIds);

        grantRoleMenus(tenantId, adminRoleId, grantedMenuIds);
    }

    private void seedSystemModuleMenus(Long tenantId, Long moduleId, List<Long> grantedMenuIds) {
        SysMenu dashboard = insertMenu(tenantId, moduleId, 0L, "menu", "dashboard",
                "仪表盘", "Dashboard", "DashboardOutlined", "SystemDashboard", "sys:dashboard:view", 1, 1);
        grantedMenuIds.add(dashboard.getId());

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 10, "user", "用户管理", "User Management",
                "UserOutlined", "SystemUser", "sys:user:view",
                Arrays.asList(
                        new String[]{"sys:user:add", "新增用户", "Add User"},
                        new String[]{"sys:user:edit", "编辑用户", "Edit User"},
                        new String[]{"sys:user:delete", "删除用户", "Delete User"},
                        new String[]{"sys:user:resetPwd", "重置密码", "Reset Password"},
                        new String[]{"sys:user:assignRole", "分配角色", "Assign Role"},
                        new String[]{"sys:user:export", "导出用户", "Export User"},
                        new String[]{"sys:excel:export:user", "执行用户导出", "Run User Export"}
                ));

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 20, "role", "角色管理", "Role Management",
                "TeamOutlined", "SystemRole", "sys:role:view",
                Arrays.asList(
                        new String[]{"sys:role:add", "新增角色", "Add Role"},
                        new String[]{"sys:role:edit", "编辑角色", "Edit Role"},
                        new String[]{"sys:role:delete", "删除角色", "Delete Role"},
                        new String[]{"sys:role:authMenu", "菜单授权", "Authorize Menu"},
                        new String[]{"sys:role:authUser", "用户授权", "Authorize User"}
                ));

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 30, "module", "模块管理", "Module Management",
                "AppstoreOutlined", "SystemModule", "sys:module:view",
                Arrays.asList(
                        new String[]{"sys:module:add", "新增模块", "Add Module"},
                        new String[]{"sys:module:edit", "编辑模块", "Edit Module"},
                        new String[]{"sys:module:delete", "删除模块", "Delete Module"}
                ));

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 40, "menu", "菜单管理", "Menu Management",
                "MenuOutlined", "SystemMenu", "sys:menu:view",
                Arrays.asList(
                        new String[]{"sys:menu:add", "新增菜单", "Add Menu"},
                        new String[]{"sys:menu:edit", "编辑菜单", "Edit Menu"},
                        new String[]{"sys:menu:delete", "删除菜单", "Delete Menu"}
                ));

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 50, "department", "部门管理", "Department Management",
                "ApartmentOutlined", "SystemDepartment", "sys:dept:view",
                Arrays.asList(
                        new String[]{"sys:dept:add", "新增部门", "Add Department"},
                        new String[]{"sys:dept:edit", "编辑部门", "Edit Department"},
                        new String[]{"sys:dept:delete", "删除部门", "Delete Department"}
                ));

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 60, "position", "岗位管理", "Position Management",
                "IdcardOutlined", "SystemPosition", "sys:position:view",
                Arrays.asList(
                        new String[]{"sys:position:add", "新增岗位", "Add Position"},
                        new String[]{"sys:position:edit", "编辑岗位", "Edit Position"},
                        new String[]{"sys:position:delete", "删除岗位", "Delete Position"}
                ));

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 70, "tenant", "租户管理", "Tenant Management",
                "BankOutlined", "SystemTenant", "sys:tenant:view",
                Arrays.asList(
                        new String[]{"sys:tenant:add", "新增租户", "Add Tenant"},
                        new String[]{"sys:tenant:edit", "编辑租户", "Edit Tenant"},
                        new String[]{"sys:tenant:delete", "删除租户", "Delete Tenant"}
                ));

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 80, "online", "在线用户", "Online Users",
                "ClusterOutlined", "SystemOnline", "sys:online:view",
                Collections.singletonList(new String[]{"sys:online:kickout", "强制下线", "Kick Out"})
        );

        SysMenu excelConfigCatalog = insertMenu(tenantId, moduleId, 0L, "catalog", "excelConfig",
                "Excel配置", "Excel Config", "FileExcelOutlined", null, null, 90, 1);
        grantedMenuIds.add(excelConfigCatalog.getId());

        addMenuWithButtons(tenantId, moduleId, excelConfigCatalog.getId(), 2, grantedMenuIds, 10,
                "excelImportConfig", "Excel导入配置", "Excel Import Config",
                "ImportOutlined", "SystemExcelImportConfig", "sys:excel:importConfig:list",
                Arrays.asList(
                        new String[]{"sys:excel:importConfig:edit", "编辑导入配置", "Edit Import Config"},
                        new String[]{"sys:excel:importConfig:delete", "删除导入配置", "Delete Import Config"},
                        new String[]{"sys:excel:template:download", "下载导入模板", "Download Template"}
                ));

        addMenuWithButtons(tenantId, moduleId, excelConfigCatalog.getId(), 2, grantedMenuIds, 20,
                "excelExportConfig", "Excel导出配置", "Excel Export Config",
                "ExportOutlined", "SystemExcelExportConfig", "sys:excel:exportConfig:list",
                Arrays.asList(
                        new String[]{"sys:excel:exportConfig:edit", "编辑导出配置", "Edit Export Config"},
                        new String[]{"sys:excel:exportConfig:delete", "删除导出配置", "Delete Export Config"}
                ));

        SysMenu pageTableConfigCatalog = insertMenu(tenantId, moduleId, 0L, "catalog", "pageTableConfig",
                "页表配置", "Page Table Config", "TableOutlined", null, null, 100, 1);
        grantedMenuIds.add(pageTableConfigCatalog.getId());

        addMenuWithButtons(tenantId, moduleId, pageTableConfigCatalog.getId(), 2, grantedMenuIds, 10,
                "tableConfig", "动态表格配置", "Dynamic Table Config",
                "TableOutlined", "SystemTableConfig", "sys:tableConfig:view",
                Arrays.asList(
                        new String[]{"sys:tableConfig:add", "新增表格配置", "Add Table Config"},
                        new String[]{"sys:tableConfig:edit", "编辑表格配置", "Edit Table Config"},
                        new String[]{"sys:tableConfig:delete", "删除表格配置", "Delete Table Config"}
                ));

        addMenuWithButtons(tenantId, moduleId, pageTableConfigCatalog.getId(), 2, grantedMenuIds, 20,
                "userTableConfig", "用户列设置", "User Table Config",
                "ColumnWidthOutlined", "SystemUserTableConfig", "sys:userTableConfig:view",
                Arrays.asList());

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 110, "tenantMessageWhitelist", "租户消息白名单", "Tenant Message Whitelist",
                "SafetyCertificateOutlined", "SystemTenantMessageWhitelist", "sys:tenant-message-whitelist:view",
                Arrays.asList(
                        new String[]{"sys:tenant-message-whitelist:create", "新增白名单", "Add Whitelist"},
                        new String[]{"sys:tenant-message-whitelist:update", "编辑白名单", "Edit Whitelist"},
                        new String[]{"sys:tenant-message-whitelist:delete", "删除白名单", "Delete Whitelist"}
                ));

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 120, "loginLog", "登录日志", "Login Log",
                "LoginOutlined", "SystemLoginLog", "sys:loginLog:view",
                Collections.singletonList(new String[]{"sys:excel:export:loginLog", "导出登录日志", "Export Login Log"})
        );

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 130, "operationLog", "操作日志", "Operation Log",
                "FileTextOutlined", "SystemOperationLog", "sys:operation-log:view",
                Collections.singletonList(new String[]{"sys:operation-log:export", "导出操作日志", "Export Operation Log"})
        );
    }

    private void seedWorkflowModuleMenus(Long tenantId, Long moduleId, List<Long> grantedMenuIds) {
        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 10, "taskConfig", "审批任务配置", "Task Config",
                "SettingOutlined", "ApprovalTaskConfig", "wf:taskConfig:view",
                Arrays.asList(
                        new String[]{"wf:taskConfig:add", "新增审批任务", "Add Task Config"},
                        new String[]{"wf:taskConfig:edit", "编辑审批任务", "Edit Task Config"},
                        new String[]{"wf:taskConfig:delete", "删除审批任务", "Delete Task Config"},
                        new String[]{"wf:taskConfig:config", "配置审批流程", "Configure Workflow"}
                ));

        SysMenu startMenu = insertMenu(tenantId, moduleId, 0L, "menu", "execution/start",
                "发起审批", "Start Approval", "PlayCircleOutlined", "ApprovalExecutionStart", "wf:execution:start", 20, 1);
        grantedMenuIds.add(startMenu.getId());

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 30, "my/pending", "我的待办", "My Pending",
                "ClockCircleOutlined", "ApprovalMyPending", "wf:myTask:pending",
                Arrays.asList(
                        new String[]{"wf:execution:approve", "同意审批", "Approve"},
                        new String[]{"wf:execution:reject", "驳回审批", "Reject"}
                ));

        SysMenu processedMenu = insertMenu(tenantId, moduleId, 0L, "menu", "my/processed",
                "我已处理", "My Processed", "CheckCircleOutlined", "ApprovalMyProcessed", "wf:myTask:processed", 40, 1);
        grantedMenuIds.add(processedMenu.getId());

        addMenuWithButtons(tenantId, moduleId, grantedMenuIds, 50, "my/initiated", "我发起的", "My Initiated",
                "SendOutlined", "ApprovalMyInitiated", "wf:myTask:initiated",
                Collections.singletonList(new String[]{"wf:execution:cancel", "撤销审批", "Cancel Execution"})
        );
    }

    private void addMenuWithButtons(Long tenantId, Long moduleId, List<Long> grantedMenuIds,
                                    int orderNum, String path, String zhName, String enName,
                                    String icon, String componentKey, String menuPermKey,
                                    List<String[]> buttonDefs) {
        addMenuWithButtons(tenantId, moduleId, 0L, 1, grantedMenuIds, orderNum, path, zhName, enName,
                icon, componentKey, menuPermKey, buttonDefs);
    }

    private void addMenuWithButtons(Long tenantId, Long moduleId, Long parentId, int menuLevel,
                                    List<Long> grantedMenuIds, int orderNum, String path,
                                    String zhName, String enName, String icon, String componentKey,
                                    String menuPermKey, List<String[]> buttonDefs) {
        SysMenu menu = insertMenu(tenantId, moduleId, parentId, "menu", path, zhName, enName, icon, componentKey, menuPermKey, orderNum, menuLevel);
        grantedMenuIds.add(menu.getId());

        int buttonOrder = 1;
        for (String[] buttonDef : buttonDefs) {
            SysMenu button = insertButton(tenantId, moduleId, menu.getId(),
                    buttonDef[0], buttonDef[1], buttonDef[2], buttonOrder++);
            grantedMenuIds.add(button.getId());
        }
    }

    private SysModule insertModule(Long tenantId, String code, String zhName, String enName, String icon, int orderNum) {
        SysModule module = new SysModule();
        module.setTenantId(tenantId);
        module.setCode(code);
        module.setName(zhName);
        module.setNameI18nJson(i18n(zhName, enName));
        module.setIcon(icon);
        module.setOrderNum(orderNum);
        module.setVisible(true);
        module.setStatus(true);
        moduleMapper.insert(module);
        return module;
    }

    private SysMenu insertMenu(Long tenantId, Long moduleId, Long parentId, String type, String path,
                               String zhName, String enName, String icon, String componentKey,
                               String permKey, int orderNum, int menuLevel) {
        SysMenu menu = new SysMenu();
        menu.setTenantId(tenantId);
        menu.setModuleId(moduleId);
        menu.setParentId(parentId);
        menu.setType(type);
        menu.setPath(path);
        menu.setName(zhName);
        menu.setNameI18nJson(i18n(zhName, enName));
        menu.setIcon(icon);
        menu.setComponentKey(componentKey);
        menu.setPermKey(permKey);
        menu.setOrderNum(orderNum);
        menu.setVisible(true);
        menu.setStatus(true);
        menu.setMenuLevel(menuLevel);
        menuMapper.insert(menu);
        return menu;
    }

    private SysMenu insertButton(Long tenantId, Long moduleId, Long parentId, String permKey,
                                 String zhName, String enName, int orderNum) {
        SysMenu button = new SysMenu();
        button.setTenantId(tenantId);
        button.setModuleId(moduleId);
        button.setParentId(parentId);
        button.setType("button");
        button.setName(zhName);
        button.setNameI18nJson(i18n(zhName, enName));
        button.setPermKey(permKey);
        button.setOrderNum(orderNum);
        button.setVisible(true);
        button.setStatus(true);
        button.setMenuLevel(3);
        menuMapper.insert(button);
        return button;
    }

    private void grantRoleMenus(Long tenantId, Long roleId, List<Long> menuIds) {
        if (roleId == null || menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setTenantId(tenantId);
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }

    private String i18n(String zhName, String enName) {
        return "{\"zh-CN\":\"" + zhName + "\",\"en-US\":\"" + enName + "\"}";
    }

    /**
     * 构造新用户并按存储策略写入密码（可逆加密或不可逆哈希）。
     * @param accountName 账号
     * @param rawPassword 明文密码
     * @param store       存储策略键
     * @return 用户实体（未落库）
     */
    private SysUser newUser(String accountName, String rawPassword, String store) {
        SysUser u = new SysUser();
        u.setAccount(accountName);
        u.setUsername(accountName);
        u.setEmail(accountName + "@local");
        u.setPhone(null);
        u.setStatus(true);
        String s = store == null ? "bcrypt" : store.toLowerCase();
        CryptoPasswordProvider provider = CryptoProviders.resolve(s, configService);
        if (provider.supportsEncrypt()) {
            u.setPassword(provider.encrypt(rawPassword));
        } else {
            u.setPassword(provider.hash(rawPassword));
        }
        return u;
    }

    /** 构造角色实体 */
    private SysRole newRole(String name, String key, Long tenantId) {
        SysRole r = new SysRole();
        r.setRoleName(name);
        r.setRoleKey(key);
        r.setStatus(true);
        r.setTenantId(tenantId);
        return r;
    }
}
