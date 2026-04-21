package com.forgex.basic.label.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.label.domain.entity.LabelPrintException;
import com.forgex.basic.label.domain.param.PrintExceptionQueryParam;
import com.forgex.basic.label.domain.vo.PlaceholderVO;
import com.forgex.basic.label.domain.vo.PrintExceptionVO;
import com.forgex.basic.label.domain.vo.TemplateValidateResultVO;
import com.forgex.basic.label.mapper.LabelPrintExceptionMapper;
import com.forgex.basic.label.service.LabelCommonService;
import com.forgex.basic.label.utils.JsonTemplateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签通用工具 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabelCommonServiceImpl implements LabelCommonService {

    private final LabelPrintExceptionMapper labelPrintExceptionMapper;

    @Override
    public List<PlaceholderVO> getStandardPlaceholders(Long tenantId) {
        // 返回标准的占位符定义
        List<PlaceholderVO> placeholders = new ArrayList<>();

        // 物料相关
        placeholders.add(createPlaceholder("materialCode", "String", "物料编码", true, "MAT001", "物料"));
        placeholders.add(createPlaceholder("materialName", "String", "物料名称", true, "电阻", "物料"));
        placeholders.add(createPlaceholder("specification", "String", "规格型号", false, "10KΩ", "物料"));
        placeholders.add(createPlaceholder("unit", "String", "单位", false, "PCS", "物料"));
        placeholders.add(createPlaceholder("brand", "String", "品牌", false, "XXX", "物料"));

        // 批次相关
        placeholders.add(createPlaceholder("lotNo", "String", "LOT号", true, "LOT20260414001", "批次"));
        placeholders.add(createPlaceholder("batchNo", "String", "批次号", false, "BATCH001", "批次"));
        placeholders.add(createPlaceholder("productionDate", "String", "生产日期", false, "2026-04-14", "批次"));
        placeholders.add(createPlaceholder("expiryDate", "String", "有效期", false, "2027-04-14", "批次"));

        // 数量相关
        placeholders.add(createPlaceholder("quantity", "Number", "数量", false, "1000", "数量"));
        placeholders.add(createPlaceholder("quantityFormatted", "String", "格式化数量", false, "1000.00", "数量"));

        // 供应商相关
        placeholders.add(createPlaceholder("supplierCode", "String", "供应商编码", false, "SUP001", "供应商"));
        placeholders.add(createPlaceholder("supplierName", "String", "供应商名称", false, "XX供应商", "供应商"));
        placeholders.add(createPlaceholder("supplierContact", "String", "联系人", false, "张三", "供应商"));

        // 客户相关
        placeholders.add(createPlaceholder("customerCode", "String", "客户编码", false, "CUS001", "客户"));
        placeholders.add(createPlaceholder("customerName", "String", "客户名称", false, "XX客户", "客户"));
        placeholders.add(createPlaceholder("customerContact", "String", "联系人", false, "李四", "客户"));

        // 工程相关
        placeholders.add(createPlaceholder("engineeringCardNo", "String", "工程卡号", false, "ENG20260414001", "工程"));
        placeholders.add(createPlaceholder("workOrderNo", "String", "工单号", false, "WO20260414001", "工程"));

        // 系统相关
        placeholders.add(createPlaceholder("printTime", "String", "打印时间", false, "2026-04-14 15:30:00", "系统"));
        placeholders.add(createPlaceholder("printDate", "String", "打印日期", false, "2026-04-14", "系统"));
        placeholders.add(createPlaceholder("operatorName", "String", "操作员", false, "操作员001", "系统"));
        placeholders.add(createPlaceholder("barcodeNo", "String", "条码号", false, "BC20260414153045A1B2", "系统"));

        log.info("获取标准占位符列表成功，共 {} 个", placeholders.size());
        return placeholders;
    }

    @Override
    public TemplateValidateResultVO validateTemplateJson(String templateContent, Long tenantId) {
        // 使用工具类进行校验
        JsonTemplateValidator.ValidationResult result =
                JsonTemplateValidator.validate(templateContent);

        // 转换为 VO
        TemplateValidateResultVO vo = new TemplateValidateResultVO();
        vo.setValid(result.isValid());
        vo.setErrors(result.getErrors());
        vo.setWarnings(result.getWarnings());
        vo.setPlaceholders(result.getPlaceholders());

        log.info("模板 JSON 校验完成，结果: {}", result.isValid() ? "通过" : "失败");
        return vo;
    }

    @Override
    public IPage<PrintExceptionVO> pageExceptions(PrintExceptionQueryParam param, Long tenantId) {
        Page<LabelPrintException> page = new Page<>(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<LabelPrintException> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelPrintException::getTenantId, tenantId);

        if (StringUtils.hasText(param.getErrorCode())) {
            wrapper.eq(LabelPrintException::getErrorCode, param.getErrorCode());
        }

        if (StringUtils.hasText(param.getPrintTimeStart())) {
            LocalDateTime startTime = LocalDateTime.parse(param.getPrintTimeStart(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.ge(LabelPrintException::getExceptionTime, startTime);
        }

        if (StringUtils.hasText(param.getPrintTimeEnd())) {
            LocalDateTime endTime = LocalDateTime.parse(param.getPrintTimeEnd(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.le(LabelPrintException::getExceptionTime, endTime);
        }

        wrapper.orderByDesc(LabelPrintException::getExceptionTime);

        IPage<LabelPrintException> entityPage = labelPrintExceptionMapper.selectPage(page, wrapper);

        // 转换为 VO
        IPage<PrintExceptionVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<PrintExceptionVO> voList = entityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 创建占位符对象
     */
    private PlaceholderVO createPlaceholder(String fieldName, String fieldType,
                                            String description, Boolean required,
                                            String exampleValue, String group) {
        PlaceholderVO vo = new PlaceholderVO();
        vo.setFieldName(fieldName);
        vo.setFieldType(fieldType);
        vo.setDescription(description);
        vo.setRequired(required);
        vo.setExampleValue(exampleValue);
        vo.setGroup(group);
        return vo;
    }

    /**
     * 转换为 VO
     */
    private PrintExceptionVO convertToVO(LabelPrintException entity) {
        PrintExceptionVO vo = new PrintExceptionVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
