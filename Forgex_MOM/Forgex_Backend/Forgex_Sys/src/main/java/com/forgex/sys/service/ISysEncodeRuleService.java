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
import com.forgex.sys.domain.dto.EncodeRuleDTO;
import com.forgex.sys.domain.param.EncodeRulePageParam;
import com.forgex.sys.domain.param.EncodeRuleSaveParam;
import com.forgex.sys.domain.vo.EncodeRuleVO;
import com.forgex.sys.domain.entity.SysEncodeRule;

import java.util.List;

/**
 * 编码规则 Service 接口
 * <p>
 * 提供编码规则管理相关的业务操作，包括编码规则的增删改查、分页查询、编码生成等功能。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #pageEncodeRules(Page, EncodeRulePageParam)} - 分页查询编码规则列表</li>
 *   <li>{@link #listEncodeRules(EncodeRulePageParam)} - 查询编码规则列表</li>
 *   <li>{@link #getEncodeRuleById(Long)} - 根据 ID 获取编码规则详情</li>
 *   <li>{@link #getEncodeRuleVOById(Long)} - 根据 ID 获取编码规则详情（VO）</li>
 *   <li>{@link #saveEncodeRule(EncodeRuleSaveParam)} - 保存编码规则（新增或更新）</li>
 *   <li>{@link #deleteEncodeRule(Long)} - 删除编码规则</li>
 *   <li>{@link #batchDeleteEncodeRules(List)} - 批量删除编码规则</li>
 *   <li>{@link #generateCode(String)} - 根据规则代码生成编码</li>
 *   <li>{@link #enableEncodeRule(Long)} - 启用编码规则</li>
 *   <li>{@link #disableEncodeRule(Long)} - 禁用编码规则</li>
 * </ul>
 * <p>使用说明：</p>
 * <ul>
 *   <li>DTO 用于内部业务逻辑处理，VO 用于返回给前端展示</li>
 *   <li>所有写操作（新增、更新、删除）均使用@Transactional 保证事务一致性</li>
 *   <li>编码生成使用 Redis 原子递增保证序列号唯一性</li>
 *   <li>支持租户隔离，确保数据安全性</li>
 *   <li>数据源配置：使用 common 库（common 数据源）</li>
 * </ul>
 * 
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see com.forgex.sys.service.impl.SysEncodeRuleServiceImpl
 * @see com.forgex.sys.domain.dto.EncodeRuleDTO
 * @see com.forgex.sys.domain.vo.EncodeRuleVO
 * @see com.forgex.sys.domain.entity.SysEncodeRule
 */
public interface ISysEncodeRuleService extends IService<SysEncodeRule> {
    
    /**
     * 分页查询编码规则列表
     * <p>
     * 根据查询条件分页查询编码规则列表，支持按规则代码、规则名称、模块等条件过滤。
     * </p>
     * 
     * @param page 分页参数，包含页码和页面大小
     * @param param 查询条件，包含规则代码、规则名称、模块等过滤条件
     * @return 编码规则分页数据
     * @see EncodeRulePageParam
     * @see EncodeRuleDTO
     */
    IPage<EncodeRuleDTO> pageEncodeRules(Page<SysEncodeRule> page, EncodeRulePageParam param);
    
    /**
     * 查询编码规则列表
     * <p>
     * 根据查询条件查询所有符合条件的编码规则列表。
     * </p>
     * 
     * @param param 查询条件
     * @return 编码规则列表
     * @see EncodeRulePageParam
     * @see EncodeRuleDTO
     */
    List<EncodeRuleDTO> listEncodeRules(EncodeRulePageParam param);
    
    /**
     * 根据 ID 获取编码规则详情
     * <p>
     * 根据编码规则 ID 查询详细信息，包含主表和明细表的完整数据。
     * </p>
     * 
     * @param id 编码规则 ID
     * @return 编码规则详情 DTO，包含完整信息；若不存在则返回 null
     * @see EncodeRuleDTO
     */
    EncodeRuleDTO getEncodeRuleById(Long id);
    
    /**
     * 根据 ID 获取编码规则详情（VO）
     * <p>
     * 根据编码规则 ID 查询详细信息，并将结果转换为 VO 返回给前端。
     * </p>
     * 
     * @param id 编码规则 ID
     * @return 编码规则详情 VO，包含展示信息；若不存在则返回 null
     * @see EncodeRuleVO
     */
    EncodeRuleVO getEncodeRuleVOById(Long id);
    
    /**
     * 保存编码规则（新增或更新）
     * <p>
     * 根据参数中是否包含 ID 来判断是新增还是更新操作。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验参数合法性</li>
     *   <li>检查规则代码唯一性</li>
     *   <li>保存或更新主表数据</li>
     *   <li>保存或更新明细数据（先删除后新增）</li>
     * </ol>
     * 
     * @param param 编码规则保存参数
     * @return 保存后的编码规则 ID
     * @throws IllegalArgumentException 当参数校验失败时抛出
     * @see EncodeRuleSaveParam
     */
    Long saveEncodeRule(EncodeRuleSaveParam param);
    
    /**
     * 删除编码规则
     * <p>
     * 根据编码规则 ID 删除规则及其关联的明细数据。
     * </p>
     * <p>处理流程：</p>
     * <ol>
     *   <li>校验 ID 是否为空</li>
     *   <li>删除明细数据</li>
     *   <li>删除主表数据</li>
     *   <li>清理 Redis 缓存</li>
     * </ol>
     * 
     * @param id 编码规则 ID
     * @throws IllegalArgumentException 当 ID 为空时抛出
     */
    void deleteEncodeRule(Long id);
    
    /**
     * 批量删除编码规则
     * <p>
     * 批量删除多个编码规则及其关联的明细数据。
     * </p>
     * 
     * @param ids 编码规则 ID 列表
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    void batchDeleteEncodeRules(List<Long> ids);
    
    /**
     * 根据规则代码生成编码
     * <p>
     * 根据指定的规则代码生成唯一的业务编码，支持序列号原子递增。
     * </p>
     * <p>编码格式：前缀 + 日期格式 + 序列号</p>
     * <p>示例：SO202604100001</p>
     * 
     * @param ruleCode 规则代码
     * @return 生成的业务编码
     * @throws IllegalArgumentException 当规则代码不存在或已禁用时抛出
     */
    String generateCode(String ruleCode);
    
    /**
     * 启用编码规则
     * <p>
     * 将指定编码规则的状态设置为启用。
     * </p>
     * 
     * @param id 编码规则 ID
     * @throws IllegalArgumentException 当 ID 为空或规则不存在时抛出
     */
    void enableEncodeRule(Long id);
    
    /**
     * 禁用编码规则
     * <p>
     * 将指定编码规则的状态设置为禁用。
     * </p>
     * 
     * @param id 编码规则 ID
     * @throws IllegalArgumentException 当 ID 为空或规则不存在时抛出
     */
    void disableEncodeRule(Long id);
}
