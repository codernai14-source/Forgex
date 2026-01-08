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
package com.forgex.sys.domain.param;

import lombok.Data;

import java.util.List;


/**
 * 系统初始化提交参数。
 * <p>
 * 结构包含四个区块：
 * 1) 安全配置（密码策略强度、验证码开关与模式、密码存储算法）；
 * 2) 用户（是否创建 test/dev/custom 及自定义账号名）；
 * 3) 租户（默认/可选租户名称及编码/简介/Logo、是否创建 Forgex/客户租户）；
 * 4) 角色与绑定（是否创建测试员/开发人员/客户角色，用户-租户、用户-角色绑定）。
 * <p>
 * 使用：前端向 {@code /sys/init/apply} 提交本参数，后端将写入安全配置并重建种子数据。
 */
@Data
public class InitApplyParam {
    /** 安全配置占位 */
    private boolean securityAccepted = true;

    /** 安全策略 */
    private String pwdStrength; // high/normal/low
    private String initialPassword; // 默认 Aa123456@
    private Boolean captchaEnabled; // 是否开启验证码
    private String captchaMode; // image/slider
    private String passwordStore; // sm2/sm4/bcrypt/argon2/scrypt/pbkdf2

    /** 用户可选：test、dev、custom */
    private boolean addTest;
    private boolean addDev;
    private boolean addCustom;
    private String customUsername;

    /** 租户初始化：名称列表（至少包含一个默认租户） */
    private List<String> tenantNames;

    /** 租户详细设置 */
    private String defaultTenantName;
    private String defaultTenantCode;
    private String defaultTenantIntro;
    private String defaultTenantLogo;
    private Boolean addForgexTenant;
    private String forgexTenantIntro;
    private Boolean addCustomerTenant;
    private String customerTenantName;
    private String customerTenantCode;
    private String customerTenantIntro;
    private String customerTenantLogo;

    /** 角色可选：测试员、开发人员、客户（系统管理员强制创建） */
    private boolean roleTester;
    private boolean roleDeveloper;
    private boolean roleCustomer;

    /**
     * 用户-租户绑定。
     * 将某账号绑定到一组租户名称（按名称匹配）。
     */
    public static class UserTenantBind { public String account; public List<String> tenants; }
    private List<UserTenantBind> userTenantBinds;

    /**
     * 用户-角色绑定（按租户）。
     * 指定账号、租户名称与角色键，建立三者绑定关系。
     */
    public static class UserRoleBind { public String account; public String tenant; public String roleKey; }
    private List<UserRoleBind> userRoleBinds;

}
