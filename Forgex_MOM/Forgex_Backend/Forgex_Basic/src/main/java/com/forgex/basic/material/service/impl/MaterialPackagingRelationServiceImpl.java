package com.forgex.basic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.material.domain.entity.BasicMaterial;
import com.forgex.basic.material.domain.entity.BasicMaterialPackagingRelation;
import com.forgex.basic.material.domain.entity.BasicPackagingType;
import com.forgex.basic.material.domain.param.MaterialPackagingSaveParam;
import com.forgex.basic.material.domain.param.MaterialPackagingSlotSaveParam;
import com.forgex.basic.material.domain.vo.MaterialPackagingRelationVO;
import com.forgex.basic.material.mapper.BasicMaterialMapper;
import com.forgex.basic.material.mapper.BasicMaterialPackagingRelationMapper;
import com.forgex.basic.material.mapper.BasicPackagingTypeMapper;
import com.forgex.basic.material.service.IMaterialPackagingRelationService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 物料包装规格关联服务实现类。
 * <p>
 * 负责小包装、中包装、大包装三个固定槽位的绑定、查询以及软删联动。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-16
 */
@Service
@RequiredArgsConstructor
public class MaterialPackagingRelationServiceImpl extends ServiceImpl<BasicMaterialPackagingRelationMapper, BasicMaterialPackagingRelation>
        implements IMaterialPackagingRelationService {

    private static final Set<String> VALID_SLOTS = Set.of("SMALL", "MEDIUM", "LARGE");

    private final BasicMaterialPackagingRelationMapper relationMapper;
    private final BasicMaterialMapper materialMapper;
    private final BasicPackagingTypeMapper packagingTypeMapper;

    /**
     * 查询指定物料的三槽包装规格。
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     * @return 三槽包装规格关联列表
     */
    @Override
    public List<MaterialPackagingRelationVO> listByMaterial(Long tenantId, Long materialId) {
        if (materialId == null) {
            return Collections.emptyList();
        }
        List<BasicMaterialPackagingRelation> relations = relationMapper.selectList(new LambdaQueryWrapper<BasicMaterialPackagingRelation>()
                .eq(BasicMaterialPackagingRelation::getTenantId, tenantId)
                .eq(BasicMaterialPackagingRelation::getMaterialId, materialId)
                .eq(BasicMaterialPackagingRelation::getDeleted, false)
                .orderByAsc(BasicMaterialPackagingRelation::getPackagingSlot));
        return toVOList(tenantId, relations);
    }

    /**
     * 查询指定包装规格已关联的物料。
     *
     * @param tenantId 租户 ID
     * @param packagingTypeId 包装规格 ID
     * @return 物料包装规格关联列表
     */
    @Override
    public List<MaterialPackagingRelationVO> listByPackagingType(Long tenantId, Long packagingTypeId) {
        if (packagingTypeId == null) {
            return Collections.emptyList();
        }
        List<BasicMaterialPackagingRelation> relations = relationMapper.selectList(new LambdaQueryWrapper<BasicMaterialPackagingRelation>()
                .eq(BasicMaterialPackagingRelation::getTenantId, tenantId)
                .eq(BasicMaterialPackagingRelation::getPackagingTypeId, packagingTypeId)
                .eq(BasicMaterialPackagingRelation::getDeleted, false)
                .orderByAsc(BasicMaterialPackagingRelation::getPackagingSlot));
        return toVOList(tenantId, relations);
    }

    /**
     * 保存指定物料的小、中、大包装规格槽位。
     *
     * @param tenantId 租户 ID
     * @param param 保存参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveByMaterial(Long tenantId, MaterialPackagingSaveParam param) {
        if (param == null || param.getMaterialId() == null) {
            throw relationException(BasicPromptEnum.MATERIAL_NOT_FOUND);
        }
        requireMaterial(tenantId, param.getMaterialId());
        upsertSlot(tenantId, param.getMaterialId(), "SMALL", param.getSmallPackagingTypeId());
        upsertSlot(tenantId, param.getMaterialId(), "MEDIUM", param.getMediumPackagingTypeId());
        upsertSlot(tenantId, param.getMaterialId(), "LARGE", param.getLargePackagingTypeId());
    }

    /**
     * 保存单个物料槽位绑定。
     *
     * @param tenantId 租户 ID
     * @param param 单槽保存参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSlot(Long tenantId, MaterialPackagingSlotSaveParam param) {
        if (param == null || param.getMaterialId() == null) {
            throw relationException(BasicPromptEnum.MATERIAL_NOT_FOUND);
        }
        validateSlot(param.getPackagingSlot());
        requireMaterial(tenantId, param.getMaterialId());
        if (param.getPackagingTypeId() != null) {
            requirePackagingType(tenantId, param.getPackagingTypeId());
        }
        upsertSlot(tenantId, param.getMaterialId(), param.getPackagingSlot(), param.getPackagingTypeId());
    }

    /**
     * 删除单条物料包装规格关联。
     *
     * @param tenantId 租户 ID
     * @param id 关联 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRelation(Long tenantId, Long id) {
        if (id == null) {
            return;
        }
        relationMapper.delete(new LambdaQueryWrapper<BasicMaterialPackagingRelation>()
                .eq(BasicMaterialPackagingRelation::getId, id)
                .eq(BasicMaterialPackagingRelation::getTenantId, tenantId));
    }

    /**
     * 按包装规格软删除关联。
     *
     * @param tenantId 租户 ID
     * @param packagingTypeIds 包装规格 ID 集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByPackagingTypeIds(Long tenantId, List<Long> packagingTypeIds) {
        if (CollectionUtils.isEmpty(packagingTypeIds)) {
            return;
        }
        relationMapper.delete(new LambdaQueryWrapper<BasicMaterialPackagingRelation>()
                .eq(BasicMaterialPackagingRelation::getTenantId, tenantId)
                .in(BasicMaterialPackagingRelation::getPackagingTypeId, packagingTypeIds));
    }

    /**
     * 按物料软删除关联。
     *
     * @param tenantId 租户 ID
     * @param materialId 物料 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByMaterialId(Long tenantId, Long materialId) {
        if (materialId == null) {
            return;
        }
        relationMapper.delete(new LambdaQueryWrapper<BasicMaterialPackagingRelation>()
                .eq(BasicMaterialPackagingRelation::getTenantId, tenantId)
                .eq(BasicMaterialPackagingRelation::getMaterialId, materialId));
    }

    private void upsertSlot(Long tenantId, Long materialId, String slot, Long packagingTypeId) {
        validateSlot(slot);
        BasicMaterialPackagingRelation existing = relationMapper.selectOne(new LambdaQueryWrapper<BasicMaterialPackagingRelation>()
                .eq(BasicMaterialPackagingRelation::getTenantId, tenantId)
                .eq(BasicMaterialPackagingRelation::getMaterialId, materialId)
                .eq(BasicMaterialPackagingRelation::getPackagingSlot, slot)
                .eq(BasicMaterialPackagingRelation::getDeleted, false)
                .last("LIMIT 1"));
        if (packagingTypeId == null) {
            if (existing != null) {
                relationMapper.deleteById(existing.getId());
            }
            return;
        }
        requirePackagingType(tenantId, packagingTypeId);
        if (existing == null) {
            BasicMaterialPackagingRelation relation = new BasicMaterialPackagingRelation();
            relation.setTenantId(tenantId);
            relation.setMaterialId(materialId);
            relation.setPackagingSlot(slot);
            relation.setPackagingTypeId(packagingTypeId);
            relationMapper.insert(relation);
            return;
        }
        existing.setPackagingTypeId(packagingTypeId);
        relationMapper.updateById(existing);
    }

    private List<MaterialPackagingRelationVO> toVOList(Long tenantId, List<BasicMaterialPackagingRelation> relations) {
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyList();
        }
        Map<Long, BasicMaterial> materialMap = loadMaterialMap(tenantId, relations);
        Map<Long, BasicPackagingType> packagingMap = loadPackagingMap(tenantId, relations);
        return relations.stream()
                .filter(item -> materialMap.containsKey(item.getMaterialId()))
                .filter(item -> packagingMap.containsKey(item.getPackagingTypeId()))
                .map(item -> toVO(item, materialMap.get(item.getMaterialId()), packagingMap.get(item.getPackagingTypeId())))
                .collect(Collectors.toList());
    }

    private Map<Long, BasicMaterial> loadMaterialMap(Long tenantId, List<BasicMaterialPackagingRelation> relations) {
        List<Long> materialIds = relations.stream()
                .map(BasicMaterialPackagingRelation::getMaterialId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (materialIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return materialMapper.selectList(new LambdaQueryWrapper<BasicMaterial>()
                        .eq(BasicMaterial::getTenantId, tenantId)
                        .eq(BasicMaterial::getDeleted, false)
                        .in(BasicMaterial::getId, materialIds))
                .stream()
                .collect(Collectors.toMap(BasicMaterial::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, BasicPackagingType> loadPackagingMap(Long tenantId, List<BasicMaterialPackagingRelation> relations) {
        List<Long> packagingTypeIds = relations.stream()
                .map(BasicMaterialPackagingRelation::getPackagingTypeId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (packagingTypeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return packagingTypeMapper.selectList(new LambdaQueryWrapper<BasicPackagingType>()
                        .eq(BasicPackagingType::getTenantId, tenantId)
                        .eq(BasicPackagingType::getDeleted, false)
                        .in(BasicPackagingType::getId, packagingTypeIds))
                .stream()
                .collect(Collectors.toMap(BasicPackagingType::getId, Function.identity(), (left, right) -> left));
    }

    private MaterialPackagingRelationVO toVO(BasicMaterialPackagingRelation relation, BasicMaterial material, BasicPackagingType packagingType) {
        MaterialPackagingRelationVO vo = new MaterialPackagingRelationVO();
        BeanUtils.copyProperties(relation, vo);
        vo.setMaterialCode(material.getMaterialCode());
        vo.setMaterialName(material.getMaterialName());
        vo.setMaterialType(material.getMaterialType());
        vo.setPackagingCode(packagingType.getPackagingCode());
        vo.setPackagingName(packagingType.getPackagingName());
        vo.setPackagingSpecType(packagingType.getPackagingSpecType());
        vo.setPackagingSlotName(slotName(relation.getPackagingSlot()));
        return vo;
    }

    private BasicMaterial requireMaterial(Long tenantId, Long materialId) {
        BasicMaterial material = materialMapper.selectOne(new LambdaQueryWrapper<BasicMaterial>()
                .eq(BasicMaterial::getTenantId, tenantId)
                .eq(BasicMaterial::getId, materialId)
                .eq(BasicMaterial::getDeleted, false)
                .last("LIMIT 1"));
        if (material == null) {
            throw relationException(BasicPromptEnum.MATERIAL_NOT_FOUND);
        }
        return material;
    }

    private BasicPackagingType requirePackagingType(Long tenantId, Long packagingTypeId) {
        BasicPackagingType packagingType = packagingTypeMapper.selectOne(new LambdaQueryWrapper<BasicPackagingType>()
                .eq(BasicPackagingType::getTenantId, tenantId)
                .eq(BasicPackagingType::getId, packagingTypeId)
                .eq(BasicPackagingType::getDeleted, false)
                .last("LIMIT 1"));
        if (packagingType == null) {
            throw relationException(BasicPromptEnum.PACKAGING_SPEC_NOT_FOUND);
        }
        return packagingType;
    }

    private void validateSlot(String slot) {
        if (!StringUtils.hasText(slot) || !VALID_SLOTS.contains(slot)) {
            throw relationException(BasicPromptEnum.PACKAGING_SLOT_INVALID);
        }
    }

    private String slotName(String slot) {
        return switch (slot) {
            case "SMALL" -> "小包装";
            case "MEDIUM" -> "中包装";
            case "LARGE" -> "大包装";
            default -> slot;
        };
    }

    private I18nBusinessException relationException(BasicPromptEnum prompt, Object... args) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt, args);
    }
}
