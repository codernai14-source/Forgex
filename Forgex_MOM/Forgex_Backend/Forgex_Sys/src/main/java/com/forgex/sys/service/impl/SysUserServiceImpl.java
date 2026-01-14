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
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserProfile;
import com.forgex.sys.domain.entity.SysUserTenant;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.mapper.SysUserProfileMapper;
import com.forgex.sys.mapper.SysUserTenantMapper;
import com.forgex.sys.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.forgex.common.tenant.TenantContext;

import cn.hutool.crypto.digest.BCrypt;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户Service实现类
 * <p>负责用户的增删改查、分页查询、批量删除等操作。</p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>用户分页查询</li>
 *   <li>用户列表查询</li>
 *   <li>根据ID查询用户</li>
 *   <li>新增用户</li>
 *   <li>更新用户</li>
 *   <li>删除用户</li>
 *   <li>批量删除用户</li>
 *   <li>验证用户是否存在</li>
 *   <li>根据账号查询用户ID</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 * @see ISysUserService
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserMapper userMapper;
    private final SysUserTenantMapper userTenantMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysPositionMapper positionMapper;
    private final SysUserProfileMapper userProfileMapper;
    private final SysUserTenantMapper userTenantMapper;
    
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
        
        // 设置默认密码并加密
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(BCrypt.hashpw("123456"));
        } else {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }
        
        userMapper.insert(user);

        // 保存附属信息（可选）
        if (userDTO.getProfile() != null) {
            saveOrUpdateProfile(user.getId(), resolveTenantId(user), userDTO.getProfile());
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

    private Long resolveTenantId(SysUser user) {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            return tenantId;
        }
        return user == null ? null : user.getTenantId();
    }

    private SysUserProfile getProfileByUserId(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return null;
        }
        LambdaQueryWrapper<SysUserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserProfile::getUserId, userId)
                .eq(SysUserProfile::getTenantId, tenantId)
                .last("limit 1");
        return userProfileMapper.selectOne(wrapper);
    }

    private void saveOrUpdateProfile(Long userId, Long tenantId, SysUserProfile profile) {
        if (userId == null || tenantId == null || profile == null) {
            return;
        }

        SysUserProfile exist = getProfileByUserId(userId, tenantId);
        if (exist == null) {
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
        userMapper.deleteBatchIds(ids);
    }
    
    /**
     * 验证用户是否存在
     * <p>根据用户ID验证用户是否存在。</p>
     * @param id 用户ID
     * @return 存在返回true，否则返回false
     */
    @Override
    public boolean existsById(Long id) {
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
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getAccount, account);
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
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getAccount, account);
        wrapper.ne(SysUser::getId, excludeId);
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
        if (account == null || account.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysUser::getId).eq(SysUser::getAccount, account).last("limit 1");
        SysUser user = userMapper.selectOne(wrapper);
        return user != null ? user.getId() : null;
    }
    
    /**
     * 重置用户密码
     * <p>将指定用户的密码重置为默认密码 "123456"，使用 BCrypt 算法进行哈希存储。</p>
     * @param id 用户ID
     */
    @Override
    public void resetPassword(Long id) {
        String hashed = BCrypt.hashpw("123456");
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(hashed);
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
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
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
        if (user == null) {
            return false;
        }
        
        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 更新新密码
        String hashedPassword = BCrypt.hashpw(newPassword);
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
    private LambdaQueryWrapper<SysUser> buildQueryWrapper(SysUserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        if (query != null) {
            wrapper.like(StringUtils.hasText(query.getAccount()), 
                SysUser::getAccount, query.getAccount());
            wrapper.like(StringUtils.hasText(query.getUsername()), 
                SysUser::getUsername, query.getUsername());
            wrapper.eq(query.getDepartmentId() != null, 
                SysUser::getDepartmentId, query.getDepartmentId());
            wrapper.eq(query.getPositionId() != null, 
                SysUser::getPositionId, query.getPositionId());
            wrapper.eq(query.getStatus() != null, 
                SysUser::getStatus, query.getStatus());
            wrapper.eq(query.getTenantId() != null, 
                SysUser::getTenantId, query.getTenantId());
        }
        
        wrapper.orderByDesc(SysUser::getCreateTime);
        return wrapper;
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
}
