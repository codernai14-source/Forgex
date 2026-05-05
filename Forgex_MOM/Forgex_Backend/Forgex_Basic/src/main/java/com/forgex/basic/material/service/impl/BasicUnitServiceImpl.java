package com.forgex.basic.material.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.material.domain.entity.BasicUnit;
import com.forgex.basic.material.domain.entity.BasicUnitConversion;
import com.forgex.basic.material.domain.entity.BasicUnitType;
import com.forgex.basic.material.domain.param.UnitConversionRowParam;
import com.forgex.basic.material.domain.param.UnitConversionSaveParam;
import com.forgex.basic.material.domain.param.UnitPageParam;
import com.forgex.basic.material.domain.param.UnitTypeParam;
import com.forgex.basic.material.domain.vo.UnitConversionVO;
import com.forgex.basic.material.domain.vo.UnitTypeTreeVO;
import com.forgex.basic.material.domain.vo.UnitVO;
import com.forgex.basic.material.mapper.BasicUnitConversionMapper;
import com.forgex.basic.material.mapper.BasicUnitMapper;
import com.forgex.basic.material.mapper.BasicUnitTypeMapper;
import com.forgex.basic.material.service.IBasicUnitService;
import com.forgex.common.api.dto.UnitConvertRequestDTO;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 计量单位服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Service
@RequiredArgsConstructor
public class BasicUnitServiceImpl extends ServiceImpl<BasicUnitMapper, BasicUnit> implements IBasicUnitService {

    private static final Long ROOT_PARENT_ID = 0L;
    private static final String UNIT_FIELD_TYPE_ID = "ID";
    private static final String UNIT_FIELD_TYPE_CODE = "CODE";

    private final BasicUnitMapper unitMapper;
    private final BasicUnitTypeMapper unitTypeMapper;
    private final BasicUnitConversionMapper conversionMapper;

    /**
     * 分页查询计量单位。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<UnitVO> pageUnits(UnitPageParam param) {
        UnitPageParam safeParam = param == null ? new UnitPageParam() : param;
        Page<BasicUnit> page = new Page<>(safeParam.getPageNum(), safeParam.getPageSize());
        Page<BasicUnit> sourcePage = unitMapper.selectPage(page, unitWrapper(safeParam));
        Page<UnitVO> result = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
        result.setRecords(toUnitVOList(sourcePage.getRecords()));
        return result;
    }

    /**
     * 查询计量单位列表。
     *
     * @param param 请求参数
     * @return 列表数据
     */
    @Override
    public List<UnitVO> listUnits(UnitPageParam param) {
        UnitPageParam safeParam = param == null ? new UnitPageParam() : param;
        return toUnitVOList(unitMapper.selectList(unitWrapper(safeParam)));
    }

    /**
     * 查询数据详情。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @Override
    public UnitVO detail(Long id) {
        BasicUnit unit = requireUnit(id);
        return toUnitVO(unit, typeMap(List.of(unit.getUnitTypeId())));
    }

    /**
     * 创建数据。
     *
     * @param unit 单位
     * @return 数据主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BasicUnit unit) {
        validateUnit(unit, true);
        unit.setUnitCode(normalizeCode(unit.getUnitCode()));
        unit.setTenantId(currentTenant());
        unitMapper.insert(unit);
        return unit.getId();
    }

    /**
     * 更新单位。
     *
     * @param unit 单位
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUnit(BasicUnit unit) {
        validateUnit(unit, false);
        BasicUnit exists = requireUnit(unit.getId());
        unit.setTenantId(currentTenant());
        unit.setUnitCode(normalizeCode(unit.getUnitCode()));
        if (!Objects.equals(exists.getUnitCode(), unit.getUnitCode())
                || !Objects.equals(exists.getUnitTypeId(), unit.getUnitTypeId())) {
            ensureUnitCodeUnique(unit.getUnitTypeId(), unit.getUnitCode(), unit.getId());
        }
        unitMapper.updateById(unit);
        return true;
    }

    /**
     * 删除单位。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUnit(Long id) {
        requireUnit(id);
        long relationCount = conversionMapper.selectCount(new LambdaQueryWrapper<BasicUnitConversion>()
                .eq(BasicUnitConversion::getTenantId, currentTenant())
                .eq(BasicUnitConversion::getDeleted, false)
                .and(w -> w.eq(BasicUnitConversion::getUnitId, id)
                        .or()
                        .eq(BasicUnitConversion::getTargetUnitId, id)));
        if (relationCount > 0) {
            throw ex(BasicPromptEnum.UNIT_HAS_CONVERSION);
        }
        unitMapper.deleteById(id);
        return true;
    }

    /**
     * 批量删除计量单位。
     *
     * @param ids 主键 ID 集合
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDeleteUnits(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        for (Long id : ids) {
            deleteUnit(id);
        }
        return true;
    }

    /**
     * 查询计量单位类型树。
     *
     * @return 列表数据
     */
    @Override
    public List<UnitTypeTreeVO> typeTree() {
        List<BasicUnitType> types = unitTypeMapper.selectList(new LambdaQueryWrapper<BasicUnitType>()
                .eq(BasicUnitType::getTenantId, currentTenant())
                .eq(BasicUnitType::getDeleted, false)
                .orderByAsc(BasicUnitType::getLevelPath)
                .orderByAsc(BasicUnitType::getUnitTypeCode));
        return buildTypeTree(types);
    }

    /**
     * 查询计量单位类型详情。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @Override
    public BasicUnitType typeDetail(Long id) {
        return requireType(id);
    }

    /**
     * 创建计量单位类型。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createType(UnitTypeParam param) {
        validateType(param, true);
        BasicUnitType type = new BasicUnitType();
        type.setUnitTypeCode(normalizeCode(param.getUnitTypeCode()));
        type.setUnitTypeName(param.getUnitTypeName() == null ? null : param.getUnitTypeName().trim());
        type.setParentId(param.getParentId() == null ? ROOT_PARENT_ID : param.getParentId());
        type.setTenantId(currentTenant());
        unitTypeMapper.insert(type);
        type.setLevelPath(resolveTypeLevelPath(type));
        unitTypeMapper.updateById(type);
        return type.getId();
    }

    /**
     * 更新类型。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateType(UnitTypeParam param) {
        validateType(param, false);
        BasicUnitType exists = requireType(param.getId());
        Long parentId = param.getParentId() == null ? ROOT_PARENT_ID : param.getParentId();
        BasicUnitType type = new BasicUnitType();
        type.setId(param.getId());
        type.setTenantId(currentTenant());
        type.setUnitTypeCode(normalizeCode(param.getUnitTypeCode()));
        type.setUnitTypeName(param.getUnitTypeName() == null ? null : param.getUnitTypeName().trim());
        type.setParentId(parentId);
        if (!Objects.equals(exists.getUnitTypeCode(), type.getUnitTypeCode())) {
            ensureTypeCodeUnique(type.getUnitTypeCode(), type.getId());
        }
        type.setLevelPath(resolveTypeLevelPath(type));
        unitTypeMapper.updateById(type);
        return true;
    }

    /**
     * 删除类型。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteType(Long id) {
        BasicUnitType type = requireType(id);
        long childCount = unitTypeMapper.selectCount(new LambdaQueryWrapper<BasicUnitType>()
                .eq(BasicUnitType::getTenantId, currentTenant())
                .eq(BasicUnitType::getParentId, type.getId())
                .eq(BasicUnitType::getDeleted, false));
        if (childCount > 0) {
            throw ex(BasicPromptEnum.UNIT_TYPE_HAS_CHILDREN);
        }
        long unitCount = unitMapper.selectCount(new LambdaQueryWrapper<BasicUnit>()
                .eq(BasicUnit::getTenantId, currentTenant())
                .eq(BasicUnit::getUnitTypeId, type.getId())
                .eq(BasicUnit::getDeleted, false));
        if (unitCount > 0) {
            throw ex(BasicPromptEnum.UNIT_TYPE_HAS_UNITS);
        }
        unitTypeMapper.deleteById(id);
        return true;
    }

    /**
     * 查询计量单位换算关系列表。
     *
     * @param unitId 单位 ID
     * @return 列表数据
     */
    @Override
    public List<UnitConversionVO> listConversions(Long unitId) {
        BasicUnit source = requireUnit(unitId);
        List<BasicUnitConversion> conversions = conversionMapper.selectList(new LambdaQueryWrapper<BasicUnitConversion>()
                .eq(BasicUnitConversion::getTenantId, currentTenant())
                .eq(BasicUnitConversion::getUnitId, unitId)
                .eq(BasicUnitConversion::getDeleted, false)
                .orderByAsc(BasicUnitConversion::getId));
        List<Long> unitIds = new ArrayList<>();
        unitIds.add(source.getId());
        unitIds.addAll(conversions.stream().map(BasicUnitConversion::getTargetUnitId).toList());
        Map<Long, BasicUnit> unitMap = unitMap(unitIds);
        return conversions.stream()
                .map(item -> toConversionVO(item, unitMap))
                .toList();
    }

    /**
     * 保存conversions。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveConversions(UnitConversionSaveParam param) {
        if (param == null || param.getUnitId() == null) {
            throw ex(BasicPromptEnum.UNIT_CONVERT_PARAM_INVALID);
        }
        BasicUnit source = requireUnit(param.getUnitId());
        List<UnitConversionRowParam> rows = param.getConversions() == null ? new ArrayList<>() : param.getConversions();
        List<Long> targetIds = rows.stream().map(UnitConversionRowParam::getTargetUnitId).filter(Objects::nonNull).toList();
        Map<Long, BasicUnit> targets = unitMap(targetIds);
        List<Long> keepIds = new ArrayList<>();
        for (UnitConversionRowParam row : rows) {
            validateConversionRow(source, row, targets);
            BasicUnitConversion conversion = row.getId() == null ? new BasicUnitConversion() : requireConversion(row.getId());
            if (!Objects.equals(conversion.getUnitId(), source.getId())) {
                throw ex(BasicPromptEnum.UNIT_CONVERSION_RELATION_NOT_FOUND);
            }
            if (row.getId() == null) {
                ensureConversionUnique(source.getId(), row.getTargetUnitId(), null);
            } else {
                ensureConversionUnique(source.getId(), row.getTargetUnitId(), row.getId());
            }
            conversion.setTenantId(currentTenant());
            conversion.setUnitId(source.getId());
            conversion.setTargetUnitId(row.getTargetUnitId());
            conversion.setConversionValue(row.getConversionValue());
            if (conversion.getId() == null) {
                conversionMapper.insert(conversion);
            } else {
                conversionMapper.updateById(conversion);
            }
            keepIds.add(conversion.getId());
        }
        LambdaQueryWrapper<BasicUnitConversion> deleteWrapper = new LambdaQueryWrapper<BasicUnitConversion>()
                .eq(BasicUnitConversion::getTenantId, currentTenant())
                .eq(BasicUnitConversion::getUnitId, source.getId())
                .eq(BasicUnitConversion::getDeleted, false);
        if (!keepIds.isEmpty()) {
            deleteWrapper.notIn(BasicUnitConversion::getId, keepIds);
        }
        conversionMapper.delete(deleteWrapper);
        return true;
    }

    /**
     * 删除conversion。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteConversion(Long id) {
        requireConversion(id);
        conversionMapper.deleteById(id);
        return true;
    }

    /**
     * 换算单位相关字段。
     *
     * @param request 请求参数
     * @return 处理结果
     */
    @Override
    public JSONObject convertFields(UnitConvertRequestDTO request) {
        validateConvertRequest(request);
        Map<String, Object> entity = request.getEntity();
        Object unitValue = entity.get(request.getUnitFieldName());
        if (unitValue == null) {
            throw ex(BasicPromptEnum.UNIT_CONVERT_FIELD_NOT_FOUND, request.getUnitFieldName());
        }
        BasicUnit source = resolveSourceUnit(unitValue, request.getUnitFieldType());
        BasicUnit target = requireUnitByCode(request.getTargetUnitCode());
        if (!Objects.equals(source.getUnitTypeId(), target.getUnitTypeId())) {
            throw ex(BasicPromptEnum.UNIT_CONVERSION_TARGET_INVALID);
        }

        JSONObject result = new JSONObject();
        result.set("targetUnitCode", target.getUnitCode());
        result.set("targetUnitId", target.getId());

        BigDecimal factor = Objects.equals(source.getId(), target.getId())
                ? BigDecimal.ONE
                : resolveConversionFactor(source.getId(), target.getId());

        for (String fieldName : request.getTargetFieldNames()) {
            if (!entity.containsKey(fieldName)) {
                throw ex(BasicPromptEnum.UNIT_CONVERT_FIELD_NOT_FOUND, fieldName);
            }
            BigDecimal value = toBigDecimal(entity.get(fieldName), fieldName);
            result.set(fieldName, value.multiply(factor, MathContext.DECIMAL128));
        }
        return result;
    }

    private LambdaQueryWrapper<BasicUnit> unitWrapper(UnitPageParam param) {
        return new LambdaQueryWrapper<BasicUnit>()
                .eq(BasicUnit::getTenantId, currentTenant())
                .eq(BasicUnit::getDeleted, false)
                .eq(param.getUnitTypeId() != null, BasicUnit::getUnitTypeId, param.getUnitTypeId())
                .like(StringUtils.hasText(param.getUnitCode()), BasicUnit::getUnitCode, param.getUnitCode())
                .like(StringUtils.hasText(param.getUnitName()), BasicUnit::getUnitName, param.getUnitName())
                .orderByAsc(BasicUnit::getUnitCode)
                .orderByDesc(BasicUnit::getCreateTime);
    }

    private void validateType(UnitTypeParam param, boolean create) {
        if (param == null || !StringUtils.hasText(param.getUnitTypeCode()) || !StringUtils.hasText(param.getUnitTypeName())) {
            throw ex(BasicPromptEnum.UNIT_CONVERT_PARAM_INVALID);
        }
        if (!create && param.getId() == null) {
            throw ex(BasicPromptEnum.UNIT_TYPE_NOT_FOUND);
        }
        if (param.getParentId() != null && !ROOT_PARENT_ID.equals(param.getParentId())) {
            requireType(param.getParentId());
        }
        if (create) {
            ensureTypeCodeUnique(normalizeCode(param.getUnitTypeCode()), null);
        }
    }

    private void validateUnit(BasicUnit unit, boolean create) {
        if (unit == null || unit.getUnitTypeId() == null || !StringUtils.hasText(unit.getUnitCode()) || !StringUtils.hasText(unit.getUnitName())) {
            throw ex(BasicPromptEnum.UNIT_CONVERT_PARAM_INVALID);
        }
        if (!create && unit.getId() == null) {
            throw ex(BasicPromptEnum.UNIT_NOT_FOUND);
        }
        requireType(unit.getUnitTypeId());
        ensureUnitCodeUnique(unit.getUnitTypeId(), normalizeCode(unit.getUnitCode()), create ? null : unit.getId());
    }

    private void validateConversionRow(BasicUnit source, UnitConversionRowParam row, Map<Long, BasicUnit> targets) {
        if (row == null || row.getTargetUnitId() == null || row.getConversionValue() == null
                || row.getConversionValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw ex(BasicPromptEnum.UNIT_CONVERT_PARAM_INVALID);
        }
        if (Objects.equals(source.getId(), row.getTargetUnitId())) {
            throw ex(BasicPromptEnum.UNIT_CONVERSION_TARGET_INVALID);
        }
        BasicUnit target = targets.get(row.getTargetUnitId());
        if (target == null || !Objects.equals(source.getUnitTypeId(), target.getUnitTypeId())) {
            throw ex(BasicPromptEnum.UNIT_CONVERSION_TARGET_INVALID);
        }
    }

    private void validateConvertRequest(UnitConvertRequestDTO request) {
        if (request == null || request.getEntity() == null || request.getEntity().isEmpty()
                || !StringUtils.hasText(request.getTargetUnitCode())
                || !StringUtils.hasText(request.getUnitFieldName())
                || !StringUtils.hasText(request.getUnitFieldType())
                || request.getTargetFieldNames() == null || request.getTargetFieldNames().isEmpty()) {
            throw ex(BasicPromptEnum.UNIT_CONVERT_PARAM_INVALID);
        }
    }

    private BasicUnit resolveSourceUnit(Object unitValue, String unitFieldType) {
        String type = unitFieldType.trim().toUpperCase();
        if (UNIT_FIELD_TYPE_ID.equals(type)) {
            try {
                return requireUnit(Long.valueOf(String.valueOf(unitValue)));
            } catch (NumberFormatException e) {
                throw ex(BasicPromptEnum.UNIT_CONVERT_PARAM_INVALID);
            }
        }
        if (UNIT_FIELD_TYPE_CODE.equals(type)) {
            return requireUnitByCode(String.valueOf(unitValue));
        }
        throw ex(BasicPromptEnum.UNIT_CONVERT_PARAM_INVALID);
    }

    private BigDecimal resolveConversionFactor(Long sourceId, Long targetId) {
        BasicUnitConversion direct = conversionMapper.selectOne(new LambdaQueryWrapper<BasicUnitConversion>()
                .eq(BasicUnitConversion::getTenantId, currentTenant())
                .eq(BasicUnitConversion::getUnitId, sourceId)
                .eq(BasicUnitConversion::getTargetUnitId, targetId)
                .eq(BasicUnitConversion::getDeleted, false)
                .last("LIMIT 1"));
        if (direct != null) {
            return direct.getConversionValue();
        }
        BasicUnitConversion reverse = conversionMapper.selectOne(new LambdaQueryWrapper<BasicUnitConversion>()
                .eq(BasicUnitConversion::getTenantId, currentTenant())
                .eq(BasicUnitConversion::getUnitId, targetId)
                .eq(BasicUnitConversion::getTargetUnitId, sourceId)
                .eq(BasicUnitConversion::getDeleted, false)
                .last("LIMIT 1"));
        if (reverse == null) {
            throw ex(BasicPromptEnum.UNIT_CONVERSION_RELATION_NOT_FOUND);
        }
        return BigDecimal.ONE.divide(reverse.getConversionValue(), MathContext.DECIMAL128);
    }

    private BasicUnit requireUnit(Long id) {
        BasicUnit unit = id == null ? null : unitMapper.selectOne(new LambdaQueryWrapper<BasicUnit>()
                .eq(BasicUnit::getTenantId, currentTenant())
                .eq(BasicUnit::getId, id)
                .eq(BasicUnit::getDeleted, false)
                .last("LIMIT 1"));
        if (unit == null) {
            throw ex(BasicPromptEnum.UNIT_NOT_FOUND);
        }
        return unit;
    }

    private BasicUnit requireUnitByCode(String code) {
        BasicUnit unit = unitMapper.selectOne(new LambdaQueryWrapper<BasicUnit>()
                .eq(BasicUnit::getTenantId, currentTenant())
                .eq(BasicUnit::getUnitCode, normalizeCode(code))
                .eq(BasicUnit::getDeleted, false)
                .last("LIMIT 1"));
        if (unit == null) {
            throw ex(BasicPromptEnum.UNIT_NOT_FOUND);
        }
        return unit;
    }

    private BasicUnitType requireType(Long id) {
        BasicUnitType type = id == null ? null : unitTypeMapper.selectOne(new LambdaQueryWrapper<BasicUnitType>()
                .eq(BasicUnitType::getTenantId, currentTenant())
                .eq(BasicUnitType::getId, id)
                .eq(BasicUnitType::getDeleted, false)
                .last("LIMIT 1"));
        if (type == null) {
            throw ex(BasicPromptEnum.UNIT_TYPE_NOT_FOUND);
        }
        return type;
    }

    private BasicUnitConversion requireConversion(Long id) {
        BasicUnitConversion conversion = id == null ? null : conversionMapper.selectOne(new LambdaQueryWrapper<BasicUnitConversion>()
                .eq(BasicUnitConversion::getTenantId, currentTenant())
                .eq(BasicUnitConversion::getId, id)
                .eq(BasicUnitConversion::getDeleted, false)
                .last("LIMIT 1"));
        if (conversion == null) {
            throw ex(BasicPromptEnum.UNIT_CONVERSION_RELATION_NOT_FOUND);
        }
        return conversion;
    }

    private void ensureTypeCodeUnique(String code, Long excludeId) {
        BasicUnitType same = unitTypeMapper.selectOne(new LambdaQueryWrapper<BasicUnitType>()
                .eq(BasicUnitType::getTenantId, currentTenant())
                .eq(BasicUnitType::getUnitTypeCode, code)
                .eq(BasicUnitType::getDeleted, false)
                .ne(excludeId != null, BasicUnitType::getId, excludeId)
                .last("LIMIT 1"));
        if (same != null) {
            throw ex(BasicPromptEnum.UNIT_TYPE_CODE_EXISTS);
        }
    }

    private void ensureUnitCodeUnique(Long unitTypeId, String code, Long excludeId) {
        BasicUnit same = unitMapper.selectOne(new LambdaQueryWrapper<BasicUnit>()
                .eq(BasicUnit::getTenantId, currentTenant())
                .eq(BasicUnit::getUnitTypeId, unitTypeId)
                .eq(BasicUnit::getUnitCode, code)
                .eq(BasicUnit::getDeleted, false)
                .ne(excludeId != null, BasicUnit::getId, excludeId)
                .last("LIMIT 1"));
        if (same != null) {
            throw ex(BasicPromptEnum.UNIT_CODE_EXISTS);
        }
    }

    private void ensureConversionUnique(Long unitId, Long targetUnitId, Long excludeId) {
        BasicUnitConversion same = conversionMapper.selectOne(new LambdaQueryWrapper<BasicUnitConversion>()
                .eq(BasicUnitConversion::getTenantId, currentTenant())
                .eq(BasicUnitConversion::getUnitId, unitId)
                .eq(BasicUnitConversion::getTargetUnitId, targetUnitId)
                .eq(BasicUnitConversion::getDeleted, false)
                .ne(excludeId != null, BasicUnitConversion::getId, excludeId)
                .last("LIMIT 1"));
        if (same != null) {
            throw ex(BasicPromptEnum.UNIT_CONVERSION_RELATION_EXISTS);
        }
    }

    private String resolveTypeLevelPath(BasicUnitType type) {
        if (ROOT_PARENT_ID.equals(type.getParentId())) {
            return ROOT_PARENT_ID + "/" + type.getId();
        }
        BasicUnitType parent = requireType(type.getParentId());
        return parent.getLevelPath() + "/" + type.getId();
    }

    private List<UnitTypeTreeVO> buildTypeTree(List<BasicUnitType> types) {
        Map<Long, UnitTypeTreeVO> nodeMap = new LinkedHashMap<>();
        for (BasicUnitType type : types) {
            UnitTypeTreeVO node = new UnitTypeTreeVO();
            BeanUtils.copyProperties(type, node);
            nodeMap.put(type.getId(), node);
        }
        List<UnitTypeTreeVO> roots = new ArrayList<>();
        for (BasicUnitType type : types) {
            UnitTypeTreeVO node = nodeMap.get(type.getId());
            UnitTypeTreeVO parent = nodeMap.get(type.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        Comparator<UnitTypeTreeVO> sorter = Comparator.comparing(UnitTypeTreeVO::getUnitTypeCode, Comparator.nullsLast(String::compareTo));
        sortTree(roots, sorter);
        return roots;
    }

    private void sortTree(List<UnitTypeTreeVO> nodes, Comparator<UnitTypeTreeVO> sorter) {
        nodes.sort(sorter);
        for (UnitTypeTreeVO node : nodes) {
            sortTree(node.getChildren(), sorter);
        }
    }

    private List<UnitVO> toUnitVOList(List<BasicUnit> units) {
        if (units == null || units.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, BasicUnitType> types = typeMap(units.stream().map(BasicUnit::getUnitTypeId).toList());
        return units.stream().map(unit -> toUnitVO(unit, types)).toList();
    }

    private UnitVO toUnitVO(BasicUnit unit, Map<Long, BasicUnitType> types) {
        UnitVO vo = new UnitVO();
        BeanUtils.copyProperties(unit, vo);
        BasicUnitType type = types.get(unit.getUnitTypeId());
        if (type != null) {
            vo.setUnitTypeCode(type.getUnitTypeCode());
            vo.setUnitTypeName(type.getUnitTypeName());
        }
        return vo;
    }

    private UnitConversionVO toConversionVO(BasicUnitConversion conversion, Map<Long, BasicUnit> unitMap) {
        UnitConversionVO vo = new UnitConversionVO();
        BeanUtils.copyProperties(conversion, vo);
        BasicUnit unit = unitMap.get(conversion.getUnitId());
        BasicUnit target = unitMap.get(conversion.getTargetUnitId());
        if (unit != null) {
            vo.setUnitCode(unit.getUnitCode());
            vo.setUnitName(unit.getUnitName());
        }
        if (target != null) {
            vo.setTargetUnitCode(target.getUnitCode());
            vo.setTargetUnitName(target.getUnitName());
        }
        return vo;
    }

    private Map<Long, BasicUnitType> typeMap(Collection<Long> typeIds) {
        List<Long> ids = typeIds == null ? new ArrayList<>() : typeIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return unitTypeMapper.selectList(new LambdaQueryWrapper<BasicUnitType>()
                        .eq(BasicUnitType::getTenantId, currentTenant())
                        .eq(BasicUnitType::getDeleted, false)
                        .in(BasicUnitType::getId, ids))
                .stream()
                .collect(Collectors.toMap(BasicUnitType::getId, item -> item));
    }

    private Map<Long, BasicUnit> unitMap(Collection<Long> unitIds) {
        List<Long> ids = unitIds == null ? new ArrayList<>() : unitIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return unitMapper.selectList(new LambdaQueryWrapper<BasicUnit>()
                        .eq(BasicUnit::getTenantId, currentTenant())
                        .eq(BasicUnit::getDeleted, false)
                        .in(BasicUnit::getId, ids))
                .stream()
                .collect(Collectors.toMap(BasicUnit::getId, item -> item));
    }

    private BigDecimal toBigDecimal(Object value, String fieldName) {
        if (value == null) {
            throw ex(BasicPromptEnum.UNIT_CONVERT_FIELD_NOT_NUMBER, fieldName);
        }
        try {
            if (value instanceof BigDecimal decimal) {
                return decimal;
            }
            if (value instanceof Number number) {
                return new BigDecimal(String.valueOf(number));
            }
            String text = String.valueOf(value).trim();
            if (!StringUtils.hasText(text)) {
                throw ex(BasicPromptEnum.UNIT_CONVERT_FIELD_NOT_NUMBER, fieldName);
            }
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw ex(BasicPromptEnum.UNIT_CONVERT_FIELD_NOT_NUMBER, fieldName);
        }
    }

    private String normalizeCode(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Long currentTenant() {
        Long tenantId = TenantContext.get();
        return tenantId == null ? 0L : tenantId;
    }

    private I18nBusinessException ex(BasicPromptEnum prompt, Object... args) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt, args);
    }

    /**
     * 换算单位相关字段。
     *
     * @param entity 实体对象
     * @param targetUnitCode 目标计量单位编码
     * @param unitFieldName 实体内单位字段名
     * @param unitFieldType 单位字段类型
     * @param targetFieldNames 需要换算的数字字段名
     * @return 换算结果
     */
    public JSONObject convertFields(Object entity, String targetUnitCode, String unitFieldName, String unitFieldType, String[] targetFieldNames) {
        UnitConvertRequestDTO request = new UnitConvertRequestDTO();
        request.setEntity(BeanUtil.beanToMap(entity, new LinkedHashMap<>(), false, true));
        request.setTargetUnitCode(targetUnitCode);
        request.setUnitFieldName(unitFieldName);
        request.setUnitFieldType(unitFieldType);
        request.setTargetFieldNames(targetFieldNames == null ? new ArrayList<>() : List.of(targetFieldNames));
        return convertFields(request);
    }
}
