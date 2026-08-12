package com.forgex.sys.service.impl;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.config.ConfigService;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.crypto.CryptoProviders;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.enums.FxExcelImportMode;
import com.forgex.common.enums.UserSourceEnum;
import com.forgex.common.service.excel.FxExcelImportHandler;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserProfile;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.entity.SysUserTenant;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserProfileMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.mapper.SysUserTenantMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User import handler registered for the common Excel import entry.
 */
@Service("sysUserImportHandler")
@RequiredArgsConstructor
public class SysUserImportProxyHandler implements FxExcelImportHandler {

    private static final String KEY_SECURITY_PASSWORD_POLICY = "security.password.policy";

    private final SysUserMapper userMapper;
    private final SysUserTenantMapper userTenantMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserProfileMapper userProfileMapper;
    private final ConfigService configService;

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        FxExcelImportMode mode = FxExcelImportMode.parse(param == null ? null : param.getImportMode());
        Long tenantId = resolveEffectiveTenantId();
        List<UserImportRow> rows = toUserImportRows(param);
        FxExcelImportResultDTO result = new FxExcelImportResultDTO();
        result.setTotalCount(rows.size());
        if (mode == FxExcelImportMode.COVER) {
            coverImportedUsers(tenantId);
        }
        for (UserImportRow row : rows) {
            try {
                handleUserImportRow(tenantId, mode, row, result);
            } catch (Exception ex) {
                result.addError(row == null || row.getAccount() == null ? "UNKNOWN" : row.getAccount());
            }
        }
        return result;
    }

    private void handleUserImportRow(Long tenantId, FxExcelImportMode mode, UserImportRow row, FxExcelImportResultDTO result) {
        if (row == null || !StringUtils.hasText(row.getAccount())) {
            result.addError("account");
            return;
        }
        SysUser existing = getByAccount(row.getAccount());
        if (existing == null) {
            if (mode == FxExcelImportMode.UPDATE) {
                result.increaseSkipped();
                return;
            }
            createImportedUser(tenantId, row);
            result.increaseCreated();
            return;
        }
        if (mode == FxExcelImportMode.ADD) {
            result.increaseSkipped();
            return;
        }
        updateImportedUser(tenantId, existing, row);
        result.increaseUpdated();
    }

    private void createImportedUser(Long tenantId, UserImportRow row) {
        SysUser user = new SysUser();
        user.setAccount(row.getAccount());
        user.setUsername(row.getUsername());
        user.setPhone(row.getPhone());
        user.setEmail(row.getEmail());
        user.setEmployeeId(row.getEmployeeId());
        user.setTenantId(tenantId);
        user.setStatus(Boolean.TRUE);
        user.setPassword(encryptPassword(resolveDefaultPassword()));
        fillUserSourceIfAbsent(user, UserSourceEnum.SITE_IMPORTED);
        if (row.getUserSource() != null) {
            user.setUserSource(row.getUserSource());
        }
        userMapper.insert(user);
        createUserTenantBinding(user.getId(), tenantId);
    }

    private void updateImportedUser(Long tenantId, SysUser existing, UserImportRow row) {
        SysUser update = new SysUser();
        update.setId(existing.getId());
        update.setUsername(row.getUsername());
        update.setPhone(row.getPhone());
        update.setEmail(row.getEmail());
        update.setEmployeeId(row.getEmployeeId());
        if (row.getUserSource() != null) {
            update.setUserSource(row.getUserSource());
        } else if (existing.getUserSource() == null) {
            update.setUserSource(UserSourceEnum.SITE_IMPORTED.getCode());
        }
        if (existing.getTenantId() == null) {
            update.setTenantId(tenantId);
        }
        userMapper.updateById(update);
        createUserTenantBinding(existing.getId(), tenantId);
    }

    private void coverImportedUsers(Long tenantId) {
        List<Long> userIds = listUserIdsByTenant(tenantId);
        if (userIds.isEmpty()) {
            return;
        }
        userProfileMapper.delete(new LambdaQueryWrapper<SysUserProfile>().in(SysUserProfile::getUserId, userIds));
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds).eq(SysUserRole::getTenantId, tenantId));
        userTenantMapper.delete(new LambdaQueryWrapper<SysUserTenant>().in(SysUserTenant::getUserId, userIds).eq(SysUserTenant::getTenantId, tenantId));
        userMapper.delete(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds).eq(SysUser::getTenantId, tenantId));
    }

    private List<UserImportRow> toUserImportRows(FxExcelImportExecuteParam param) {
        if (param == null || param.getImportData() == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> rows = param.getImportData().getOrDefault("main", Collections.emptyList());
        return rows.stream().map(row -> {
            UserImportRow item = new UserImportRow();
            item.setAccount(toStringValue(row.get("account")));
            item.setUsername(toStringValue(row.get("username")));
            item.setPhone(toStringValue(row.get("phone")));
            item.setEmail(toStringValue(row.get("email")));
            item.setEmployeeId(toLongValue(row.get("employeeId")));
            item.setUserSource(toIntegerValue(row.get("userSource")));
            return item;
        }).collect(Collectors.toList());
    }

    private SysUser getByAccount(String account) {
        if (!StringUtils.hasText(account)) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getAccount, account)
            .last("limit 1"));
    }

    private List<Long> listUserIdsByTenant(Long tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }
        List<Long> userIds = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId)
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getDeleted, false))
            .stream()
            .map(SysUser::getId)
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toList());
        List<Long> bindingUserIds = userTenantMapper.selectList(new LambdaQueryWrapper<SysUserTenant>()
                .select(SysUserTenant::getUserId)
                .eq(SysUserTenant::getTenantId, tenantId))
            .stream()
            .map(SysUserTenant::getUserId)
            .filter(id -> id != null && id > 0)
            .toList();
        if (!bindingUserIds.isEmpty()) {
            userIds.addAll(bindingUserIds);
        }
        return userIds.stream().distinct().collect(Collectors.toList());
    }

    private void createUserTenantBinding(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return;
        }

        SysUserTenant existing = userTenantMapper.selectOne(new LambdaQueryWrapper<SysUserTenant>()
            .eq(SysUserTenant::getUserId, userId)
            .eq(SysUserTenant::getTenantId, tenantId)
            .last("limit 1"));
        if (existing != null) {
            return;
        }

        SysUserTenant userTenant = new SysUserTenant();
        userTenant.setUserId(userId);
        userTenant.setTenantId(tenantId);
        userTenant.setPrefOrder(0);
        userTenant.setIsDefault(Boolean.TRUE);
        userTenant.setLastUsed(null);
        userTenantMapper.insert(userTenant);
    }

    private Long resolveEffectiveTenantId() {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            return tenantId;
        }
        return CurrentUserUtils.getTenantId();
    }

    private PasswordPolicyConfig getPasswordPolicy() {
        PasswordPolicyConfig defaults = new PasswordPolicyConfig();
        PasswordPolicyConfig policy = configService.getJson(KEY_SECURITY_PASSWORD_POLICY, PasswordPolicyConfig.class, defaults);
        return policy == null ? defaults : policy;
    }

    private String resolveDefaultPassword() {
        PasswordPolicyConfig policy = getPasswordPolicy();
        String defaultPassword = policy.getDefaultPassword();
        return StringUtils.hasText(defaultPassword) ? defaultPassword : "123456";
    }

    private String resolvePasswordStore() {
        PasswordPolicyConfig policy = getPasswordPolicy();
        String store = policy.getStore();
        return StringUtils.hasText(store) ? store : "bcrypt";
    }

    private String encryptPassword(String rawPassword) {
        CryptoPasswordProvider provider = CryptoProviders.resolve(resolvePasswordStore(), configService);
        if (provider.supportsEncrypt()) {
            return provider.encrypt(rawPassword);
        }
        if (provider.supportsHash()) {
            return provider.hash(rawPassword);
        }
        throw new IllegalStateException("Unsupported password store: " + provider.name());
    }

    private void fillUserSourceIfAbsent(SysUser user, UserSourceEnum sourceEnum) {
        if (user != null && user.getUserSource() == null && sourceEnum != null) {
            user.setUserSource(sourceEnum.getCode());
        }
    }

    private String toStringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return StringUtils.hasText(text) ? text : null;
    }

    private Long toLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = toStringValue(value);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return Long.valueOf(text);
    }

    private Integer toIntegerValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = toStringValue(value);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return Integer.valueOf(text);
    }

    @Data
    private static class UserImportRow {
        private String account;
        private String username;
        private String phone;
        private String email;
        private Long employeeId;
        private Integer userSource;
    }
}
