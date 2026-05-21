package com.forgex.basic.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson.JSON;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.material.domain.dto.MaterialDTO;
import com.forgex.basic.material.domain.dto.MaterialExtendDTO;
import com.forgex.basic.material.domain.dto.MaterialExtendFieldValueDTO;
import com.forgex.basic.material.domain.entity.BasicMaterial;
import com.forgex.basic.material.domain.entity.BasicMaterialExtend;
import com.forgex.basic.material.domain.param.MaterialPageParam;
import com.forgex.basic.material.domain.response.MaterialDetailResponse;
import com.forgex.basic.material.domain.response.MaterialExtendConfigVO;
import com.forgex.basic.material.domain.response.MaterialExtendViewVO;
import com.forgex.basic.material.domain.vo.MaterialExtendVO;
import com.forgex.basic.material.domain.vo.MaterialVO;
import com.forgex.basic.material.mapper.BasicMaterialExtendMapper;
import com.forgex.basic.material.mapper.BasicMaterialMapper;
import com.forgex.basic.material.service.IMaterialExtendConfigService;
import com.forgex.basic.material.service.IMaterialPackagingRelationService;
import com.forgex.basic.material.service.IMaterialService;
import com.forgex.basic.material.util.MaterialCodeGenerator;
import com.forgex.common.api.dto.MaterialAggregateDTO;
import com.forgex.common.api.dto.MaterialExtendSyncDTO;
import com.forgex.common.api.dto.MaterialThirdPartyInvokeDTO;
import com.forgex.common.api.dto.MaterialThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.MaterialThirdPartySyncResultDTO;
import com.forgex.common.api.feign.IntegrationInternalMaterialFeignClient;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.enums.FxExcelImportMode;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 物料管理服务实现。
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl extends ServiceImpl<BasicMaterialMapper, BasicMaterial> implements IMaterialService {

    private static final Long PUBLIC_TENANT_ID = 0L;
    private static final String DEFAULT_MATERIAL_SYNC_API_CODE = "basic_material_sync";
    private static final String DEFAULT_MATERIAL_PULL_API_CODE = "basic_material_pull";
    private static final List<String> EXTEND_MODULES = List.of("PURCHASE", "INVENTORY", "PRODUCTION", "SALES");

    private final BasicMaterialMapper materialMapper;
    private final BasicMaterialExtendMapper materialExtendMapper;
    private final IMaterialExtendConfigService extendConfigService;
    private final IMaterialPackagingRelationService materialPackagingRelationService;
    private final MaterialCodeGenerator materialCodeGenerator;
    private final IntegrationInternalMaterialFeignClient integrationInternalMaterialFeignClient;

    /**
     * 分页查询物料主数据。
     *
     * @param tenantId 租户 ID
     * @param param 查询参数
     * @return 物料分页列表
     */
    @Override
    public Page<MaterialVO> pageMaterials(Long tenantId, MaterialPageParam param) {
        Page<BasicMaterial> entityPage = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<BasicMaterial> wrapper = buildMaterialQuery(tenantId, param);
        Page<BasicMaterial> materialPage = materialMapper.selectPage(entityPage, wrapper);

        Page<MaterialVO> voPage = new Page<>(materialPage.getCurrent(), materialPage.getSize(), materialPage.getTotal());
        List<MaterialVO> voList = materialPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 查询物料主数据列表。
     *
     * @param tenantId 租户 ID
     * @param param 查询参数
     * @return 物料列表
     */
    @Override
    public List<MaterialVO> listMaterials(Long tenantId, MaterialPageParam param) {
        return materialMapper.selectList(buildMaterialQuery(tenantId, param)).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 查询物料详情。
     *
     * @param tenantId 租户 ID
     * @param id 物料 ID
     * @return 物料详情响应
     */
    @Override
    public MaterialDetailResponse getMaterialDetail(Long tenantId, Long id) {
        BasicMaterial material = requireMaterial(tenantId, id);
        MaterialDetailResponse response = new MaterialDetailResponse();
        MaterialDTO baseInfo = new MaterialDTO();
        BeanUtils.copyProperties(material, baseInfo);

        List<MaterialExtendVO> extendVOList = listExtends(id, tenantId).stream()
                .map(this::convertToExtendVO)
                .collect(Collectors.toList());
        baseInfo.setExtendList(extendVOList);
        baseInfo.setExtendValueList(toExtendValueList(extendVOList));
        response.setBaseInfo(baseInfo);
        response.setExtendList(extendVOList);
        response.setExtendViewList(buildExtendViewList(tenantId, material, listExtends(id, tenantId)));
        response.setCreateBy(material.getCreateBy());
        response.setCreateTime(material.getCreateTime());
        response.setUpdateBy(material.getUpdateBy());
        response.setUpdateTime(material.getUpdateTime());
        return response;
    }

    /**
     * 创建物料主数据。
     *
     * @param tenantId 租户 ID
     * @param dto 物料 DTO
     * @return 物料 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createMaterial(Long tenantId, MaterialDTO dto) {
        if (!StringUtils.hasText(dto.getMaterialCode())) {
            dto.setMaterialCode(materialCodeGenerator.generateMaterialCode(tenantId));
        } else if (!materialCodeGenerator.validateMaterialCodeUnique(dto.getMaterialCode(), tenantId)) {
            throw materialException(BasicPromptEnum.MATERIAL_CODE_EXISTS);
        }

        BasicMaterial material = new BasicMaterial();
        BeanUtils.copyProperties(dto, material);
        material.setMaterialCode(normalizeCode(material.getMaterialCode()));
        material.setTenantId(tenantId);
        material.setStatus(material.getStatus() == null ? 1 : material.getStatus());
        material.setApprovalStatus(StringUtils.hasText(material.getApprovalStatus())
                ? material.getApprovalStatus()
                : "NO_APPROVAL_REQUIRED");
        materialMapper.insert(material);
        saveMaterialExtends(material.getId(), tenantId, material.getMaterialType(), dto, Collections.emptyMap());
        log.info("创建物料成功，materialId={}, materialCode={}", material.getId(), material.getMaterialCode());
        return material.getId();
    }

    /**
     * 更新物料。
     *
     * @param tenantId 租户 ID
     * @param dto 物料 DTO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMaterial(Long tenantId, MaterialDTO dto) {
        BasicMaterial existing = requireMaterial(tenantId, dto.getId());
        if (StringUtils.hasText(dto.getMaterialCode())) {
            Integer count = materialMapper.countByMaterialCodeExclude(normalizeCode(dto.getMaterialCode()), dto.getId(), tenantId);
            if (count != null && count > 0) {
                throw materialException(BasicPromptEnum.MATERIAL_CODE_EXISTS);
            }
        }

        BasicMaterial material = new BasicMaterial();
        BeanUtils.copyProperties(dto, material);
        material.setId(existing.getId());
        material.setTenantId(tenantId);
        material.setMaterialCode(StringUtils.hasText(material.getMaterialCode())
                ? normalizeCode(material.getMaterialCode())
                : existing.getMaterialCode());
        materialMapper.updateById(material);
        Map<String, Map<String, Object>> previousValues = listExtends(dto.getId(), tenantId).stream()
                .collect(Collectors.toMap(BasicMaterialExtend::getModule, item -> parseJsonMap(item.getExtendJson()), (left, right) -> left));
        deleteExtends(dto.getId(), tenantId);
        saveMaterialExtends(dto.getId(), tenantId, material.getMaterialType(), dto, previousValues);
        log.info("更新物料成功，materialId={}", dto.getId());
    }

    /**
     * 删除物料。
     *
     * @param tenantId 租户 ID
     * @param id 物料 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteMaterial(Long tenantId, Long id) {
        requireMaterial(tenantId, id);
        materialMapper.deleteById(id);
        deleteExtends(id, tenantId);
        materialPackagingRelationService.deleteByMaterialId(tenantId, id);
        log.info("删除物料成功，materialId={}", id);
    }

    /**
     * 同步物料到第三方系统。
     *
     * @param request 第三方调用请求
     * @return 同步结果
     */
    @Override
    public MaterialThirdPartySyncResultDTO syncToThirdParty(MaterialThirdPartyInvokeDTO request) {
        MaterialThirdPartyInvokeDTO safeRequest = request == null ? new MaterialThirdPartyInvokeDTO() : request;
        if (!StringUtils.hasText(safeRequest.getApiCode())) {
            safeRequest.setApiCode(DEFAULT_MATERIAL_SYNC_API_CODE);
        }
        if (safeRequest.getTenantId() == null) {
            safeRequest.setTenantId(OptionalTenant.current());
        }
        R<MaterialThirdPartySyncResultDTO> response = integrationInternalMaterialFeignClient.syncMaterials(safeRequest);
        if (response == null || response.getData() == null) {
            throw materialException(BasicPromptEnum.MATERIAL_THIRD_PARTY_SYNC_FAILED);
        }
        return response.getData();
    }

    /**
     * 从第三方系统拉取物料。
     *
     * @param request 第三方调用请求
     * @return 写入结果
     */
    @Override
    public MaterialThirdPartySyncResultDTO pullFromThirdParty(MaterialThirdPartyInvokeDTO request) {
        MaterialThirdPartyInvokeDTO safeRequest = request == null ? new MaterialThirdPartyInvokeDTO() : request;
        if (!StringUtils.hasText(safeRequest.getApiCode())) {
            safeRequest.setApiCode(DEFAULT_MATERIAL_PULL_API_CODE);
        }
        if (safeRequest.getTenantId() == null) {
            safeRequest.setTenantId(OptionalTenant.current());
        }
        R<MaterialThirdPartySyncResultDTO> response = integrationInternalMaterialFeignClient.pullMaterials(safeRequest);
        if (response == null || response.getData() == null) {
            throw materialException(BasicPromptEnum.MATERIAL_THIRD_PARTY_SYNC_FAILED);
        }
        return response.getData();
    }

    /**
     * 写入第三方物料数据。
     *
     * @param request 同步请求
     * @return 写入结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public MaterialThirdPartySyncResultDTO syncThirdPartyMaterials(MaterialThirdPartySyncRequestDTO request) {
        MaterialThirdPartySyncResultDTO result = new MaterialThirdPartySyncResultDTO();
        List<MaterialAggregateDTO> materials = request == null ? Collections.emptyList() : request.getMaterials();
        result.setTotalCount(materials == null ? 0 : materials.size());
        if (CollectionUtils.isEmpty(materials)) {
            return result;
        }
        Long tenantId = request.getTenantId() == null ? OptionalTenant.current() : request.getTenantId();
        for (MaterialAggregateDTO aggregate : materials) {
            String materialCode = aggregate == null ? null : normalizeCode(aggregate.getMaterialCode());
            try {
                validateAggregate(aggregate);
                boolean created = upsertAggregate(tenantId, aggregate);
                if (created) {
                    result.setCreatedCount(result.getCreatedCount() + 1);
                } else {
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                }
            } catch (Exception ex) {
                log.warn("同步物料失败，materialCode={}", materialCode, ex);
                result.setFailedCount(result.getFailedCount() + 1);
                result.getFailedMaterialCodes().add(StringUtils.hasText(materialCode) ? materialCode : "UNKNOWN");
            }
        }
        return result;
    }

    /**
     * 导出第三方同步使用的物料聚合数据。
     *
     * @param request 导出请求
     * @return 物料聚合数据
     */
    @Override
    public List<MaterialAggregateDTO> exportThirdPartyMaterials(MaterialThirdPartySyncRequestDTO request) {
        Long tenantId = request == null || request.getTenantId() == null ? OptionalTenant.current() : request.getTenantId();
        return materialMapper.selectList(new LambdaQueryWrapper<BasicMaterial>()
                        .eq(BasicMaterial::getTenantId, tenantId)
                        .eq(BasicMaterial::getDeleted, false)
                        .orderByDesc(BasicMaterial::getCreateTime))
                .stream()
                .map(item -> convertToAggregate(item, true))
                .collect(Collectors.toList());
    }

    /**
     * 执行公共导入。
     *
     * @param param 公共导入参数
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param) {
        FxExcelImportMode mode = FxExcelImportMode.parse(param == null ? null : param.getImportMode());
        List<MaterialAggregateDTO> materials = readImportData(param);
        FxExcelImportResultDTO result = new FxExcelImportResultDTO();
        result.setTotalCount(materials.size());
        Long tenantId = OptionalTenant.current();
        if (mode == FxExcelImportMode.COVER) {
            coverMaterials(tenantId);
        }
        for (MaterialAggregateDTO aggregate : materials) {
            String materialCode = aggregate == null ? null : normalizeCode(aggregate.getMaterialCode());
            try {
                validateAggregate(aggregate);
                handleImportAggregate(tenantId, mode, aggregate, result);
            } catch (Exception ex) {
                log.warn("公共导入物料失败，materialCode={}", materialCode, ex);
                result.addError(StringUtils.hasText(materialCode) ? materialCode : "UNKNOWN");
            }
        }
        return result;
    }

    private LambdaQueryWrapper<BasicMaterial> buildMaterialQuery(Long tenantId, MaterialPageParam param) {
        LambdaQueryWrapper<BasicMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicMaterial::getTenantId, tenantId)
                .eq(BasicMaterial::getDeleted, false);
        if (param == null) {
            return wrapper.orderByDesc(BasicMaterial::getCreateTime);
        }
        wrapper.like(StringUtils.hasText(param.getMaterialCode()), BasicMaterial::getMaterialCode, param.getMaterialCode())
                .like(StringUtils.hasText(param.getMaterialName()), BasicMaterial::getMaterialName, param.getMaterialName())
                .eq(StringUtils.hasText(param.getMaterialType()), BasicMaterial::getMaterialType, param.getMaterialType())
                .like(StringUtils.hasText(param.getMaterialCategory()), BasicMaterial::getMaterialCategory, param.getMaterialCategory())
                .like(StringUtils.hasText(param.getUnit()), BasicMaterial::getUnit, param.getUnit())
                .like(StringUtils.hasText(param.getBrand()), BasicMaterial::getBrand, param.getBrand())
                .eq(param.getStatus() != null, BasicMaterial::getStatus, param.getStatus())
                .eq(StringUtils.hasText(param.getApprovalStatus()), BasicMaterial::getApprovalStatus, param.getApprovalStatus())
                .orderByDesc(BasicMaterial::getCreateTime);
        return wrapper;
    }

    private BasicMaterial requireMaterial(Long tenantId, Long id) {
        BasicMaterial material = id == null ? null : materialMapper.selectById(id);
        if (material == null || !Objects.equals(material.getTenantId(), tenantId) || Boolean.TRUE.equals(material.getDeleted())) {
            throw materialException(BasicPromptEnum.MATERIAL_NOT_FOUND);
        }
        return material;
    }

    private BasicMaterial findByCode(Long tenantId, String materialCode) {
        String code = normalizeCode(materialCode);
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return materialMapper.selectOne(new LambdaQueryWrapper<BasicMaterial>()
                .eq(BasicMaterial::getTenantId, tenantId)
                .eq(BasicMaterial::getMaterialCode, code)
                .eq(BasicMaterial::getDeleted, false)
                .last("LIMIT 1"));
    }

    private boolean upsertAggregate(Long tenantId, MaterialAggregateDTO aggregate) {
        BasicMaterial existing = findByCode(tenantId, aggregate.getMaterialCode());
        saveAggregate(tenantId, existing, aggregate);
        return existing == null;
    }

    private void saveAggregate(Long tenantId, BasicMaterial existing, MaterialAggregateDTO aggregate) {
        BasicMaterial target = existing == null ? new BasicMaterial() : existing;
        fillMaterialFromAggregate(target, aggregate);
        target.setTenantId(tenantId);
        target.setStatus(target.getStatus() == null ? 1 : target.getStatus());
        target.setApprovalStatus(StringUtils.hasText(target.getApprovalStatus())
                ? target.getApprovalStatus()
                : "NO_APPROVAL_REQUIRED");
        if (existing == null) {
            materialMapper.insert(target);
        } else {
            materialMapper.updateById(target);
            deleteExtends(target.getId(), tenantId);
        }
        saveSyncExtends(target.getId(), tenantId, aggregate.getExtendList());
    }

    private void fillMaterialFromAggregate(BasicMaterial material, MaterialAggregateDTO aggregate) {
        material.setMaterialCode(normalizeCode(aggregate.getMaterialCode()));
        material.setMaterialName(trimToNull(aggregate.getMaterialName()));
        material.setMaterialType(trimToNull(aggregate.getMaterialType()));
        material.setMaterialCategory(trimToNull(aggregate.getMaterialCategory()));
        material.setSpecification(trimToNull(aggregate.getSpecification()));
        material.setUnit(trimToNull(aggregate.getUnit()));
        material.setBrand(trimToNull(aggregate.getBrand()));
        material.setImageUrl(trimToNull(aggregate.getImageUrl()));
        material.setRemark(trimToNull(aggregate.getRemark()));
        material.setDescription(trimToNull(aggregate.getDescription()));
        material.setStatus(aggregate.getStatus());
        material.setApprovalStatus(trimToNull(aggregate.getApprovalStatus()));
    }

    private void handleImportAggregate(Long tenantId, FxExcelImportMode mode, MaterialAggregateDTO aggregate, FxExcelImportResultDTO result) {
        BasicMaterial existing = findByCode(tenantId, aggregate.getMaterialCode());
        if (existing == null) {
            if (mode == FxExcelImportMode.UPDATE) {
                result.increaseSkipped();
                return;
            }
            saveAggregate(tenantId, null, aggregate);
            result.increaseCreated();
            return;
        }
        if (mode == FxExcelImportMode.ADD) {
            result.increaseSkipped();
            return;
        }
        saveAggregate(tenantId, existing, aggregate);
        result.increaseUpdated();
    }

    private void coverMaterials(Long tenantId) {
        List<Long> materialIds = materialMapper.selectList(new LambdaQueryWrapper<BasicMaterial>()
                        .select(BasicMaterial::getId)
                        .eq(BasicMaterial::getTenantId, tenantId)
                        .eq(BasicMaterial::getDeleted, false))
                .stream()
                .map(BasicMaterial::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (materialIds.isEmpty()) {
            return;
        }
        materialExtendMapper.delete(new LambdaQueryWrapper<BasicMaterialExtend>()
                .eq(BasicMaterialExtend::getTenantId, tenantId)
                .in(BasicMaterialExtend::getMaterialId, materialIds));
        materialMapper.delete(new LambdaQueryWrapper<BasicMaterial>()
                .eq(BasicMaterial::getTenantId, tenantId)
                .in(BasicMaterial::getId, materialIds));
    }

    private List<MaterialAggregateDTO> readImportData(FxExcelImportExecuteParam param) {
        if (param == null || param.getImportData() == null) {
            return Collections.emptyList();
        }
        Map<String, MaterialAggregateDTO> aggregateMap = new LinkedHashMap<>();
        Set<String> mainCodes = readMainImportData(param.getImportData().get("main"), aggregateMap);
        readExtendImportData(param.getImportData().get("extend"), aggregateMap, mainCodes);
        return new ArrayList<>(aggregateMap.values());
    }

    private Set<String> readMainImportData(List<Map<String, Object>> rows, Map<String, MaterialAggregateDTO> aggregateMap) {
        Set<String> mainCodes = new java.util.HashSet<>();
        if (CollectionUtils.isEmpty(rows)) {
            return mainCodes;
        }
        for (Map<String, Object> row : rows) {
            String materialCode = normalizeCode(value(row, "materialCode"));
            if (!StringUtils.hasText(materialCode)) {
                continue;
            }
            MaterialAggregateDTO dto = aggregateMap.computeIfAbsent(materialCode, code -> {
                MaterialAggregateDTO item = new MaterialAggregateDTO();
                item.setMaterialCode(code);
                return item;
            });
            mainCodes.add(materialCode);
            dto.setMaterialName(value(row, "materialName"));
            dto.setMaterialType(value(row, "materialType"));
            dto.setMaterialCategory(value(row, "materialCategory"));
            dto.setSpecification(value(row, "specification"));
            dto.setUnit(value(row, "unit"));
            dto.setBrand(value(row, "brand"));
            dto.setImageUrl(value(row, "imageUrl"));
            dto.setExtendJson(value(row, "extendJson"));
            dto.setRemark(value(row, "remark"));
            dto.setDescription(value(row, "description"));
            dto.setStatus(parseInteger(value(row, "status")));
            dto.setApprovalStatus(value(row, "approvalStatus"));
        }
        return mainCodes;
    }

    private void readExtendImportData(List<Map<String, Object>> rows, Map<String, MaterialAggregateDTO> aggregateMap, Set<String> mainCodes) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        for (Map<String, Object> row : rows) {
            String materialCode = normalizeCode(value(row, "materialCode"));
            if (!StringUtils.hasText(materialCode)) {
                continue;
            }
            if (!mainCodes.contains(materialCode)) {
                MaterialAggregateDTO item = new MaterialAggregateDTO();
                item.setMaterialCode(materialCode);
                aggregateMap.putIfAbsent(materialCode, item);
            }
            MaterialAggregateDTO aggregate = aggregateMap.get(materialCode);
            if (aggregate.getExtendList() == null) {
                aggregate.setExtendList(new ArrayList<>());
            }
            MaterialExtendSyncDTO extend = new MaterialExtendSyncDTO();
            extend.setModule(value(row, "module"));
            extend.setExtendJson(value(row, "extendJson"));
            aggregate.getExtendList().add(extend);
        }
    }

    private void validateAggregate(MaterialAggregateDTO aggregate) {
        if (aggregate == null) {
            throw materialException(BasicPromptEnum.MATERIAL_DATA_REQUIRED);
        }
        if (!StringUtils.hasText(normalizeCode(aggregate.getMaterialCode()))) {
            throw materialException(BasicPromptEnum.MATERIAL_CODE_REQUIRED);
        }
        if (!StringUtils.hasText(aggregate.getMaterialName())) {
            throw materialException(BasicPromptEnum.MATERIAL_NAME_REQUIRED);
        }
        if (!StringUtils.hasText(aggregate.getMaterialType())) {
            throw materialException(BasicPromptEnum.MATERIAL_TYPE_REQUIRED);
        }
    }

    private String value(Map<String, Object> row, String key) {
        if (row == null || key == null) {
            return null;
        }
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    private void saveExtends(Long materialId, Long tenantId, List<? extends MaterialExtendDTO> extendList) {
        if (CollectionUtils.isEmpty(extendList)) {
            return;
        }
        for (MaterialExtendDTO extendDTO : extendList) {
            if (extendDTO == null || !StringUtils.hasText(extendDTO.getModule())) {
                continue;
            }
            BasicMaterialExtend extend = new BasicMaterialExtend();
            extend.setMaterialId(materialId);
            extend.setModule(trimToNull(extendDTO.getModule()));
            extend.setExtendJson(trimToNull(extendDTO.getExtendJson()));
            extend.setTenantId(tenantId);
            materialExtendMapper.insert(extend);
        }
    }

    private void saveMaterialExtends(Long materialId, Long tenantId, String materialType, MaterialDTO dto,
                                     Map<String, Map<String, Object>> previousValues) {
        if (!CollectionUtils.isEmpty(dto.getExtendValueList())) {
            saveStructuredExtends(materialId, tenantId, materialType, dto.getExtendValueList(), previousValues);
            return;
        }
        saveExtends(materialId, tenantId, dto.getExtendList());
    }

    private void saveStructuredExtends(Long materialId, Long tenantId, String materialType,
                                       List<MaterialExtendFieldValueDTO> extendValueList,
                                       Map<String, Map<String, Object>> previousValues) {
        for (MaterialExtendFieldValueDTO item : extendValueList) {
            if (item == null || !StringUtils.hasText(item.getModule())) {
                continue;
            }
            String module = trimToNull(item.getModule());
            Map<String, Object> values = new LinkedHashMap<>();
            Map<String, Object> previous = previousValues == null ? Collections.emptyMap() : previousValues.getOrDefault(module, Collections.emptyMap());
            values.putAll(previous);
            List<MaterialExtendConfigVO> configs = extendConfigService.getConfigsByScope(tenantId, module, materialType);
            Map<String, Object> submittedValues = item.getValues() == null ? Collections.emptyMap() : item.getValues();
            for (MaterialExtendConfigVO config : configs) {
                Object value = submittedValues.get(config.getFieldName());
                validateExtendValue(config, value);
                if (value == null || (value instanceof String stringValue && !StringUtils.hasText(stringValue))) {
                    values.remove(config.getFieldName());
                } else {
                    values.put(config.getFieldName(), normalizeExtendValue(config, value));
                }
            }
            BasicMaterialExtend extend = new BasicMaterialExtend();
            extend.setMaterialId(materialId);
            extend.setModule(module);
            extend.setExtendJson(JSON.toJSONString(values));
            extend.setTenantId(tenantId);
            materialExtendMapper.insert(extend);
        }
    }

    private List<MaterialExtendFieldValueDTO> toExtendValueList(List<MaterialExtendVO> extendVOList) {
        if (CollectionUtils.isEmpty(extendVOList)) {
            return Collections.emptyList();
        }
        return extendVOList.stream().map(item -> {
            MaterialExtendFieldValueDTO dto = new MaterialExtendFieldValueDTO();
            dto.setModule(item.getModule());
            dto.setValues(parseJsonMap(item.getExtendJson()));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<MaterialExtendViewVO> buildExtendViewList(Long tenantId, BasicMaterial material, List<BasicMaterialExtend> extendsList) {
        Map<String, BasicMaterialExtend> existingMap = CollectionUtils.isEmpty(extendsList)
                ? Collections.emptyMap()
                : extendsList.stream().collect(Collectors.toMap(BasicMaterialExtend::getModule, item -> item, (left, right) -> left, LinkedHashMap::new));
        List<MaterialExtendViewVO> result = new ArrayList<>();
        for (String module : EXTEND_MODULES) {
            BasicMaterialExtend extend = existingMap.get(module);
            Map<String, Object> values = extend == null ? new LinkedHashMap<>() : parseJsonMap(extend.getExtendJson());
            List<MaterialExtendConfigVO> configs = extendConfigService.getConfigsByScope(tenantId, module, material.getMaterialType());
            Map<String, Object> unknownValues = new LinkedHashMap<>(values);
            List<MaterialExtendViewVO.FieldValue> fields = configs.stream().map(config -> {
                unknownValues.remove(config.getFieldName());
                return buildFieldValue(config, values.get(config.getFieldName()));
            }).collect(Collectors.toList());

            MaterialExtendViewVO view = new MaterialExtendViewVO();
            view.setModule(module);
            view.setModuleName(moduleName(module));
            view.setMaterialType(material.getMaterialType());
            view.setExtendId(extend == null ? null : extend.getId());
            view.setExtendJson(extend == null ? "{}" : extend.getExtendJson());
            view.setFields(fields);
            view.setUnknownValues(unknownValues);
            result.add(view);
        }
        return result;
    }

    private MaterialExtendViewVO.FieldValue buildFieldValue(MaterialExtendConfigVO config, Object value) {
        MaterialExtendViewVO.FieldValue fieldValue = new MaterialExtendViewVO.FieldValue();
        fieldValue.setConfigId(config.getId());
        fieldValue.setFieldName(config.getFieldName());
        fieldValue.setFieldLabel(config.getFieldLabel());
        fieldValue.setFieldType(config.getFieldType());
        fieldValue.setFieldTypeName(config.getFieldTypeName());
        fieldValue.setOptions(config.getOptions());
        fieldValue.setRequired(config.getRequired());
        fieldValue.setDefaultValue(config.getDefaultValue());
        fieldValue.setOrderNum(config.getOrderNum());
        Object finalValue = value == null ? config.getDefaultValue() : value;
        fieldValue.setValue(finalValue);
        fieldValue.setDisplayValue(displayExtendValue(config, finalValue));
        return fieldValue;
    }

    private void validateExtendValue(MaterialExtendConfigVO config, Object value) {
        if (Integer.valueOf(1).equals(config.getRequired()) && isEmptyExtendValue(value)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.MATERIAL_DATA_REQUIRED);
        }
    }

    private boolean isEmptyExtendValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String stringValue) {
            return !StringUtils.hasText(stringValue);
        }
        if (value instanceof List<?> listValue) {
            return listValue.isEmpty();
        }
        return false;
    }

    private Object normalizeExtendValue(MaterialExtendConfigVO config, Object value) {
        if (value == null) {
            return null;
        }
        String fieldType = config.getFieldType();
        if ("NUMBER".equals(fieldType)) {
            if (value instanceof Number) {
                return value;
            }
            String text = String.valueOf(value).trim();
            return StringUtils.hasText(text) ? new java.math.BigDecimal(text) : null;
        }
        if ("BOOLEAN".equals(fieldType)) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return value;
    }

    private String displayExtendValue(MaterialExtendConfigVO config, Object value) {
        if (value == null) {
            return "";
        }
        if ("SELECT".equals(config.getFieldType())) {
            return optionLabel(config.getOptions(), String.valueOf(value));
        }
        if ("MULTI_SELECT".equals(config.getFieldType()) && value instanceof List<?> listValue) {
            return listValue.stream()
                    .map(item -> optionLabel(config.getOptions(), String.valueOf(item)))
                    .collect(Collectors.joining(", "));
        }
        if ("BOOLEAN".equals(config.getFieldType())) {
            return Boolean.parseBoolean(String.valueOf(value)) ? "是" : "否";
        }
        return String.valueOf(value);
    }

    private String optionLabel(List<Map<String, String>> options, String value) {
        if (CollectionUtils.isEmpty(options)) {
            return value;
        }
        return options.stream()
                .filter(option -> Objects.equals(option.get("value"), value))
                .map(option -> option.getOrDefault("label", value))
                .findFirst()
                .orElse(value);
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> parsed = JSON.parseObject(json, Map.class);
            return parsed == null ? new LinkedHashMap<>() : new LinkedHashMap<>(parsed);
        } catch (Exception ex) {
            Map<String, Object> raw = new LinkedHashMap<>();
            raw.put("_raw", json);
            return raw;
        }
    }

    private void saveSyncExtends(Long materialId, Long tenantId, List<MaterialExtendSyncDTO> extendList) {
        if (CollectionUtils.isEmpty(extendList)) {
            return;
        }
        for (MaterialExtendSyncDTO extendDTO : extendList) {
            if (extendDTO == null || !StringUtils.hasText(extendDTO.getModule())) {
                continue;
            }
            BasicMaterialExtend extend = new BasicMaterialExtend();
            extend.setMaterialId(materialId);
            extend.setModule(trimToNull(extendDTO.getModule()));
            extend.setExtendJson(trimToNull(extendDTO.getExtendJson()));
            extend.setTenantId(tenantId);
            materialExtendMapper.insert(extend);
        }
    }

    private void deleteExtends(Long materialId, Long tenantId) {
        materialExtendMapper.delete(new LambdaQueryWrapper<BasicMaterialExtend>()
                .eq(BasicMaterialExtend::getMaterialId, materialId)
                .eq(BasicMaterialExtend::getTenantId, tenantId));
    }

    private List<BasicMaterialExtend> listExtends(Long materialId, Long tenantId) {
        return materialExtendMapper.selectList(new LambdaQueryWrapper<BasicMaterialExtend>()
                .eq(BasicMaterialExtend::getMaterialId, materialId)
                .eq(BasicMaterialExtend::getTenantId, tenantId)
                .eq(BasicMaterialExtend::getDeleted, false));
    }

    private MaterialAggregateDTO convertToAggregate(BasicMaterial material, boolean withExtends) {
        MaterialAggregateDTO dto = new MaterialAggregateDTO();
        BeanUtils.copyProperties(material, dto);
        if (withExtends) {
            dto.setExtendList(listExtends(material.getId(), material.getTenantId()).stream()
                    .map(this::convertToExtendSyncDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private MaterialVO convertToVO(BasicMaterial material) {
        MaterialVO vo = new MaterialVO();
        BeanUtils.copyProperties(material, vo);
        return vo;
    }

    private MaterialExtendVO convertToExtendVO(BasicMaterialExtend extend) {
        MaterialExtendVO vo = new MaterialExtendVO();
        BeanUtils.copyProperties(extend, vo);
        return vo;
    }

    private MaterialExtendSyncDTO convertToExtendSyncDTO(BasicMaterialExtend extend) {
        MaterialExtendSyncDTO dto = new MaterialExtendSyncDTO();
        BeanUtils.copyProperties(extend, dto);
        return dto;
    }

    private String normalizeCode(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String moduleName(String module) {
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

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }

    private I18nBusinessException materialException(BasicPromptEnum prompt, Object... args) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt, args);
    }

    private static final class OptionalTenant {
        private OptionalTenant() {
        }

        private static Long current() {
            Long tenantId = TenantContext.get();
            return tenantId == null ? PUBLIC_TENANT_ID : tenantId;
        }
    }
}
