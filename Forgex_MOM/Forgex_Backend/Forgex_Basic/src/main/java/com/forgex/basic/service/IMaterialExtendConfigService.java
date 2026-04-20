package com.forgex.basic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.domain.entity.BasicMaterialExtendConfig;
import com.forgex.basic.domain.response.MaterialExtendConfigVO;

import java.util.List;

/**
 * 物料扩展配置服务接口
 * <p>
 * 提供物料扩展字段配置相关的业务方法，包括：
 * 1. 扩展字段配置的 CRUD 操作
 * 2. 按模块查询扩展字段配置
 * 3. 字段选项 JSON 解析
 * 4. 字段配置的唯一性校验
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
public interface IMaterialExtendConfigService extends IService<BasicMaterialExtendConfig> {

    /**
     * 分页查询扩展配置列表
     *
     * @param tenantId 租户 ID
     * @param module 模块编码（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 扩展配置分页列表
     */
    IPage<MaterialExtendConfigVO> pageExtendConfigs(Long tenantId, String module, Integer pageNum, Integer pageSize);

    /**
     * 根据模块查询扩展配置列表
     *
     * @param tenantId 租户 ID
     * @param module 模块编码
     * @return 扩展配置 VO 列表
     */
    List<MaterialExtendConfigVO> getConfigsByModule(Long tenantId, String module);

    /**
     * 根据 ID 查询扩展配置详情
     *
     * @param tenantId 租户 ID
     * @param id 配置 ID
     * @return 扩展配置 VO
     */
    MaterialExtendConfigVO getConfigById(Long tenantId, Long id);

    /**
     * 创建扩展配置
     *
     * @param tenantId 租户 ID
     * @param config 扩展配置实体
     * @return 配置 ID
     */
    Long createConfig(Long tenantId, BasicMaterialExtendConfig config);

    /**
     * 更新扩展配置
     *
     * @param tenantId 租户 ID
     * @param config 扩展配置实体
     */
    void updateConfig(Long tenantId, BasicMaterialExtendConfig config);

    /**
     * 删除扩展配置
     *
     * @param tenantId 租户 ID
     * @param id 配置 ID
     */
    void deleteConfig(Long tenantId, Long id);

    /**
     * 批量删除扩展配置
     *
     * @param tenantId 租户 ID
     * @param ids 配置 ID 列表
     */
    void batchDeleteConfigs(Long tenantId, List<Long> ids);

    /**
     * 启用/禁用扩展配置
     *
     * @param tenantId 租户 ID
     * @param id 配置 ID
     * @param status 状态（0=禁用，1=启用）
     */
    void updateConfigStatus(Long tenantId, Long id, Integer status);
}
