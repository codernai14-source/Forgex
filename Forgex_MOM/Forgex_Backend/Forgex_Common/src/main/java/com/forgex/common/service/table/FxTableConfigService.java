package com.forgex.common.service.table;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.common.domain.entity.table.FxTableConfig;

/**
 * 表格配置服务接口
 * <p>
 * 提供表格配置的查询和分页功能，支持多租户和用户级别的配置管理。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>根据表编码、租户ID和用户ID获取表格配置</li>
 *   <li>支持表格配置的分页查询</li>
 *   <li>支持根据查询条件过滤表格配置</li>
 * </ul>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>动态表格渲染：根据配置自动生成表格列和查询字段</li>
 *   <li>表格配置管理：提供表格配置的增删改查功能</li>
 *   <li>多租户支持：不同租户可以有独立的表格配置</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see FxTableConfigServiceImpl
 * @see FxTableConfigDTO
 */
public interface FxTableConfigService {
    /**
     * 获取表格配置
     * <p>
     * 根据表编码、租户ID和用户ID获取表格配置，
     * 包括表格列配置和查询字段配置。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>查询表编码和租户ID匹配的表格配置</li>
     *   <li>检查表格是否启用</li>
     *   <li>查询表格的所有列配置</li>
     *   <li>过滤启用的列并按排序字段排序</li>
     *   <li>解析列标题的国际化文本</li>
     *   <li>构建查询字段列表</li>
     *   <li>组装表格配置DTO并返回</li>
     * </ol>
     * 
     * @param tableCode 表编码，不能为空
     * @param tenantId 租户ID，不能为空
     * @param userId 用户ID，预留参数，当前版本未使用
     * @return 表格配置DTO，参数无效或表格不存在或未启用时返回null
     */
    FxTableConfigDTO getTableConfig(String tableCode, Long tenantId, Long userId);
    
    /**
     * 分页查询表格配置
     * <p>
     * 根据分页参数和查询条件分页查询表格配置列表。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>根据查询条件执行分页查询</li>
     *   <li>遍历查询结果，构建表格配置DTO列表</li>
     *   <li>解析表格名称的国际化文本</li>
     *   <li>组装分页结果并返回</li>
     * </ol>
     * 
     * @param page 分页参数
     * @param wrapper 查询条件包装器
     * @param tenantId 租户ID，预留参数，当前版本未使用
     * @param userId 用户ID，预留参数，当前版本未使用
     * @return 表格配置DTO分页结果
     */
    IPage<FxTableConfigDTO> page(Page<FxTableConfig> page, com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FxTableConfig> wrapper, Long tenantId, Long userId);
}

