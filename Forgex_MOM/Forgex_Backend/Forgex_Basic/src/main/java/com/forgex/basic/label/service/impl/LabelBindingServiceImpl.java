package com.forgex.basic.label.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.factory.domain.entity.BasicFactory;
import com.forgex.basic.factory.service.FactoryService;
import com.forgex.basic.label.domain.entity.LabelBinding;
import com.forgex.basic.label.domain.entity.LabelTemplate;
import com.forgex.basic.label.enums.MatchPriorityEnum;
import com.forgex.basic.label.mapper.LabelBindingMapper;
import com.forgex.basic.label.service.LabelBindingService;
import com.forgex.basic.label.service.LabelTemplateService;
import com.forgex.basic.label.domain.param.LabelBindingSaveParam;
import com.forgex.basic.label.domain.vo.BindingVO;
import com.forgex.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签绑定 Service 实现类
 * <p>
 * 提供标签绑定管理相关的业务操作，包括：
 * 1. 按物料绑定模板
 * 2. 按供应商绑定模板
 * 3. 按客户绑定模板
 * 4. 模板匹配逻辑（多级降级策略）
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see LabelBindingService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabelBindingServiceImpl extends ServiceImpl<LabelBindingMapper, LabelBinding> implements LabelBindingService {

    private final LabelBindingMapper labelBindingMapper;
    private final LabelTemplateService labelTemplateService;
    private final FactoryService factoryService;

    /**
     * 分页查询绑定关系列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param templateId 模板 ID
     * @param bindingType 绑定类型
     * @param bindingValue 绑定值
     * @param factoryId 工厂 ID
     * @param tenantId 租户 ID
     * @return 绑定关系分页数据
     */
    @Override
    public IPage<BindingVO> pageBindings(Integer pageNum, Integer pageSize, Long templateId, String templateCode,
                                         String bindingType, String bindingValue, Long factoryId, Long tenantId) {
        Page<LabelBinding> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<LabelBinding> wrapper = new LambdaQueryWrapper<>();

        if (templateId != null) {
            wrapper.eq(LabelBinding::getTemplateId, templateId);
        }
        if (StringUtils.hasText(bindingType)) {
            wrapper.eq(LabelBinding::getBindingType, bindingType);
        }
        if (StringUtils.hasText(bindingValue)) {
            wrapper.like(LabelBinding::getBindingValue, bindingValue);
        }
        if (factoryId != null) {
            wrapper.eq(LabelBinding::getFactoryId, factoryId);
        }

        wrapper.orderByDesc(LabelBinding::getPriority)
                .orderByDesc(LabelBinding::getCreateTime);

        IPage<LabelBinding> entityPage = labelBindingMapper.selectPage(page, wrapper);

        // 转换为 VO 并填充工厂名称和模板信息
        IPage<BindingVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<BindingVO> voList = entityPage.getRecords().stream()
                .map(entity -> {
                    BindingVO vo = new BindingVO();
                    vo.setId(entity.getId());
                    vo.setTemplateId(entity.getTemplateId());
                    vo.setBindingType(entity.getBindingType());
                    vo.setBindingValue(entity.getBindingValue());
                    vo.setPriority(entity.getPriority());
                    vo.setFactoryId(entity.getFactoryId());
                    vo.setTenantId(entity.getTenantId());
                    vo.setCreateBy(entity.getCreateBy());
                    vo.setCreateTime(entity.getCreateTime());
                    vo.setUpdateBy(entity.getUpdateBy());
                    vo.setUpdateTime(entity.getUpdateTime());

                    // 查询模板名称和编码
                    if (entity.getTemplateId() != null) {
                        LabelTemplate template = labelTemplateService.getById(entity.getTemplateId());
                        if (template != null) {
                            vo.setTemplateName(template.getTemplateName());
                            vo.setTemplateCode(template.getTemplateCode());
                        }
                    }

                    // 查询工厂名称
                    if (entity.getFactoryId() != null) {
                        BasicFactory factory = factoryService.getById(entity.getFactoryId());
                        if (factory != null) {
                            vo.setFactoryName(factory.getFactoryName());
                        }
                    }

                    // 设置绑定名称（根据绑定类型查询对应的名称）
                    if (entity.getBindingValue() != null) {
                        vo.setBindingName(entity.getBindingValue()); // 暂时使用绑定值
                    }

                    // 使用枚举设置绑定类型名称
                    MatchPriorityEnum priorityEnum = MatchPriorityEnum.getByCode(entity.getBindingType());
                    if (priorityEnum != null) {
                        vo.setBindingTypeName(priorityEnum.getDescription());
                    }

                    return vo;
                })
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 新增绑定关系
     *
     * @param param 绑定保存参数
     * @param tenantId 租户 ID
     * @return 绑定 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addBinding(LabelBindingSaveParam param, Long tenantId) {
        // 校验模板是否存在
        LabelTemplate template = labelTemplateService.getById(param.getTemplateId());
        if (template == null || !template.getTenantId().equals(tenantId)) {
            throw new BusinessException("模板不存在");
        }

        // 验证绑定类型是否有效
        MatchPriorityEnum priorityEnum = MatchPriorityEnum.getByCode(param.getBindingType());
        if (priorityEnum == null) {
            throw new BusinessException("无效的绑定类型: " + param.getBindingType());
        }

        // 检查是否已存在相同维度的绑定
        LambdaQueryWrapper<LabelBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBinding::getTemplateId, param.getTemplateId())
                .eq(LabelBinding::getBindingType, param.getBindingType())
                .eq(LabelBinding::getBindingValue, param.getBindingValue());

        if (labelBindingMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该绑定关系已存在");
        }

        LabelBinding binding = new LabelBinding();
        BeanUtils.copyProperties(param, binding);
        binding.setTenantId(tenantId);
        if (binding.getPriority() == null) {
            // 使用枚举中的默认优先级
            binding.setPriority(priorityEnum.getPriority());
        }

        labelBindingMapper.insert(binding);
        log.info("新增绑定关系成功，绑定 ID: {}, 绑定类型: {}, 绑定值: {}",
                binding.getId(), binding.getBindingType(), binding.getBindingValue());
        return binding.getId();
    }

    /**
     * 更新绑定关系
     *
     * @param id 绑定 ID
     * @param priority 优先级
     * @param factoryId 工厂 ID
     * @param tenantId 租户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateBinding(Long id, Integer priority, Long factoryId, Long tenantId) {
        LabelBinding binding = labelBindingMapper.selectById(id);
        if (binding == null || !binding.getTenantId().equals(tenantId)) {
            throw new BusinessException("绑定关系不存在");
        }

        if (priority != null) {
            binding.setPriority(priority);
        }
        if (factoryId != null) {
            binding.setFactoryId(factoryId);
        }

        labelBindingMapper.updateById(binding);
        log.info("更新绑定关系成功，绑定 ID: {}", id);
    }

    /**
     * 删除绑定关系
     *
     * @param id 绑定 ID
     * @param tenantId 租户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBinding(Long id, Long tenantId) {
        LabelBinding binding = labelBindingMapper.selectById(id);
        if (binding == null || !binding.getTenantId().equals(tenantId)) {
            throw new BusinessException("绑定关系不存在");
        }

        binding.setDeleted(true);
        labelBindingMapper.updateById(binding);
        log.info("删除绑定关系成功，绑定 ID: {}", id);
    }

    /**
     * 根据绑定条件匹配模板
     * <p>
     * 匹配优先级：
     * 1. 按物料精确匹配
     * 2. 按供应商匹配
     * 3. 按客户匹配
     * 4. 返回默认模板
     * </p>
     *
     * @param factoryId 工厂 ID
     * @param templateType 模板类型
     * @param materialId 物料 ID（可选）
     * @param supplierId 供应商 ID（可选）
     * @param customerId 客户 ID（可选）
     * @param tenantId 租户 ID
     * @return 匹配的模板 ID，未找到则抛出异常
     */
    @Override
    public Long matchTemplate(Long factoryId, String templateType, Long materialId,
                              Long supplierId, Long customerId, Long tenantId) {

        // 尝试多级匹配
        Long templateId = tryMatchTemplate(factoryId, templateType, materialId, supplierId, customerId, tenantId);

        if (templateId != null) {
            return templateId;
        }

        // 降级到默认模板
        LabelTemplate defaultTemplate = labelTemplateService.getDefaultTemplate(templateType, tenantId);
        if (defaultTemplate != null) {
            log.info("使用默认模板，模板类型: {}, 模板 ID: {}", templateType, defaultTemplate.getId());
            return defaultTemplate.getId();
        }

        throw new BusinessException("未找到可用模板，请检查模板配置");
    }

    /**
     * 尝试多级匹配模板
     * <p>
     * 按照 MatchPriorityEnum 定义的优先级顺序进行匹配：
     * 1. MATERIAL - 按物料匹配（优先级 1）
     * 2. SUPPLIER - 按供应商匹配（优先级 2）
     * 3. CUSTOMER - 按客户匹配（优先级 3）
     * </p>
     *
     * @param factoryId 工厂 ID
     * @param templateType 模板类型
     * @param materialId 物料 ID
     * @param supplierId 供应商 ID
     * @param customerId 客户 ID
     * @param tenantId 租户 ID
     * @return 匹配的模板 ID，未找到返回 null
     */
    private Long tryMatchTemplate(Long factoryId, String templateType, Long materialId,
                                  Long supplierId, Long customerId, Long tenantId) {

        // 1. 优先按物料匹配（优先级最高）
        if (materialId != null) {
            Long templateId = findBindingByValue(factoryId, templateType,
                    MatchPriorityEnum.MATERIAL.getCode(), String.valueOf(materialId), tenantId);
            if (templateId != null) {
                log.debug("按物料匹配成功，物料ID: {}, 模板ID: {}", materialId, templateId);
                return templateId;
            }
        }

        // 2. 按供应商匹配
        if (supplierId != null) {
            Long templateId = findBindingByValue(factoryId, templateType,
                    MatchPriorityEnum.SUPPLIER.getCode(), String.valueOf(supplierId), tenantId);
            if (templateId != null) {
                log.debug("按供应商匹配成功，供应商ID: {}, 模板ID: {}", supplierId, templateId);
                return templateId;
            }
        }

        // 3. 按客户匹配
        if (customerId != null) {
            Long templateId = findBindingByValue(factoryId, templateType,
                    MatchPriorityEnum.CUSTOMER.getCode(), String.valueOf(customerId), tenantId);
            if (templateId != null) {
                log.debug("按客户匹配成功，客户ID: {}, 模板ID: {}", customerId, templateId);
                return templateId;
            }
        }

        return null;
    }

    /**
     * 根据绑定值查找模板
     *
     * @param factoryId 工厂 ID
     * @param templateType 模板类型
     * @param bindingType 绑定类型
     * @param bindingValue 绑定值
     * @param tenantId 租户 ID
     * @return 模板 ID，未找到返回 null
     */
    private Long findBindingByValue(Long factoryId, String templateType, String bindingType,
                                    String bindingValue, Long tenantId) {
        LambdaQueryWrapper<LabelBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBinding::getFactoryId, factoryId)
                .eq(LabelBinding::getBindingType, bindingType)
                .eq(LabelBinding::getBindingValue, bindingValue)
                .orderByDesc(LabelBinding::getPriority)
                .last("LIMIT 1");

        LabelBinding binding = labelBindingMapper.selectOne(wrapper);
        return binding != null ? binding.getTemplateId() : null;
    }


    /**
     * 创建或更新绑定关系
     *
     * @param binding 绑定实体
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateBinding(LabelBinding binding) {
        // 检查是否已存在相同维度的绑定
        LambdaQueryWrapper<LabelBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBinding::getFactoryId, binding.getFactoryId())
                .eq(LabelBinding::getBindingType, binding.getBindingType())
                .eq(LabelBinding::getBindingValue, binding.getBindingValue());

        LabelBinding existing = labelBindingMapper.selectOne(wrapper);
        if (existing != null) {
            // 更新现有绑定
            existing.setTemplateId(binding.getTemplateId());
            existing.setPriority(binding.getPriority());
            labelBindingMapper.updateById(existing);
            log.info("更新标签绑定成功，绑定 ID: {}", existing.getId());
        } else {
            // 新增绑定
            labelBindingMapper.insert(binding);
            log.info("创建标签绑定成功，绑定 ID: {}", binding.getId());
        }
    }
}
