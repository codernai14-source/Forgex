package com.forgex.sys.service.impl;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.param.EncodeRuleDetailSaveParam;
import com.forgex.sys.enums.SysPromptEnum;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 校验并规范化编码规则明细，保证持久化配置可以被编码渲染器稳定执行。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
final class EncodeRuleDetailValidator {

    private static final int DEFAULT_SEQUENCE_LENGTH = 4;
    private static final int MAX_SEQUENCE_LENGTH = 20;
    private static final Set<String> SUPPORTED_TYPES = Set.of("FIXED", "DATE", "SEQUENCE", "VARIABLE");

    private EncodeRuleDetailValidator() {
    }

    /**
     * 校验明细顺序和各段配置，并将历史类型别名转换成标准类型。
     *
     * @param details 待保存明细
     * @throws I18nBusinessException 明细为空或配置非法时抛出
     */
    static void validateAndNormalize(List<EncodeRuleDetailSaveParam> details) {
        if (CollectionUtils.isEmpty(details)) {
            throw new I18nBusinessException(
                StatusCode.BUSINESS_ERROR,
                SysPromptEnum.ENCODE_RULE_DETAIL_REQUIRED
            );
        }

        for (int index = 0; index < details.size(); index++) {
            EncodeRuleDetailSaveParam detail = details.get(index);
            if (detail == null || detail.getSegmentOrder() == null || detail.getSegmentOrder() != index + 1) {
                invalid("段序号必须从 1 开始连续递增");
            }

            String type = normalizeType(detail.getSegmentType());
            if (!SUPPORTED_TYPES.contains(type)) {
                invalid("不支持的段类型");
            }
            detail.setSegmentType(type);

            switch (type) {
                case "FIXED", "VARIABLE" -> validateTextSegment(detail);
                case "DATE" -> validateDateSegment(detail);
                case "SEQUENCE" -> validateSequenceSegment(detail);
                default -> invalid("不支持的段类型");
            }
        }

        boolean hasSequence = details.stream()
            .anyMatch(detail -> "SEQUENCE".equals(detail.getSegmentType()));
        if (!hasSequence) {
            invalid("编码规则必须包含流水段");
        }
    }

    private static String normalizeType(String segmentType) {
        if (!StringUtils.hasText(segmentType)) {
            return "";
        }
        return switch (segmentType.trim().toUpperCase(Locale.ROOT)) {
            case "SEQ", "SERIAL" -> "SEQUENCE";
            case "CUSTOM", "EXPRESSION" -> "VARIABLE";
            default -> segmentType.trim().toUpperCase(Locale.ROOT);
        };
    }

    private static void validateTextSegment(EncodeRuleDetailSaveParam detail) {
        if (!StringUtils.hasText(detail.getSegmentValue())) {
            invalid("固定字符或自定义段值不能为空");
        }
    }

    private static void validateDateSegment(EncodeRuleDetailSaveParam detail) {
        String dateFormat = StringUtils.hasText(detail.getDateFormat())
            ? detail.getDateFormat().trim()
            : detail.getSegmentValue();
        if (!StringUtils.hasText(dateFormat)) {
            dateFormat = "yyyyMMdd";
        }
        try {
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateFormat));
        } catch (java.time.DateTimeException | java.lang.IllegalArgumentException exception) {
            invalid("日期格式不合法");
        }
        detail.setDateFormat(dateFormat);
        detail.setSegmentValue(dateFormat);
    }

    private static void validateSequenceSegment(EncodeRuleDetailSaveParam detail) {
        int sequenceStart = detail.getSequenceStart() == null ? 1 : detail.getSequenceStart();
        int length = detail.getSegmentLength() == null ? DEFAULT_SEQUENCE_LENGTH : detail.getSegmentLength();
        String paddingChar = StringUtils.hasText(detail.getPaddingChar()) ? detail.getPaddingChar() : "0";
        String paddingSide = StringUtils.hasText(detail.getPaddingSide())
            ? detail.getPaddingSide().trim().toUpperCase(Locale.ROOT)
            : "LEFT";

        if (sequenceStart < 0 || length < 1 || length > MAX_SEQUENCE_LENGTH
            || paddingChar.length() > 10 || !("LEFT".equals(paddingSide) || "RIGHT".equals(paddingSide))) {
            invalid("流水段配置不合法");
        }

        detail.setSequenceStart(sequenceStart);
        detail.setSegmentLength(length);
        detail.setPaddingChar(paddingChar);
        detail.setPaddingSide(paddingSide);
    }

    private static void invalid(String reason) {
        throw new I18nBusinessException(
            StatusCode.BUSINESS_ERROR,
            SysPromptEnum.ENCODE_RULE_DETAIL_INVALID,
            reason
        );
    }
}
