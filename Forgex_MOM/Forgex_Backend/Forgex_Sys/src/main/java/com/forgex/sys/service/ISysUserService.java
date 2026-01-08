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
package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.entity.SysUser;

import java.util.List;

/**
 * 用户Service接口
 * 
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
public interface ISysUserService extends IService<SysUser> {
    
    /**
     * 分页查询用户列表
     * 
     * @param page 分页参数
     * @param query 查询条件
     * @return 用户分页数据
     */
    IPage<SysUserDTO> pageUsers(Page<SysUser> page, SysUserQueryDTO query);
    
    /**
     * 查询用户列表
     * 
     * @param query 查询条件
     * @return 用户列表
     */
    List<SysUserDTO> listUsers(SysUserQueryDTO query);
    
    /**
     * 根据ID获取用户详情
     * 
     * @param id 用户ID
     * @return 用户详情
     */
    SysUserDTO getUserById(Long id);
    
    /**
     * 新增用户
     * 
     * @param userDTO 用户信息
     */
    void addUser(SysUserDTO userDTO);
    
    /**
     * 更新用户
     * 
     * @param userDTO 用户信息
     */
    void updateUser(SysUserDTO userDTO);
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     */
    void deleteUser(Long id);
    
    /**
     * 批量删除用户
     * 
     * @param ids 用户ID列表
     */
    void batchDeleteUsers(List<Long> ids);
    
    /**
     * 检查用户是否存在
     * 
     * @param id 用户ID
     * @return 是否存在
     */
    boolean existsById(Long id);
    
    /**
     * 检查账号是否存在
     * 
     * @param account 账号
     * @return 是否存在
     */
    boolean existsByAccount(String account);
    
    /**
     * 检查账号是否存在（排除指定ID）
     * 
     * @param account 账号
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean existsByAccountExcludeId(String account, Long excludeId);
    
    /**
     * 根据账号获取用户ID
     * 
     * @param account 账号
     * @return 用户ID，不存在则返回null
     */
    Long getUserIdByAccount(String account);
}
