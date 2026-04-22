package com.forgex.basic.label.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.label.domain.dto.LabelTemplateDTO;
import com.forgex.basic.label.domain.entity.LabelTemplate;
import com.forgex.basic.label.domain.param.LabelTemplateQueryParam;
import com.forgex.basic.label.domain.param.LabelTemplateSaveParam;
import com.forgex.basic.label.domain.param.LabelTemplateUpdateParam;
import com.forgex.basic.label.domain.vo.TemplateVO;

import java.util.List;

/**
 * 标签模板 Service 接口
 * <p>
 * 提供标签模板管理相关的业务操作，包括：
 * 1. 模板 CRUD 操作
 * 2. 模板版本管理
 * 3. 默认模板设置
 * 4. 模板唯一性校验
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
public interface LabelTemplateService extends IService<LabelTemplate> {

    /**
     * 分页查询模板列表
     *
     * @param param 查询参数
     * @param tenantId 租户 ID
     * @return 模板分页数据
     */
    IPage<TemplateVO> pageTemplates(LabelTemplateQueryParam param, Long tenantId);

    /**
     * 根据 ID 查询模板详情
     *
     * @param id 模板 ID
     * @param tenantId 租户 ID
     * @return 模板 DTO
     */
    LabelTemplateDTO getTemplateById(Long id, Long tenantId);

    /**
     * 新增模板
     *
     * @param param 模板保存参数
     * @param tenantId 租户 ID
     * @return 模板 ID
     */
    Long addTemplate(LabelTemplateSaveParam param, Long tenantId);

    /**
     * 更新模板（创建新版本）
     *
     * @param param 模板更新参数
     * @param tenantId 租户 ID
     */
    void updateTemplate(LabelTemplateUpdateParam param, Long tenantId);

    /**
     * 删除模板
     *
     * @param id 模板 ID
     * @param tenantId 租户 ID
     */
    void deleteTemplate(Long id, Long tenantId);

    /**
     * 批量删除模板
     *
     * @param ids 模板 ID 列表
     * @param tenantId 租户 ID
     */
    void batchDeleteTemplates(List<Long> ids, Long tenantId);

    /**
     * 设置默认模板
     *
     * @param id 模板 ID
     * @param templateType 模板类型
     * @param tenantId 租户 ID
     */
    void setDefaultTemplate(Long id, String templateType, Long tenantId);

    /**
     * 检查模板编码是否存在
     *
     * @param templateCode 模板编码
     * @param tenantId 租户 ID
     * @return true-存在，false-不存在
     */
    boolean existsByCode(String templateCode, Long tenantId);

    /**
     * 检查模板编码是否存在（排除指定 ID）
     *
     * @param templateCode 模板编码
     * @param excludeId 排除的模板 ID
     * @param tenantId 租户 ID
     * @return true-存在，false-不存在
     */
    boolean existsByCodeExcludeId(String templateCode, Long excludeId, Long tenantId);

    /**
     * 获取指定类型的默认模板
     *
     * @param templateType 模板类型
     * @param tenantId 租户 ID
     * @return 默认模板，不存在则返回 null
     */
    LabelTemplate getDefaultTemplate(String templateType, Long tenantId);
}
