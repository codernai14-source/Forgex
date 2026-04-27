package com.forgex.basic.label.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.label.domain.dto.LabelTemplateDTO;
import com.forgex.basic.label.domain.entity.LabelTemplate;
import com.forgex.basic.label.domain.param.LabelTemplateQueryParam;
import com.forgex.basic.label.domain.param.LabelTemplateSaveParam;
import com.forgex.basic.label.domain.param.LabelTemplateUpdateParam;
import com.forgex.basic.label.domain.vo.TemplateVO;
import com.forgex.basic.label.mapper.LabelTemplateMapper;
import com.forgex.basic.label.service.LabelTemplateService;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签模板 Service 实现类
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
 * @see LabelTemplateService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabelTemplateServiceImpl extends ServiceImpl<LabelTemplateMapper, LabelTemplate> implements LabelTemplateService {

    private final LabelTemplateMapper labelTemplateMapper;

    /**
     * 分页查询模板列表
     *
     * @param param 查询参数
     * @param tenantId 租户 ID
     * @return 模板分页数据
     */
    @Override
    public IPage<TemplateVO> pageTemplates(LabelTemplateQueryParam param, Long tenantId) {
        Page<LabelTemplate> page = new Page<>(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<LabelTemplate> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(param.getTemplateCode())) {
            wrapper.like(LabelTemplate::getTemplateCode, param.getTemplateCode());
        }
        if (StringUtils.hasText(param.getTemplateName())) {
            wrapper.like(LabelTemplate::getTemplateName, param.getTemplateName());
        }
        if (StringUtils.hasText(param.getTemplateType())) {
            wrapper.eq(LabelTemplate::getTemplateType, param.getTemplateType());
        }
        if (param.getIsDefault() != null) {
            wrapper.eq(LabelTemplate::getIsDefault, param.getIsDefault());
        }
        if (param.getStatus() != null) {
            wrapper.eq(LabelTemplate::getStatus, param.getStatus());
        }
        if (param.getFactoryId() != null) {
            wrapper.eq(LabelTemplate::getFactoryId, param.getFactoryId());
        }

        wrapper.orderByDesc(LabelTemplate::getCreateTime);

        IPage<LabelTemplate> entityPage = labelTemplateMapper.selectPage(page, wrapper);

        // 转换为 VO
        IPage<TemplateVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<TemplateVO> voList = entityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 根据 ID 查询模板详情
     *
     * @param id 模板 ID
     * @param tenantId 租户 ID
     * @return 模板 DTO
     */
    @Override
    public LabelTemplateDTO getTemplateById(Long id, Long tenantId) {
        LabelTemplate template = labelTemplateMapper.selectById(id);
        log.warn("查询模板 - ID: {}, 查询结果: {}, 模板租户ID: {}, 当前租户ID: {}",
                id, template != null ? "找到" : "未找到",
                template != null ? template.getTenantId() : "N/A",
                tenantId);
        if (template == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_NOT_FOUND_WITH_ID, id);
        }

        if (!template.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(
                    StatusCode.BUSINESS_ERROR,
                    BasicPromptEnum.LABEL_TEMPLATE_ACCESS_DENIED,
                    template.getTenantId(),
                    tenantId
            );
        }


        LabelTemplateDTO dto = new LabelTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        return dto;
    }

    /**
     * 新增模板
     *
     * @param param 模板保存参数
     * @param tenantId 租户 ID
     * @return 模板 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addTemplate(LabelTemplateSaveParam param, Long tenantId) {
        // 校验模板编码唯一性
        if (existsByCode(param.getTemplateCode(), tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_CODE_EXISTS, param.getTemplateCode());
        }

        LabelTemplate template = new LabelTemplate();
        BeanUtils.copyProperties(param, template);
        template.setTenantId(tenantId);
        template.setTemplateVersion(1);
        if (template.getIsDefault() == null) {
            template.setIsDefault(false);
        }
        if (template.getStatus() == null) {
            template.setStatus(1);
        }

        labelTemplateMapper.insert(template);
        log.info("新增模板成功，模板 ID: {}, 模板编码: {}", template.getId(), template.getTemplateCode());
        return template.getId();
    }

    /**
     * 更新模板（创建新版本）
     *
     * @param param 模板更新参数
     * @param tenantId 租户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTemplate(LabelTemplateUpdateParam param, Long tenantId) {
        LabelTemplate existing = labelTemplateMapper.selectById(param.getId());
        if (existing == null || !existing.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_NOT_FOUND);
        }

        // 创建新版本
        LabelTemplate newVersion = new LabelTemplate();
        newVersion.setTemplateCode(existing.getTemplateCode());
        newVersion.setTemplateName(param.getTemplateName() != null ? param.getTemplateName() : existing.getTemplateName());
        // 如果传入了新的模板类型，使用新类型；否则使用旧类型
        newVersion.setTemplateType(param.getTemplateType() != null ? param.getTemplateType() : existing.getTemplateType());
        newVersion.setTemplateVersion(existing.getTemplateVersion() + 1);
        newVersion.setTemplateContent(param.getTemplateContent() != null ? param.getTemplateContent() : existing.getTemplateContent());
        newVersion.setDescription(param.getDescription() != null ? param.getDescription() : existing.getDescription());
        newVersion.setStatus(param.getStatus() != null ? param.getStatus() : existing.getStatus());
        newVersion.setIsDefault(existing.getIsDefault());
        newVersion.setFactoryId(param.getFactoryId() != null ? param.getFactoryId() : existing.getFactoryId());
        newVersion.setTenantId(tenantId);

        labelTemplateMapper.insert(newVersion);

        // 将旧版本设置为非默认
        if (existing.getIsDefault()) {
            existing.setIsDefault(false);
            labelTemplateMapper.updateById(existing);
        }

        log.info("更新模板成功，原模板 ID: {}, 新版本 ID: {}, 新版本号: {}, 模板类型: {}",
                existing.getId(), newVersion.getId(), newVersion.getTemplateVersion(), newVersion.getTemplateType());
    }

    /**
     * 删除模板
     *
     * @param id 模板 ID
     * @param tenantId 租户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTemplate(Long id, Long tenantId) {
        LabelTemplate template = labelTemplateMapper.selectById(id);
        if (template == null || !template.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_NOT_FOUND);
        }

        // 只更新 deleted 字段，避免更新其他字段
        LambdaUpdateWrapper<LabelTemplate> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LabelTemplate::getId, id)
                .set(LabelTemplate::getDeleted, true);

        labelTemplateMapper.update(null, updateWrapper);
        log.info("删除模板成功，模板 ID: {}", id);
    }

    /**
     * 批量删除模板
     *
     * @param ids 模板 ID 列表
     * @param tenantId 租户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDeleteTemplates(List<Long> ids, Long tenantId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (Long id : ids) {
            try {
                deleteTemplate(id, tenantId);
                successCount++;
            } catch (I18nBusinessException e) {
                // 业务异常（如模板不存在、权限不足），记录警告但不中断
                log.warn("跳过删除模板 ID: {}, 原因: {}", id, e.getMessage());
                failCount++;
            } catch (Exception e) {
                // 其他异常，记录错误
                log.error("删除模板失败，模板 ID: {}", id, e);
                failCount++;
            }
        }

        log.info("批量删除模板完成，总数: {}, 成功: {}, 失败: {}", ids.size(), successCount, failCount);
    }

    /**
     * 设置默认模板
     *
     * @param id 模板 ID
     * @param templateType 模板类型
     * @param tenantId 租户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setDefaultTemplate(Long id, String templateType, Long tenantId) {
        LabelTemplate template = labelTemplateMapper.selectById(id);
        if (template == null || !template.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_NOT_FOUND);
        }

        if (!template.getTemplateType().equals(templateType)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_TYPE_MISMATCH);
        }

        // 取消同类型其他模板的默认状态
        LambdaQueryWrapper<LabelTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelTemplate::getTemplateType, templateType);

        LabelTemplate updateEntity = new LabelTemplate();
        updateEntity.setIsDefault(false);
        labelTemplateMapper.update(updateEntity, wrapper);

        // 设置当前模板为默认
        template.setIsDefault(true);
        labelTemplateMapper.updateById(template);

        log.info("设置默认模板成功，模板 ID: {}, 模板类型: {}", id, templateType);
    }

    /**
     * 检查模板编码是否存在
     *
     * @param templateCode 模板编码
     * @param tenantId 租户 ID
     * @return true-存在，false-不存在
     */
    @Override
    public boolean existsByCode(String templateCode, Long tenantId) {
        LambdaQueryWrapper<LabelTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelTemplate::getTemplateCode, templateCode);
        return labelTemplateMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查模板编码是否存在（排除指定 ID）
     *
     * @param templateCode 模板编码
     * @param excludeId 排除的模板 ID
     * @param tenantId 租户 ID
     * @return true-存在，false-不存在
     */
    @Override
    public boolean existsByCodeExcludeId(String templateCode, Long excludeId, Long tenantId) {
        LambdaQueryWrapper<LabelTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelTemplate::getTemplateCode, templateCode)
                .ne(LabelTemplate::getId, excludeId);
        return labelTemplateMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取指定类型的默认模板
     *
     * @param templateType 模板类型
     * @param tenantId 租户 ID
     * @return 默认模板，不存在则返回 null
     */
    @Override
    public LabelTemplate getDefaultTemplate(String templateType, Long tenantId) {
        LambdaQueryWrapper<LabelTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelTemplate::getTemplateType, templateType)
                .eq(LabelTemplate::getIsDefault, true)
                .orderByDesc(LabelTemplate::getTemplateVersion)
                .last("LIMIT 1");
        return labelTemplateMapper.selectOne(wrapper);
    }

    /**
     * 转换为 VO
     *
     * @param entity 实体对象
     * @return VO 对象
     */
    private TemplateVO convertToVO(LabelTemplate entity) {
        TemplateVO vo = new TemplateVO();
        BeanUtils.copyProperties(entity, vo);
        // 可以在这里添加额外的转换逻辑，如枚举值转名称等
        return vo;
    }
}
