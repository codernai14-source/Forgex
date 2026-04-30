package com.forgex.basic.supplier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.supplier.domain.dto.SupplierDTO;
import com.forgex.basic.supplier.domain.entity.BasicSupplier;
import com.forgex.basic.supplier.domain.entity.BasicSupplierContact;
import com.forgex.basic.supplier.domain.entity.BasicSupplierDetail;
import com.forgex.basic.supplier.domain.entity.BasicSupplierQualification;
import com.forgex.basic.supplier.domain.param.SupplierPageParam;
import com.forgex.basic.supplier.domain.param.SupplierReviewStartParam;
import com.forgex.basic.supplier.domain.param.SupplierSaveParam;
import com.forgex.basic.supplier.domain.param.SupplierWorkflowCallbackParam;
import com.forgex.basic.supplier.mapper.BasicSupplierContactMapper;
import com.forgex.basic.supplier.mapper.BasicSupplierDetailMapper;
import com.forgex.basic.supplier.mapper.BasicSupplierMapper;
import com.forgex.basic.supplier.mapper.BasicSupplierQualificationMapper;
import com.forgex.basic.supplier.service.ISupplierService;
import com.forgex.common.api.dto.SupplierAggregateDTO;
import com.forgex.common.api.dto.SupplierContactDTO;
import com.forgex.common.api.dto.SupplierDetailDTO;
import com.forgex.common.api.dto.SupplierOptionDTO;
import com.forgex.common.api.dto.SupplierQualificationDTO;
import com.forgex.common.api.dto.SupplierQueryRequestDTO;
import com.forgex.common.api.dto.SupplierThirdPartyInvokeDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.SupplierThirdPartySyncResultDTO;
import com.forgex.common.api.dto.SysTenantCreateRequestDTO;
import com.forgex.common.api.dto.SysTenantSimpleDTO;
import com.forgex.common.api.dto.WorkflowExecutionStartRequestDTO;
import com.forgex.common.api.feign.IntegrationInternalSupplierFeignClient;
import com.forgex.common.api.feign.SysTenantFeignClient;
import com.forgex.common.api.feign.WorkflowExecutionFeignClient;
import com.forgex.common.enums.TenantTypeEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.service.EncodeRuleService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 供应商主数据服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<BasicSupplierMapper, BasicSupplier> implements ISupplierService {

    private static final Long PUBLIC_TENANT_ID = 0L;
    private static final String REVIEW_TASK_CODE = "SUPPLIER_QUALIFICATION_REVIEW";
    private static final String DEFAULT_SUPPLIER_SYNC_API_CODE = "basic_supplier_sync";
    private static final String SUPPLIER_CODE_RULE = "SUPPLIER_CODE";
    private static final int REVIEW_NONE = 0;
    private static final int REVIEW_PENDING = 1;
    private static final int REVIEW_PROCESSING = 2;
    private static final int REVIEW_APPROVED = 3;
    private static final Set<String> COOPERATION_STATUS_VALUES = Set.of("1", "2", "3", "4");
    private static final Set<String> CREDIT_LEVEL_VALUES = Set.of("A", "B", "C", "D");
    private static final Set<String> SUPPLIER_LEVEL_VALUES = Set.of("1", "2", "3");
    private static final Set<Integer> REVIEW_STATUS_VALUES = Set.of(REVIEW_NONE, REVIEW_PENDING, REVIEW_PROCESSING, REVIEW_APPROVED);
    private static final Set<String> ENTERPRISE_NATURE_VALUES = Set.of("1", "2", "3", "4");
    private static final List<String> MAIN_HEADERS = Arrays.asList(
            "供应商编码", "供应商全称", "简称", "供应商Logo", "英文名", "现地址", "主联系人", "联系电话",
            "合作状态", "信用等级", "风险等级", "分级", "关联租户编号", "审查状态", "备注");
    private static final List<String> DETAIL_HEADERS = Arrays.asList(
            "供应商编码", "法人代表", "注册资本", "成立日期", "企业性质", "行业分类", "注册地址",
            "经营地址", "邮箱", "税号", "开户银行", "银行账号", "发票类型", "默认税率");
    private static final List<String> CONTACT_HEADERS = Arrays.asList(
            "供应商编码", "联系人姓名", "联系人电话", "联系人职位", "联系人邮箱");
    private static final List<String> QUALIFICATION_HEADERS = Arrays.asList(
            "供应商编码", "资质类型", "证书编号", "发证日期", "有效期至", "附件", "是否有效");

    private final BasicSupplierMapper supplierMapper;
    private final BasicSupplierDetailMapper detailMapper;
    private final BasicSupplierContactMapper contactMapper;
    private final BasicSupplierQualificationMapper qualificationMapper;
    private final SysTenantFeignClient sysTenantFeignClient;
    private final WorkflowExecutionFeignClient workflowExecutionFeignClient;
    private final IntegrationInternalSupplierFeignClient integrationInternalSupplierFeignClient;
    private final EncodeRuleService encodeRuleService;
    private final ObjectMapper objectMapper;

    @Override
    public Page<SupplierDTO> page(SupplierPageParam param) {
        SupplierPageParam safeParam = param == null ? new SupplierPageParam() : param;
        Page<BasicSupplier> entityPage = new Page<>(safeParam.getPageNum(), safeParam.getPageSize());
        Page<BasicSupplier> supplierPage = supplierMapper.selectPage(entityPage, buildPageWrapper(safeParam));
        Page<SupplierDTO> dtoPage = new Page<>(supplierPage.getCurrent(), supplierPage.getSize(), supplierPage.getTotal());
        dtoPage.setRecords(supplierPage.getRecords().stream()
                .map(item -> convertToDTO(item, false))
                .toList());
        return dtoPage;
    }

    @Override
    public List<SupplierDTO> list(SupplierPageParam param) {
        SupplierPageParam safeParam = param == null ? new SupplierPageParam() : param;
        return supplierMapper.selectList(buildPageWrapper(safeParam)).stream()
                .map(item -> convertToDTO(item, false))
                .toList();
    }

    @Override
    public SupplierDTO getDetailById(Long id) {
        if (id == null) {
            return null;
        }
        BasicSupplier supplier = supplierMapper.selectById(id);
        return supplier == null ? null : convertToDTO(supplier, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SupplierSaveParam param) {
        prepareSupplierCode(param);
        validateSaveParam(param, true);
        if (findByCode(param.getSupplierCode()) != null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_CODE_EXISTS);
        }
        BasicSupplier supplier = new BasicSupplier();
        fillSupplier(supplier, param);
        supplier.setTenantId(PUBLIC_TENANT_ID);
        supplier.setReviewStatus(param.getReviewStatus() == null ? REVIEW_PENDING : param.getReviewStatus());
        supplierMapper.insert(supplier);
        saveChildren(supplier.getId(), param.getDetail(), param.getContactList(), param.getQualificationList(), true, true);
        return supplier.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SupplierSaveParam param) {
        validateSaveParam(param, false);
        BasicSupplier exists = supplierMapper.selectById(param.getId());
        if (exists == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_NOT_FOUND);
        }
        if (!Objects.equals(normalizeCode(exists.getSupplierCode()), normalizeCode(param.getSupplierCode()))) {
            throw supplierException(BasicPromptEnum.SUPPLIER_CODE_IMMUTABLE);
        }
        fillSupplier(exists, param);
        exists.setTenantId(PUBLIC_TENANT_ID);
        supplierMapper.updateById(exists);
        saveChildren(exists.getId(), param.getDetail(), param.getContactList(), param.getQualificationList(), true, true);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        BasicSupplier supplier = requireSupplier(id);
        if (StringUtils.hasText(supplier.getRelatedTenantCode())) {
            throw supplierException(BasicPromptEnum.SUPPLIER_LINKED_TENANT_DELETE_FORBIDDEN);
        }
        supplierMapper.deleteById(id);
        deleteChildren(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateTenant(Long id) {
        BasicSupplier supplier = requireSupplier(id);
        String tenantCode = buildTenantCode(supplier.getSupplierCode());
        if (StringUtils.hasText(supplier.getRelatedTenantCode())) {
            SysTenantSimpleDTO linkedTenant = getTenantByCode(supplier.getRelatedTenantCode());
            if (linkedTenant != null) {
                return supplier.getRelatedTenantCode();
            }
            throw supplierException(BasicPromptEnum.SUPPLIER_LINKED_TENANT_NOT_FOUND);
        }

        SysTenantSimpleDTO existedTenant = getTenantByCode(tenantCode);
        BasicSupplier occupiedSupplier = findByRelatedTenantCode(tenantCode);
        if (existedTenant != null && occupiedSupplier != null && !Objects.equals(occupiedSupplier.getId(), supplier.getId())) {
            throw supplierException(BasicPromptEnum.SUPPLIER_TENANT_CODE_OCCUPIED);
        }
        if (existedTenant == null) {
            SysTenantCreateRequestDTO request = new SysTenantCreateRequestDTO();
            request.setTenantCode(tenantCode);
            request.setTenantName(supplier.getSupplierFullName());
            request.setTenantType(TenantTypeEnum.SUPPLIER_TENANT);
            request.setDescription("供应商主数据自动生成");
            request.setStatus(true);
            R<Long> response = sysTenantFeignClient.create(request);
            if (response == null || response.getCode() == null || response.getCode() != 200) {
                throw supplierException(BasicPromptEnum.SUPPLIER_TENANT_CREATE_FAILED);
            }
        }
        supplier.setRelatedTenantCode(tenantCode);
        supplierMapper.updateById(supplier);
        return tenantCode;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startQualificationReview(SupplierReviewStartParam param) {
        if (param == null || param.getSupplierId() == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_ID_REQUIRED);
        }
        BasicSupplier supplier = requireSupplier(param.getSupplierId());
        Integer reviewStatus = supplier.getReviewStatus();
        if (Objects.equals(reviewStatus, REVIEW_NONE)) {
            throw supplierException(BasicPromptEnum.SUPPLIER_REVIEW_NONE_FORBIDDEN);
        }
        if (Objects.equals(reviewStatus, REVIEW_PROCESSING)) {
            throw supplierException(BasicPromptEnum.SUPPLIER_REVIEW_PROCESSING_FORBIDDEN);
        }
        if (!Objects.equals(reviewStatus, REVIEW_PENDING)) {
            throw supplierException(BasicPromptEnum.SUPPLIER_REVIEW_ONLY_PENDING);
        }

        WorkflowExecutionStartRequestDTO request = new WorkflowExecutionStartRequestDTO();
        request.setTaskCode(REVIEW_TASK_CODE);
        request.setSelectedApprovers(param.getSelectedApprovers());
        request.setFormContent(buildReviewFormContent(supplier));
        R<Long> response = workflowExecutionFeignClient.startExecution(request);
        if (response == null || response.getCode() == null || response.getCode() != 200 || response.getData() == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_REVIEW_START_FAILED);
        }
        supplier.setReviewStatus(REVIEW_PROCESSING);
        supplierMapper.updateById(supplier);
        return response.getData();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleWorkflowCallback(SupplierWorkflowCallbackParam param) {
        if (param == null || !REVIEW_TASK_CODE.equals(param.getTaskCode())) {
            return true;
        }
        Long supplierId = resolveSupplierIdFromFormContent(param.getFormContent());
        if (supplierId == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_REVIEW_CALLBACK_SUPPLIER_ID_MISSING);
        }
        BasicSupplier supplier = requireSupplier(supplierId);
        supplier.setReviewStatus(Objects.equals(param.getStatus(), 2) ? REVIEW_APPROVED : REVIEW_PENDING);
        supplierMapper.updateById(supplier);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SupplierThirdPartySyncResultDTO syncThirdPartySuppliers(SupplierThirdPartySyncRequestDTO request) {
        SupplierThirdPartySyncResultDTO result = new SupplierThirdPartySyncResultDTO();
        List<SupplierAggregateDTO> suppliers = request == null ? Collections.emptyList() : request.getSuppliers();
        result.setTotalCount(suppliers == null ? 0 : suppliers.size());
        if (CollectionUtils.isEmpty(suppliers)) {
            return result;
        }
        for (SupplierAggregateDTO aggregate : suppliers) {
            String supplierCode = aggregate == null ? null : normalizeCode(aggregate.getSupplierCode());
            try {
                validateAggregate(aggregate);
                boolean created = upsertAggregate(aggregate);
                if (created) {
                    result.setCreatedCount(result.getCreatedCount() + 1);
                } else {
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                }
            } catch (Exception ex) {
                log.warn("同步供应商失败，supplierCode={}", supplierCode, ex);
                result.setFailedCount(result.getFailedCount() + 1);
                result.getFailedSupplierCodes().add(StringUtils.hasText(supplierCode) ? supplierCode : "UNKNOWN");
            }
        }
        return result;
    }

    @Override
    public List<SupplierAggregateDTO> exportThirdPartySuppliers(SupplierThirdPartySyncRequestDTO request) {
        LambdaQueryWrapper<BasicSupplier> wrapper = new LambdaQueryWrapper<BasicSupplier>()
                .eq(BasicSupplier::getDeleted, false)
                .orderByDesc(BasicSupplier::getCreateTime);
        return supplierMapper.selectList(wrapper).stream()
                .map(item -> (SupplierAggregateDTO) convertToDTO(item, true))
                .toList();
    }

    @Override
    public SupplierThirdPartySyncResultDTO syncToThirdParty(SupplierThirdPartyInvokeDTO request) {
        SupplierThirdPartyInvokeDTO safeRequest = request == null ? new SupplierThirdPartyInvokeDTO() : request;
        if (!StringUtils.hasText(safeRequest.getApiCode())) {
            safeRequest.setApiCode(DEFAULT_SUPPLIER_SYNC_API_CODE);
        }
        if (safeRequest.getTenantId() == null) {
            safeRequest.setTenantId(Optional.ofNullable(TenantContext.get()).orElse(PUBLIC_TENANT_ID));
        }
        R<SupplierThirdPartySyncResultDTO> response = integrationInternalSupplierFeignClient.syncSuppliers(safeRequest);
        if (response == null || response.getData() == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_THIRD_PARTY_SYNC_FAILED);
        }
        return response.getData();
    }

    @Override
    public SupplierAggregateDTO getByCode(SupplierQueryRequestDTO request) {
        String supplierCode = request == null ? null : request.getSupplierCode();
        BasicSupplier supplier = findByCode(supplierCode);
        if (supplier == null) {
            return null;
        }
        if (request != null && Boolean.TRUE.equals(request.getAvailableOnly()) && !isAvailable(supplier)) {
            return null;
        }
        return convertToDTO(supplier, true);
    }

    @Override
    public List<SupplierAggregateDTO> listByCodes(SupplierQueryRequestDTO request) {
        if (request == null || CollectionUtils.isEmpty(request.getSupplierCodes())) {
            return Collections.emptyList();
        }
        List<String> codes = request.getSupplierCodes().stream()
                .map(this::normalizeCode)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (codes.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BasicSupplier> wrapper = new LambdaQueryWrapper<BasicSupplier>()
                .in(BasicSupplier::getSupplierCode, codes)
                .eq(BasicSupplier::getDeleted, false);
        return supplierMapper.selectList(wrapper).stream()
                .filter(item -> !Boolean.TRUE.equals(request.getAvailableOnly()) || isAvailable(item))
                .map(item -> (SupplierAggregateDTO) convertToDTO(item, true))
                .toList();
    }

    @Override
    public List<SupplierOptionDTO> listOptions(SupplierQueryRequestDTO request) {
        LambdaQueryWrapper<BasicSupplier> wrapper = new LambdaQueryWrapper<BasicSupplier>()
                .eq(BasicSupplier::getDeleted, false)
                .orderByDesc(BasicSupplier::getCreateTime);
        if (request != null && Boolean.TRUE.equals(request.getAvailableOnly())) {
            wrapper.eq(BasicSupplier::getCooperationStatus, "2");
        }
        return supplierMapper.selectList(wrapper).stream()
                .map(this::convertOption)
                .toList();
    }

    @Override
    public void writeImportTemplate(OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            createSheetWithHeader(workbook, "主表", MAIN_HEADERS);
            createSheetWithHeader(workbook, "详情", DETAIL_HEADERS);
            createSheetWithHeader(workbook, "联系人", CONTACT_HEADERS);
            createSheetWithHeader(workbook, "资质", QUALIFICATION_HEADERS);
            workbook.write(outputStream);
        }
    }

    @Override
    public SupplierThirdPartySyncResultDTO importExcel(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw supplierException(BasicPromptEnum.SUPPLIER_IMPORT_FILE_REQUIRED);
        }
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            List<SupplierAggregateDTO> aggregates = readWorkbook(workbook);
            SupplierThirdPartySyncRequestDTO request = new SupplierThirdPartySyncRequestDTO();
            request.setTenantId(PUBLIC_TENANT_ID);
            request.setSuppliers(aggregates);
            return syncThirdPartySuppliers(request);
        }
    }

    @Override
    public void exportExcel(SupplierPageParam param, OutputStream outputStream) throws IOException {
        List<SupplierDTO> suppliers = list(param);
        List<SupplierDTO> details = suppliers.stream()
                .map(item -> getDetailById(item.getId()))
                .filter(Objects::nonNull)
                .toList();
        try (Workbook workbook = new XSSFWorkbook()) {
            writeMainSheet(workbook, details);
            writeDetailSheet(workbook, details);
            writeContactSheet(workbook, details);
            writeQualificationSheet(workbook, details);
            workbook.write(outputStream);
        }
    }

    private LambdaQueryWrapper<BasicSupplier> buildPageWrapper(SupplierPageParam param) {
        LambdaQueryWrapper<BasicSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicSupplier::getDeleted, false);
        if (StringUtils.hasText(param.getSupplierCode())) {
            wrapper.like(BasicSupplier::getSupplierCode, param.getSupplierCode().trim());
        }
        String supplierName = StringUtils.hasText(param.getSupplierFullName())
                ? param.getSupplierFullName()
                : param.getSupplierName();
        if (StringUtils.hasText(supplierName)) {
            wrapper.like(BasicSupplier::getSupplierFullName, supplierName.trim());
        }
        if (StringUtils.hasText(param.getCooperationStatus())) {
            wrapper.eq(BasicSupplier::getCooperationStatus, param.getCooperationStatus().trim());
        }
        if (StringUtils.hasText(param.getCreditLevel())) {
            wrapper.eq(BasicSupplier::getCreditLevel, param.getCreditLevel().trim());
        }
        if (StringUtils.hasText(param.getRiskLevel())) {
            wrapper.eq(BasicSupplier::getRiskLevel, param.getRiskLevel().trim());
        }
        if (StringUtils.hasText(param.getSupplierLevel())) {
            wrapper.eq(BasicSupplier::getSupplierLevel, param.getSupplierLevel().trim());
        }
        if (StringUtils.hasText(param.getRelatedTenantCode())) {
            wrapper.like(BasicSupplier::getRelatedTenantCode, param.getRelatedTenantCode().trim());
        }
        if (param.getReviewStatus() != null) {
            wrapper.eq(BasicSupplier::getReviewStatus, param.getReviewStatus());
        }
        wrapper.orderByDesc(BasicSupplier::getCreateTime);
        return wrapper;
    }

    private void validateSaveParam(SupplierSaveParam param, boolean create) {
        if (param == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_PARAM_REQUIRED);
        }
        if (!create && param.getId() == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_ID_REQUIRED);
        }
        if (!StringUtils.hasText(param.getSupplierCode())) {
            throw supplierException(BasicPromptEnum.SUPPLIER_CODE_REQUIRED);
        }
        if (!StringUtils.hasText(param.getSupplierFullName())) {
            throw supplierException(BasicPromptEnum.SUPPLIER_FULL_NAME_REQUIRED);
        }
        SupplierAggregateDTO aggregate = new SupplierAggregateDTO();
        aggregate.setSupplierCode(param.getSupplierCode());
        aggregate.setSupplierFullName(param.getSupplierFullName());
        aggregate.setLogoUrl(param.getLogoUrl());
        aggregate.setCooperationStatus(param.getCooperationStatus());
        aggregate.setCreditLevel(param.getCreditLevel());
        aggregate.setSupplierLevel(param.getSupplierLevel());
        aggregate.setReviewStatus(param.getReviewStatus());
        aggregate.setDetail(param.getDetail());
        aggregate.setContactList(param.getContactList());
        aggregate.setQualificationList(param.getQualificationList());
        validateAggregate(aggregate);
    }

    private void prepareSupplierCode(SupplierSaveParam param) {
        if (param == null || !Boolean.TRUE.equals(param.getAutoGenerateCode())) {
            return;
        }
        param.setSupplierCode(encodeRuleService.generateCode(SUPPLIER_CODE_RULE));
    }

    private void fillSupplier(BasicSupplier supplier, SupplierSaveParam param) {
        supplier.setSupplierCode(normalizeCode(param.getSupplierCode()));
        supplier.setSupplierFullName(trimToNull(param.getSupplierFullName()));
        supplier.setSupplierShortName(trimToNull(param.getSupplierShortName()));
        supplier.setLogoUrl(trimToNull(param.getLogoUrl()));
        supplier.setEnglishName(trimToNull(param.getEnglishName()));
        supplier.setCurrentAddress(trimToNull(param.getCurrentAddress()));
        supplier.setPrimaryContact(trimToNull(param.getPrimaryContact()));
        supplier.setContactPhone(trimToNull(param.getContactPhone()));
        supplier.setCooperationStatus(trimToNull(param.getCooperationStatus()));
        supplier.setCreditLevel(trimToNull(param.getCreditLevel()));
        supplier.setRiskLevel(trimToNull(param.getRiskLevel()));
        supplier.setSupplierLevel(trimToNull(param.getSupplierLevel()));
        supplier.setRelatedTenantCode(trimToNull(param.getRelatedTenantCode()));
        supplier.setRemark(trimToNull(param.getRemark()));
        if (param.getReviewStatus() != null) {
            supplier.setReviewStatus(param.getReviewStatus());
        }
    }

    private SupplierDTO convertToDTO(BasicSupplier supplier, boolean withChildren) {
        SupplierDTO dto = new SupplierDTO();
        BeanUtils.copyProperties(supplier, dto);
        dto.setHasRelatedTenant(StringUtils.hasText(supplier.getRelatedTenantCode()));
        if (!withChildren) {
            return dto;
        }
        dto.setDetail(queryDetailDTO(supplier.getId()));
        dto.setContactList(queryContactDTOs(supplier.getId()));
        dto.setQualificationList(queryQualificationDTOs(supplier.getId()));
        return dto;
    }

    private SupplierDetailDTO queryDetailDTO(Long supplierId) {
        BasicSupplierDetail detail = detailMapper.selectOne(new LambdaQueryWrapper<BasicSupplierDetail>()
                .eq(BasicSupplierDetail::getSupplierId, supplierId)
                .eq(BasicSupplierDetail::getDeleted, false)
                .last("LIMIT 1"));
        if (detail == null) {
            return null;
        }
        SupplierDetailDTO dto = new SupplierDetailDTO();
        BeanUtils.copyProperties(detail, dto);
        return dto;
    }

    private List<SupplierContactDTO> queryContactDTOs(Long supplierId) {
        return contactMapper.selectList(new LambdaQueryWrapper<BasicSupplierContact>()
                        .eq(BasicSupplierContact::getSupplierId, supplierId)
                        .eq(BasicSupplierContact::getDeleted, false)
                        .orderByAsc(BasicSupplierContact::getId))
                .stream()
                .map(item -> {
                    SupplierContactDTO dto = new SupplierContactDTO();
                    BeanUtils.copyProperties(item, dto);
                    return dto;
                })
                .toList();
    }

    private List<SupplierQualificationDTO> queryQualificationDTOs(Long supplierId) {
        return qualificationMapper.selectList(new LambdaQueryWrapper<BasicSupplierQualification>()
                        .eq(BasicSupplierQualification::getSupplierId, supplierId)
                        .eq(BasicSupplierQualification::getDeleted, false)
                        .orderByAsc(BasicSupplierQualification::getId))
                .stream()
                .map(item -> {
                    SupplierQualificationDTO dto = new SupplierQualificationDTO();
                    BeanUtils.copyProperties(item, dto);
                    return dto;
                })
                .toList();
    }

    private void saveChildren(Long supplierId,
                              SupplierDetailDTO detail,
                              List<SupplierContactDTO> contacts,
                              List<SupplierQualificationDTO> qualifications,
                              boolean replaceContacts,
                              boolean replaceQualifications) {
        saveDetail(supplierId, detail);
        if (replaceContacts) {
            contactMapper.delete(new LambdaQueryWrapper<BasicSupplierContact>()
                    .eq(BasicSupplierContact::getSupplierId, supplierId));
            if (!CollectionUtils.isEmpty(contacts)) {
                for (SupplierContactDTO contact : contacts) {
                    BasicSupplierContact entity = new BasicSupplierContact();
                    BeanUtils.copyProperties(contact, entity);
                    entity.setId(null);
                    entity.setSupplierId(supplierId);
                    entity.setTenantId(PUBLIC_TENANT_ID);
                    contactMapper.insert(entity);
                }
            }
        }
        if (replaceQualifications) {
            qualificationMapper.delete(new LambdaQueryWrapper<BasicSupplierQualification>()
                    .eq(BasicSupplierQualification::getSupplierId, supplierId));
            if (!CollectionUtils.isEmpty(qualifications)) {
                for (SupplierQualificationDTO qualification : qualifications) {
                    BasicSupplierQualification entity = new BasicSupplierQualification();
                    BeanUtils.copyProperties(qualification, entity);
                    entity.setId(null);
                    entity.setSupplierId(supplierId);
                    entity.setTenantId(PUBLIC_TENANT_ID);
                    qualificationMapper.insert(entity);
                }
            }
        }
    }

    private void saveDetail(Long supplierId, SupplierDetailDTO detail) {
        BasicSupplierDetail exists = detailMapper.selectOne(new LambdaQueryWrapper<BasicSupplierDetail>()
                .eq(BasicSupplierDetail::getSupplierId, supplierId)
                .eq(BasicSupplierDetail::getDeleted, false)
                .last("LIMIT 1"));
        if (detail == null) {
            if (exists != null) {
                detailMapper.deleteById(exists.getId());
            }
            return;
        }
        BasicSupplierDetail entity = exists == null ? new BasicSupplierDetail() : exists;
        BeanUtils.copyProperties(detail, entity);
        entity.setId(exists == null ? null : exists.getId());
        entity.setSupplierId(supplierId);
        entity.setTenantId(PUBLIC_TENANT_ID);
        if (exists == null) {
            detailMapper.insert(entity);
        } else {
            detailMapper.updateById(entity);
        }
    }

    private void deleteChildren(Long supplierId) {
        detailMapper.delete(new LambdaQueryWrapper<BasicSupplierDetail>().eq(BasicSupplierDetail::getSupplierId, supplierId));
        contactMapper.delete(new LambdaQueryWrapper<BasicSupplierContact>().eq(BasicSupplierContact::getSupplierId, supplierId));
        qualificationMapper.delete(new LambdaQueryWrapper<BasicSupplierQualification>().eq(BasicSupplierQualification::getSupplierId, supplierId));
    }

    private boolean upsertAggregate(SupplierAggregateDTO aggregate) {
        BasicSupplier supplier = findByCode(aggregate.getSupplierCode());
        boolean created = supplier == null;
        if (created) {
            supplier = new BasicSupplier();
            supplier.setTenantId(PUBLIC_TENANT_ID);
            supplier.setReviewStatus(aggregate.getReviewStatus() == null ? REVIEW_PENDING : aggregate.getReviewStatus());
            fillSupplierFromAggregate(supplier, aggregate);
            supplierMapper.insert(supplier);
        } else {
            fillSupplierFromAggregate(supplier, aggregate);
            supplier.setTenantId(PUBLIC_TENANT_ID);
            supplierMapper.updateById(supplier);
        }
        saveChildren(supplier.getId(),
                aggregate.getDetail(),
                aggregate.getContactList(),
                aggregate.getQualificationList(),
                aggregate.getContactList() != null,
                aggregate.getQualificationList() != null);
        return created;
    }

    private void fillSupplierFromAggregate(BasicSupplier supplier, SupplierAggregateDTO aggregate) {
        supplier.setSupplierCode(normalizeCode(aggregate.getSupplierCode()));
        supplier.setSupplierFullName(trimToNull(aggregate.getSupplierFullName()));
        supplier.setSupplierShortName(trimToNull(aggregate.getSupplierShortName()));
        supplier.setLogoUrl(trimToNull(aggregate.getLogoUrl()));
        supplier.setEnglishName(trimToNull(aggregate.getEnglishName()));
        supplier.setCurrentAddress(trimToNull(aggregate.getCurrentAddress()));
        supplier.setPrimaryContact(trimToNull(aggregate.getPrimaryContact()));
        supplier.setContactPhone(trimToNull(aggregate.getContactPhone()));
        supplier.setCooperationStatus(trimToNull(aggregate.getCooperationStatus()));
        supplier.setCreditLevel(trimToNull(aggregate.getCreditLevel()));
        supplier.setRiskLevel(trimToNull(aggregate.getRiskLevel()));
        supplier.setSupplierLevel(trimToNull(aggregate.getSupplierLevel()));
        supplier.setRelatedTenantCode(trimToNull(aggregate.getRelatedTenantCode()));
        supplier.setRemark(trimToNull(aggregate.getRemark()));
        if (aggregate.getReviewStatus() != null) {
            supplier.setReviewStatus(aggregate.getReviewStatus());
        }
    }

    private void validateAggregate(SupplierAggregateDTO aggregate) {
        if (aggregate == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_DATA_REQUIRED);
        }
        String supplierCode = normalizeCode(aggregate.getSupplierCode());
        if (!StringUtils.hasText(supplierCode)) {
            throw supplierException(BasicPromptEnum.SUPPLIER_CODE_REQUIRED);
        }
        if (!StringUtils.hasText(aggregate.getSupplierFullName())) {
            throw supplierException(BasicPromptEnum.SUPPLIER_FULL_NAME_REQUIRED);
        }
        validateInValues("合作状态", aggregate.getCooperationStatus(), COOPERATION_STATUS_VALUES);
        validateInValues("信用等级", aggregate.getCreditLevel(), CREDIT_LEVEL_VALUES);
        validateInValues("分级", aggregate.getSupplierLevel(), SUPPLIER_LEVEL_VALUES);
        validateInValues("审查状态", aggregate.getReviewStatus(), REVIEW_STATUS_VALUES);
        validateDetail(aggregate.getDetail());
    }

    private void validateDetail(SupplierDetailDTO detail) {
        if (detail == null) {
            return;
        }
        validateInValues("企业性质", detail.getEnterpriseNature(), ENTERPRISE_NATURE_VALUES);
        BigDecimal taxRate = detail.getDefaultTaxRate();
        if (taxRate != null && (taxRate.compareTo(BigDecimal.ZERO) < 0 || taxRate.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw supplierException(BasicPromptEnum.SUPPLIER_DEFAULT_TAX_RATE_RANGE);
        }
    }

    private <T> void validateInValues(String fieldName, T value, Set<T> values) {
        if (value == null) {
            return;
        }
        if (value instanceof String text && !StringUtils.hasText(text)) {
            return;
        }
        if (!values.contains(value)) {
            throw supplierException(BasicPromptEnum.SUPPLIER_FIELD_VALUE_INVALID, fieldName, value);
        }
    }

    private BasicSupplier requireSupplier(Long id) {
        if (id == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_ID_REQUIRED);
        }
        BasicSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_NOT_FOUND);
        }
        return supplier;
    }

    private BasicSupplier findByCode(String supplierCode) {
        String code = normalizeCode(supplierCode);
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return supplierMapper.selectOne(new LambdaQueryWrapper<BasicSupplier>()
                .eq(BasicSupplier::getSupplierCode, code)
                .eq(BasicSupplier::getDeleted, false)
                .last("LIMIT 1"));
    }

    private BasicSupplier findByRelatedTenantCode(String tenantCode) {
        if (!StringUtils.hasText(tenantCode)) {
            return null;
        }
        return supplierMapper.selectOne(new LambdaQueryWrapper<BasicSupplier>()
                .eq(BasicSupplier::getRelatedTenantCode, tenantCode.trim())
                .eq(BasicSupplier::getDeleted, false)
                .last("LIMIT 1"));
    }

    private SysTenantSimpleDTO getTenantByCode(String tenantCode) {
        if (!StringUtils.hasText(tenantCode)) {
            return null;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("tenantCode", tenantCode.trim());
        R<SysTenantSimpleDTO> response = sysTenantFeignClient.getByCode(param);
        return response == null ? null : response.getData();
    }

    private String buildTenantCode(String supplierCode) {
        String code = normalizeCode(supplierCode).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        return "sup_" + code;
    }

    private String buildReviewFormContent(BasicSupplier supplier) {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("supplierId", supplier.getId());
        form.put("supplierCode", supplier.getSupplierCode());
        form.put("supplierFullName", supplier.getSupplierFullName());
        form.put("qualificationSummary", buildQualificationSummary(supplier.getId()));
        form.put("currentReviewStatus", supplier.getReviewStatus());
        try {
            return objectMapper.writeValueAsString(form);
        } catch (Exception ex) {
            throw supplierException(BasicPromptEnum.SUPPLIER_REVIEW_FORM_SERIALIZE_FAILED);
        }
    }

    private String buildQualificationSummary(Long supplierId) {
        List<SupplierQualificationDTO> qualifications = queryQualificationDTOs(supplierId);
        if (qualifications.isEmpty()) {
            return "暂无资质";
        }
        return qualifications.stream()
                .map(item -> String.format("%s/%s/%s",
                        Optional.ofNullable(item.getQualificationType()).orElse("-"),
                        Optional.ofNullable(item.getCertificateNo()).orElse("-"),
                        Boolean.TRUE.equals(item.getValid()) ? "有效" : "无效"))
                .collect(Collectors.joining("; "));
    }

    private Long resolveSupplierIdFromFormContent(String formContent) {
        if (!StringUtils.hasText(formContent)) {
            return null;
        }
        try {
            Map<String, Object> form = objectMapper.readValue(formContent, new TypeReference<Map<String, Object>>() {});
            Object value = form.get("supplierId");
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value instanceof String str && StringUtils.hasText(str)) {
                return Long.parseLong(str);
            }
        } catch (Exception ex) {
            throw supplierException(BasicPromptEnum.SUPPLIER_REVIEW_CALLBACK_FORM_PARSE_FAILED);
        }
        return null;
    }

    private SupplierOptionDTO convertOption(BasicSupplier supplier) {
        SupplierOptionDTO dto = new SupplierOptionDTO();
        dto.setId(supplier.getId());
        dto.setSupplierCode(supplier.getSupplierCode());
        dto.setSupplierFullName(supplier.getSupplierFullName());
        dto.setSupplierShortName(supplier.getSupplierShortName());
        dto.setRelatedTenantCode(supplier.getRelatedTenantCode());
        dto.setHasRelatedTenant(StringUtils.hasText(supplier.getRelatedTenantCode()));
        dto.setReviewStatus(supplier.getReviewStatus());
        return dto;
    }

    private boolean isAvailable(BasicSupplier supplier) {
        return Objects.equals(supplier.getCooperationStatus(), "2");
    }

    private void createSheetWithHeader(Workbook workbook, String sheetName, List<String> headers) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            header.createCell(i).setCellValue(headers.get(i));
            sheet.setColumnWidth(i, 18 * 256);
        }
    }

    private List<SupplierAggregateDTO> readWorkbook(Workbook workbook) {
        Map<String, SupplierAggregateDTO> aggregateMap = new LinkedHashMap<>();
        Set<String> mainCodes = readMainSheet(workbook.getSheet("主表"), aggregateMap);
        readDetailImportSheet(workbook.getSheet("详情"), aggregateMap, mainCodes);
        readContactImportSheet(workbook.getSheet("联系人"), aggregateMap, mainCodes);
        readQualificationImportSheet(workbook.getSheet("资质"), aggregateMap, mainCodes);
        List<SupplierAggregateDTO> aggregates = new ArrayList<>(aggregateMap.values());
        aggregates.forEach(this::validateAggregate);
        return aggregates;
    }

    private Set<String> readMainSheet(Sheet sheet, Map<String, SupplierAggregateDTO> aggregateMap) {
        Set<String> mainCodes = new java.util.HashSet<>();
        if (sheet == null) {
            throw supplierException(BasicPromptEnum.SUPPLIER_IMPORT_MAIN_SHEET_MISSING);
        }
        Map<String, Integer> header = readHeader(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String supplierCode = normalizeCode(cell(row, header, "供应商编码"));
            if (!StringUtils.hasText(supplierCode)) {
                continue;
            }
            if (!mainCodes.add(supplierCode)) {
                throw supplierException(BasicPromptEnum.SUPPLIER_IMPORT_MAIN_CODE_DUPLICATED, supplierCode);
            }
            SupplierAggregateDTO dto = aggregateMap.computeIfAbsent(supplierCode, code -> {
                SupplierAggregateDTO item = new SupplierAggregateDTO();
                item.setSupplierCode(code);
                return item;
            });
            dto.setSupplierFullName(cell(row, header, "供应商全称"));
            dto.setSupplierShortName(cell(row, header, "简称"));
            dto.setLogoUrl(cell(row, header, "供应商Logo"));
            dto.setEnglishName(cell(row, header, "英文名"));
            dto.setCurrentAddress(cell(row, header, "现地址"));
            dto.setPrimaryContact(cell(row, header, "主联系人"));
            dto.setContactPhone(cell(row, header, "联系电话"));
            dto.setCooperationStatus(cell(row, header, "合作状态"));
            dto.setCreditLevel(cell(row, header, "信用等级"));
            dto.setRiskLevel(cell(row, header, "风险等级"));
            dto.setSupplierLevel(cell(row, header, "分级"));
            dto.setRelatedTenantCode(cell(row, header, "关联租户编号"));
            dto.setReviewStatus(parseInteger(cell(row, header, "审查状态")));
            dto.setRemark(cell(row, header, "备注"));
        }
        if (mainCodes.isEmpty()) {
            throw supplierException(BasicPromptEnum.SUPPLIER_IMPORT_MAIN_SHEET_EMPTY);
        }
        return mainCodes;
    }

    private void readDetailImportSheet(Sheet sheet, Map<String, SupplierAggregateDTO> aggregateMap, Set<String> mainCodes) {
        if (sheet == null) {
            return;
        }
        Map<String, Integer> header = readHeader(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String supplierCode = normalizeCode(cell(row, header, "供应商编码"));
            if (!StringUtils.hasText(supplierCode)) {
                continue;
            }
            validateImportSupplierCode(supplierCode, mainCodes, "详情", i);
            SupplierAggregateDTO aggregate = aggregateMap.computeIfAbsent(supplierCode, code -> {
                SupplierAggregateDTO item = new SupplierAggregateDTO();
                item.setSupplierCode(code);
                return item;
            });
            SupplierDetailDTO detail = new SupplierDetailDTO();
            detail.setLegalRepresentative(cell(row, header, "法人代表"));
            detail.setRegisteredCapital(parseBigDecimal(cell(row, header, "注册资本")));
            detail.setEstablishmentDate(parseDate(cell(row, header, "成立日期")));
            detail.setEnterpriseNature(cell(row, header, "企业性质"));
            detail.setIndustryCategory(cell(row, header, "行业分类"));
            detail.setRegisteredAddress(cell(row, header, "注册地址"));
            detail.setBusinessAddress(cell(row, header, "经营地址"));
            detail.setEmail(cell(row, header, "邮箱"));
            detail.setTaxNumber(cell(row, header, "税号"));
            detail.setBankName(cell(row, header, "开户银行"));
            detail.setBankAccount(cell(row, header, "银行账号"));
            detail.setInvoiceType(cell(row, header, "发票类型"));
            detail.setDefaultTaxRate(parseBigDecimal(cell(row, header, "默认税率")));
            aggregate.setDetail(detail);
        }
    }

    private void readContactImportSheet(Sheet sheet, Map<String, SupplierAggregateDTO> aggregateMap, Set<String> mainCodes) {
        if (sheet == null) {
            return;
        }
        Map<String, Integer> header = readHeader(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String supplierCode = normalizeCode(cell(row, header, "供应商编码"));
            if (!StringUtils.hasText(supplierCode)) {
                continue;
            }
            validateImportSupplierCode(supplierCode, mainCodes, "联系人", i);
            SupplierAggregateDTO aggregate = aggregateMap.computeIfAbsent(supplierCode, code -> {
                SupplierAggregateDTO item = new SupplierAggregateDTO();
                item.setSupplierCode(code);
                return item;
            });
            if (aggregate.getContactList() == null) {
                aggregate.setContactList(new ArrayList<>());
            }
            SupplierContactDTO contact = new SupplierContactDTO();
            contact.setContactName(cell(row, header, "联系人姓名"));
            contact.setContactPhone(cell(row, header, "联系人电话"));
            contact.setContactPosition(cell(row, header, "联系人职位"));
            contact.setContactEmail(cell(row, header, "联系人邮箱"));
            aggregate.getContactList().add(contact);
        }
    }

    private void readQualificationImportSheet(Sheet sheet, Map<String, SupplierAggregateDTO> aggregateMap, Set<String> mainCodes) {
        if (sheet == null) {
            return;
        }
        Map<String, Integer> header = readHeader(sheet.getRow(0));
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String supplierCode = normalizeCode(cell(row, header, "供应商编码"));
            if (!StringUtils.hasText(supplierCode)) {
                continue;
            }
            validateImportSupplierCode(supplierCode, mainCodes, "资质", i);
            SupplierAggregateDTO aggregate = aggregateMap.computeIfAbsent(supplierCode, code -> {
                SupplierAggregateDTO item = new SupplierAggregateDTO();
                item.setSupplierCode(code);
                return item;
            });
            if (aggregate.getQualificationList() == null) {
                aggregate.setQualificationList(new ArrayList<>());
            }
            SupplierQualificationDTO qualification = new SupplierQualificationDTO();
            qualification.setQualificationType(cell(row, header, "资质类型"));
            qualification.setCertificateNo(cell(row, header, "证书编号"));
            qualification.setIssueDate(parseDate(cell(row, header, "发证日期")));
            qualification.setExpireDate(parseDate(cell(row, header, "有效期至")));
            qualification.setAttachment(cell(row, header, "附件"));
            qualification.setValid(parseBoolean(cell(row, header, "是否有效")));
            aggregate.getQualificationList().add(qualification);
        }
    }

    private void validateImportSupplierCode(String supplierCode, Set<String> mainCodes, String sheetName, int rowIndex) {
        if (mainCodes == null || !mainCodes.contains(supplierCode)) {
            throw supplierException(BasicPromptEnum.SUPPLIER_IMPORT_ORPHAN_CODE, sheetName, rowIndex + 1, supplierCode);
        }
    }

    private Map<String, Integer> readHeader(Row row) {
        Map<String, Integer> header = new HashMap<>();
        if (row == null) {
            return header;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String value = cellToString(row.getCell(i));
            if (StringUtils.hasText(value)) {
                header.put(value.trim(), i);
            }
        }
        return header;
    }

    private String cell(Row row, Map<String, Integer> header, String name) {
        if (row == null || header == null || !header.containsKey(name)) {
            return null;
        }
        return cellToString(row.getCell(header.get(name)));
    }

    private String cellToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return trimToNull(cell.getStringCellValue());
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
            }
            BigDecimal value = BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros();
            return value.toPlainString();
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return Boolean.toString(cell.getBooleanCellValue());
        }
        if (cell.getCellType() == CellType.FORMULA) {
            try {
                return trimToNull(cell.getStringCellValue());
            } catch (Exception ignored) {
                return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            }
        }
        return null;
    }

    private void writeMainSheet(Workbook workbook, List<SupplierDTO> suppliers) {
        Sheet sheet = workbook.createSheet("主表");
        writeHeader(sheet, MAIN_HEADERS);
        int rowIndex = 1;
        for (SupplierDTO supplier : suppliers) {
            Row row = sheet.createRow(rowIndex++);
            writeCells(row,
                    supplier.getSupplierCode(),
                    supplier.getSupplierFullName(),
                    supplier.getSupplierShortName(),
                    supplier.getLogoUrl(),
                    supplier.getEnglishName(),
                    supplier.getCurrentAddress(),
                    supplier.getPrimaryContact(),
                    supplier.getContactPhone(),
                    supplier.getCooperationStatus(),
                    supplier.getCreditLevel(),
                    supplier.getRiskLevel(),
                    supplier.getSupplierLevel(),
                    supplier.getRelatedTenantCode(),
                    supplier.getReviewStatus(),
                    supplier.getRemark());
        }
    }

    private void writeDetailSheet(Workbook workbook, List<SupplierDTO> suppliers) {
        Sheet sheet = workbook.createSheet("详情");
        writeHeader(sheet, DETAIL_HEADERS);
        int rowIndex = 1;
        for (SupplierDTO supplier : suppliers) {
            SupplierDetailDTO detail = supplier.getDetail();
            if (detail == null) {
                continue;
            }
            Row row = sheet.createRow(rowIndex++);
            writeCells(row,
                    supplier.getSupplierCode(),
                    detail.getLegalRepresentative(),
                    detail.getRegisteredCapital(),
                    detail.getEstablishmentDate(),
                    detail.getEnterpriseNature(),
                    detail.getIndustryCategory(),
                    detail.getRegisteredAddress(),
                    detail.getBusinessAddress(),
                    detail.getEmail(),
                    detail.getTaxNumber(),
                    detail.getBankName(),
                    detail.getBankAccount(),
                    detail.getInvoiceType(),
                    detail.getDefaultTaxRate());
        }
    }

    private void writeContactSheet(Workbook workbook, List<SupplierDTO> suppliers) {
        Sheet sheet = workbook.createSheet("联系人");
        writeHeader(sheet, CONTACT_HEADERS);
        int rowIndex = 1;
        for (SupplierDTO supplier : suppliers) {
            for (SupplierContactDTO contact : Optional.ofNullable(supplier.getContactList()).orElse(Collections.emptyList())) {
                Row row = sheet.createRow(rowIndex++);
                writeCells(row,
                        supplier.getSupplierCode(),
                        contact.getContactName(),
                        contact.getContactPhone(),
                        contact.getContactPosition(),
                        contact.getContactEmail());
            }
        }
    }

    private void writeQualificationSheet(Workbook workbook, List<SupplierDTO> suppliers) {
        Sheet sheet = workbook.createSheet("资质");
        writeHeader(sheet, QUALIFICATION_HEADERS);
        int rowIndex = 1;
        for (SupplierDTO supplier : suppliers) {
            for (SupplierQualificationDTO qualification : Optional.ofNullable(supplier.getQualificationList()).orElse(Collections.emptyList())) {
                Row row = sheet.createRow(rowIndex++);
                writeCells(row,
                        supplier.getSupplierCode(),
                        qualification.getQualificationType(),
                        qualification.getCertificateNo(),
                        qualification.getIssueDate(),
                        qualification.getExpireDate(),
                        qualification.getAttachment(),
                        qualification.getValid());
            }
        }
    }

    private void writeHeader(Sheet sheet, List<String> headers) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            header.createCell(i).setCellValue(headers.get(i));
            sheet.setColumnWidth(i, 18 * 256);
        }
    }

    private void writeCells(Row row, Object... values) {
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            row.createCell(i).setCellValue(value == null ? "" : value.toString());
        }
    }

    private String normalizeCode(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private I18nBusinessException supplierException(BasicPromptEnum prompt, Object... args) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt, args);
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }

    private BigDecimal parseBigDecimal(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("yyyy/M/d"),
                DateTimeFormatter.ofPattern("yyyy.MM.dd"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        for (DateTimeFormatter formatter : formatters) {
            try {
                if (formatter == formatters.get(3)) {
                    return LocalDateTime.parse(text, formatter).toLocalDate();
                }
                return LocalDate.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw supplierException(BasicPromptEnum.SUPPLIER_DATE_FORMAT_INVALID, text);
    }

    private Boolean parseBoolean(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        return "1".equals(text) || "true".equalsIgnoreCase(text) || "是".equals(text) || "有效".equals(text);
    }
}
