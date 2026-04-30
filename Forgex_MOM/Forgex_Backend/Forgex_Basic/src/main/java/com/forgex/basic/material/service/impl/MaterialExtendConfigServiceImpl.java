package com.forgex.basic.material.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.basic.material.domain.entity.BasicMaterialExtendConfig;
import com.forgex.basic.material.domain.response.MaterialExtendConfigVO;
import com.forgex.basic.material.mapper.BasicMaterialExtendConfigMapper;
import com.forgex.basic.material.service.IMaterialExtendConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物料扩展配置服务实现类
 * <p>
 * 提供物料扩展字段配置相关的业务逻辑处理，包括：
 * 1. 扩展字段配置的 CRUD 操作
 * 2. 按模块查询扩展字段配置
 * 3. 字段选项 JSON 解析
 * 4. 字段配置的唯一性校验
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see IMaterialExtendConfigService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialExtendConfigServiceImpl extends ServiceImpl<BasicMaterialExtendConfigMapper, BasicMaterialExtendConfig>
        implements IMaterialExtendConfigService {

    private final BasicMaterialExtendConfigMapper extendConfigMapper;

    /**
     * 分页查询扩展配置列表
     *
     * @param tenantId 租户 ID
     * @param module 模块编码（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 扩展配置分页列表
     */
    public IPage<MaterialExtendConfigVO> pageExtendConfigs(Long tenantId, String module,
                                                           Integer pageNum, Integer pageSize) {
        Page<BasicMaterialExtendConfig> entityPage = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<BasicMaterialExtendConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtendConfig::getTenantId, tenantId)
                .eq(BasicMaterialExtendConfig::getDeleted, 0);

        if (StringUtils.hasText(module)) {
            wrapper.eq(BasicMaterialExtendConfig::getModule, module);
        }

        wrapper.orderByAsc(BasicMaterialExtendConfig::getOrderNum)
                .orderByDesc(BasicMaterialExtendConfig::getCreateTime);

        Page<BasicMaterialExtendConfig> configPage = extendConfigMapper.selectPage(entityPage, wrapper);

        IPage<MaterialExtendConfigVO> voPage = new Page<>(configPage.getCurrent(), configPage.getSize(), configPage.getTotal());
        List<MaterialExtendConfigVO> voList = configPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 根据模块查询扩展配置列表
     *
     * @param tenantId 租户 ID
     * @param module 模块编码
     * @return 扩展配置 VO 列表
     */
    public List<MaterialExtendConfigVO> getConfigsByModule(Long tenantId, String module) {
        if (!StringUtils.hasText(module)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.MODULE_CODE_EMPTY);
        }

        LambdaQueryWrapper<BasicMaterialExtendConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtendConfig::getModule, module)
                .eq(BasicMaterialExtendConfig::getTenantId, tenantId)
                .eq(BasicMaterialExtendConfig::getStatus, 1)
                .eq(BasicMaterialExtendConfig::getDeleted, 0)
                .orderByAsc(BasicMaterialExtendConfig::getOrderNum);

        List<BasicMaterialExtendConfig> configList = extendConfigMapper.selectList(wrapper);

        return configList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 查询扩展配置详情
     *
     * @param tenantId 租户 ID
     * @param id 配置 ID
     * @return 扩展配置 VO
     */
    public MaterialExtendConfigVO getConfigById(Long tenantId, Long id) {
        BasicMaterialExtendConfig config = extendConfigMapper.selectById(id);
        if (config == null || !config.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.MATERIAL_EXTEND_CONFIG_NOT_FOUND);
        }

        return convertToVO(config);
    }

    /**
     * 创建扩展配置
     *
     * @param tenantId 租户 ID
     * @param config 扩展配置实体
     * @return 配置 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createConfig(Long tenantId, BasicMaterialExtendConfig config) {
        validateConfigUniqueness(tenantId, config.getModule(), config.getFieldName(), null);

        config.setTenantId(tenantId);
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getOrderNum() == null) {
            config.setOrderNum(0);
        }

        extendConfigMapper.insert(config);
        log.info("创建物料扩展配置成功，模块: {}, 字段: {}, 配置 ID: {}",
                config.getModule(), config.getFieldName(), config.getId());

        return config.getId();
    }

    /**
     * 更新扩展配置
     *
     * @param tenantId 租户 ID
     * @param config 扩展配置实体
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long tenantId, BasicMaterialExtendConfig config) {
        BasicMaterialExtendConfig existing = extendConfigMapper.selectById(config.getId());
        if (existing == null || !existing.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.MATERIAL_EXTEND_CONFIG_NOT_FOUND);
        }

        validateConfigUniqueness(tenantId, config.getModule(), config.getFieldName(), config.getId());

        extendConfigMapper.updateById(config);
        log.info("更新物料扩展配置成功，配置 ID: {}", config.getId());
    }

    /**
     * 删除扩展配置
     *
     * @param tenantId 租户 ID
     * @param id 配置 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long tenantId, Long id) {
        BasicMaterialExtendConfig config = extendConfigMapper.selectById(id);
        if (config == null || !config.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.MATERIAL_EXTEND_CONFIG_NOT_FOUND);
        }

        extendConfigMapper.deleteById(id);
        log.info("删除物料扩展配置成功，配置 ID: {}", id);
    }

    /**
     * 批量删除扩展配置
     *
     * @param tenantId 租户 ID
     * @param ids 配置 ID 列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteConfigs(Long tenantId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<BasicMaterialExtendConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtendConfig::getTenantId, tenantId)
                .in(BasicMaterialExtendConfig::getId, ids);

        extendConfigMapper.delete(wrapper);
        log.info("批量删除物料扩展配置成功，数量: {}", ids.size());
    }

    /**
     * 启用/禁用扩展配置
     *
     * @param tenantId 租户 ID
     * @param id 配置 ID
     * @param status 状态（0=禁用，1=启用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfigStatus(Long tenantId, Long id, Integer status) {
        BasicMaterialExtendConfig config = extendConfigMapper.selectById(id);
        if (config == null || !config.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.MATERIAL_EXTEND_CONFIG_NOT_FOUND);
        }

        config.setStatus(status);
        extendConfigMapper.updateById(config);
        log.info("更新物料扩展配置状态成功，配置 ID: {}, 状态: {}", id, status);
    }

    /**
     * 校验配置唯一性（同一模块下字段名称唯一）
     *
     * @param tenantId 租户 ID
     * @param module 模块编码
     * @param fieldName 字段名称
     * @param excludeId 排除的配置 ID（用于更新时）
     */
    private void validateConfigUniqueness(Long tenantId, String module, String fieldName, Long excludeId) {
        LambdaQueryWrapper<BasicMaterialExtendConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterialExtendConfig::getTenantId, tenantId)
                .eq(BasicMaterialExtendConfig::getModule, module)
                .eq(BasicMaterialExtendConfig::getFieldName, fieldName)
                .eq(BasicMaterialExtendConfig::getDeleted, 0);

        if (excludeId != null) {
            wrapper.ne(BasicMaterialExtendConfig::getId, excludeId);
        }

        Long count = extendConfigMapper.selectCount(wrapper);
        if (count > 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.FIELD_NAME_EXISTS, module, fieldName);
        }
    }

    /**
     * 转换为 MaterialExtendConfigVO
     *
     * @param config 扩展配置实体
     * @return 扩展配置 VO
     */
    private MaterialExtendConfigVO convertToVO(BasicMaterialExtendConfig config) {
        MaterialExtendConfigVO vo = new MaterialExtendConfigVO();
        BeanUtils.copyProperties(config, vo);

        vo.setModuleName(getModuleName(config.getModule()));
        vo.setFieldTypeName(getFieldTypeName(config.getFieldType()));
        vo.setOptions(parseOptions(config.getFieldOptions()));

        return vo;
    }

    /**
     * 获取模块名称
     *
     * @param module 模块编码
     * @return 模块名称
     */
    private String getModuleName(String module) {
        if (!StringUtils.hasText(module)) {
            return "";
        }
        return switch (module) {
            case "PURCHASE" -> "采购";
            case "INVENTORY" -> "库存";
            case "PRODUCTION" -> "生产";
            case "SALES" -> "销售";
            default -> module;
        };
    }

    /**
     * 获取字段类型名称
     *
     * @param fieldType 字段类型
     * @return 字段类型名称
     */
    private String getFieldTypeName(String fieldType) {
        if (!StringUtils.hasText(fieldType)) {
            return "";
        }
        return switch (fieldType) {
            case "STRING" -> "字符串";
            case "NUMBER" -> "数字";
            case "DATE" -> "日期";
            case "BOOLEAN" -> "布尔";
            case "SELECT" -> "下拉框";
            case "MULTI_SELECT" -> "多选";
            default -> fieldType;
        };
    }

    /**
     * 解析字段选项 JSON
     *
     * @param fieldOptions 选项 JSON 字符串
     * @return 选项列表
     */
    private List<Map<String, String>> parseOptions(String fieldOptions) {
        if (!StringUtils.hasText(fieldOptions)) {
            return Collections.emptyList();
        }

        try {
            List<Map<String, String>> options = new ArrayList<>();
            List<Map> rawList = JSON.parseArray(fieldOptions, Map.class);
            for (Map map : rawList) {
                Map<String, String> optionMap = new HashMap<>();
                map.forEach((key, value) -> {
                    if (key instanceof String && value instanceof String) {
                        optionMap.put((String) key, (String) value);
                    }
                });
                options.add(optionMap);
            }
            return options;
        } catch (Exception e) {
            log.warn("解析字段选项 JSON 失败: {}", fieldOptions, e);
            return Collections.emptyList();
        }
    }
}
