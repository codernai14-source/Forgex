package com.forgex.basic.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.basic.customer.domain.dto.CustomerContactDTO;
import com.forgex.basic.customer.domain.dto.CustomerDTO;
import com.forgex.basic.customer.domain.dto.CustomerExtraDTO;
import com.forgex.basic.customer.domain.dto.CustomerInvoiceDTO;
import com.forgex.basic.customer.domain.entity.BasicCustomer;
import com.forgex.basic.customer.domain.entity.BasicCustomerContact;
import com.forgex.basic.customer.domain.entity.BasicCustomerExtra;
import com.forgex.basic.customer.domain.entity.BasicCustomerInvoice;
import com.forgex.basic.customer.domain.param.CustomerApprovalStartParam;
import com.forgex.basic.customer.domain.param.CustomerPageParam;
import com.forgex.basic.customer.domain.param.CustomerSaveParam;
import com.forgex.basic.customer.domain.param.CustomerWorkflowCallbackParam;
import com.forgex.basic.customer.mapper.BasicCustomerContactMapper;
import com.forgex.basic.customer.mapper.BasicCustomerExtraMapper;
import com.forgex.basic.customer.mapper.BasicCustomerInvoiceMapper;
import com.forgex.basic.customer.mapper.BasicCustomerMapper;
import com.forgex.basic.customer.service.ICustomerService;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.common.api.dto.CustomerAggregateDTO;
import com.forgex.common.api.dto.CustomerContactSyncDTO;
import com.forgex.common.api.dto.CustomerExtraSyncDTO;
import com.forgex.common.api.dto.CustomerInvoiceSyncDTO;
import com.forgex.common.api.dto.CustomerThirdPartyInvokeDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncRequestDTO;
import com.forgex.common.api.dto.CustomerThirdPartySyncResultDTO;
import com.forgex.common.api.dto.SysTenantCreateRequestDTO;
import com.forgex.common.api.dto.SysTenantSimpleDTO;
import com.forgex.common.api.dto.WorkflowExecutionStartRequestDTO;
import com.forgex.common.api.feign.IntegrationInternalCustomerFeignClient;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.enums.FxExcelImportMode;
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

/**
 * 客户主数据服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends ServiceImpl<BasicCustomerMapper, BasicCustomer> implements ICustomerService {

    private static final Long PUBLIC_TENANT_ID = 0L;
    private static final String CUSTOMER_CODE_RULE = "CUSTOMER_CODE";
    private static final String CUSTOMER_APPROVAL_TASK_CODE = "CUSTOMER_MASTER_APPROVAL";
    private static final String DEFAULT_CUSTOMER_SYNC_API_CODE = "basic_customer_sync";
    private static final String DEFAULT_CUSTOMER_PULL_API_CODE = "basic_customer_pull";
    private static final int APPROVAL_PENDING = 0;
    private static final int APPROVAL_PROCESSING = 1;
    private static final int APPROVAL_APPROVED = 2;
    private static final int APPROVAL_REJECTED = 3;
    private static final List<String> MAIN_HEADERS = Arrays.asList(
            "客户编码", "客户全称", "客户简称", "客户名称", "客户价值等级", "客户信用等级", "经营状态",
            "实际经营地址", "收款地址", "发货地址", "审批状态", "是否关联租户", "关联租户编码",
            "运输方式", "付款条款", "国家", "企业性质", "状态", "备注");

    private final BasicCustomerMapper customerMapper;
    private final BasicCustomerContactMapper contactMapper;
    private final BasicCustomerInvoiceMapper invoiceMapper;
    private final BasicCustomerExtraMapper extraMapper;
    private final SysTenantFeignClient sysTenantFeignClient;
    private final WorkflowExecutionFeignClient workflowExecutionFeignClient;
    private final EncodeRuleService encodeRuleService;
    private final IntegrationInternalCustomerFeignClient integrationInternalCustomerFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 分页查询数据。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<CustomerDTO> page(CustomerPageParam param) {
        CustomerPageParam safeParam = param == null ? new CustomerPageParam() : param;
        Page<BasicCustomer> entityPage = new Page<>(safeParam.getPageNum(), safeParam.getPageSize());
        Page<BasicCustomer> customerPage = customerMapper.selectPage(entityPage, buildPageWrapper(safeParam));
        Page<CustomerDTO> dtoPage = new Page<>(customerPage.getCurrent(), customerPage.getSize(), customerPage.getTotal());
        dtoPage.setRecords(customerPage.getRecords().stream().map(item -> convertToDTO(item, false)).toList());
        return dtoPage;
    }

    /**
     * 查询数据列表。
     *
     * @param param 请求参数
     * @return 列表数据
     */
    @Override
    public List<CustomerDTO> list(CustomerPageParam param) {
        CustomerPageParam safeParam = param == null ? new CustomerPageParam() : param;
        return customerMapper.selectList(buildPageWrapper(safeParam)).stream()
                .map(item -> convertToDTO(item, false))
                .toList();
    }

    /**
     * 根据主键查询详情。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @Override
    public CustomerDTO getDetailById(Long id) {
        if (id == null) {
            return null;
        }
        BasicCustomer customer = customerMapper.selectById(id);
        return customer == null ? null : convertToDTO(customer, true);
    }

    /**
     * 创建数据。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CustomerSaveParam param) {
        validateSaveParam(param, true);
        if (Boolean.TRUE.equals(param.getAutoGenerateCode())) {
            param.setCustomerCode(encodeRuleService.generateCode(CUSTOMER_CODE_RULE));
        }
        if (findByCode(param.getCustomerCode()) != null) {
            throw basicException(BasicPromptEnum.CUSTOMER_CODE_EXISTS);
        }
        BasicCustomer customer = new BasicCustomer();
        fillCustomer(customer, param);
        customer.setTenantId(PUBLIC_TENANT_ID);
        customer.setApprovalStatus(param.getApprovalStatus() == null ? APPROVAL_PENDING : param.getApprovalStatus());
        customerMapper.insert(customer);
        saveChildren(customer.getId(), param);
        return customer.getId();
    }

    /**
     * 更新数据。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(CustomerSaveParam param) {
        validateSaveParam(param, false);
        BasicCustomer exists = requireCustomer(param.getId());
        if (!Objects.equals(normalizeCode(exists.getCustomerCode()), normalizeCode(param.getCustomerCode()))) {
            throw basicException(BasicPromptEnum.CUSTOMER_CODE_IMMUTABLE);
        }
        fillCustomer(exists, param);
        exists.setTenantId(PUBLIC_TENANT_ID);
        customerMapper.updateById(exists);
        saveChildren(exists.getId(), param);
        return true;
    }

    /**
     * 删除数据。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        BasicCustomer customer = requireCustomer(id);
        if (Boolean.TRUE.equals(customer.getIsRelatedTenant()) || StringUtils.hasText(customer.getRelatedTenantCode())) {
            throw basicException(BasicPromptEnum.CUSTOMER_LINKED_TENANT_DELETE_FORBIDDEN);
        }
        customerMapper.deleteById(id);
        deleteChildren(id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        for (Long id : ids) {
            delete(id);
        }
        return true;
    }

    /**
     * 生成关联租户。
     *
     * @param id 主键 ID
     * @return 字符串结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateTenant(Long id) {
        BasicCustomer customer = requireCustomer(id);
        String tenantCode = buildTenantCode(customer.getCustomerCode());
        if (StringUtils.hasText(customer.getRelatedTenantCode())) {
            SysTenantSimpleDTO linkedTenant = getTenantByCode(customer.getRelatedTenantCode());
            if (linkedTenant != null) {
                customer.setIsRelatedTenant(true);
                customerMapper.updateById(customer);
                return customer.getRelatedTenantCode();
            }
            throw basicException(BasicPromptEnum.CUSTOMER_LINKED_TENANT_NOT_FOUND);
        }
        SysTenantSimpleDTO existedTenant = getTenantByCode(tenantCode);
        BasicCustomer occupiedCustomer = findByRelatedTenantCode(tenantCode);
        if (existedTenant != null && occupiedCustomer != null && !Objects.equals(occupiedCustomer.getId(), customer.getId())) {
            throw basicException(BasicPromptEnum.CUSTOMER_TENANT_CODE_OCCUPIED);
        }
        if (existedTenant == null) {
            SysTenantCreateRequestDTO request = new SysTenantCreateRequestDTO();
            request.setTenantCode(tenantCode);
            request.setTenantName(customerName(customer));
            request.setTenantType(TenantTypeEnum.CUSTOMER_TENANT);
            request.setDescription("客户主数据自动生成");
            request.setStatus(true);
            R<Long> response = sysTenantFeignClient.create(request);
            if (response == null || response.getCode() == null || response.getCode() != StatusCode.SUCCESS) {
                throw basicException(BasicPromptEnum.CUSTOMER_TENANT_CREATE_FAILED);
            }
        }
        customer.setIsRelatedTenant(true);
        customer.setRelatedTenantCode(tenantCode);
        customerMapper.updateById(customer);
        return tenantCode;
    }

    /**
     * 发起审批流程。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startApproval(CustomerApprovalStartParam param) {
        if (param == null || param.getCustomerId() == null) {
            throw basicException(BasicPromptEnum.CUSTOMER_ID_REQUIRED);
        }
        BasicCustomer customer = requireCustomer(param.getCustomerId());
        if (Objects.equals(customer.getApprovalStatus(), APPROVAL_PROCESSING)) {
            throw basicException(BasicPromptEnum.CUSTOMER_APPROVAL_PROCESSING_FORBIDDEN);
        }
        WorkflowExecutionStartRequestDTO request = new WorkflowExecutionStartRequestDTO();
        request.setTaskCode(CUSTOMER_APPROVAL_TASK_CODE);
        request.setSelectedApprovers(param.getSelectedApprovers());
        request.setFormContent(buildApprovalFormContent(customer));
        R<Long> response = workflowExecutionFeignClient.startExecution(request);
        if (response == null || response.getCode() == null || response.getCode() != StatusCode.SUCCESS || response.getData() == null) {
            throw basicException(BasicPromptEnum.CUSTOMER_APPROVAL_START_FAILED);
        }
        customer.setApprovalStatus(APPROVAL_PROCESSING);
        customerMapper.updateById(customer);
        return response.getData();
    }

    /**
     * 处理工作流回调。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleWorkflowCallback(CustomerWorkflowCallbackParam param) {
        if (param == null || !CUSTOMER_APPROVAL_TASK_CODE.equals(param.getTaskCode())) {
            return true;
        }
        Long customerId = resolveCustomerIdFromFormContent(param.getFormContent());
        BasicCustomer customer = requireCustomer(customerId);
        customer.setApprovalStatus(Objects.equals(param.getStatus(), 2) ? APPROVAL_APPROVED : APPROVAL_REJECTED);
        customerMapper.updateById(customer);
        return true;
    }

    /**
     * 同步第三方客户数据。
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerThirdPartySyncResultDTO syncThirdPartyCustomers(CustomerThirdPartySyncRequestDTO request) {
        CustomerThirdPartySyncResultDTO result = new CustomerThirdPartySyncResultDTO();
        List<CustomerAggregateDTO> customers = request == null ? Collections.emptyList() : request.getCustomers();
        result.setTotalCount(customers == null ? 0 : customers.size());
        if (CollectionUtils.isEmpty(customers)) {
            return result;
        }
        for (CustomerAggregateDTO aggregate : customers) {
            String customerCode = aggregate == null ? null : normalizeCode(aggregate.getCustomerCode());
            try {
                validateAggregate(aggregate);
                boolean created = upsertAggregate(aggregate);
                if (created) {
                    result.setCreatedCount(result.getCreatedCount() + 1);
                } else {
                    result.setUpdatedCount(result.getUpdatedCount() + 1);
                }
            } catch (Exception ex) {
                log.warn("同步客户失败，customerCode={}", customerCode, ex);
                result.setFailedCount(result.getFailedCount() + 1);
                result.getFailedCustomerCodes().add(StringUtils.hasText(customerCode) ? customerCode : "UNKNOWN");
            }
        }
        return result;
    }

    /**
     * 导出第三方客户数据。
     *
     * @param request 导出请求
     * @return 客户聚合列表
     */
    @Override
    public List<CustomerAggregateDTO> exportThirdPartyCustomers(CustomerThirdPartySyncRequestDTO request) {
        return customerMapper.selectList(new LambdaQueryWrapper<BasicCustomer>()
                        .eq(BasicCustomer::getDeleted, false)
                        .orderByDesc(BasicCustomer::getCreateTime))
                .stream()
                .map(item -> convertToAggregate(item, true))
                .toList();
    }

    /**
     * 调接口平台同步客户到第三方。
     *
     * @param request 调用请求
     * @return 同步结果
     */
    @Override
    public CustomerThirdPartySyncResultDTO syncToThirdParty(CustomerThirdPartyInvokeDTO request) {
        CustomerThirdPartyInvokeDTO safeRequest = request == null ? new CustomerThirdPartyInvokeDTO() : request;
        if (!StringUtils.hasText(safeRequest.getApiCode())) {
            safeRequest.setApiCode(DEFAULT_CUSTOMER_SYNC_API_CODE);
        }
        if (safeRequest.getTenantId() == null) {
            safeRequest.setTenantId(Optional.ofNullable(TenantContext.get()).orElse(PUBLIC_TENANT_ID));
        }
        R<CustomerThirdPartySyncResultDTO> response = integrationInternalCustomerFeignClient.syncCustomers(safeRequest);
        if (response == null || response.getData() == null) {
            throw basicException(BasicPromptEnum.CUSTOMER_THIRD_PARTY_SYNC_FAILED);
        }
        return response.getData();
    }

    /**
     * 从第三方拉取客户数据。
     *
     * @param request 调用请求
     * @return 写入结果
     */
    @Override
    public CustomerThirdPartySyncResultDTO pullFromThirdParty(CustomerThirdPartyInvokeDTO request) {
        CustomerThirdPartyInvokeDTO safeRequest = request == null ? new CustomerThirdPartyInvokeDTO() : request;
        if (!StringUtils.hasText(safeRequest.getApiCode())) {
            safeRequest.setApiCode(DEFAULT_CUSTOMER_PULL_API_CODE);
        }
        if (safeRequest.getTenantId() == null) {
            safeRequest.setTenantId(Optional.ofNullable(TenantContext.get()).orElse(PUBLIC_TENANT_ID));
        }
        R<CustomerThirdPartySyncResultDTO> response = integrationInternalCustomerFeignClient.pullCustomers(safeRequest);
        if (response == null || response.getData() == null) {
            throw basicException(BasicPromptEnum.CUSTOMER_THIRD_PARTY_SYNC_FAILED);
        }
        return response.getData();
    }

    /**
     * 写入客户导入模板。
     *
     * @param outputStream 输出流
     */
    @Override
    public void writeImportTemplate(OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            createSheetWithHeader(workbook, "主表", MAIN_HEADERS);
            workbook.write(outputStream);
        }
    }

    /**
     * 导入客户 Excel。
     *
     * @param file Excel 文件
     * @return 导入结果
     */
    @Override
    public CustomerThirdPartySyncResultDTO importExcel(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw basicException(BasicPromptEnum.CUSTOMER_IMPORT_FILE_REQUIRED);
        }
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = new XSSFWorkbook(inputStream)) {
            CustomerThirdPartySyncRequestDTO request = new CustomerThirdPartySyncRequestDTO();
            request.setTenantId(PUBLIC_TENANT_ID);
            request.setCustomers(readWorkbook(workbook));
            return syncThirdPartyCustomers(request);
        }
    }

    /**
     * 执行客户公共导入。
     *
     * @param param 公共导入参数
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param) {
        FxExcelImportMode mode = FxExcelImportMode.parse(param == null ? null : param.getImportMode());
        List<CustomerAggregateDTO> customers = readCommonImportData(param);
        FxExcelImportResultDTO result = new FxExcelImportResultDTO();
        result.setTotalCount(customers.size());
        if (mode == FxExcelImportMode.COVER) {
            coverCustomers();
        }
        for (CustomerAggregateDTO aggregate : customers) {
            String customerCode = aggregate == null ? null : normalizeCode(aggregate.getCustomerCode());
            try {
                validateAggregate(aggregate);
                handleCustomerAggregate(mode, aggregate, result);
            } catch (Exception ex) {
                log.warn("公共导入客户失败，customerCode={}", customerCode, ex);
                result.addError(StringUtils.hasText(customerCode) ? customerCode : "UNKNOWN");
            }
        }
        return result;
    }

    /**
     * 导出客户 Excel。
     *
     * @param param 查询参数
     * @param outputStream 输出流
     */
    @Override
    public void exportExcel(CustomerPageParam param, OutputStream outputStream) throws IOException {
        List<CustomerDTO> customers = list(param).stream()
                .map(item -> getDetailById(item.getId()))
                .filter(Objects::nonNull)
                .toList();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("主表");
            writeHeader(sheet, MAIN_HEADERS);
            for (int i = 0; i < customers.size(); i++) {
                CustomerDTO item = customers.get(i);
                Row row = sheet.createRow(i + 1);
                writeCell(row, 0, item.getCustomerCode());
                writeCell(row, 1, item.getCustomerFullName());
                writeCell(row, 2, item.getCustomerShortName());
                writeCell(row, 3, item.getCustomerName());
                writeCell(row, 4, item.getCustomerValueLevel());
                writeCell(row, 5, item.getCustomerCreditLevel());
                writeCell(row, 6, item.getBusinessStatus());
                writeCell(row, 7, item.getActualBusinessAddress());
                writeCell(row, 8, item.getCollectionAddress());
                writeCell(row, 9, item.getShippingAddress());
                writeCell(row, 10, item.getApprovalStatus());
                writeCell(row, 11, item.getIsRelatedTenant());
                writeCell(row, 12, item.getRelatedTenantCode());
                writeCell(row, 13, item.getTransportMode());
                writeCell(row, 14, item.getPaymentTerms());
                writeCell(row, 15, item.getCountry());
                writeCell(row, 16, item.getEnterpriseNature());
                writeCell(row, 17, item.getStatus());
                writeCell(row, 18, item.getRemark());
            }
            workbook.write(outputStream);
        }
    }

    private LambdaQueryWrapper<BasicCustomer> buildPageWrapper(CustomerPageParam param) {
        return new LambdaQueryWrapper<BasicCustomer>()
                .eq(BasicCustomer::getDeleted, false)
                .like(StringUtils.hasText(param.getCustomerCode()), BasicCustomer::getCustomerCode, param.getCustomerCode())
                .like(StringUtils.hasText(param.getCustomerName()), BasicCustomer::getCustomerName, param.getCustomerName())
                .like(StringUtils.hasText(param.getCustomerFullName()), BasicCustomer::getCustomerFullName, param.getCustomerFullName())
                .eq(StringUtils.hasText(param.getCustomerValueLevel()), BasicCustomer::getCustomerValueLevel, param.getCustomerValueLevel())
                .eq(StringUtils.hasText(param.getCustomerCreditLevel()), BasicCustomer::getCustomerCreditLevel, param.getCustomerCreditLevel())
                .eq(StringUtils.hasText(param.getBusinessStatus()), BasicCustomer::getBusinessStatus, param.getBusinessStatus())
                .eq(param.getApprovalStatus() != null, BasicCustomer::getApprovalStatus, param.getApprovalStatus())
                .eq(param.getIsRelatedTenant() != null, BasicCustomer::getIsRelatedTenant, param.getIsRelatedTenant())
                .eq(param.getStatus() != null, BasicCustomer::getStatus, param.getStatus())
                .orderByDesc(BasicCustomer::getCreateTime);
    }

    private void validateSaveParam(CustomerSaveParam param, boolean create) {
        if (param == null) {
            throw basicException(BasicPromptEnum.CUSTOMER_PARAM_REQUIRED);
        }
        if (!create && param.getId() == null) {
            throw basicException(BasicPromptEnum.CUSTOMER_ID_REQUIRED);
        }
        if (!Boolean.TRUE.equals(param.getAutoGenerateCode()) && !StringUtils.hasText(param.getCustomerCode())) {
            throw basicException(BasicPromptEnum.CUSTOMER_CODE_REQUIRED);
        }
        if (!StringUtils.hasText(param.getCustomerFullName()) && !StringUtils.hasText(param.getCustomerName())) {
            throw basicException(BasicPromptEnum.CUSTOMER_FULL_NAME_REQUIRED);
        }
    }

    private void fillCustomer(BasicCustomer customer, CustomerSaveParam param) {
        customer.setCustomerCode(normalizeCode(param.getCustomerCode()));
        customer.setCustomerShortName(trimToNull(param.getCustomerShortName()));
        customer.setCustomerFullName(trimToNull(param.getCustomerFullName()));
        customer.setCustomerName(trimToNull(param.getCustomerName()));
        customer.setCustomerValueLevel(trimToNull(param.getCustomerValueLevel()));
        customer.setCustomerCreditLevel(trimToNull(param.getCustomerCreditLevel()));
        customer.setActualBusinessAddress(trimToNull(param.getActualBusinessAddress()));
        customer.setBusinessStatus(trimToNull(param.getBusinessStatus()));
        customer.setCollectionAddress(trimToNull(param.getCollectionAddress()));
        customer.setShippingAddress(trimToNull(param.getShippingAddress()));
        customer.setApprovalStatus(param.getApprovalStatus());
        customer.setIsRelatedTenant(Boolean.TRUE.equals(param.getIsRelatedTenant()));
        customer.setRelatedTenantCode(trimToNull(param.getRelatedTenantCode()));
        customer.setTransportMode(trimToNull(param.getTransportMode()));
        customer.setPaymentTerms(trimToNull(param.getPaymentTerms()));
        customer.setCountry(trimToNull(param.getCountry()));
        customer.setEnterpriseNature(trimToNull(param.getEnterpriseNature()));
        customer.setStatus(param.getStatus() == null ? 1 : param.getStatus());
        customer.setRemark(trimToNull(param.getRemark()));
    }

    private void saveChildren(Long customerId, CustomerSaveParam param) {
        deleteChildren(customerId);
        if (!CollectionUtils.isEmpty(param.getContactList())) {
            for (CustomerContactDTO dto : param.getContactList()) {
                BasicCustomerContact contact = new BasicCustomerContact();
                BeanUtils.copyProperties(dto, contact);
                contact.setId(null);
                contact.setCustomerId(customerId);
                contact.setTenantId(PUBLIC_TENANT_ID);
                contactMapper.insert(contact);
            }
        }
        if (param.getInvoice() != null) {
            BasicCustomerInvoice invoice = new BasicCustomerInvoice();
            BeanUtils.copyProperties(param.getInvoice(), invoice);
            invoice.setId(null);
            invoice.setCustomerId(customerId);
            invoice.setTenantId(PUBLIC_TENANT_ID);
            invoiceMapper.insert(invoice);
        }
        if (param.getExtra() != null) {
            BasicCustomerExtra extra = new BasicCustomerExtra();
            BeanUtils.copyProperties(param.getExtra(), extra);
            extra.setId(null);
            extra.setCustomerId(customerId);
            extra.setTenantId(PUBLIC_TENANT_ID);
            extraMapper.insert(extra);
        }
    }

    private void deleteChildren(Long customerId) {
        contactMapper.delete(new LambdaQueryWrapper<BasicCustomerContact>().eq(BasicCustomerContact::getCustomerId, customerId));
        invoiceMapper.delete(new LambdaQueryWrapper<BasicCustomerInvoice>().eq(BasicCustomerInvoice::getCustomerId, customerId));
        extraMapper.delete(new LambdaQueryWrapper<BasicCustomerExtra>().eq(BasicCustomerExtra::getCustomerId, customerId));
    }

    private CustomerDTO convertToDTO(BasicCustomer customer, boolean detail) {
        CustomerDTO dto = new CustomerDTO();
        BeanUtils.copyProperties(customer, dto);
        dto.setHasRelatedTenant(Boolean.TRUE.equals(customer.getIsRelatedTenant()) || StringUtils.hasText(customer.getRelatedTenantCode()));
        if (detail) {
            dto.setContactList(queryContacts(customer.getId()));
            dto.setInvoice(queryInvoice(customer.getId()));
            dto.setExtra(queryExtra(customer.getId()));
        }
        return dto;
    }

    private CustomerAggregateDTO convertToAggregate(BasicCustomer customer, boolean detail) {
        CustomerAggregateDTO dto = new CustomerAggregateDTO();
        BeanUtils.copyProperties(customer, dto);
        dto.setHasRelatedTenant(Boolean.TRUE.equals(customer.getIsRelatedTenant()) || StringUtils.hasText(customer.getRelatedTenantCode()));
        if (detail) {
            dto.setContactList(queryContacts(customer.getId()).stream().map(this::convertContact).toList());
            CustomerInvoiceDTO invoice = queryInvoice(customer.getId());
            if (invoice != null) {
                CustomerInvoiceSyncDTO invoiceSync = new CustomerInvoiceSyncDTO();
                BeanUtils.copyProperties(invoice, invoiceSync);
                dto.setInvoice(invoiceSync);
            }
            CustomerExtraDTO extra = queryExtra(customer.getId());
            if (extra != null) {
                CustomerExtraSyncDTO extraSync = new CustomerExtraSyncDTO();
                BeanUtils.copyProperties(extra, extraSync);
                dto.setExtra(extraSync);
            }
        }
        return dto;
    }

    private CustomerContactSyncDTO convertContact(CustomerContactDTO source) {
        CustomerContactSyncDTO target = new CustomerContactSyncDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    private List<CustomerContactDTO> queryContacts(Long customerId) {
        return contactMapper.selectList(new LambdaQueryWrapper<BasicCustomerContact>()
                        .eq(BasicCustomerContact::getCustomerId, customerId)
                        .eq(BasicCustomerContact::getDeleted, false))
                .stream().map(item -> {
                    CustomerContactDTO dto = new CustomerContactDTO();
                    BeanUtils.copyProperties(item, dto);
                    return dto;
                }).toList();
    }

    private CustomerInvoiceDTO queryInvoice(Long customerId) {
        BasicCustomerInvoice invoice = invoiceMapper.selectOne(new LambdaQueryWrapper<BasicCustomerInvoice>()
                .eq(BasicCustomerInvoice::getCustomerId, customerId)
                .eq(BasicCustomerInvoice::getDeleted, false)
                .last("LIMIT 1"));
        if (invoice == null) {
            return null;
        }
        CustomerInvoiceDTO dto = new CustomerInvoiceDTO();
        BeanUtils.copyProperties(invoice, dto);
        return dto;
    }

    private CustomerExtraDTO queryExtra(Long customerId) {
        BasicCustomerExtra extra = extraMapper.selectOne(new LambdaQueryWrapper<BasicCustomerExtra>()
                .eq(BasicCustomerExtra::getCustomerId, customerId)
                .eq(BasicCustomerExtra::getDeleted, false)
                .last("LIMIT 1"));
        if (extra == null) {
            return null;
        }
        CustomerExtraDTO dto = new CustomerExtraDTO();
        BeanUtils.copyProperties(extra, dto);
        return dto;
    }

    private BasicCustomer requireCustomer(Long id) {
        if (id == null) {
            throw basicException(BasicPromptEnum.CUSTOMER_ID_REQUIRED);
        }
        BasicCustomer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw basicException(BasicPromptEnum.CUSTOMER_NOT_FOUND);
        }
        return customer;
    }

    private BasicCustomer findByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return customerMapper.selectOne(new LambdaQueryWrapper<BasicCustomer>()
                .eq(BasicCustomer::getCustomerCode, normalizeCode(code))
                .eq(BasicCustomer::getDeleted, false)
                .last("LIMIT 1"));
    }

    private BasicCustomer findByRelatedTenantCode(String tenantCode) {
        if (!StringUtils.hasText(tenantCode)) {
            return null;
        }
        return customerMapper.selectOne(new LambdaQueryWrapper<BasicCustomer>()
                .eq(BasicCustomer::getRelatedTenantCode, tenantCode.trim())
                .eq(BasicCustomer::getDeleted, false)
                .last("LIMIT 1"));
    }

    private boolean upsertAggregate(CustomerAggregateDTO aggregate) {
        BasicCustomer existing = findByCode(aggregate.getCustomerCode());
        saveAggregate(existing, aggregate);
        return existing == null;
    }

    private void handleCustomerAggregate(FxExcelImportMode mode, CustomerAggregateDTO aggregate, FxExcelImportResultDTO result) {
        BasicCustomer existing = findByCode(aggregate.getCustomerCode());
        if (existing == null) {
            if (mode == FxExcelImportMode.UPDATE) {
                result.increaseSkipped();
                return;
            }
            saveAggregate(null, aggregate);
            result.increaseCreated();
            return;
        }
        if (mode == FxExcelImportMode.ADD) {
            result.increaseSkipped();
            return;
        }
        saveAggregate(existing, aggregate);
        result.increaseUpdated();
    }

    private void saveAggregate(BasicCustomer existing, CustomerAggregateDTO aggregate) {
        BasicCustomer target = existing == null ? new BasicCustomer() : existing;
        fillCustomerFromAggregate(target, aggregate);
        target.setTenantId(PUBLIC_TENANT_ID);
        if (target.getStatus() == null) {
            target.setStatus(1);
        }
        if (target.getApprovalStatus() == null) {
            target.setApprovalStatus(APPROVAL_PENDING);
        }
        if (existing == null) {
            customerMapper.insert(target);
        } else {
            customerMapper.updateById(target);
        }
        saveAggregateChildren(target.getId(), aggregate);
    }

    private void fillCustomerFromAggregate(BasicCustomer customer, CustomerAggregateDTO aggregate) {
        customer.setCustomerCode(normalizeCode(aggregate.getCustomerCode()));
        customer.setCustomerShortName(trimToNull(aggregate.getCustomerShortName()));
        customer.setCustomerFullName(trimToNull(aggregate.getCustomerFullName()));
        customer.setCustomerName(trimToNull(aggregate.getCustomerName()));
        customer.setCustomerValueLevel(trimToNull(aggregate.getCustomerValueLevel()));
        customer.setCustomerCreditLevel(trimToNull(aggregate.getCustomerCreditLevel()));
        customer.setActualBusinessAddress(trimToNull(aggregate.getActualBusinessAddress()));
        customer.setBusinessStatus(trimToNull(aggregate.getBusinessStatus()));
        customer.setCollectionAddress(trimToNull(aggregate.getCollectionAddress()));
        customer.setShippingAddress(trimToNull(aggregate.getShippingAddress()));
        customer.setApprovalStatus(aggregate.getApprovalStatus());
        customer.setIsRelatedTenant(Boolean.TRUE.equals(aggregate.getIsRelatedTenant()) || StringUtils.hasText(aggregate.getRelatedTenantCode()));
        customer.setRelatedTenantCode(trimToNull(aggregate.getRelatedTenantCode()));
        customer.setTransportMode(trimToNull(aggregate.getTransportMode()));
        customer.setPaymentTerms(trimToNull(aggregate.getPaymentTerms()));
        customer.setCountry(trimToNull(aggregate.getCountry()));
        customer.setEnterpriseNature(trimToNull(aggregate.getEnterpriseNature()));
        customer.setStatus(aggregate.getStatus());
        customer.setRemark(trimToNull(aggregate.getRemark()));
    }

    private void saveAggregateChildren(Long customerId, CustomerAggregateDTO aggregate) {
        deleteChildren(customerId);
        if (!CollectionUtils.isEmpty(aggregate.getContactList())) {
            for (CustomerContactSyncDTO dto : aggregate.getContactList()) {
                BasicCustomerContact contact = new BasicCustomerContact();
                BeanUtils.copyProperties(dto, contact);
                contact.setId(null);
                contact.setCustomerId(customerId);
                contact.setTenantId(PUBLIC_TENANT_ID);
                contactMapper.insert(contact);
            }
        }
        if (aggregate.getInvoice() != null) {
            BasicCustomerInvoice invoice = new BasicCustomerInvoice();
            BeanUtils.copyProperties(aggregate.getInvoice(), invoice);
            invoice.setId(null);
            invoice.setCustomerId(customerId);
            invoice.setTenantId(PUBLIC_TENANT_ID);
            invoiceMapper.insert(invoice);
        }
        if (aggregate.getExtra() != null) {
            BasicCustomerExtra extra = new BasicCustomerExtra();
            BeanUtils.copyProperties(aggregate.getExtra(), extra);
            extra.setId(null);
            extra.setCustomerId(customerId);
            extra.setTenantId(PUBLIC_TENANT_ID);
            extraMapper.insert(extra);
        }
    }

    private void coverCustomers() {
        List<Long> customerIds = customerMapper.selectList(new LambdaQueryWrapper<BasicCustomer>()
                        .select(BasicCustomer::getId)
                        .eq(BasicCustomer::getDeleted, false))
                .stream()
                .map(BasicCustomer::getId)
                .filter(Objects::nonNull)
                .toList();
        if (customerIds.isEmpty()) {
            return;
        }
        contactMapper.delete(new LambdaQueryWrapper<BasicCustomerContact>().in(BasicCustomerContact::getCustomerId, customerIds));
        invoiceMapper.delete(new LambdaQueryWrapper<BasicCustomerInvoice>().in(BasicCustomerInvoice::getCustomerId, customerIds));
        extraMapper.delete(new LambdaQueryWrapper<BasicCustomerExtra>().in(BasicCustomerExtra::getCustomerId, customerIds));
        customerMapper.delete(new LambdaQueryWrapper<BasicCustomer>().in(BasicCustomer::getId, customerIds));
    }

    private void validateAggregate(CustomerAggregateDTO aggregate) {
        if (aggregate == null) {
            throw basicException(BasicPromptEnum.CUSTOMER_PARAM_REQUIRED);
        }
        if (!StringUtils.hasText(normalizeCode(aggregate.getCustomerCode()))) {
            throw basicException(BasicPromptEnum.CUSTOMER_CODE_REQUIRED);
        }
        if (!StringUtils.hasText(aggregate.getCustomerFullName()) && !StringUtils.hasText(aggregate.getCustomerName())) {
            throw basicException(BasicPromptEnum.CUSTOMER_FULL_NAME_REQUIRED);
        }
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

    private String buildTenantCode(String customerCode) {
        String code = normalizeCode(customerCode).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        return "cus_" + code;
    }

    private String buildApprovalFormContent(BasicCustomer customer) {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("customerId", customer.getId());
        form.put("customerCode", customer.getCustomerCode());
        form.put("customerName", customerName(customer));
        form.put("currentApprovalStatus", customer.getApprovalStatus());
        try {
            return objectMapper.writeValueAsString(form);
        } catch (Exception ex) {
            throw basicException(BasicPromptEnum.CUSTOMER_APPROVAL_FORM_SERIALIZE_FAILED);
        }
    }

    private Long resolveCustomerIdFromFormContent(String formContent) {
        if (!StringUtils.hasText(formContent)) {
            throw basicException(BasicPromptEnum.CUSTOMER_APPROVAL_CALLBACK_CUSTOMER_ID_MISSING);
        }
        try {
            Map<String, Object> form = objectMapper.readValue(formContent, new TypeReference<Map<String, Object>>() {});
            Object value = form.get("customerId");
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value instanceof String str && StringUtils.hasText(str)) {
                return Long.parseLong(str);
            }
        } catch (Exception ex) {
            throw basicException(BasicPromptEnum.CUSTOMER_APPROVAL_CALLBACK_FORM_PARSE_FAILED);
        }
        throw basicException(BasicPromptEnum.CUSTOMER_APPROVAL_CALLBACK_CUSTOMER_ID_MISSING);
    }

    private String customerName(BasicCustomer customer) {
        if (StringUtils.hasText(customer.getCustomerFullName())) {
            return customer.getCustomerFullName();
        }
        if (StringUtils.hasText(customer.getCustomerName())) {
            return customer.getCustomerName();
        }
        return customer.getCustomerCode();
    }

    private String normalizeCode(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private List<CustomerAggregateDTO> readCommonImportData(FxExcelImportExecuteParam param) {
        if (param == null || param.getImportData() == null) {
            return Collections.emptyList();
        }
        Map<String, CustomerAggregateDTO> aggregateMap = new LinkedHashMap<>();
        Set<String> mainCodes = readCustomerMainImportData(param.getImportData().get("main"), aggregateMap);
        readCustomerContactImportData(param.getImportData().get("contact"), aggregateMap, mainCodes);
        readCustomerInvoiceImportData(param.getImportData().get("invoice"), aggregateMap, mainCodes);
        readCustomerExtraImportData(param.getImportData().get("extra"), aggregateMap, mainCodes);
        return new ArrayList<>(aggregateMap.values());
    }

    private Set<String> readCustomerMainImportData(List<Map<String, Object>> rows, Map<String, CustomerAggregateDTO> aggregateMap) {
        Set<String> mainCodes = new java.util.HashSet<>();
        if (CollectionUtils.isEmpty(rows)) {
            return mainCodes;
        }
        for (Map<String, Object> row : rows) {
            String customerCode = normalizeCode(value(row, "customerCode"));
            if (!StringUtils.hasText(customerCode)) {
                continue;
            }
            CustomerAggregateDTO aggregate = aggregateMap.computeIfAbsent(customerCode, code -> {
                CustomerAggregateDTO item = new CustomerAggregateDTO();
                item.setCustomerCode(code);
                return item;
            });
            mainCodes.add(customerCode);
            aggregate.setCustomerFullName(trimToNull(value(row, "customerFullName")));
            aggregate.setCustomerShortName(trimToNull(value(row, "customerShortName")));
            aggregate.setCustomerName(trimToNull(value(row, "customerName")));
            aggregate.setCustomerValueLevel(trimToNull(value(row, "customerValueLevel")));
            aggregate.setCustomerCreditLevel(trimToNull(value(row, "customerCreditLevel")));
            aggregate.setBusinessStatus(trimToNull(value(row, "businessStatus")));
            aggregate.setActualBusinessAddress(trimToNull(value(row, "actualBusinessAddress")));
            aggregate.setCollectionAddress(trimToNull(value(row, "collectionAddress")));
            aggregate.setShippingAddress(trimToNull(value(row, "shippingAddress")));
            aggregate.setApprovalStatus(parseInteger(value(row, "approvalStatus")));
            aggregate.setIsRelatedTenant(parseBoolean(value(row, "isRelatedTenant")));
            aggregate.setRelatedTenantCode(trimToNull(value(row, "relatedTenantCode")));
            aggregate.setTransportMode(trimToNull(value(row, "transportMode")));
            aggregate.setPaymentTerms(trimToNull(value(row, "paymentTerms")));
            aggregate.setCountry(trimToNull(value(row, "country")));
            aggregate.setEnterpriseNature(trimToNull(value(row, "enterpriseNature")));
            aggregate.setStatus(parseInteger(value(row, "status")));
            aggregate.setRemark(trimToNull(value(row, "remark")));
        }
        return mainCodes;
    }

    private void readCustomerContactImportData(List<Map<String, Object>> rows, Map<String, CustomerAggregateDTO> aggregateMap, Set<String> mainCodes) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        for (Map<String, Object> row : rows) {
            String customerCode = normalizeCode(value(row, "customerCode"));
            if (!StringUtils.hasText(customerCode)) {
                continue;
            }
            CustomerAggregateDTO aggregate = aggregateMap.computeIfAbsent(customerCode, code -> {
                CustomerAggregateDTO item = new CustomerAggregateDTO();
                item.setCustomerCode(code);
                return item;
            });
            mainCodes.add(customerCode);
            if (aggregate.getContactList() == null) {
                aggregate.setContactList(new ArrayList<>());
            }
            CustomerContactSyncDTO contact = new CustomerContactSyncDTO();
            contact.setContactName(trimToNull(value(row, "contactName")));
            contact.setContactPosition(trimToNull(value(row, "contactPosition")));
            contact.setContactPhone(trimToNull(value(row, "contactPhone")));
            aggregate.getContactList().add(contact);
        }
    }

    private void readCustomerInvoiceImportData(List<Map<String, Object>> rows, Map<String, CustomerAggregateDTO> aggregateMap, Set<String> mainCodes) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        for (Map<String, Object> row : rows) {
            String customerCode = normalizeCode(value(row, "customerCode"));
            if (!StringUtils.hasText(customerCode)) {
                continue;
            }
            CustomerAggregateDTO aggregate = aggregateMap.computeIfAbsent(customerCode, code -> {
                CustomerAggregateDTO item = new CustomerAggregateDTO();
                item.setCustomerCode(code);
                return item;
            });
            mainCodes.add(customerCode);
            CustomerInvoiceSyncDTO invoice = new CustomerInvoiceSyncDTO();
            invoice.setInvoiceFullName(trimToNull(value(row, "invoiceFullName")));
            invoice.setTaxNumber(trimToNull(value(row, "taxNumber")));
            invoice.setRegisteredAddress(trimToNull(value(row, "registeredAddress")));
            invoice.setRegisteredPhone(trimToNull(value(row, "registeredPhone")));
            invoice.setBankName(trimToNull(value(row, "bankName")));
            invoice.setBankAccount(trimToNull(value(row, "bankAccount")));
            invoice.setInvoiceRequired(parseBoolean(value(row, "invoiceRequired")));
            aggregate.setInvoice(invoice);
        }
    }

    private void readCustomerExtraImportData(List<Map<String, Object>> rows, Map<String, CustomerAggregateDTO> aggregateMap, Set<String> mainCodes) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        for (Map<String, Object> row : rows) {
            String customerCode = normalizeCode(value(row, "customerCode"));
            if (!StringUtils.hasText(customerCode)) {
                continue;
            }
            CustomerAggregateDTO aggregate = aggregateMap.computeIfAbsent(customerCode, code -> {
                CustomerAggregateDTO item = new CustomerAggregateDTO();
                item.setCustomerCode(code);
                return item;
            });
            mainCodes.add(customerCode);
            CustomerExtraSyncDTO extra = new CustomerExtraSyncDTO();
            extra.setOfficialWebsite(trimToNull(value(row, "officialWebsite")));
            extra.setSwitchboardPhone(trimToNull(value(row, "switchboardPhone")));
            extra.setOfficialEmailDomain(trimToNull(value(row, "officialEmailDomain")));
            extra.setFaxNumber(trimToNull(value(row, "faxNumber")));
            extra.setSocialMediaAccount(trimToNull(value(row, "socialMediaAccount")));
            extra.setEquityPenetrationLevel(parseInteger(value(row, "equityPenetrationLevel")));
            extra.setHoldingRelationFlag(trimToNull(value(row, "holdingRelationFlag")));
            extra.setRelatedEnterpriseIds(trimToNull(value(row, "relatedEnterpriseIds")));
            extra.setGroupCustomerLevel(trimToNull(value(row, "groupCustomerLevel")));
            extra.setChannelPartnerLevel(trimToNull(value(row, "channelPartnerLevel")));
            extra.setCooperationAuthStartDate(parseLocalDate(value(row, "cooperationAuthStartDate")));
            extra.setCooperationAuthEndDate(parseLocalDate(value(row, "cooperationAuthEndDate")));
            extra.setNationalIndustryCode(trimToNull(value(row, "nationalIndustryCode")));
            extra.setCustomIndustryCategory(trimToNull(value(row, "customIndustryCategory")));
            extra.setRegisteredCapital(parseBigDecimal(value(row, "registeredCapital")));
            extra.setRegisteredCapitalCurrency(trimToNull(value(row, "registeredCapitalCurrency")));
            extra.setPaidInCapital(parseBigDecimal(value(row, "paidInCapital")));
            extra.setPaidInCapitalCurrency(trimToNull(value(row, "paidInCapitalCurrency")));
            extra.setBusinessTermStart(parseLocalDate(value(row, "businessTermStart")));
            extra.setBusinessTermEnd(parseLocalDate(value(row, "businessTermEnd")));
            extra.setRegistrationAuthority(trimToNull(value(row, "registrationAuthority")));
            extra.setBusinessScope(trimToNull(value(row, "businessScope")));
            aggregate.setExtra(extra);
        }
    }

    private void createSheetWithHeader(Workbook workbook, String sheetName, List<String> headers) {
        Sheet sheet = workbook.createSheet(sheetName);
        writeHeader(sheet, headers);
    }

    private void writeHeader(Sheet sheet, List<String> headers) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            header.createCell(i).setCellValue(headers.get(i));
            sheet.setColumnWidth(i, 18 * 256);
        }
    }

    private List<CustomerAggregateDTO> readWorkbook(Workbook workbook) {
        Sheet sheet = workbook.getSheet("主表");
        if (sheet == null) {
            throw basicException(BasicPromptEnum.CUSTOMER_IMPORT_MAIN_SHEET_MISSING);
        }
        Map<String, Integer> header = readHeader(sheet.getRow(0));
        List<CustomerAggregateDTO> customers = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            String customerCode = normalizeCode(cell(row, header, "客户编码"));
            String customerFullName = trimToNull(cell(row, header, "客户全称"));
            String customerName = trimToNull(cell(row, header, "客户名称"));
            if (!StringUtils.hasText(customerCode) && !StringUtils.hasText(customerFullName) && !StringUtils.hasText(customerName)) {
                continue;
            }
            CustomerAggregateDTO dto = new CustomerAggregateDTO();
            dto.setCustomerCode(customerCode);
            dto.setCustomerFullName(customerFullName);
            dto.setCustomerShortName(trimToNull(cell(row, header, "客户简称")));
            dto.setCustomerName(customerName);
            dto.setCustomerValueLevel(trimToNull(cell(row, header, "客户价值等级")));
            dto.setCustomerCreditLevel(trimToNull(cell(row, header, "客户信用等级")));
            dto.setBusinessStatus(trimToNull(cell(row, header, "经营状态")));
            dto.setActualBusinessAddress(trimToNull(cell(row, header, "实际经营地址")));
            dto.setCollectionAddress(trimToNull(cell(row, header, "收款地址")));
            dto.setShippingAddress(trimToNull(cell(row, header, "发货地址")));
            dto.setApprovalStatus(parseInteger(cell(row, header, "审批状态")));
            dto.setIsRelatedTenant(parseBoolean(cell(row, header, "是否关联租户")));
            dto.setRelatedTenantCode(trimToNull(cell(row, header, "关联租户编码")));
            dto.setTransportMode(trimToNull(cell(row, header, "运输方式")));
            dto.setPaymentTerms(trimToNull(cell(row, header, "付款条款")));
            dto.setCountry(trimToNull(cell(row, header, "国家")));
            dto.setEnterpriseNature(trimToNull(cell(row, header, "企业性质")));
            dto.setStatus(parseInteger(cell(row, header, "状态")));
            dto.setRemark(trimToNull(cell(row, header, "备注")));
            validateAggregate(dto);
            customers.add(dto);
        }
        return customers;
    }

    private Map<String, Integer> readHeader(Row row) {
        Map<String, Integer> header = new HashMap<>();
        if (row == null) {
            return header;
        }
        for (Cell cell : row) {
            String value = cellValue(cell);
            if (StringUtils.hasText(value)) {
                header.put(value.trim(), cell.getColumnIndex());
            }
        }
        return header;
    }

    private String cell(Row row, Map<String, Integer> header, String name) {
        Integer index = header.get(name);
        if (index == null) {
            return null;
        }
        return cellValue(row.getCell(index));
    }

    private String cellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    private String value(Map<String, Object> row, String key) {
        if (row == null || key == null) {
            return null;
        }
        Object raw = row.get(key);
        return raw == null ? null : String.valueOf(raw).trim();
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim()).intValue();
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim().toLowerCase(Locale.ROOT);
        return "true".equals(text) || "1".equals(text) || "是".equals(text) || "yes".equals(text);
    }

    private LocalDate parseLocalDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private void writeCell(Row row, int index, Object value) {
        if (value == null) {
            row.createCell(index).setCellValue("");
            return;
        }
        if (value instanceof Number number) {
            row.createCell(index).setCellValue(number.doubleValue());
            return;
        }
        if (value instanceof Boolean bool) {
            row.createCell(index).setCellValue(bool);
            return;
        }
        row.createCell(index).setCellValue(String.valueOf(value));
    }

    private I18nBusinessException basicException(BasicPromptEnum prompt, Object... args) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt, args);
    }
}
