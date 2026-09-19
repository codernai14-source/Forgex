package com.forgex.basic.label.service.impl;

import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.label.domain.entity.LabelTemplate;
import com.forgex.basic.label.domain.param.LabelPrintExecuteParam;
import com.forgex.basic.label.domain.param.LabelPrintRenderParam;
import com.forgex.basic.label.domain.vo.LabelRenderVO;
import com.forgex.basic.label.handler.DataAssemblyHandler;
import com.forgex.basic.label.handler.PlaceholderHandler;
import com.forgex.basic.label.handler.PrintSnapshotHandler;
import com.forgex.basic.label.mapper.LabelTemplateMapper;
import com.forgex.basic.label.service.LabelBindingService;
import com.forgex.basic.label.service.LabelPrintService;
import com.forgex.basic.label.service.LabelTemplateService;
import com.forgex.basic.label.utils.PlaceholderExtractor;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelPrintServiceImpl implements LabelPrintService {

    private final LabelTemplateMapper labelTemplateMapper;
    private final LabelBindingService labelBindingService;
    private final LabelTemplateService labelTemplateService;
    private final DataAssemblyHandler dataAssemblyHandler;
    private final PlaceholderHandler placeholderHandler;
    private final PrintSnapshotHandler snapshotHandler;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> executePrint(LabelPrintExecuteParam param, Long userId, Long tenantId) {
        LabelTemplate template = getTemplate(param, tenantId);
        validatePrintCount(param.getPrintCount());
        Map<String, Object> completeData = dataAssemblyHandler.assemblePrintData(param.getPrintData());
        validateDataCompleteness(template.getTemplateContent(), completeData);
        List<String> printResults = generateLabelContents(template.getTemplateContent(), completeData, param.getPrintCount());
        snapshotHandler.createSnapshot(
                template.getId(),
                template.getTemplateName(),
                template.getTemplateType(),
                completeData,
                printResults,
                param.getFactoryId(),
                userId,
                tenantId
        );
        return printResults;
    }

    @Override
    public List<String> previewPrint(LabelPrintExecuteParam param, Long tenantId) {
        LabelTemplate template = getTemplate(param, tenantId);
        if (param.getPrintCount() != null && param.getPrintCount() > 10) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_PREVIEW_LIMIT_EXCEEDED);
        }
        Map<String, Object> completeData = dataAssemblyHandler.assemblePrintData(param.getPrintData());
        validateDataCompleteness(template.getTemplateContent(), completeData);
        return generateLabelContents(template.getTemplateContent(), completeData, param.getPrintCount());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> reprintLabel(Long recordId, Integer reprintCount, Long userId, Long tenantId) {
        List<String> printResults = snapshotHandler.restorePrintResults(recordId, tenantId);
        snapshotHandler.createReprintSnapshot(recordId, reprintCount, userId, tenantId);
        return printResults;
    }

    @Override
    public LabelRenderVO render(LabelPrintRenderParam param, Long tenantId) {
        return labelTemplateService.render(param, tenantId);
    }

    private LabelTemplate getTemplate(LabelPrintExecuteParam param, Long tenantId) {
        LabelTemplate template;
        if (param.getTemplateId() != null) {
            template = labelTemplateMapper.selectById(param.getTemplateId());
            if (template == null || !template.getTenantId().equals(tenantId)) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_NOT_FOUND);
            }
        } else {
            Long templateId = labelBindingService.matchTemplate(
                    param.getFactoryId(),
                    param.getTemplateType(),
                    getLongValue(param.getPrintData(), "materialId"),
                    getLongValue(param.getPrintData(), "supplierId"),
                    getLongValue(param.getPrintData(), "customerId"),
                    tenantId
            );
            template = labelTemplateMapper.selectById(templateId);
            if (template == null) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_AVAILABLE_NOT_FOUND);
            }
        }
        if (template.getStatus() != 1 || Boolean.FALSE.equals(template.getIsEnabled())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_TEMPLATE_DISABLED);
        }
        return template;
    }

    private void validatePrintCount(Integer printCount) {
        if (printCount == null || printCount <= 0) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_PRINT_COUNT_REQUIRED);
        }
        if (printCount > 1000) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_PRINT_COUNT_LIMIT_EXCEEDED);
        }
    }

    private void validateDataCompleteness(String templateContent, Map<String, Object> printData) {
        if (!StringUtils.hasText(templateContent)) {
            return;
        }
        Set<String> placeholders = PlaceholderExtractor.extractUnique(templateContent);
        List<String> missingFields = dataAssemblyHandler.validateDataCompleteness(printData, new ArrayList<>(placeholders));
        if (!missingFields.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_PRINT_DATA_INCOMPLETE, missingFields);
        }
    }

    private List<String> generateLabelContents(String templateContent, Map<String, Object> printData, Integer printCount) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < printCount; i++) {
            results.add(placeholderHandler.process(templateContent, printData));
        }
        return results;
    }

    private Long getLongValue(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return null;
        }
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
