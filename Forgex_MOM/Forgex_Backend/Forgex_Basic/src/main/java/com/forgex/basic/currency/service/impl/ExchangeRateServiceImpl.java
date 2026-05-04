package com.forgex.basic.currency.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.basic.currency.domain.entity.MdmCurrency;
import com.forgex.basic.currency.domain.entity.MdmCurrencyExchangeRate;
import com.forgex.basic.currency.domain.entity.MdmExchangeRateLog;
import com.forgex.basic.currency.domain.entity.MdmExchangeRateType;
import com.forgex.basic.currency.domain.param.ExchangeRateApprovalStartParam;
import com.forgex.basic.currency.domain.param.ExchangeRateCurrentParam;
import com.forgex.basic.currency.domain.param.ExchangeRatePageParam;
import com.forgex.basic.currency.domain.param.ExchangeRateWorkflowCallbackParam;
import com.forgex.basic.currency.mapper.MdmCurrencyExchangeRateMapper;
import com.forgex.basic.currency.mapper.MdmExchangeRateLogMapper;
import com.forgex.basic.currency.service.IExchangeRateService;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.common.api.dto.WorkflowExecutionStartRequestDTO;
import com.forgex.common.api.feign.WorkflowExecutionFeignClient;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 汇率服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl extends ServiceImpl<MdmCurrencyExchangeRateMapper, MdmCurrencyExchangeRate> implements IExchangeRateService {

    private static final Long PUBLIC_TENANT_ID = 0L;
    private static final LocalDate FOREVER = LocalDate.of(9999, 12, 31);
    private static final String RATE_APPROVAL_TASK_CODE = "CURRENCY_RATE_APPROVAL";
    private static final int APPROVE_PENDING = 0;
    private static final int APPROVE_EFFECTIVE = 1;
    private static final int APPROVE_REJECTED = 2;
    private static final int APPROVE_PROCESSING = 3;

    private final MdmCurrencyExchangeRateMapper exchangeRateMapper;
    private final MdmExchangeRateLogMapper logMapper;
    private final CurrencyServiceImpl currencyService;
    private final RateTypeServiceImpl rateTypeService;
    private final WorkflowExecutionFeignClient workflowExecutionFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 分页查询数据。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<MdmCurrencyExchangeRate> page(ExchangeRatePageParam param) {
        ExchangeRatePageParam safeParam = param == null ? new ExchangeRatePageParam() : param;
        return exchangeRateMapper.selectPage(new Page<>(safeParam.getPageNum(), safeParam.getPageSize()), wrapper(safeParam));
    }

    /**
     * 查询数据详情。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @Override
    public MdmCurrencyExchangeRate detail(Long id) {
        return id == null ? null : exchangeRateMapper.selectById(id);
    }

    /**
     * 创建数据。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MdmCurrencyExchangeRate param) {
        validateRate(param);
        param.setSourceCurrencyCode(normalizeCode(param.getSourceCurrencyCode()));
        param.setTargetCurrencyCode(normalizeCode(param.getTargetCurrencyCode()));
        param.setRateTypeCode(normalizeCode(param.getRateTypeCode()));
        param.setTenantId(PUBLIC_TENANT_ID);
        param.setApproveStatus(param.getApproveStatus() == null ? APPROVE_PENDING : param.getApproveStatus());
        param.setExpireDate(param.getExpireDate() == null ? FOREVER : param.getExpireDate());
        ensureNoOverlap(param, null);
        exchangeRateMapper.insert(param);
        writeLog(param.getId(), "CREATE", "新增汇率");
        return param.getId();
    }

    /**
     * 更新数据。
     *
     * @param param 请求参数
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(MdmCurrencyExchangeRate param) {
        MdmCurrencyExchangeRate exists = requireRate(param.getId());
        validateRate(param);
        BeanUtils.copyProperties(param, exists, "id", "createTime", "createBy", "deleted");
        exists.setSourceCurrencyCode(normalizeCode(param.getSourceCurrencyCode()));
        exists.setTargetCurrencyCode(normalizeCode(param.getTargetCurrencyCode()));
        exists.setRateTypeCode(normalizeCode(param.getRateTypeCode()));
        exists.setTenantId(PUBLIC_TENANT_ID);
        exists.setExpireDate(param.getExpireDate() == null ? FOREVER : param.getExpireDate());
        ensureNoOverlap(exists, exists.getId());
        exchangeRateMapper.updateById(exists);
        writeLog(exists.getId(), "UPDATE", "修改汇率");
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
        requireRate(id);
        exchangeRateMapper.deleteById(id);
        writeLog(id, "DELETE", "删除汇率");
        return true;
    }

    /**
     * 发起审批流程。
     *
     * @param param 请求参数
     * @return 数据主键 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startApproval(ExchangeRateApprovalStartParam param) {
        if (param == null || param.getRateId() == null) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_NOT_FOUND);
        }
        MdmCurrencyExchangeRate rate = requireRate(param.getRateId());
        if (Objects.equals(rate.getApproveStatus(), APPROVE_PROCESSING)) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_APPROVAL_PROCESSING_FORBIDDEN);
        }
        WorkflowExecutionStartRequestDTO request = new WorkflowExecutionStartRequestDTO();
        request.setTaskCode(RATE_APPROVAL_TASK_CODE);
        request.setSelectedApprovers(param.getSelectedApprovers());
        request.setFormContent(buildApprovalFormContent(rate));
        R<Long> response = workflowExecutionFeignClient.startExecution(request);
        if (response == null || response.getCode() == null || response.getCode() != StatusCode.SUCCESS || response.getData() == null) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_APPROVAL_START_FAILED);
        }
        rate.setApproveStatus(APPROVE_PROCESSING);
        exchangeRateMapper.updateById(rate);
        writeLog(rate.getId(), "SUBMIT", "发起汇率审批");
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
    public Boolean handleWorkflowCallback(ExchangeRateWorkflowCallbackParam param) {
        if (param == null || !RATE_APPROVAL_TASK_CODE.equals(param.getTaskCode())) {
            return true;
        }
        Long rateId = resolveRateIdFromFormContent(param.getFormContent());
        MdmCurrencyExchangeRate rate = requireRate(rateId);
        if (Objects.equals(param.getStatus(), 2)) {
            activateRate(rate);
        } else {
            rate.setApproveStatus(APPROVE_REJECTED);
            exchangeRateMapper.updateById(rate);
            writeLog(rate.getId(), "REJECT", "汇率审批驳回");
        }
        return true;
    }

    /**
     * 查询当前有效汇率。
     *
     * @param param 请求参数
     * @return 处理结果
     */
    @Override
    public MdmCurrencyExchangeRate current(ExchangeRateCurrentParam param) {
        if (param == null
                || !StringUtils.hasText(param.getSourceCurrencyCode())
                || !StringUtils.hasText(param.getTargetCurrencyCode())
                || !StringUtils.hasText(param.getRateTypeCode())) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_NOT_FOUND);
        }
        LocalDate rateDate = param.getRateDate() == null ? LocalDate.now() : param.getRateDate();
        return exchangeRateMapper.selectOne(new LambdaQueryWrapper<MdmCurrencyExchangeRate>()
                .eq(MdmCurrencyExchangeRate::getSourceCurrencyCode, normalizeCode(param.getSourceCurrencyCode()))
                .eq(MdmCurrencyExchangeRate::getTargetCurrencyCode, normalizeCode(param.getTargetCurrencyCode()))
                .eq(MdmCurrencyExchangeRate::getRateTypeCode, normalizeCode(param.getRateTypeCode()))
                .eq(param.getOrgId() != null, MdmCurrencyExchangeRate::getOrgId, param.getOrgId())
                .isNull(param.getOrgId() == null, MdmCurrencyExchangeRate::getOrgId)
                .eq(MdmCurrencyExchangeRate::getApproveStatus, APPROVE_EFFECTIVE)
                .eq(MdmCurrencyExchangeRate::getDeleted, false)
                .le(MdmCurrencyExchangeRate::getEffectiveDate, rateDate)
                .ge(MdmCurrencyExchangeRate::getExpireDate, rateDate)
                .orderByDesc(MdmCurrencyExchangeRate::getEffectiveDate)
                .last("LIMIT 1"));
    }

    /**
     * 分页查询汇率变更日志。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<MdmExchangeRateLog> logPage(ExchangeRatePageParam param) {
        ExchangeRatePageParam safeParam = param == null ? new ExchangeRatePageParam() : param;
        LambdaQueryWrapper<MdmExchangeRateLog> wrapper = new LambdaQueryWrapper<MdmExchangeRateLog>()
                .eq(MdmExchangeRateLog::getDeleted, false)
                .orderByDesc(MdmExchangeRateLog::getCreateTime);
        return logMapper.selectPage(new Page<>(safeParam.getPageNum(), safeParam.getPageSize()), wrapper);
    }

    private void activateRate(MdmCurrencyExchangeRate rate) {
        List<MdmCurrencyExchangeRate> activeRates = exchangeRateMapper.selectList(dimensionWrapper(rate)
                .eq(MdmCurrencyExchangeRate::getApproveStatus, APPROVE_EFFECTIVE)
                .eq(MdmCurrencyExchangeRate::getDeleted, false)
                .lt(MdmCurrencyExchangeRate::getEffectiveDate, rate.getEffectiveDate())
                .ge(MdmCurrencyExchangeRate::getExpireDate, rate.getEffectiveDate()));
        for (MdmCurrencyExchangeRate active : activeRates) {
            active.setExpireDate(rate.getEffectiveDate().minusDays(1));
            exchangeRateMapper.updateById(active);
            writeLog(active.getId(), "EXPIRE", "新汇率生效，旧汇率自动失效");
        }
        rate.setApproveStatus(APPROVE_EFFECTIVE);
        rate.setApproveTime(LocalDateTime.now());
        rate.setExpireDate(rate.getExpireDate() == null ? FOREVER : rate.getExpireDate());
        exchangeRateMapper.updateById(rate);
        writeLog(rate.getId(), "APPROVE", "汇率审批通过并生效");
    }

    private void validateRate(MdmCurrencyExchangeRate param) {
        if (param == null) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_NOT_FOUND);
        }
        String source = normalizeCode(param.getSourceCurrencyCode());
        String target = normalizeCode(param.getTargetCurrencyCode());
        if (Objects.equals(source, target)) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_PAIR_INVALID);
        }
        MdmCurrency sourceCurrency = currencyService.requireCurrencyByCode(source);
        MdmCurrency targetCurrency = currencyService.requireCurrencyByCode(target);
        if (!Objects.equals(sourceCurrency.getStatus(), 1) || !Objects.equals(targetCurrency.getStatus(), 1)) {
            throw ex(BasicPromptEnum.CURRENCY_DISABLED_FOR_RATE);
        }
        MdmExchangeRateType rateType = rateTypeService.requireRateTypeByCode(param.getRateTypeCode());
        if (!Objects.equals(rateType.getStatus(), 1)) {
            throw ex(BasicPromptEnum.RATE_TYPE_NOT_FOUND);
        }
        if (param.getEffectiveDate() == null || param.getExpireDate() != null && param.getExpireDate().isBefore(param.getEffectiveDate())) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_DATE_INVALID);
        }
    }

    private void ensureNoOverlap(MdmCurrencyExchangeRate rate, Long excludeId) {
        List<MdmCurrencyExchangeRate> overlaps = exchangeRateMapper.selectList(dimensionWrapper(rate)
                .eq(MdmCurrencyExchangeRate::getDeleted, false)
                .ne(excludeId != null, MdmCurrencyExchangeRate::getId, excludeId)
                .le(MdmCurrencyExchangeRate::getEffectiveDate, rate.getExpireDate())
                .ge(MdmCurrencyExchangeRate::getExpireDate, rate.getEffectiveDate()));
        if (!overlaps.isEmpty()) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_OVERLAP);
        }
    }

    private LambdaQueryWrapper<MdmCurrencyExchangeRate> dimensionWrapper(MdmCurrencyExchangeRate rate) {
        return new LambdaQueryWrapper<MdmCurrencyExchangeRate>()
                .eq(MdmCurrencyExchangeRate::getSourceCurrencyCode, normalizeCode(rate.getSourceCurrencyCode()))
                .eq(MdmCurrencyExchangeRate::getTargetCurrencyCode, normalizeCode(rate.getTargetCurrencyCode()))
                .eq(MdmCurrencyExchangeRate::getRateTypeCode, normalizeCode(rate.getRateTypeCode()))
                .eq(rate.getOrgId() != null, MdmCurrencyExchangeRate::getOrgId, rate.getOrgId())
                .isNull(rate.getOrgId() == null, MdmCurrencyExchangeRate::getOrgId);
    }

    private LambdaQueryWrapper<MdmCurrencyExchangeRate> wrapper(ExchangeRatePageParam param) {
        return new LambdaQueryWrapper<MdmCurrencyExchangeRate>()
                .eq(MdmCurrencyExchangeRate::getDeleted, false)
                .eq(StringUtils.hasText(param.getSourceCurrencyCode()), MdmCurrencyExchangeRate::getSourceCurrencyCode, normalizeCode(param.getSourceCurrencyCode()))
                .eq(StringUtils.hasText(param.getTargetCurrencyCode()), MdmCurrencyExchangeRate::getTargetCurrencyCode, normalizeCode(param.getTargetCurrencyCode()))
                .eq(StringUtils.hasText(param.getRateTypeCode()), MdmCurrencyExchangeRate::getRateTypeCode, normalizeCode(param.getRateTypeCode()))
                .eq(param.getApproveStatus() != null, MdmCurrencyExchangeRate::getApproveStatus, param.getApproveStatus())
                .eq(param.getOrgId() != null, MdmCurrencyExchangeRate::getOrgId, param.getOrgId())
                .orderByDesc(MdmCurrencyExchangeRate::getEffectiveDate);
    }

    private MdmCurrencyExchangeRate requireRate(Long id) {
        MdmCurrencyExchangeRate rate = id == null ? null : exchangeRateMapper.selectById(id);
        if (rate == null) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_NOT_FOUND);
        }
        return rate;
    }

    private String buildApprovalFormContent(MdmCurrencyExchangeRate rate) {
        Map<String, Object> form = new LinkedHashMap<>();
        form.put("rateId", rate.getId());
        form.put("sourceCurrencyCode", rate.getSourceCurrencyCode());
        form.put("targetCurrencyCode", rate.getTargetCurrencyCode());
        form.put("rateTypeCode", rate.getRateTypeCode());
        form.put("effectiveDate", rate.getEffectiveDate());
        try {
            return objectMapper.writeValueAsString(form);
        } catch (Exception ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.EXCHANGE_RATE_CALLBACK_FORM_PARSE_FAILED);
        }
    }

    private Long resolveRateIdFromFormContent(String formContent) {
        if (!StringUtils.hasText(formContent)) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_CALLBACK_RATE_ID_MISSING);
        }
        try {
            Map<String, Object> form = objectMapper.readValue(formContent, new TypeReference<Map<String, Object>>() {});
            Object value = form.get("rateId");
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value instanceof String str && StringUtils.hasText(str)) {
                return Long.parseLong(str);
            }
        } catch (Exception ex) {
            throw ex(BasicPromptEnum.EXCHANGE_RATE_CALLBACK_FORM_PARSE_FAILED);
        }
        throw ex(BasicPromptEnum.EXCHANGE_RATE_CALLBACK_RATE_ID_MISSING);
    }

    private void writeLog(Long rateId, String type, String content) {
        MdmExchangeRateLog log = new MdmExchangeRateLog();
        log.setRateId(rateId);
        log.setOperationType(type);
        log.setOperationContent(content);
        log.setTenantId(PUBLIC_TENANT_ID);
        logMapper.insert(log);
    }

    private String normalizeCode(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : null;
    }

    private I18nBusinessException ex(BasicPromptEnum prompt) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt);
    }
}
