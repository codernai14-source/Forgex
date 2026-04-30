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
import com.forgex.common.api.dto.SysTenantCreateRequestDTO;
import com.forgex.common.api.dto.SysTenantSimpleDTO;
import com.forgex.common.api.dto.WorkflowExecutionStartRequestDTO;
import com.forgex.common.api.feign.SysTenantFeignClient;
import com.forgex.common.api.feign.WorkflowExecutionFeignClient;
import com.forgex.common.enums.TenantTypeEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.service.EncodeRuleService;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

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
    private static final int APPROVAL_PENDING = 0;
    private static final int APPROVAL_PROCESSING = 1;
    private static final int APPROVAL_APPROVED = 2;
    private static final int APPROVAL_REJECTED = 3;

    private final BasicCustomerMapper customerMapper;
    private final BasicCustomerContactMapper contactMapper;
    private final BasicCustomerInvoiceMapper invoiceMapper;
    private final BasicCustomerExtraMapper extraMapper;
    private final SysTenantFeignClient sysTenantFeignClient;
    private final WorkflowExecutionFeignClient workflowExecutionFeignClient;
    private final EncodeRuleService encodeRuleService;
    private final ObjectMapper objectMapper;

    @Override
    public Page<CustomerDTO> page(CustomerPageParam param) {
        CustomerPageParam safeParam = param == null ? new CustomerPageParam() : param;
        Page<BasicCustomer> entityPage = new Page<>(safeParam.getPageNum(), safeParam.getPageSize());
        Page<BasicCustomer> customerPage = customerMapper.selectPage(entityPage, buildPageWrapper(safeParam));
        Page<CustomerDTO> dtoPage = new Page<>(customerPage.getCurrent(), customerPage.getSize(), customerPage.getTotal());
        dtoPage.setRecords(customerPage.getRecords().stream().map(item -> convertToDTO(item, false)).toList());
        return dtoPage;
    }

    @Override
    public List<CustomerDTO> list(CustomerPageParam param) {
        CustomerPageParam safeParam = param == null ? new CustomerPageParam() : param;
        return customerMapper.selectList(buildPageWrapper(safeParam)).stream()
                .map(item -> convertToDTO(item, false))
                .toList();
    }

    @Override
    public CustomerDTO getDetailById(Long id) {
        if (id == null) {
            return null;
        }
        BasicCustomer customer = customerMapper.selectById(id);
        return customer == null ? null : convertToDTO(customer, true);
    }

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

    private I18nBusinessException basicException(BasicPromptEnum prompt, Object... args) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt, args);
    }
}
