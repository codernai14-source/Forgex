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
import com.forgex.common.api.dto.UserThirdPartyPullResultDTO;
import com.forgex.common.api.dto.UserThirdPartySyncDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.sys.domain.dto.SysUserDTO;
import com.forgex.sys.domain.dto.SysUserQueryDTO;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.entity.SysUserTenant;
import com.forgex.sys.domain.vo.SysUserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户 Service 接口
 * <p>
 * 提供用户管理相关的业务操作，包括用户增删改查、批量操作、状态管理、密码管理等功能。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #pageUsers(Page, SysUserQueryDTO)} - 分页查询用户列表（返回 DTO）</li>
 *   <li>{@link #listUsers(SysUserQueryDTO)} - 查询用户列表（返回 DTO）</li>
 *   <li>{@link #getUserById(Long)} - 根据 ID 获取用户详情（返回 DTO）</li>
 *   <li>{@link #pageUserVOs(Page, SysUserQueryDTO)} - 分页查询用户列表（返回 VO）</li>
 *   <li>{@link #listUserVOs(SysUserQueryDTO)} - 查询用户列表（返回 VO）</li>
 *   <li>{@link #getUserVOById(Long)} - 根据 ID 获取用户详情（返回 VO）</li>
 *   <li>{@link #addUser(SysUserDTO)} - 新增用户</li>
 *   <li>{@link #updateUser(SysUserDTO)} - 更新用户</li>
 *   <li>{@link #deleteUser(Long)} - 删除用户</li>
 *   <li>{@link #batchDeleteUsers(List)} - 批量删除用户</li>
 *   <li>{@link #existsById(Long)} - 检查用户是否存在</li>
 *   <li>{@link #existsByAccount(String)} - 检查账号是否存在</li>
 *   <li>{@link #existsByAccountExcludeId(String, Long)} - 检查账号是否存在（排除指定 ID）</li>
 *   <li>{@link #getUserIdByAccount(String)} - 根据账号获取用户 ID</li>
 *   <li>{@link #listUserTenants(Long)} - 查询用户关联的租户列表</li>
 *   <li>{@link #resetPassword(Long)} - 重置用户密码</li>
 *   <li>{@link #updateStatus(Long, Boolean)} - 更新用户状态</li>
 *   <li>{@link #changePassword(Long, String, String)} - 修改密码</li>
 * </ul>
 * <p>使用说明：</p>
 * <ul>
 *   <li>DTO 用于内部业务逻辑处理，VO 用于返回给前端展示</li>
 *   <li>所有写操作（新增、更新、删除）均使用@Transactional 保证事务一致性</li>
 *   <li>状态字段 status：true=启用，false=禁用</li>
 *   <li>密码使用 BCrypt 算法加密存储</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-07
 * @see com.forgex.sys.service.impl.SysUserServiceImpl
 * @see com.forgex.sys.domain.dto.SysUserDTO
 * @see com.forgex.sys.domain.vo.SysUserVO
 * @see com.forgex.sys.domain.entity.SysUser
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表（返回 DTO）
     * <p>
     * 根据查询条件分页查询用户列表，并将结果转换为用户 DTO 返回。
     * </p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>账号模糊查询</li>
     *   <li>用户名模糊查询</li>
     *   <li>部门 ID 精确查询</li>
     *   <li>状态精确查询</li>
     *   <li>租户 ID 精确查询</li>
     * </ul>
     *
     * @param page 分页参数，包含页码和页面大小
     * @param query 查询条件，包含账号、用户名、部门 ID、状态等过滤条件
     * @return 用户分页数据，包含用户 DTO 列表和总数
     * @see SysUserQueryDTO
     * @see SysUserDTO
     */
    IPage<SysUserDTO> pageUsers(Page<SysUser> page, SysUserQueryDTO query);

    /**
     * 查询用户列表（返回 DTO）
     * <p>
     * 根据查询条件查询所有符合条件的用户列表，并将结果转换为用户 DTO 返回。
     * </p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>账号模糊查询</li>
     *   <li>用户名模糊查询</li>
     *   <li>部门 ID 精确查询</li>
     *   <li>状态精确查询</li>
     *   <li>租户 ID 精确查询</li>
     * </ul>
     *
     * @param query 查询条件，包含账号、用户名、部门 ID、状态等过滤条件
     * @return 用户列表，包含所有符合条件的用户 DTO
     * @see SysUserQueryDTO
     * @see SysUserDTO
     */
    List<SysUserDTO> listUsers(SysUserQueryDTO query);

    /**
     * 根据 ID 获取用户详情（返回 DTO）
     * <p>
     * 根据用户 ID 查询用户详细信息，并将结果转换为用户 DTO 返回。
     * </p>
     * <p>包含用户基本信息和扩展档案信息。</p>
     *
     * @param id 用户 ID，必填参数
     * @return 用户详情 DTO，包含用户完整信息；若用户不存在则返回 null
     * @see SysUserDTO
     */
    SysUserDTO getUserById(Long id);

    /**
     * 分页查询用户列表（返回 VO）
     * <p>
     * 根据查询条件分页查询用户列表，并将结果转换为用户 VO 返回。
     * </p>
     * <p>VO 包含更多展示字段，适用于前端页面展示。</p>
     *
     * @param page 分页参数，包含页码和页面大小
     * @param query 查询条件，包含账号、用户名、部门 ID、状态等过滤条件
     * @return 用户分页数据，包含用户 VO 列表和总数
     * @see SysUserQueryDTO
     * @see SysUserVO
     */
    IPage<SysUserVO> pageUserVOs(Page<SysUser> page, SysUserQueryDTO query);

    /**
     * 查询用户列表（返回 VO）
     * <p>
     * 根据查询条件查询所有符合条件的用户列表，并将结果转换为用户 VO 返回。
     * </p>
     * <p>VO 包含更多展示字段，适用于前端页面展示。</p>
     *
     * @param query 查询条件，包含账号、用户名、部门 ID、状态等过滤条件
     * @return 用户列表，包含所有符合条件的用户 VO
     * @see SysUserQueryDTO
     * @see SysUserVO
     */
    List<SysUserVO> listUserVOs(SysUserQueryDTO query);

    /**
     * 根据 ID 获取用户详情（返回 VO）
     * <p>
     * 根据用户 ID 查询用户详细信息，并将结果转换为用户 VO 返回。
     * </p>
     * <p>VO 包含更多展示字段，适用于前端页面展示。</p>
     *
     * @param id 用户 ID，必填参数
     * @return 用户详情 VO，包含用户完整展示信息；若用户不存在则返回 null
     * @see SysUserVO
     */
    SysUserVO getUserVOById(Long id);

    /**
     * 新增用户
     * <p>
     * 将用户 DTO 转换为实体对象，并插入数据库。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>将 DTO 转换为实体对象</li>
     *   <li>如果未设置密码，使用默认密码 "123456"</li>
     *   <li>使用 BCrypt 算法对密码进行加密</li>
     *   <li>插入用户数据</li>
     *   <li>保存用户扩展档案信息（可选）</li>
     * </ol>
     *
     * @param userDTO 用户信息，包含账号、用户名、密码等必填字段
     * @throws IllegalArgumentException 当必填字段缺失时抛出
     * @see SysUserDTO
     */
    void addUser(SysUserDTO userDTO);

    /**
     * 更新用户
     * <p>
     * 将用户 DTO 转换为实体对象，并更新数据库。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>将 DTO 转换为实体对象</li>
     *   <li>更新用户基本信息</li>
     *   <li>更新用户扩展档案信息（可选）</li>
     * </ol>
     *
     * @param userDTO 用户信息，包含用户 ID 和需要更新的字段
     * @throws IllegalArgumentException 当用户 ID 为空时抛出
     * @see SysUserDTO
     */
    void updateUser(SysUserDTO userDTO);

    /**
     * 删除用户
     * <p>
     * 根据用户 ID 删除用户记录（级联删除相关档案信息）。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验用户 ID 是否为空</li>
     *   <li>删除用户扩展档案</li>
     *   <li>删除用户记录</li>
     * </ol>
     *
     * @param id 用户 ID，必填参数
     * @throws IllegalArgumentException 当用户 ID 为空时抛出
     */
    void deleteUser(Long id);

    /**
     * 批量删除用户
     * <p>
     * 批量删除多个用户记录。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>遍历用户 ID 列表</li>
     *   <li>逐个调用 {@link #deleteUser(Long)} 方法删除</li>
     * </ol>
     *
     * @param ids 用户 ID 列表，必填参数
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    void batchDeleteUsers(List<Long> ids);

    /**
     * 检查用户是否存在
     * <p>
     * 根据用户 ID 检查用户是否存在。
     * </p>
     *
     * @param id 用户 ID，必填参数
     * @return true-用户存在，false-用户不存在
     */
    boolean existsById(Long id);

    /**
     * 检查账号是否存在
     * <p>
     * 根据账号检查用户是否存在。
     * </p>
     *
     * @param account 账号，必填参数
     * @return true-账号已存在，false-账号不存在
     */
    boolean existsByAccount(String account);

    /**
     * 检查账号是否存在（排除指定 ID）
     * <p>
     * 根据账号检查用户是否存在，排除指定的用户 ID。
     * </p>
     * <p>用途：用于更新用户时验证账号唯一性。</p>
     *
     * @param account 账号，必填参数
     * @param excludeId 排除的用户 ID，更新操作时传入当前用户 ID
     * @return true-账号已存在（非当前用户），false-账号不存在或是当前用户
     */
    boolean existsByAccountExcludeId(String account, Long excludeId);

    /**
     * 根据账号获取用户 ID
     * <p>
     * 根据账号查询用户 ID。
     * </p>
     *
     * @param account 账号，必填参数
     * @return 用户 ID；若账号不存在则返回 null
     */
    Long getUserIdByAccount(String account);

    /**
     * 根据登录态标识解析用户 ID，兼容数字 ID 和账号字符串。
     *
     * @param loginId 登录态标识
     * @return 用户 ID，不存在时返回 null
     */
    Long resolveUserIdByLoginId(Object loginId);

    /**
     * 查询用户关联的租户列表
     * <p>
     * 查询用户与租户的绑定关系列表。
     * </p>
     *
     * @param userId 用户 ID，必填参数
     * @return 用户租户关联列表，包含用户绑定的所有租户信息
     */
    List<SysUserTenant> listUserTenants(Long userId);

    /**
     * 重置用户密码
     * <p>
     * 将指定用户的密码重置为默认密码 "123456"。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验用户 ID 是否为空</li>
     *   <li>查询用户是否存在</li>
     *   <li>使用 BCrypt 算法对默认密码进行加密</li>
     *   <li>更新用户密码</li>
     * </ol>
     *
     * @param id 用户 ID，必填参数
     * @throws IllegalArgumentException 当用户 ID 为空时抛出
     * @throws RuntimeException 当用户不存在时抛出
     */
    void resetPassword(Long id);

    /**
     * 更新用户状态
     * <p>
     * 更新用户的启用/禁用状态。
     * </p>
     *
     * @param id 用户 ID，必填参数
     * @param status 状态值：true=启用，false=禁用
     * @throws IllegalArgumentException 当用户 ID 为空时抛出
     */
    void updateStatus(Long id, Boolean status);

    /**
     * 修改密码
     * <p>
     * 验证旧密码并更新为新密码。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验参数是否为空</li>
     *   <li>查询用户信息</li>
     *   <li>验证旧密码是否正确（使用 BCrypt 比对）</li>
     *   <li>对新密码进行 BCrypt 加密</li>
     *   <li>更新用户密码</li>
     * </ol>
     *
     * @param id 用户 ID，必填参数
     * @param oldPassword 旧密码，必填参数
     * @param newPassword 新密码，必填参数
     * @return true-修改成功，false-修改失败（旧密码错误）
     * @throws IllegalArgumentException 当参数为空时抛出
     * @throws RuntimeException 当用户不存在时抛出
     */
    boolean changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 执行sys用户的列表thirdpartyusers操作。
     *
     * @param tenantId 租户 ID
     * @return 列表数据
     */
    List<UserThirdPartySyncDTO> listThirdPartyUsers(Long tenantId);

    /**
     * 执行sys用户的同步thirdpartyusers操作。
     *
     * @param tenantId 租户 ID
     * @param users users
     * @return 处理结果
     */
    UserThirdPartyPullResultDTO syncThirdPartyUsers(Long tenantId, List<UserThirdPartySyncDTO> users);

    /**
     * 执行sys用户的导入users操作。
     *
     * @param tenantId 租户 ID
     * @param file 文件
     * @return 处理结果
     */
    UserThirdPartyPullResultDTO importUsers(Long tenantId, MultipartFile file) throws Exception;

    /**
     * 执行公共导入。
     *
     * @param param 公共导入参数
     * @return 导入结果
     */
    FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param);

    /**
     * 执行sys用户的列表用户idsbydeptids操作。
     *
     * @param tenantId 租户 ID
     * @param deptIds deptids
     * @return 列表数据
     */
    List<Long> listUserIdsByDeptIds(Long tenantId, List<Long> deptIds);

    /**
     * 执行sys用户的列表用户idsby角色ids操作。
     *
     * @param tenantId 租户 ID
     * @param roleIds 角色ids
     * @return 列表数据
     */
    List<Long> listUserIdsByRoleIds(Long tenantId, List<Long> roleIds);

    /**
     * 执行sys用户的列表用户idsbypositionids操作。
     *
     * @param tenantId 租户 ID
     * @param positionIds positionids
     * @return 列表数据
     */
    List<Long> listUserIdsByPositionIds(Long tenantId, List<Long> positionIds);
}
