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
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户Service实现类
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> 
    implements ISysUserService {
    
    private final SysUserMapper userMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysPositionMapper positionMapper;
    
    @Override
    public IPage<SysUserDTO> pageUsers(Page<SysUser> page, SysUserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = buildQueryWrapper(query);
        IPage<SysUser> userPage = userMapper.selectPage(page, wrapper);
        return userPage.convert(this::convertToDTO);
    }
    
    @Override
    public List<SysUserDTO> listUsers(SysUserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = buildQueryWrapper(query);
        List<SysUser> users = userMapper.selectList(wrapper);
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    public SysUserDTO getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        return user != null ? convertToDTO(user) : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUserDTO userDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        userMapper.insert(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUserDTO userDTO) {
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userDTO, user);
        userMapper.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> ids) {
        userMapper.deleteBatchIds(ids);
    }
    
    @Override
    public boolean existsById(Long id) {
        return userMapper.selectById(id) != null;
    }
    
    @Override
    public boolean existsByAccount(String account) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getAccount, account);
        return userMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public boolean existsByAccountExcludeId(String account, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getAccount, account);
        wrapper.ne(SysUser::getId, excludeId);
        return userMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 构建查询条件
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
        
        return dto;
    }
}
