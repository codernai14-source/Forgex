package com.forgex.basic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.material.domain.entity.BasicPackagingType;
import com.forgex.basic.material.domain.entity.BasicUnit;
import com.forgex.basic.material.domain.param.PackagingTypePageParam;
import com.forgex.basic.material.domain.vo.PackagingTypeVO;
import com.forgex.basic.material.mapper.BasicPackagingTypeMapper;
import com.forgex.basic.material.mapper.BasicUnitMapper;
import com.forgex.basic.material.service.IBasicPackagingTypeService;
import com.forgex.basic.material.service.IMaterialPackagingRelationService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.entity.SysDict;
import com.forgex.sys.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 包装规格服务实现类。
 * <p>
 * 实现包装规格主数据查询、字典校验、单位名称补齐和删除关联联动。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@Service
@RequiredArgsConstructor
public class BasicPackagingTypeServiceImpl extends ServiceImpl<BasicPackagingTypeMapper, BasicPackagingType> implements IBasicPackagingTypeService {

    private static final String PACKAGING_SPEC_TYPE_DICT = "packaging_spec_type";

    private final BasicPackagingTypeMapper packagingTypeMapper;
    private final BasicUnitMapper unitMapper;
    private final SysDictMapper dictMapper;
    private final IMaterialPackagingRelationService relationService;

    /**
     * 分页查询包装规格。
     *
     * @param tenantId 租户 ID
     * @param param 请求参数
     * @return 包装规格分页结果
     */
    @Override
    public Page<PackagingTypeVO> pagePackagingTypes(Long tenantId, PackagingTypePageParam param) {
        PackagingTypePageParam safeParam = param == null ? new PackagingTypePageParam() : param;
        Page<BasicPackagingType> page = new Page<>(safeParam.getPageNum(), safeParam.getPageSize());
        Page<BasicPackagingType> entityPage = packagingTypeMapper.selectPage(page, buildQuery(tenantId, safeParam));

        Page<PackagingTypeVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(toVOList(entityPage.getRecords()));
        return voPage;
    }

    /**
     * 查询启用的包装规格列表。
     *
     * @param tenantId 租户 ID
     * @return 包装规格列表
     */
    @Override
    public List<PackagingTypeVO> listAvailable(Long tenantId) {
        List<BasicPackagingType> list = packagingTypeMapper.selectList(new LambdaQueryWrapper<BasicPackagingType>()
                .eq(BasicPackagingType::getTenantId, tenantId)
                .eq(BasicPackagingType::getStatus, 1)
                .eq(BasicPackagingType::getDeleted, false)
                .orderByAsc(BasicPackagingType::getSortOrder)
                .orderByDesc(BasicPackagingType::getCreateTime));
        return toVOList(list);
    }

    /**
     * 创建包装规格。
     *
     * @param tenantId 租户 ID
     * @param packagingType 包装规格实体
     * @return 包装规格 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPackagingType(Long tenantId, BasicPackagingType packagingType) {
        validatePackagingType(tenantId, packagingType, false);
        packagingType.setTenantId(tenantId);
        packagingType.setPackagingCode(normalizeCode(packagingType.getPackagingCode()));
        packagingType.setStatus(packagingType.getStatus() == null ? 1 : packagingType.getStatus());
        packagingType.setSortOrder(packagingType.getSortOrder() == null ? 0 : packagingType.getSortOrder());
        packagingTypeMapper.insert(packagingType);
        return packagingType.getId();
    }

    /**
     * 更新包装规格。
     *
     * @param tenantId 租户 ID
     * @param packagingType 包装规格实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePackagingType(Long tenantId, BasicPackagingType packagingType) {
        validatePackagingType(tenantId, packagingType, true);
        packagingType.setTenantId(tenantId);
        packagingType.setPackagingCode(normalizeCode(packagingType.getPackagingCode()));
        packagingTypeMapper.updateById(packagingType);
    }

    /**
     * 删除包装规格并同步删除关联。
     *
     * @param tenantId 租户 ID
     * @param id 包装规格 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePackagingType(Long tenantId, Long id) {
        if (id == null) {
            throw packagingException(BasicPromptEnum.PACKAGING_SPEC_NOT_FOUND);
        }
        packagingTypeMapper.deleteById(id);
        relationService.deleteByPackagingTypeIds(tenantId, Collections.singletonList(id));
    }

    /**
     * 批量删除包装规格并同步删除关联。
     *
     * @param tenantId 租户 ID
     * @param ids 包装规格 ID 集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeletePackagingTypes(Long tenantId, List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        packagingTypeMapper.deleteBatchIds(ids);
        relationService.deleteByPackagingTypeIds(tenantId, ids);
    }

    private LambdaQueryWrapper<BasicPackagingType> buildQuery(Long tenantId, PackagingTypePageParam param) {
        LambdaQueryWrapper<BasicPackagingType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicPackagingType::getTenantId, tenantId)
                .eq(BasicPackagingType::getDeleted, false)
                .like(StringUtils.hasText(param.getPackagingCode()), BasicPackagingType::getPackagingCode, param.getPackagingCode())
                .like(StringUtils.hasText(param.getPackagingName()), BasicPackagingType::getPackagingName, param.getPackagingName())
                .eq(StringUtils.hasText(param.getPackagingSpecType()), BasicPackagingType::getPackagingSpecType, param.getPackagingSpecType())
                .eq(param.getStatus() != null, BasicPackagingType::getStatus, param.getStatus())
                .orderByAsc(BasicPackagingType::getSortOrder)
                .orderByDesc(BasicPackagingType::getCreateTime);
        return wrapper;
    }

    private List<PackagingTypeVO> toVOList(List<BasicPackagingType> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Map<Long, BasicUnit> unitMap = loadUnitMap(records);
        return records.stream().map(item -> {
            PackagingTypeVO vo = new PackagingTypeVO();
            BeanUtils.copyProperties(item, vo);
            vo.setSizeUnitName(unitName(unitMap, item.getSizeUnitId()));
            vo.setVolumeUnitName(unitName(unitMap, item.getVolumeUnitId()));
            vo.setWeightUnitName(unitName(unitMap, item.getWeightUnitId()));
            vo.setSize(formatSize(vo));
            vo.setVolume(formatValueWithUnit(vo.getVolumeValue(), vo.getVolumeUnitName()));
            vo.setWeight(formatValueWithUnit(vo.getWeightValue(), vo.getWeightUnitName()));
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<Long, BasicUnit> loadUnitMap(List<BasicPackagingType> records) {
        Set<Long> unitIds = new HashSet<>();
        records.forEach(item -> {
            addId(unitIds, item.getSizeUnitId());
            addId(unitIds, item.getVolumeUnitId());
            addId(unitIds, item.getWeightUnitId());
        });
        if (unitIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return unitMapper.selectList(new LambdaQueryWrapper<BasicUnit>()
                        .in(BasicUnit::getId, unitIds)
                        .eq(BasicUnit::getDeleted, false))
                .stream()
                .collect(Collectors.toMap(BasicUnit::getId, Function.identity(), (left, right) -> left));
    }

    private void addId(Collection<Long> ids, Long id) {
        if (id != null) {
            ids.add(id);
        }
    }

    private String unitName(Map<Long, BasicUnit> unitMap, Long unitId) {
        BasicUnit unit = unitMap.get(unitId);
        return unit == null ? null : unit.getUnitName();
    }

    private String formatSize(PackagingTypeVO vo) {
        List<String> values = java.util.stream.Stream.of(vo.getLengthValue(), vo.getWidthValue(), vo.getHeightValue())
                .filter(Objects::nonNull)
                .map(value -> value.stripTrailingZeros().toPlainString())
                .collect(Collectors.toList());
        if (values.isEmpty()) {
            return null;
        }
        String size = String.join(" x ", values);
        return StringUtils.hasText(vo.getSizeUnitName()) ? size + " " + vo.getSizeUnitName() : size;
    }

    private String formatValueWithUnit(java.math.BigDecimal value, String unitName) {
        if (value == null) {
            return null;
        }
        String text = value.stripTrailingZeros().toPlainString();
        return StringUtils.hasText(unitName) ? text + " " + unitName : text;
    }

    private void validatePackagingType(Long tenantId, BasicPackagingType packagingType, boolean update) {
        if (packagingType == null) {
            throw packagingException(BasicPromptEnum.PACKAGING_SPEC_DATA_REQUIRED);
        }
        if (update && packagingType.getId() == null) {
            throw packagingException(BasicPromptEnum.PACKAGING_SPEC_NOT_FOUND);
        }
        if (!StringUtils.hasText(packagingType.getPackagingCode())) {
            throw packagingException(BasicPromptEnum.PACKAGING_SPEC_CODE_REQUIRED);
        }
        if (!StringUtils.hasText(packagingType.getPackagingName())) {
            throw packagingException(BasicPromptEnum.PACKAGING_SPEC_NAME_REQUIRED);
        }
        if (!StringUtils.hasText(packagingType.getPackagingSpecType())) {
            throw packagingException(BasicPromptEnum.PACKAGING_SPEC_TYPE_REQUIRED);
        }
        validateDictValue(tenantId, packagingType.getPackagingSpecType());
        validateCodeUnique(tenantId, packagingType, update);
    }

    private void validateDictValue(Long tenantId, String dictValue) {
        SysDict root = dictMapper.selectOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictCode, PACKAGING_SPEC_TYPE_DICT)
                .eq(SysDict::getDeleted, false)
                .last("LIMIT 1"));
        if (root == null) {
            return;
        }
        Long currentTenant = tenantId == null ? 0L : tenantId;
        Long rootTenant = root.getTenantId() == null ? 0L : root.getTenantId();
        Long count = dictMapper.selectCount(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getParentId, root.getId())
                .eq(SysDict::getDictValue, dictValue)
                .eq(SysDict::getStatus, 1)
                .eq(SysDict::getDeleted, false)
                .in(SysDict::getTenantId, rootTenant, currentTenant));
        if (count == null || count == 0) {
            throw packagingException(BasicPromptEnum.PACKAGING_SPEC_TYPE_INVALID);
        }
    }

    private void validateCodeUnique(Long tenantId, BasicPackagingType packagingType, boolean update) {
        LambdaQueryWrapper<BasicPackagingType> wrapper = new LambdaQueryWrapper<BasicPackagingType>()
                .eq(BasicPackagingType::getTenantId, tenantId)
                .eq(BasicPackagingType::getPackagingCode, normalizeCode(packagingType.getPackagingCode()))
                .eq(BasicPackagingType::getDeleted, false);
        if (update) {
            wrapper.ne(BasicPackagingType::getId, packagingType.getId());
        }
        Long count = packagingTypeMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw packagingException(BasicPromptEnum.PACKAGING_SPEC_CODE_EXISTS);
        }
    }

    private String normalizeCode(String code) {
        return Objects.toString(code, "").trim().toUpperCase();
    }

    private I18nBusinessException packagingException(BasicPromptEnum prompt, Object... args) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt, args);
    }
}
