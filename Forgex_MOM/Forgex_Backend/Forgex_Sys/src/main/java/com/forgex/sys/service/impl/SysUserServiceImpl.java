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

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.api.dto.UserThirdPartyPullResultDTO;
import com.forgex.common.api.dto.UserThirdPartySyncDTO;
import com.forgex.common.config.ConfigService;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.crypto.CryptoProviders;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.domain.dto.excel.FxExcelImportConfigDTO;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.enums.FxExcelImportMode;
import com.forgex.common.enums.UserSourceEnum;
import com.forgex.common.service.excel.ExcelConfigService;
import com.forgex.common.service.excel.ExcelFileService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.domain.entity.SysRole;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserProfile;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.entity.SysUserTenant;
import com.forgex.sys.domain.vo.SysUserVO;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.mapper.SysRoleMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserProfileMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.mapper.SysUserTenantMapper;
import com.forgex.sys.service.ISysUserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类。
 * <p>
 * 负责用户基础资料、租户绑定、角色回显、用户档案、密码策略、第三方同步和导入等业务逻辑。
 * 查询类方法统一处理当前租户隔离，保存类方法负责维护用户主表与关联表的一致性。
 * </p>
 * <p>
 * 工作流审批取人依赖本服务提供的部门、角色、岗位用户 ID 查询能力，查询时只返回当前租户下
 * 未删除且启用的有效用户。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-07
 * @see ISysUserService
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    /**
     * 密码策略配置项编码。
     */
    private static final String KEY_SECURITY_PASSWORD_POLICY = "security.password.policy";

    /**
     * 用户导入模板对应的表格编码。
     */
    private static final String USER_IMPORT_TABLE_CODE = "sys_user";

    /**
     * 用户 Mapper。
     */
    private final SysUserMapper userMapper;

    /**
     * 系统配置服务。
     */
    private final ConfigService configService;

    /**
     * 用户租户关联 Mapper。
     */
    private final SysUserTenantMapper userTenantMapper;

    /**
     * 用户角色绑定 Mapper。
     */
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 部门 Mapper。
     */
    private final SysDepartmentMapper departmentMapper;

    /**
     * 岗位 Mapper。
     */
    private final SysPositionMapper positionMapper;

    /**
     * 角色 Mapper。
     */
    private final SysRoleMapper roleMapper;

    /**
     * 用户档案 Mapper。
     */
    private final SysUserProfileMapper userProfileMapper;

    /**
     * Excel 配置服务，用于读取用户导入配置。
     */
    private final ExcelConfigService excelConfigService;

    /**
     * Excel 文件服务，用于解析用户导入文件。
     */
    private final ExcelFileService excelFileService;

    /**
     * 分页查询用户 DTO。
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 分页用户 DTO
     */
    @Override
    public IPage<SysUserDTO> pageUsers(Page<SysUser> page, SysUserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = buildQueryWrapper(query);
        IPage<SysUser> userPage = userMapper.selectPage(page, wrapper);
        IPage<SysUserDTO> dtoPage = userPage.convert(this::convertToDTO);
        enrichUserRoles(dtoPage.getRecords(), resolveQueryTenantId(query));
        return dtoPage;
    }

    /**
     * 查询用户 DTO 列表。
     *
     * @param query 查询条件
     * @return 用户 DTO 列表
     */
    @Override
    public List<SysUserDTO> listUsers(SysUserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = buildQueryWrapper(query);
        List<SysUser> users = userMapper.selectList(wrapper);
        List<SysUserDTO> dtos = users.stream().map(this::convertToDTO).collect(Collectors.toList());
        enrichUserRoles(dtos, resolveQueryTenantId(query));
        return dtos;
    }

    /**
     * 根据用户 ID 查询用户详情。
     *
     * @param id 用户 ID
     * @return 用户 DTO，不存在时返回 null
     */
    @Override
    public SysUserDTO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        SysUserDTO dto = convertToDTO(user);
        dto.setProfile(getProfileByUserId(user.getId(), resolveTenantId(user)));
        enrichUserRoles(Collections.singletonList(dto), resolveTenantId(user));
        return dto;
    }

    /**
     * 新增用户。
     * <p>
     * 写入用户主表后同步创建当前租户绑定，并按需保存用户档案。
     * </p>
     *
     * @param userDTO 用户 DTO
     */
    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public void addUser(SysUserDTO userDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        Long effectiveTenantId = resolveEffectiveTenantId(userDTO.getTenantId());
        user.setTenantId(effectiveTenantId);
        fillUserSourceIfAbsent(user, UserSourceEnum.SITE_CREATED);
        if (user.getStatus() == null) {
            user.setStatus(Boolean.TRUE);
        }

        // 密码未传时使用系统默认密码；传入明文时统一按当前密码策略加密。
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(encryptPassword(resolveDefaultPassword()));
        } else {
            user.setPassword(encryptPassword(user.getPassword()));
        }

        userMapper.insert(user);
        createUserTenantBinding(user.getId(), effectiveTenantId);

        // 档案属于用户扩展资料，前端传入时随用户主数据一起维护。
        if (userDTO.getProfile() != null) {
            saveOrUpdateProfile(user.getId(), effectiveTenantId, userDTO.getProfile());
        }
    }

    /**
     * 更新用户。
     * <p>
     * 更新时保留未从前端传入的租户和用户来源字段，避免编辑基础资料时覆盖系统同步来源。
     * </p>
     *
     * @param userDTO 用户 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUserDTO userDTO) {
        SysUser existing = userMapper.selectById(userDTO.getId());
        if (existing == null) {
            return;
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        if (userDTO.getUserSource() == null) {
            user.setUserSource(existing.getUserSource());
        }
        if (userDTO.getTenantId() == null) {
            user.setTenantId(existing.getTenantId());
        }
        userMapper.updateById(user);

        Long effectiveTenantId = userDTO.getTenantId() != null ? userDTO.getTenantId() : resolveTenantId(existing);
        createUserTenantBinding(existing.getId(), effectiveTenantId);
        // 编辑页面带回档案信息时同步更新扩展档案。
        if (userDTO.getId() != null && userDTO.getProfile() != null) {
            saveOrUpdateProfile(userDTO.getId(), effectiveTenantId, userDTO.getProfile());
        }
    }

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    /**
     * 批量删除用户。
     * <p>
     * 先清理用户档案和租户绑定，再删除用户主数据，避免残留无效关联。
     * </p>
     *
     * @param ids 用户 ID 列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            // 用户档案、租户关联不一定有数据库级联，这里显式清理。
            LambdaQueryWrapper<SysUserProfile> profileWrapper = new LambdaQueryWrapper<>();
            profileWrapper.in(SysUserProfile::getUserId, ids);
            userProfileMapper.delete(profileWrapper);

            LambdaQueryWrapper<SysUserTenant> tenantWrapper = new LambdaQueryWrapper<>();
            tenantWrapper.in(SysUserTenant::getUserId, ids);
            userTenantMapper.delete(tenantWrapper);
        }
        removeByIds(ids);
    }

    /**
     * 判断 ID 对应数据是否存在。
     *
     * @param id 用户 ID
     * @return true 表示存在
     */
    @Override
    public boolean existsById(Long id) {
        return userMapper.selectById(id) != null;
    }

    /**
     * 判断账号是否存在。
     *
     * @param account 账号
     * @return true 表示存在
     */
    @Override
    public boolean existsByAccount(String account) {
        return userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getAccount, account)) > 0;
    }

    /**
     * 判断账号是否被其他用户占用。
     *
     * @param account   账号
     * @param excludeId 排除的用户 ID
     * @return true 表示被占用
     */
    @Override
    public boolean existsByAccountExcludeId(String account, Long excludeId) {
        return userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getAccount, account)
            .ne(SysUser::getId, excludeId)) > 0;
    }

    /**
     * 根据账号查询用户 ID。
     *
     * @param account 账号
     * @return 用户 ID，不存在时返回 null
     */
    @Override
    public Long getUserIdByAccount(String account) {
        if (!StringUtils.hasText(account)) {
            return null;
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getId)
            .eq(SysUser::getAccount, account)
            .last("limit 1"));
        return user == null ? null : user.getId();
    }

    /**
     * 根据登录 ID 解析用户 ID。
     *
     * @param loginId login ID
     * @return 数据主键 ID
     */
    @Override
    public Long resolveUserIdByLoginId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        if (loginId instanceof Number number) {
            long value = number.longValue();
            return value > 0 ? value : null;
        }
        String loginIdValue = String.valueOf(loginId).trim();
        if (!StringUtils.hasText(loginIdValue)) {
            return null;
        }
        try {
            long parsed = Long.parseLong(loginIdValue);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ignored) {
            return getUserIdByAccount(loginIdValue);
        }
    }

    /**
     * 查询用户关联的租户列表。
     *
     * @param userId 用户 ID
     * @return 用户租户关联列表
     */
    @Override
    public List<SysUserTenant> listUserTenants(Long userId) {
        LambdaQueryWrapper<SysUserTenant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserTenant::getUserId, userId);
        wrapper.orderByDesc(SysUserTenant::getIsDefault);
        wrapper.orderByDesc(SysUserTenant::getLastUsed);
        return userTenantMapper.selectList(wrapper);
    }

    /**
     * 重置用户密码为系统默认密码。
     *
     * @param id 用户 ID
     */
    @Override
    public void resetPassword(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(encryptPassword(resolveDefaultPassword()));
        userMapper.updateById(user);
    }

    /**
     * 更新用户启用状态。
     *
     * @param id     用户 ID
     * @param status 启用状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Boolean status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    /**
     * 修改用户密码。
     * <p>
     * 先校验旧密码，再按当前密码策略保存新密码。
     * </p>
     *
     * @param id          用户 ID
     * @param oldPassword 旧密码明文
     * @param newPassword 新密码明文
     * @return true 表示修改成功
     */
    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return false;
        }
        if (!verifyPassword(oldPassword, user.getPassword())) {
            return false;
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setPassword(encryptPassword(newPassword));
        userMapper.updateById(updateUser);
        return true;
    }

    /**
     * 查询第三方用户列表。
     *
     * @param tenantId 租户 ID
     * @return 第三方用户同步 DTO 列表
     */
    @Override
    public List<UserThirdPartySyncDTO> listThirdPartyUsers(Long tenantId) {
        List<Long> userIds = listUserIdsByTenant(tenantId);
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectBatchIds(userIds).stream()
            .filter(user -> user != null && StringUtils.hasText(user.getAccount()))
            .map(this::convertToThirdPartySyncDTO)
            .collect(Collectors.toList());
    }

    /**
     * 同步第三方用户。
     * <p>
     * 按账号执行新增或更新，统计新增、更新和失败账号，单条失败不影响后续用户处理。
     * </p>
     *
     * @param tenantId 租户 ID
     * @param users    第三方用户列表
     * @return 同步结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserThirdPartyPullResultDTO syncThirdPartyUsers(Long tenantId, List<UserThirdPartySyncDTO> users) {
        UserThirdPartyPullResultDTO result = new UserThirdPartyPullResultDTO();
        if (tenantId == null || users == null || users.isEmpty()) {
            return result;
        }

        result.setTotalCount(users.size());
        for (UserThirdPartySyncDTO syncUser : users) {
            String account = syncUser == null ? null : syncUser.getAccount();
            if (!StringUtils.hasText(account)) {
                result.getFailedAccounts().add(account);
                continue;
            }
            try {
                upsertThirdPartyUser(tenantId, syncUser, result);
            } catch (Exception ex) {
                result.getFailedAccounts().add(account);
            }
        }
        return result;
    }

    /**
     * 导入用户数据。
     * <p>
     * 根据用户导入配置解析 Excel，逐行按账号新增或更新用户。
     * </p>
     *
     * @param tenantId 租户 ID
     * @param file     导入文件
     * @return 导入结果
     * @throws Exception 文件读取或解析异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserThirdPartyPullResultDTO importUsers(Long tenantId, MultipartFile file) throws Exception {
        UserThirdPartyPullResultDTO result = new UserThirdPartyPullResultDTO();
        if (tenantId == null || file == null || file.isEmpty()) {
            return result;
        }

        FxExcelImportConfigDTO importConfig = excelConfigService.getImportConfigByCode(USER_IMPORT_TABLE_CODE);
        if (importConfig == null) {
            return result;
        }

        // 使用公共导入配置解析 Excel，保持模板字段与解析字段一致。
        List<UserImportRow> rows;
        try (InputStream inputStream = file.getInputStream()) {
            rows = excelFileService.parseImportFile(importConfig, inputStream, UserImportRow.class);
        }
        if (rows == null || rows.isEmpty()) {
            return result;
        }

        result.setTotalCount(rows.size());
        for (UserImportRow row : rows) {
            if (row == null || !StringUtils.hasText(row.getAccount())) {
                result.getFailedAccounts().add(row == null ? null : row.getAccount());
                continue;
            }
            try {
                upsertImportedUser(tenantId, row, result);
            } catch (Exception ex) {
                result.getFailedAccounts().add(row.getAccount());
            }
        }
        return result;
    }

    /**
     * 执行通用导入。
     *
     * @param param 请求参数
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param) {
        return handle(param);
    }

    /**
     * 处理导入数据。
     *
     * @param param 请求参数
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public FxExcelImportResultDTO handle(FxExcelImportExecuteParam param) {
        FxExcelImportMode mode = FxExcelImportMode.parse(param == null ? null : param.getImportMode());
        Long tenantId = resolveEffectiveTenantId(null);
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

    /**
     * 按部门 ID 查询用户 ID 列表。
     * <p>
     * 供工作流审批节点按部门匹配审批人使用。
     * </p>
     *
     * @param tenantId 租户 ID
     * @param deptIds  部门 ID 列表
     * @return 去重后的有效用户 ID 列表
     */
    @Override
    public List<Long> listUserIdsByDeptIds(Long tenantId, List<Long> deptIds) {
        if (tenantId == null || deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId)
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getDeleted, false)
                .eq(SysUser::getStatus, true)
                .in(SysUser::getDepartmentId, deptIds))
            .stream()
            .map(SysUser::getId)
            .filter(id -> id != null && id > 0)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 按角色 ID 查询用户 ID 列表。
     * <p>
     * 供工作流审批节点按角色匹配审批人使用，先查用户角色绑定，再回查有效用户。
     * </p>
     *
     * @param tenantId 租户 ID
     * @param roleIds  角色 ID 列表
     * @return 去重后的有效用户 ID 列表
     */
    @Override
    public List<Long> listUserIdsByRoleIds(Long tenantId, List<Long> roleIds) {
        if (tenantId == null || roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 角色与用户是多对多关系，先从绑定表取候选用户，再过滤用户有效状态。
        List<Long> userIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .select(SysUserRole::getUserId)
                .eq(SysUserRole::getTenantId, tenantId)
                .in(SysUserRole::getRoleId, roleIds))
            .stream()
            .map(SysUserRole::getUserId)
            .filter(id -> id != null && id > 0)
            .distinct()
            .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId)
                .in(SysUser::getId, userIds)
                .eq(SysUser::getDeleted, false)
                .eq(SysUser::getStatus, true))
            .stream()
            .map(SysUser::getId)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 按岗位 ID 查询用户 ID 列表。
     * <p>
     * 供工作流审批节点按岗位匹配审批人使用。
     * </p>
     *
     * @param tenantId    租户 ID
     * @param positionIds 岗位 ID 列表
     * @return 去重后的有效用户 ID 列表
     */
    @Override
    public List<Long> listUserIdsByPositionIds(Long tenantId, List<Long> positionIds) {
        if (tenantId == null || positionIds == null || positionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId)
                .eq(SysUser::getTenantId, tenantId)
                .eq(SysUser::getDeleted, false)
                .eq(SysUser::getStatus, true)
                .in(SysUser::getPositionId, positionIds))
            .stream()
            .map(SysUser::getId)
            .filter(id -> id != null && id > 0)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 获取密码策略配置。
     *
     * @return 密码策略配置
     */
    private PasswordPolicyConfig getPasswordPolicy() {
        PasswordPolicyConfig defaults = new PasswordPolicyConfig();
        defaults.setStore("bcrypt");
        defaults.setDefaultPassword("Aa123456");
        PasswordPolicyConfig policy = configService.getJson(KEY_SECURITY_PASSWORD_POLICY, PasswordPolicyConfig.class, defaults);
        return policy == null ? defaults : policy;
    }

    /**
     * 解析默认密码。
     *
     * @return 默认密码明文
     */
    private String resolveDefaultPassword() {
        PasswordPolicyConfig policy = getPasswordPolicy();
        return StringUtils.hasText(policy.getDefaultPassword()) ? policy.getDefaultPassword() : "Aa123456";
    }

    /**
     * 解析密码存储方式。
     *
     * @return 密码存储方式
     */
    private String resolvePasswordStore() {
        PasswordPolicyConfig policy = getPasswordPolicy();
        return StringUtils.hasText(policy.getStore()) ? policy.getStore() : "bcrypt";
    }

    /**
     * 按当前密码策略加密密码。
     *
     * @param rawPassword 密码明文
     * @return 加密或哈希后的密码
     */
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

    /**
     * 校验密码。
     *
     * @param rawPassword     密码明文
     * @param encodedPassword 已保存的密文或哈希
     * @return true 表示校验通过
     */
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        CryptoPasswordProvider provider = CryptoProviders.resolve(resolvePasswordStore(), configService);
        return provider.verify(rawPassword, encodedPassword);
    }

    /**
     * 构建用户查询条件。
     * <p>
     * 统一处理租户隔离、基础字段筛选、角色筛选和排序。
     * </p>
     *
     * @param query 查询条件
     * @return MyBatis-Plus 查询包装器
     */
    private LambdaQueryWrapper<SysUser> buildQueryWrapper(SysUserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        Long tenantId = resolveQueryTenantId(query);
        if (tenantId != null) {
            // 用户可能通过 sys_user.tenant_id 或 sys_user_tenant 绑定租户，两种来源都纳入查询。
            List<Long> userIds = listUserIdsByTenant(tenantId);
            if (userIds.isEmpty()) {
                wrapper.eq(SysUser::getId, -1L);
            } else {
                wrapper.in(SysUser::getId, userIds);
            }
        }

        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getAccount()), SysUser::getAccount, query.getAccount());
            wrapper.like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername());
            wrapper.like(StringUtils.hasText(query.getPhone()), SysUser::getPhone, query.getPhone());
            wrapper.eq(query.getDepartmentId() != null, SysUser::getDepartmentId, query.getDepartmentId());
            wrapper.eq(query.getPositionId() != null, SysUser::getPositionId, query.getPositionId());
            wrapper.eq(query.getEmployeeId() != null, SysUser::getEmployeeId, query.getEmployeeId());
            wrapper.eq(query.getUserSource() != null, SysUser::getUserSource, query.getUserSource());
            wrapper.ge(query.getEntryDateStart() != null, SysUser::getEntryDate, query.getEntryDateStart());
            wrapper.le(query.getEntryDateEnd() != null, SysUser::getEntryDate, query.getEntryDateEnd());

            List<Long> roleIds = normalizeQueryRoleIds(query);
            if (!roleIds.isEmpty()) {
                // 角色条件需要通过用户角色绑定表转换为用户 ID 条件。
                List<Long> userIds = listUserIdsByRoles(roleIds, resolveRoleQueryTenantId(query));
                if (userIds.isEmpty()) {
                    wrapper.eq(SysUser::getId, -1L);
                } else {
                    wrapper.in(SysUser::getId, userIds);
                }
            }
            wrapper.eq(query.getStatus() != null, SysUser::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(SysUser::getCreateTime);
        return wrapper;
    }

    /**
     * 解析角色查询使用的租户 ID。
     *
     * @param query 查询条件
     * @return 租户 ID
     */
    private Long resolveRoleQueryTenantId(SysUserQueryDTO query) {
        return resolveQueryTenantId(query);
    }

    /**
     * 根据角色查询用户 ID。
     *
     * @param roleIds  角色 ID 列表
     * @param tenantId 租户 ID
     * @return 用户 ID 列表
     */
    private List<Long> listUserIdsByRoles(List<Long> roleIds, Long tenantId) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<SysUserRole>()
            .select(SysUserRole::getUserId)
            .in(SysUserRole::getRoleId, roleIds);
        if (tenantId != null) {
            wrapper.eq(SysUserRole::getTenantId, tenantId);
        }
        return userRoleMapper.selectList(wrapper).stream()
            .map(SysUserRole::getUserId)
            .distinct()
            .collect(Collectors.toList());
    }

    /**
     * 规范化角色查询条件。
     * <p>
     * 同时兼容单角色字段和多角色字段，过滤无效 ID 并去重。
     * </p>
     *
     * @param query 查询条件
     * @return 角色 ID 列表
     */
    private List<Long> normalizeQueryRoleIds(SysUserQueryDTO query) {
        if (query == null) {
            return Collections.emptyList();
        }
        Set<Long> roleIds = new LinkedHashSet<>();
        if (query.getRoleId() != null && query.getRoleId() > 0) {
            roleIds.add(query.getRoleId());
        }
        if (query.getRoleIds() != null) {
            for (Long roleId : query.getRoleIds()) {
                if (roleId != null && roleId > 0) {
                    roleIds.add(roleId);
                }
            }
        }
        return new ArrayList<>(roleIds);
    }

    /**
     * 解析用户查询使用的租户 ID。
     * <p>
     * 优先使用租户上下文，其次使用当前登录用户租户，最后回退到查询参数。
     * </p>
     *
     * @param query 查询条件
     * @return 租户 ID
     */
    private Long resolveQueryTenantId(SysUserQueryDTO query) {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            return tenantId;
        }
        tenantId = CurrentUserUtils.getTenantId();
        if (tenantId != null) {
            return tenantId;
        }
        return query == null ? null : query.getTenantId();
    }

    /**
     * 查询租户下的用户 ID。
     * <p>
     * 同时兼容用户主表租户字段和用户租户关联表。
     * </p>
     *
     * @param tenantId 租户 ID
     * @return 用户 ID 列表
     */
    private List<Long> listUserIdsByTenant(Long tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }
        Set<Long> userIds = new LinkedHashSet<>();
        userIds.addAll(userTenantMapper.selectList(new LambdaQueryWrapper<SysUserTenant>()
                .select(SysUserTenant::getUserId)
                .eq(SysUserTenant::getTenantId, tenantId))
            .stream()
            .map(SysUserTenant::getUserId)
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toSet()));
        userIds.addAll(userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .select(SysUser::getId)
                .eq(SysUser::getTenantId, tenantId))
            .stream()
            .map(SysUser::getId)
            .filter(id -> id != null && id > 0)
            .collect(Collectors.toSet()));
        return new ArrayList<>(userIds);
    }

    /**
     * 批量补充用户角色信息。
     *
     * @param dtos     用户 DTO 列表
     * @param tenantId 租户 ID
     */
    private void enrichUserRoles(List<SysUserDTO> dtos, Long tenantId) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        for (SysUserDTO dto : dtos) {
            if (dto != null) {
                dto.setRoleIds(Collections.emptyList());
                dto.setRoleNames(Collections.emptyList());
            }
        }
        if (tenantId == null) {
            return;
        }

        List<Long> userIds = dtos.stream()
            .map(SysUserDTO::getId)
            .filter(id -> id != null && id > 0)
            .distinct()
            .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }

        List<SysUserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
            .in(SysUserRole::getUserId, userIds)
            .eq(SysUserRole::getTenantId, tenantId)
            .orderByAsc(SysUserRole::getId));
        if (userRoles == null || userRoles.isEmpty()) {
            return;
        }

        List<Long> roleIds = userRoles.stream()
            .map(SysUserRole::getRoleId)
            .filter(id -> id != null && id > 0)
            .distinct()
            .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return;
        }

        Map<Long, SysRole> roleMap = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getTenantId, tenantId)
                .eq(SysRole::getDeleted, false))
            .stream()
            .collect(Collectors.toMap(SysRole::getId, role -> role, (left, right) -> left, LinkedHashMap::new));

        Map<Long, List<Long>> userRoleIdMap = new LinkedHashMap<>();
        Map<Long, List<String>> userRoleNameMap = new LinkedHashMap<>();
        for (SysUserRole userRole : userRoles) {
            if (userRole == null || userRole.getUserId() == null || userRole.getRoleId() == null) {
                continue;
            }
            SysRole role = roleMap.get(userRole.getRoleId());
            if (role == null) {
                continue;
            }
            userRoleIdMap.computeIfAbsent(userRole.getUserId(), key -> new ArrayList<>());
            List<Long> currentRoleIds = userRoleIdMap.get(userRole.getUserId());
            if (!currentRoleIds.contains(role.getId())) {
                currentRoleIds.add(role.getId());
            }

            userRoleNameMap.computeIfAbsent(userRole.getUserId(), key -> new ArrayList<>());
            List<String> currentRoleNames = userRoleNameMap.get(userRole.getUserId());
            if (StringUtils.hasText(role.getRoleName()) && !currentRoleNames.contains(role.getRoleName())) {
                currentRoleNames.add(role.getRoleName());
            }
        }

        for (SysUserDTO dto : dtos) {
            if (dto == null || dto.getId() == null) {
                continue;
            }
            dto.setRoleIds(userRoleIdMap.getOrDefault(dto.getId(), Collections.emptyList()));
            dto.setRoleNames(userRoleNameMap.getOrDefault(dto.getId(), Collections.emptyList()));
        }
    }

    /**
     * 用户实体转换为 DTO。
     *
     * @param user 用户实体
     * @return 用户 DTO
     */
    private SysUserDTO convertToDTO(SysUser user) {
        SysUserDTO dto = new SysUserDTO();
        BeanUtils.copyProperties(user, dto);

        // 列表需要展示部门名称，按用户部门 ID 补充部门信息。
        if (user.getDepartmentId() != null) {
            SysDepartment department = departmentMapper.selectById(user.getDepartmentId());
            if (department != null) {
                dto.setDepartmentName(department.getDeptName());
            }
        }

        // 列表需要展示岗位名称，按用户岗位 ID 补充岗位信息。
        if (user.getPositionId() != null) {
            SysPosition position = positionMapper.selectById(user.getPositionId());
            if (position != null) {
                dto.setPositionName(position.getPositionName());
            }
        }

        dto.setTenantList(listUserTenants(user.getId()));
        return dto;
    }

    /**
     * 用户 DTO 转换为 VO。
     *
     * @param dto 用户 DTO
     * @return 用户 VO
     */
    private SysUserVO convertToVO(SysUserDTO dto) {
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }

    /**
     * 用户实体转换为 VO。
     *
     * @param user 用户实体
     * @return 用户 VO
     */
    private SysUserVO convertToVO(SysUser user) {
        return convertToVO(convertToDTO(user));
    }

    /**
     * 分页查询用户 VO。
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 分页用户 VO
     */
    @Override
    public IPage<SysUserVO> pageUserVOs(Page<SysUser> page, SysUserQueryDTO query) {
        IPage<SysUserDTO> dtoPage = pageUsers(page, query);
        return dtoPage.convert(this::convertToVO);
    }

    /**
     * 查询用户 VO 列表。
     *
     * @param query 查询条件
     * @return 用户 VO 列表
     */
    @Override
    public List<SysUserVO> listUserVOs(SysUserQueryDTO query) {
        List<SysUserDTO> dtos = listUsers(query);
        return dtos.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 根据用户 ID 查询用户 VO。
     *
     * @param id 用户 ID
     * @return 用户 VO，不存在时返回 null
     */
    @Override
    public SysUserVO getUserVOById(Long id) {
        SysUserDTO dto = getUserById(id);
        return dto == null ? null : convertToVO(dto);
    }

    /**
     * 解析租户 ID。
     * <p>
     * 优先从租户上下文获取，缺失时从用户实体获取。
     * </p>
     *
     * @param user 用户实体
     * @return 租户 ID
     */
    private Long resolveTenantId(SysUser user) {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            return tenantId;
        }
        return user == null ? null : user.getTenantId();
    }

    /**
     * 解析写入时使用的有效租户 ID。
     *
     * @param requestTenantId 请求中的租户 ID
     * @return 有效租户 ID
     */
    private Long resolveEffectiveTenantId(Long requestTenantId) {
        if (requestTenantId != null) {
            return requestTenantId;
        }
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            return tenantId;
        }
        return CurrentUserUtils.getTenantId();
    }

    /**
     * 创建用户租户绑定。
     * <p>
     * 已存在相同绑定时不重复写入。
     * </p>
     *
     * @param userId   用户 ID
     * @param tenantId 租户 ID
     */
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

    /**
     * 根据用户 ID 查询用户档案。
     *
     * @param userId   用户 ID
     * @param tenantId 租户 ID
     * @return 用户档案，不存在时返回 null
     */
    private SysUserProfile getProfileByUserId(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return null;
        }
        return userProfileMapper.selectOne(new LambdaQueryWrapper<SysUserProfile>()
            .eq(SysUserProfile::getUserId, userId)
            .last("limit 1"));
    }

    /**
     * 保存或更新用户档案。
     *
     * @param userId   用户 ID
     * @param tenantId 租户 ID
     * @param profile  用户档案
     */
    private void saveOrUpdateProfile(Long userId, Long tenantId, SysUserProfile profile) {
        if (userId == null || tenantId == null || profile == null) {
            return;
        }

        SysUserProfile exist = getProfileByUserId(userId, tenantId);
        if (exist == null) {
            // 档案不存在时新增，清空 ID 避免复用前端对象中的旧主键。
            SysUserProfile insert = new SysUserProfile();
            BeanUtils.copyProperties(profile, insert);
            insert.setId(null);
            insert.setUserId(userId);
            userProfileMapper.insert(insert);
            return;
        }

        exist.setPoliticalStatus(profile.getPoliticalStatus());
        exist.setHomeAddress(profile.getHomeAddress());
        exist.setEmergencyContact(profile.getEmergencyContact());
        exist.setEmergencyPhone(profile.getEmergencyPhone());
        exist.setReferrer(profile.getReferrer());
        exist.setEducation(profile.getEducation());
        exist.setBirthPlace(profile.getBirthPlace());
        exist.setIntro(profile.getIntro());
        exist.setWorkHistory(profile.getWorkHistory());
        userProfileMapper.updateById(exist);
    }

    /**
     * 新增或更新第三方同步用户。
     *
     * @param tenantId 租户 ID
     * @param syncUser 第三方用户数据
     * @param result   同步结果统计
     */
    private void upsertThirdPartyUser(Long tenantId, UserThirdPartySyncDTO syncUser, UserThirdPartyPullResultDTO result) {
        SysUser existing = getByAccount(syncUser.getAccount());
        if (existing == null) {
            SysUser user = new SysUser();
            user.setAccount(syncUser.getAccount());
            applyThirdPartyFields(user, syncUser);
            user.setTenantId(tenantId);
            user.setPassword(encryptPassword(resolveDefaultPassword()));
            fillUserSourceIfAbsent(user, UserSourceEnum.THIRD_PARTY_SYNC);
            if (user.getStatus() == null) {
                user.setStatus(Boolean.TRUE);
            }
            userMapper.insert(user);
            createUserTenantBinding(user.getId(), tenantId);
            result.setCreatedCount(result.getCreatedCount() + 1);
            return;
        }

        SysUser update = new SysUser();
        update.setId(existing.getId());
        applyThirdPartyFields(update, syncUser);
        if (existing.getUserSource() == null) {
            update.setUserSource(UserSourceEnum.THIRD_PARTY_SYNC.getCode());
        }
        if (existing.getTenantId() == null) {
            update.setTenantId(tenantId);
        }
        userMapper.updateById(update);
        createUserTenantBinding(existing.getId(), tenantId);
        result.setUpdatedCount(result.getUpdatedCount() + 1);
    }

    /**
     * 新增或更新导入用户。
     *
     * @param tenantId 租户 ID
     * @param row      导入行数据
     * @param result   导入结果统计
     */
    private void upsertImportedUser(Long tenantId, UserImportRow row, UserThirdPartyPullResultDTO result) {
        SysUser existing = getByAccount(row.getAccount());
        if (existing == null) {
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
            userMapper.insert(user);
            createUserTenantBinding(user.getId(), tenantId);
            result.setCreatedCount(result.getCreatedCount() + 1);
            return;
        }

        SysUser update = new SysUser();
        update.setId(existing.getId());
        update.setUsername(row.getUsername());
        update.setPhone(row.getPhone());
        update.setEmail(row.getEmail());
        update.setEmployeeId(row.getEmployeeId());
        if (existing.getUserSource() == null) {
            update.setUserSource(UserSourceEnum.SITE_IMPORTED.getCode());
        }
        if (existing.getTenantId() == null) {
            update.setTenantId(tenantId);
        }
        userMapper.updateById(update);
        createUserTenantBinding(existing.getId(), tenantId);
        result.setUpdatedCount(result.getUpdatedCount() + 1);
    }

    /**
     * 按公共导入模式处理单行用户数据。
     *
     * @param tenantId 租户 ID
     * @param mode     导入模式
     * @param row      用户行
     * @param result   结果统计
     */
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

    /**
     * 新增导入用户。
     *
     * @param tenantId 租户 ID
     * @param row      用户行
     */
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

    /**
     * 更新导入用户。
     *
     * @param tenantId 租户 ID
     * @param existing 已存在用户
     * @param row      用户行
     */
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

    /**
     * 覆盖导入前清理当前租户用户数据。
     *
     * @param tenantId 租户 ID
     */
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

    /**
     * 转换公共导入行数据。
     *
     * @param param 导入参数
     * @return 用户导入行
     */
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

    /**
     * 根据账号查询用户。
     *
     * @param account 账号
     * @return 用户实体，不存在时返回 null
     */
    private SysUser getByAccount(String account) {
        if (!StringUtils.hasText(account)) {
            return null;
        }
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getAccount, account)
            .last("limit 1"));
    }

    /**
     * 将第三方用户字段写入用户实体。
     *
     * @param target 目标用户实体
     * @param source 第三方用户数据
     */
    private void applyThirdPartyFields(SysUser target, UserThirdPartySyncDTO source) {
        target.setUsername(source.getUsername());
        target.setEmail(source.getEmail());
        target.setPhone(source.getPhone());
        target.setStatus(source.getStatus());
        target.setGender(source.getGender());
        target.setAvatar(source.getAvatar());
        target.setDepartmentId(source.getDepartmentId());
        target.setPositionId(source.getPositionId());
        target.setEmployeeId(source.getEmployeeId());
    }

    /**
     * 用户来源为空时填充默认来源。
     *
     * @param user       用户实体
     * @param sourceEnum 用户来源枚举
     */
    private void fillUserSourceIfAbsent(SysUser user, UserSourceEnum sourceEnum) {
        if (user != null && user.getUserSource() == null && sourceEnum != null) {
            user.setUserSource(sourceEnum.getCode());
        }
    }

    /**
     * 用户实体转换为第三方同步 DTO。
     *
     * @param user 用户实体
     * @return 第三方同步 DTO
     */
    private UserThirdPartySyncDTO convertToThirdPartySyncDTO(SysUser user) {
        UserThirdPartySyncDTO dto = new UserThirdPartySyncDTO();
        dto.setId(user.getId());
        dto.setTenantId(user.getTenantId());
        dto.setAccount(user.getAccount());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setGender(user.getGender());
        dto.setAvatar(user.getAvatar());
        dto.setDepartmentId(user.getDepartmentId());
        dto.setPositionId(user.getPositionId());
        dto.setEmployeeId(user.getEmployeeId());
        dto.setUserSource(user.getUserSource());
        return dto;
    }

    /**
     * 用户导入行模型。
     */
    @Data
    private static class UserImportRow {

        /**
         * 账号。
         */
        private String account;

        /**
         * 用户名。
         */
        private String username;

        /**
         * 手机号。
         */
        private String phone;

        /**
         * 邮箱。
         */
        private String email;

        /**
         * 员工 ID。
         */
        private Long employeeId;

        /**
         * 用户来源。
         */
        private Integer userSource;
    }
}
