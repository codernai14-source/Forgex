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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.config.ConfigService;
import com.forgex.common.crypto.CryptoPasswordProvider;
import com.forgex.common.crypto.CryptoProviders;
import com.forgex.common.domain.config.PasswordPolicyConfig;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserProfile;
import com.forgex.sys.domain.entity.SysUserRole;
import com.forgex.sys.domain.entity.SysUserTenant;
import com.forgex.sys.domain.vo.SysUserVO;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserProfileMapper;
import com.forgex.sys.mapper.SysUserRoleMapper;
import com.forgex.sys.mapper.SysUserTenantMapper;
import com.forgex.sys.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.forgex.common.tenant.TenantContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类。
 * <p>负责用户的增删改查、分页查询、批量删除等操作。</p>
 * <p>核心功能：</p>
 * <ul>
 *   <li>{@link #pageUsers(Page, SysUserQueryDTO)} - 用户分页查询</li>
 *   <li>{@link #listUsers(SysUserQueryDTO)} - 用户列表查询</li>
 *   <li>{@link #getUserById(Long)} - 根据ID查询用户</li>
 *   <li>{@link #addUser(SysUserDTO)} - 新增用户</li>
 *   <li>{@link #updateUser(SysUserDTO)} - 更新用户</li>
 *   <li>{@link #deleteUser(Long)} - 删除用户</li>
 *   <li>{@link #batchDeleteUsers(List)} - 批量删除用户</li>
 *   <li>{@link #existsById(Long)} - 验证用户是否存在</li>
 *   <li>{@link #existsByAccount(String)} - 根据账号验证用户是否存在</li>
 *   <li>{@link #getUserIdByAccount(String)} - 根据账号查询用户ID</li>
 *   <li>{@link #resetPassword(Long)} - 重置用户密码</li>
 *   <li>{@link #updateStatus(Long, Boolean)} - 更新用户状态</li>
 *   <li>{@link #changePassword(Long, String, String)} - 修改密码</li>
 *   <li>{@link #listUserTenants(Long)} - 查询用户关联的租户列表</li>
 * </ul>
 * <p>技术特点：</p>
 * <ul>
 *   <li>支持多租户数据隔离</li>
 *   <li>使用BCrypt加密用户密码</li>
 *   <li>支持用户扩展信息管理</li>
 *   <li>关联查询部门和职位信息</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-07
 * @see ISysUserService
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private static final String KEY_SECURITY_PASSWORD_POLICY = "security.password.policy";
    /**
     * 用户Mapper
     */
    private final SysUserMapper userMapper;

    private final ConfigService configService;

    /**
     * 用户租户关联Mapper
     */
    private final SysUserTenantMapper userTenantMapper;

    /**
     * 用户角色绑定Mapper
     */
    private final SysUserRoleMapper userRoleMapper;
    
    /**
     * 部门Mapper
     */
    private final SysDepartmentMapper departmentMapper;
    
    /**
     * 职位Mapper
     */
    private final SysPositionMapper positionMapper;
    
    /**
     * 用户档案Mapper
     */
    private final SysUserProfileMapper userProfileMapper;
    
    /**
     * 用户分页查询
     * <p>根据查询条件分页查询用户列表，并将结果转换为DTO返回。</p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>账号模糊查询</li>
     *   <li>用户名模糊查询</li>
     *   <li>部门ID精确查询</li>
     *   <li>状态精确查询</li>
     *   <li>租户ID精确查询</li>
     * </ul>
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页用户DTO列表
     * @see #buildQueryWrapper(SysUserQueryDTO)
     * @see #convertToDTO(SysUser)
     */
    @Override
    public IPage<SysUserDTO> pageUsers(Page<SysUser> page, SysUserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = buildQueryWrapper(query);
        IPage<SysUser> userPage = userMapper.selectPage(page, wrapper);
        return userPage.convert(this::convertToDTO);
    }
    
    /**
     * 用户列表查询
     * <p>根据查询条件查询用户列表，并将结果转换为DTO返回。</p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>账号模糊查询</li>
     *   <li>用户名模糊查询</li>
     *   <li>部门ID精确查询</li>
     *   <li>状态精确查询</li>
     *   <li>租户ID精确查询</li>
     * </ul>
     * @param query 查询条件
     * @return 用户DTO列表
     * @see #buildQueryWrapper(SysUserQueryDTO)
     * @see #convertToDTO(SysUser)
     */
    @Override
    public List<SysUserDTO> listUsers(SysUserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = buildQueryWrapper(query);
        List<SysUser> users = userMapper.selectList(wrapper);
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * 根据ID查询用户
     * <p>根据用户ID查询用户信息，并将结果转换为DTO返回。</p>
     * @param id 用户ID
     * @return 用户DTO，若不存在则返回null
     * @see #convertToDTO(SysUser)
     */
    @Override
    public SysUserDTO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        SysUserDTO dto = convertToDTO(user);
        dto.setProfile(getProfileByUserId(user.getId(), resolveTenantId(user)));
        return dto;
    }
    
    /**
     * 新增用户
     * <p>将用户DTO转换为实体，并插入数据库。</p>
     * @param userDTO 用户DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUserDTO userDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        Long effectiveTenantId = resolveEffectiveTenantId(userDTO.getTenantId());
        user.setTenantId(effectiveTenantId);
        
        // 设置默认密码并加密
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(encryptPassword(resolveDefaultPassword()));
        } else {
            user.setPassword(encryptPassword(user.getPassword()));
        }
        
        userMapper.insert(user);
        createUserTenantBinding(user.getId(), effectiveTenantId);

        // 保存附属信息（可选）
        if (userDTO.getProfile() != null) {
            saveOrUpdateProfile(user.getId(), effectiveTenantId, userDTO.getProfile());
        }
    }
    
    /**
     * 更新用户
     * <p>将用户DTO转换为实体，并更新数据库。</p>
     * @param userDTO 用户DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUserDTO userDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        userMapper.updateById(user);

        // 保存附属信息（可选）
        if (userDTO.getId() != null && userDTO.getProfile() != null) {
            saveOrUpdateProfile(userDTO.getId(), resolveTenantId(user), userDTO.getProfile());
        }
    }

    /**
     * 解析租户ID。
     * <p>
     * 优先从TenantContext获取租户ID，若为空则从用户实体获取。
     * </p>
     *
     * @param user 用户实体
     * @return 租户ID
     */
    private Long resolveTenantId(SysUser user) {
        // 优先从上下文获取租户ID
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            return tenantId;
        }
        // 从用户实体获取租户ID
        return user == null ? null : user.getTenantId();
    }

    private Long resolveEffectiveTenantId(Long requestTenantId) {
        if (requestTenantId != null) {
            return requestTenantId;
        }
        return TenantContext.get();
    }

    private void createUserTenantBinding(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return;
        }

        SysUserTenant existing = userTenantMapper.selectOne(
            new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, userId)
                .eq(SysUserTenant::getTenantId, tenantId)
                .last("limit 1")
        );
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
     * 根据用户ID查询用户档案。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 用户档案，不存在时返回null
     */
    private SysUserProfile getProfileByUserId(Long userId, Long tenantId) {
        // 参数校验
        if (userId == null || tenantId == null) {
            return null;
        }
        // 构建查询条件
        LambdaQueryWrapper<SysUserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserProfile::getUserId, userId)
                .last("limit 1");
        // 查询用户档案
        return userProfileMapper.selectOne(wrapper);
    }

    /**
     * 保存或更新用户档案。
     * <p>
     * 如果档案不存在则新增，否则更新。
     * </p>
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param profile  用户档案
     */
    private void saveOrUpdateProfile(Long userId, Long tenantId, SysUserProfile profile) {
        // 参数校验
        if (userId == null || tenantId == null || profile == null) {
            return;
        }

        // 查询现有档案
        SysUserProfile exist = getProfileByUserId(userId, tenantId);
        if (exist == null) {
            // 档案不存在，新增
            SysUserProfile insert = new SysUserProfile();
            // 复制属性
            BeanUtils.copyProperties(profile, insert);
            // 清空ID，让数据库自增
            insert.setId(null);
            // 设置用户ID
            insert.setUserId(userId);
            // 插入档案
            userProfileMapper.insert(insert);
            return;
        }

        // 档案存在，更新
        exist.setPoliticalStatus(profile.getPoliticalStatus());
        exist.setHomeAddress(profile.getHomeAddress());
        exist.setEmergencyContact(profile.getEmergencyContact());
        exist.setEmergencyPhone(profile.getEmergencyPhone());
        exist.setReferrer(profile.getReferrer());
        exist.setEducation(profile.getEducation());
        exist.setBirthPlace(profile.getBirthPlace());
        exist.setIntro(profile.getIntro());
        exist.setWorkHistory(profile.getWorkHistory());
        // 更新档案
        userProfileMapper.updateById(exist);
    }
    
    /**
     * 删除用户
     * <p>根据用户ID删除用户。</p>
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
    
    /**
     * 批量删除用户
     * <p>根据用户ID列表批量删除用户。</p>
     * @param ids 用户ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> ids) {
        // Delete user profiles
        if (ids != null && !ids.isEmpty()) {
            LambdaQueryWrapper<SysUserProfile> profileWrapper = new LambdaQueryWrapper<>();
            profileWrapper.in(SysUserProfile::getUserId, ids);
            userProfileMapper.delete(profileWrapper);
            
            // Delete user tenant associations
            LambdaQueryWrapper<SysUserTenant> tenantWrapper = new LambdaQueryWrapper<>();
            tenantWrapper.in(SysUserTenant::getUserId, ids);
            userTenantMapper.delete(tenantWrapper);
        }
        
        // Delete users
        removeByIds(ids);
    }
    
    /**
     * 验证用户是否存在
     * <p>根据用户ID验证用户是否存在。</p>
     * @param id 用户ID
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean existsById(Long id) {
        // 根据ID查询用户，判断是否存在
        return userMapper.selectById(id) != null;
    }
    
    /**
     * 根据账号验证用户是否存在
     * <p>根据账号验证用户是否存在。</p>
     * @param account 账号
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean existsByAccount(String account) {
        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getAccount, account);
        // 查询用户数量，大于0表示存在
        return userMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 根据账号验证用户是否存在（排除指定ID）
     * <p>根据账号验证用户是否存在，排除指定ID的用户。</p>
     * @param account 账号
     * @param excludeId 排除的用户ID
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean existsByAccountExcludeId(String account, Long excludeId) {
        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getAccount, account);
        // 排除指定ID
        wrapper.ne(SysUser::getId, excludeId);
        // 查询用户数量，大于0表示存在
        return userMapper.selectCount(wrapper) > 0;
    }

    /**
     * 根据账号查询用户ID
     * <p>根据账号查询用户ID，若账号为空或不存在则返回null。</p>
     * @param account 账号
     * @return 用户ID，若不存在则返回null
     */
    @Override
    public Long getUserIdByAccount(String account) {
        // 参数校验
        if (account == null || account.isEmpty()) {
            return null;
        }
        // 构建查询条件，只查询ID字段
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysUser::getId).eq(SysUser::getAccount, account).last("limit 1");
        // 查询用户
        SysUser user = userMapper.selectOne(wrapper);
        // 返回用户ID
        return user != null ? user.getId() : null;
    }
    
    /**
     * 重置用户密码
     * <p>将指定用户的密码重置为默认密码 "123456"，使用 BCrypt 算法进行哈希存储。</p>
     * @param id 用户ID
     */
    @Override
    public void resetPassword(Long id) {
        // 生成默认密码的哈希值
        String hashed = encryptPassword(resolveDefaultPassword());
        // 创建更新实体
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(hashed);
        // 更新密码
        userMapper.updateById(user);
    }
    
    /**
     * 更新用户状态
     * <p>更新用户的启用/禁用状态。</p>
     * @param id 用户ID
     * @param status 状态：true=启用，false=禁用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Boolean status) {
        // 创建更新实体
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        // 更新用户状态
        userMapper.updateById(user);
    }
    
    /**
     * 修改密码
     * <p>验证旧密码后更新新密码。</p>
     * @param id 用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        // 查询用户
        SysUser user = userMapper.selectById(id);
        // 用户不存在时返回false
        if (user == null) {
            return false;
        }
        
        // 验证旧密码
        if (!verifyPassword(oldPassword, user.getPassword())) {
            // 旧密码不正确
            return false;
        }
        
        // 更新新密码
        String hashedPassword = encryptPassword(newPassword);
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setPassword(hashedPassword);
        userMapper.updateById(updateUser);
        
        return true;
    }
    
    /**
     * 查询用户关联的租户列表
     * <p>根据用户ID查询该用户关联的所有租户。</p>
     * 
     * @param userId 用户ID
     * @return 租户列表
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
     * 构建查询条件
     * <p>根据查询DTO构建Lambda查询条件。</p>
     * @param query 查询条件DTO
     * @return Lambda查询条件
     */
    private PasswordPolicyConfig getPasswordPolicy() {
        PasswordPolicyConfig defaults = new PasswordPolicyConfig();
        defaults.setStore("bcrypt");
        defaults.setDefaultPassword("Aa123456");
        PasswordPolicyConfig policy = configService.getJson(KEY_SECURITY_PASSWORD_POLICY, PasswordPolicyConfig.class, defaults);
        return policy == null ? defaults : policy;
    }

    private String resolveDefaultPassword() {
        PasswordPolicyConfig policy = getPasswordPolicy();
        return StringUtils.hasText(policy.getDefaultPassword()) ? policy.getDefaultPassword() : "Aa123456";
    }

    private String resolvePasswordStore() {
        PasswordPolicyConfig policy = getPasswordPolicy();
        return StringUtils.hasText(policy.getStore()) ? policy.getStore() : "bcrypt";
    }

    private String encryptPassword(String rawPassword) {
        CryptoPasswordProvider provider = CryptoProviders.resolve(resolvePasswordStore(), configService);
        return provider.encrypt(rawPassword);
    }

    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        CryptoPasswordProvider provider = CryptoProviders.resolve(resolvePasswordStore(), configService);
        return provider.verify(rawPassword, encodedPassword);
    }

    private LambdaQueryWrapper<SysUser> buildQueryWrapper(SysUserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        Long tenantId = resolveQueryTenantId(query);
        if (tenantId != null) {
            List<Long> userIds = listUserIdsByTenant(tenantId);
            if (userIds.isEmpty()) {
                wrapper.eq(SysUser::getId, -1L);
            } else {
                wrapper.in(SysUser::getId, userIds);
            }
        }
        
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getAccount()), 
                SysUser::getAccount, query.getAccount());
            wrapper.like(StringUtils.hasText(query.getUsername()), 
                SysUser::getUsername, query.getUsername());
            wrapper.like(StringUtils.hasText(query.getPhone()),
                SysUser::getPhone, query.getPhone());
            wrapper.eq(query.getDepartmentId() != null, 
                SysUser::getDepartmentId, query.getDepartmentId());
            wrapper.eq(query.getPositionId() != null, 
                SysUser::getPositionId, query.getPositionId());
            wrapper.ge(query.getEntryDateStart() != null,
                SysUser::getEntryDate, query.getEntryDateStart());
            wrapper.le(query.getEntryDateEnd() != null,
                SysUser::getEntryDate, query.getEntryDateEnd());
            if (query.getRoleId() != null) {
                List<Long> userIds = listUserIdsByRole(query.getRoleId(), resolveRoleQueryTenantId(query));
                if (userIds.isEmpty()) {
                    wrapper.eq(SysUser::getId, -1L);
                } else {
                    wrapper.in(SysUser::getId, userIds);
                }
            }
            wrapper.eq(query.getStatus() != null, 
                SysUser::getStatus, query.getStatus());
        }
        
        wrapper.orderByDesc(SysUser::getCreateTime);
        return wrapper;
    }

    private Long resolveRoleQueryTenantId(SysUserQueryDTO query) {
        return resolveQueryTenantId(query);
    }

    private List<Long> listUserIdsByRole(Long roleId, Long tenantId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<SysUserRole>()
            .select(SysUserRole::getUserId)
            .eq(SysUserRole::getRoleId, roleId);
        if (tenantId != null) {
            wrapper.eq(SysUserRole::getTenantId, tenantId);
        }
        return userRoleMapper.selectList(wrapper).stream()
            .map(SysUserRole::getUserId)
            .distinct()
            .collect(Collectors.toList());
    }

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

    private List<Long> listUserIdsByTenant(Long tenantId) {
        return userTenantMapper.selectList(new LambdaQueryWrapper<SysUserTenant>()
                .select(SysUserTenant::getUserId)
                .eq(SysUserTenant::getTenantId, tenantId))
            .stream()
            .map(SysUserTenant::getUserId)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * 实体转DTO
     * <p>将用户实体转换为DTO，并关联查询部门名称、职位名称和租户列表。</p>
     * <p>关联查询：</p>
     * <ul>
     *   <li>根据部门ID查询部门名称</li>
     *   <li>根据职位ID查询职位名称</li>
     *   <li>根据用户ID查询关联租户列表</li>
     * </ul>
     * @param user 用户实体
     * @return 用户DTO
     */
    private SysUserDTO convertToDTO(SysUser user) {
        SysUserDTO dto = new SysUserDTO();
        BeanUtils.copyProperties(user, dto);
        
        // 关联查询部门名称
        if (user.getDepartmentId() != null) {
            SysDepartment department = departmentMapper.selectById(user.getDepartmentId());
            if (department != null) {
                dto.setDepartmentName(department.getDeptName());
            }
        }
        
        // 关联查询职位名称
        if (user.getPositionId() != null) {
            SysPosition position = positionMapper.selectById(user.getPositionId());
            if (position != null) {
                dto.setPositionName(position.getPositionName());
            }
        }
        
        // 关联查询租户列表
        List<SysUserTenant> tenantList = listUserTenants(user.getId());
        dto.setTenantList(tenantList);
        
        return dto;
    }
    
    /**
     * DTO转VO
     * <p>将用户DTO转换为VO，用于向前端返回数据。</p>
     * @param dto 用户DTO
     * @return 用户VO
     */
    private SysUserVO convertToVO(SysUserDTO dto) {
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }
    
    /**
     * 实体转VO
     * <p>将用户实体转换为VO，并关联查询部门名称、职位名称。</p>
     * @param user 用户实体
     * @return 用户VO
     */
    private SysUserVO convertToVO(SysUser user) {
        return convertToVO(convertToDTO(user));
    }
    
    /**
     * 分页查询用户列表（返回VO）
     * 
     * @param page 分页参数
     * @param query 查询条件
     * @return 用户分页数据
     */
    @Override
    public IPage<SysUserVO> pageUserVOs(Page<SysUser> page, SysUserQueryDTO query) {
        IPage<SysUserDTO> dtoPage = pageUsers(page, query);
        return dtoPage.convert(this::convertToVO);
    }
    
    /**
     * 查询用户列表（返回VO）
     * 
     * @param query 查询条件
     * @return 用户列表
     */
    @Override
    public List<SysUserVO> listUserVOs(SysUserQueryDTO query) {
        List<SysUserDTO> dtos = listUsers(query);
        return dtos.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    /**
     * 根据ID获取用户详情（返回VO）
     * 
     * @param id 用户ID
     * @return 用户详情
     */
    @Override
    public SysUserVO getUserVOById(Long id) {
        SysUserDTO dto = getUserById(id);
        return convertToVO(dto);
    }
}
