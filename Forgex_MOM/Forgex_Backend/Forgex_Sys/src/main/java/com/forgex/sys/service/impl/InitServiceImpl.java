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
        Boolean ok = redis.opsForValue().setIfAbsent(lockKey, "1", Duration.ofMinutes(2)); // 获取锁，设置2分钟过期
        if (Boolean.FALSE.equals(ok)) { // 获取失败（正在初始化）
            return R.fail(500, "正在初始化中，请稍后再试"); // 返回失败提示
        }
        try {
            writeSecurityConfigsToCommon(param); // 写入安全配置到 common 配置库
            PasswordPolicyConfig policyCheck = configService.getJson("security.password.policy", PasswordPolicyConfig.class, null);
            String initPwd = (param == null || param.getInitialPassword() == null || param.getInitialPassword().isEmpty()) ? "Aa123456@" : param.getInitialPassword();
            if (!validatePassword(initPwd, policyCheck)) { return R.fail(500, "初始化密码不符合安全策略要求"); }
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
        String store = p == null ? null : p.getPasswordStore();
        String storeLower = store == null ? "sm2" : store.toLowerCase();
        pwdPolicy.setStore(storeLower);
        configService.setJson("security.password.policy", pwdPolicy);

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
            userRoleMapper.delete(new LambdaQueryWrapper<>()); // 清空用户-角色绑定
            userTenantMapper.delete(new LambdaQueryWrapper<>()); // 清空用户-租户绑定
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
        String rawInitPwd = (p == null || p.getInitialPassword() == null || p.getInitialPassword().isEmpty()) ? "Aa123456@" : p.getInitialPassword();

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
        roles.add(newRole("系统管理员", "admin", defaultTenantId));
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

        SysModule sysModule = new SysModule();
        sysModule.setTenantId(defaultTenantId);
        sysModule.setCode("sys");
        sysModule.setName("系统");
        sysModule.setIcon("setting");
        sysModule.setOrderNum(10);
        sysModule.setVisible(true);
        sysModule.setStatus(true);
        moduleMapper.insert(sysModule);
        Long mid = sysModule.getId();

        SysMenu userMenu = new SysMenu();
        userMenu.setTenantId(defaultTenantId);
        userMenu.setModuleId(mid);
        userMenu.setParentId(0L);
        userMenu.setType("menu");
        userMenu.setPath("user");
        userMenu.setName("用户管理");
        userMenu.setIcon("user");
        userMenu.setComponentKey("SysUser");
        userMenu.setOrderNum(10);
        userMenu.setVisible(true);
        userMenu.setStatus(true);
        menuMapper.insert(userMenu);

        SysMenu roleMenu = new SysMenu();
        roleMenu.setTenantId(defaultTenantId);
        roleMenu.setModuleId(mid);
        roleMenu.setParentId(0L);
        roleMenu.setType("menu");
        roleMenu.setPath("role");
        roleMenu.setName("角色管理");
        roleMenu.setIcon("team");
        roleMenu.setComponentKey("SysRole");
        roleMenu.setOrderNum(20);
        roleMenu.setVisible(true);
        roleMenu.setStatus(true);
        menuMapper.insert(roleMenu);

        SysMenu userView = new SysMenu();
        userView.setTenantId(defaultTenantId);
        userView.setModuleId(mid);
        userView.setParentId(userMenu.getId());
        userView.setType("button");
        userView.setName("用户查看");
        userView.setPermKey("user.view");
        userView.setOrderNum(1);
        userView.setVisible(true);
        userView.setStatus(true);
        menuMapper.insert(userView);

        SysMenu userEdit = new SysMenu();
        userEdit.setTenantId(defaultTenantId);
        userEdit.setModuleId(mid);
        userEdit.setParentId(userMenu.getId());
        userEdit.setType("button");
        userEdit.setName("用户编辑");
        userEdit.setPermKey("user.edit");
        userEdit.setOrderNum(2);
        userEdit.setVisible(true);
        userEdit.setStatus(true);
        menuMapper.insert(userEdit);

        SysMenu roleView = new SysMenu();
        roleView.setTenantId(defaultTenantId);
        roleView.setModuleId(mid);
        roleView.setParentId(roleMenu.getId());
        roleView.setType("button");
        roleView.setName("角色查看");
        roleView.setPermKey("role.view");
        roleView.setOrderNum(1);
        roleView.setVisible(true);
        roleView.setStatus(true);
        menuMapper.insert(roleView);

        SysMenu roleEdit = new SysMenu();
        roleEdit.setTenantId(defaultTenantId);
        roleEdit.setModuleId(mid);
        roleEdit.setParentId(roleMenu.getId());
        roleEdit.setType("button");
        roleEdit.setName("角色编辑");
        roleEdit.setPermKey("role.edit");
        roleEdit.setOrderNum(2);
        roleEdit.setVisible(true);
        roleEdit.setStatus(true);
        menuMapper.insert(roleEdit);

        Long adminRoleId = roleIdByKey.get("admin");
        if (adminRoleId != null) {
            for (SysMenu m : Arrays.asList(userMenu, roleMenu, userView, userEdit, roleView, roleEdit)) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setTenantId(defaultTenantId);
                rm.setRoleId(adminRoleId);
                rm.setMenuId(m.getId());
                roleMenuMapper.insert(rm);
            }
        }
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
