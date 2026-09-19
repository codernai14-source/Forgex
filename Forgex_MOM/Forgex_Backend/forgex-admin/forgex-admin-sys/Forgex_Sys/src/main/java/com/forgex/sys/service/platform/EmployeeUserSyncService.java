package com.forgex.sys.service.platform;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.api.dto.SysEmployeeUserRequest;
import com.forgex.common.api.dto.SysEmployeeUserResult;
import com.forgex.common.config.ConfigService;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.crypto.CryptoProviders;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.enums.UserSourceEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.license.LicenseManager;
import com.forgex.common.security.password.PasswordPolicyValidator;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserTenant;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserTenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 人员同步平台用户。每次调用独立事务，用户变更与租户绑定任一失败均回滚。
 * Basic 不参与本事务的写操作，因此无需跨服务删除补偿；响应丢失可按同一工号和人员 ID 重试。
 */
@Service
@RequiredArgsConstructor
public class EmployeeUserSyncService {

    private final SysUserMapper userMapper;
    private final SysUserTenantMapper userTenantMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysPositionMapper positionMapper;
    private final ConfigService configService;
    private final LicenseManager licenseManager;
    private final BasicPlatformRequestGuard requestGuard;

    /**
     * 创建或更新一个人员的用户和租户绑定。
     *
     * @param request 人员同步命令
     * @return 事务成功后的用户标识
     */
    @DSTransactional(rollbackFor = Exception.class)
    public SysEmployeeUserResult sync(SysEmployeeUserRequest request) {
        if (request == null || request.employeeId() == null || !StringUtils.hasText(request.employeeNo())
                || !StringUtils.hasText(request.employeeName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        Long tenantId = requestGuard.requireEmployeeSync(request.tenantId());
        validateOrganization(request, tenantId);

        // 工号为稳定账号。禁止跨租户修改全局账号，也禁止把已绑定其他人员的账号抢绑。
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getAccount, request.employeeNo().trim()).last("LIMIT 1"));
        boolean created = user == null;
        if (created) {
            user = new SysUser();
            user.setAccount(request.employeeNo().trim());
            user.setTenantId(tenantId);
            user.setPassword(defaultPassword());
            user.setUserSource(UserSourceEnum.SITE_CREATED.getCode());
            licenseManager.checkUserLimit(userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getDeleted, false)));
        } else if (!Objects.equals(user.getTenantId(), tenantId)
                || (user.getEmployeeId() != null && !Objects.equals(user.getEmployeeId(), request.employeeId()))) {
            throw new I18nBusinessException(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
        }

        // 同步人员主数据；不接收客户端传入的密码、角色或数据权限。
        user.setUsername(request.employeeName().trim());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setGender(request.gender());
        user.setAvatar(request.avatar());
        user.setEntryDate(request.entryDate());
        user.setDepartmentId(request.departmentId());
        user.setPositionId(request.positionId());
        user.setStatus(request.status() == null || request.status());
        user.setEmployeeId(request.employeeId());
        int affected = created ? userMapper.insert(user) : userMapper.updateById(user);
        if (affected != 1 || user.getId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.DATA_ACCESS_ERROR);
        }

        // 与用户写入共用事务，绑定失败直接抛出，由动态数据源事务回滚全部写入。
        SysUserTenant binding = userTenantMapper.selectOne(new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, user.getId())
                .eq(SysUserTenant::getTenantId, tenantId).last("LIMIT 1"));
        if (binding == null) {
            binding = new SysUserTenant();
            binding.setUserId(user.getId());
            binding.setTenantId(tenantId);
            binding.setPrefOrder(0);
            binding.setIsDefault(Boolean.TRUE);
            if (userTenantMapper.insert(binding) != 1) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.DATA_ACCESS_ERROR);
            }
        }
        return new SysEmployeeUserResult(user.getId(), created);
    }

    private void validateOrganization(SysEmployeeUserRequest request, Long tenantId) {
        if (request.departmentId() != null) {
            SysDepartment department = departmentMapper.selectById(request.departmentId());
            if (department == null || !Objects.equals(department.getTenantId(), tenantId)
                    || Boolean.TRUE.equals(department.getDeleted())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.DEPARTMENT_NOT_FOUND);
            }
        }
        if (request.positionId() != null) {
            SysPosition position = positionMapper.selectById(request.positionId());
            if (position == null || !Objects.equals(position.getTenantId(), tenantId)
                    || Boolean.TRUE.equals(position.getDeleted())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.POSITION_NOT_FOUND);
            }
        }
    }

    private String defaultPassword() {
        PasswordPolicyConfig defaults = new PasswordPolicyConfig();
        defaults.setStore("bcrypt");
        defaults.setMinLength(8);
        defaults.setRequireNumbers(true);
        defaults.setRequireUppercase(true);
        defaults.setRequireLowercase(true);
        defaults.setRequireSymbols(true);
        PasswordPolicyConfig policy = configService.getJson("security.password.policy", PasswordPolicyConfig.class, defaults);
        if (policy == null) {
            policy = defaults;
        }
        String rawPassword = PasswordPolicyValidator.requireConfiguredDefaultPassword(policy);
        String store = StringUtils.hasText(policy.getStore()) ? policy.getStore() : "bcrypt";
        CryptoPasswordProvider provider = CryptoProviders.resolvePassword(store, configService);
        if (provider.supportsEncrypt()) {
            return provider.encrypt(rawPassword);
        }
        if (provider.supportsHash()) {
            return provider.hash(rawPassword);
        }
        throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.DATA_ACCESS_ERROR);
    }
}
