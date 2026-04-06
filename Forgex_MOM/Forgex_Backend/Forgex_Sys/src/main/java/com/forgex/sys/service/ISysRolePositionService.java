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
import com.forgex.sys.domain.dto.RoleGrantDTO;
import com.forgex.sys.domain.entity.SysRolePosition;
import com.forgex.sys.domain.param.RoleGrantQueryDTO;
import com.forgex.sys.domain.vo.RoleGrantVO;

/**
 * 角色职位关联 Service 接口
 * <p>
 * 提供角色与职位关联关系的管理功能，包括查询已授权职位列表、批量授权职位、批量取消授权职位等。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #getGrantedPositions(Page, RoleGrantQueryDTO)} - 分页查询已授权职位列表</li>
 *   <li>{@link #grantPositions(RoleGrantDTO)} - 批量授权职位</li>
 *   <li>{@link #revokePositions(RoleGrantDTO)} - 批量取消授权职位</li>
 * </ul>
 * <p>使用说明：</p>
 * <ul>
 *   <li>所有操作均需要传入租户 ID 进行租户隔离</li>
 *   <li>授权操作会自动查询职位下的所有用户并建立角色 - 用户关联</li>
 *   <li>取消授权操作会删除职位下所有用户的对应角色关联</li>
 * </ul>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-06
 * @see com.forgex.sys.service.impl.SysRolePositionServiceImpl
 * @see SysRolePosition
 * @see RoleGrantVO
 * @see RoleGrantDTO
 */
public interface ISysRolePositionService extends IService<SysRolePosition> {

    /**
     * 分页查询已授权职位列表
     * <p>
     * 根据查询条件分页查询已授予指定角色的职位列表，并将结果转换为 VO 返回。
     * </p>
     * <p>查询条件支持：</p>
     * <ul>
     *   <li>角色 ID 精确查询（必填）</li>
     *   <li>租户 ID 精确查询（必填）</li>
     *   <li>职位名称模糊查询（通过 keyword）</li>
     * </ul>
     * <p>返回数据包含：</p>
     * <ul>
     *   <li>职位 ID、职位名称、职位编码</li>
     *   <li>角色 ID、角色名称</li>
     *   <li>授权时间、授权人</li>
     * </ul>
     *
     * @param page 分页参数，包含页码和页面大小
     * @param query 查询条件，包含角色 ID、租户 ID、关键字等过滤条件
     * @return 已授权职位分页数据，包含职位 VO 列表和总数
     * @throws IllegalArgumentException 当角色 ID 或租户 ID 为空时抛出
     * @see RoleGrantQueryDTO
     * @see RoleGrantVO
     */
    IPage<RoleGrantVO> getGrantedPositions(Page<SysRolePosition> page, RoleGrantQueryDTO query);

    /**
     * 批量授权职位
     * <p>
     * 将指定角色批量授予多个职位。系统会自动查询职位下的所有用户，
     * 并为这些用户建立角色 - 用户关联关系。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验角色和职位是否存在</li>
     *   <li>查询职位下的所有用户</li>
     *   <li>查询已存在的角色 - 用户关联，避免重复授权</li>
     *   <li>批量插入缺失的角色 - 用户关联</li>
     *   <li>插入角色 - 职位关联记录</li>
     * </ol>
     *
     * @param grantDTO 授权参数，包含角色 ID、租户 ID、职位 ID 列表
     * @throws IllegalArgumentException 当必填字段缺失时抛出
     * @throws BusinessException 当角色或职位不存在时抛出
     * @see RoleGrantDTO
     */
    void grantPositions(RoleGrantDTO grantDTO);

    /**
     * 批量取消授权职位
     * <p>
     * 取消指定角色在多个职位的授权。系统会自动查询职位下的所有用户，
     * 并删除这些用户的角色 - 用户关联关系。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验角色和职位是否存在</li>
     *   <li>查询职位下的所有用户</li>
     *   <li>批量删除这些用户的角色 - 用户关联</li>
     *   <li>删除角色 - 职位关联记录</li>
     * </ol>
     *
     * @param revokeDTO 取消授权参数，包含角色 ID、租户 ID、职位 ID 列表
     * @throws IllegalArgumentException 当必填字段缺失时抛出
     * @throws BusinessException 当角色或职位不存在时抛出
     * @see RoleGrantDTO
     */
    void revokePositions(RoleGrantDTO revokeDTO);
}
