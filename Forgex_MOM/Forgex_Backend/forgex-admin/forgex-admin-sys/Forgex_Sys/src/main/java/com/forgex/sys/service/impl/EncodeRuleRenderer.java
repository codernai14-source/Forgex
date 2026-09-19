package com.forgex.sys.service.impl;

import com.forgex.sys.domain.entity.SysEncodeRule;
import com.forgex.sys.domain.entity.SysEncodeRuleDetail;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 按编码规则明细渲染业务编码，并兼容历史主表规则。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
final class EncodeRuleRenderer {

    private static final int DEFAULT_SERIAL_LENGTH = 4;

    private EncodeRuleRenderer() {
    }

    /**
     * 渲染业务编码。
     *
     * @param rule 主规则
     * @param details 明细规则
     * @param serial Redis 原子流水值，从 1 开始
     * @param dateTime 当前业务日期时间
     * @return 渲染后的业务编码
     */
    static String render(SysEncodeRule rule, List<SysEncodeRuleDetail> details, long serial, LocalDateTime dateTime) {
        if (CollectionUtils.isEmpty(details)) {
            return renderLegacy(rule, serial, dateTime);
        }

        List<SysEncodeRuleDetail> ordered = details.stream()
            .filter(detail -> detail != null && StringUtils.hasText(detail.getSegmentType()))
            .sorted(Comparator.comparing(
                SysEncodeRuleDetail::getSegmentOrder,
                Comparator.nullsLast(Integer::compareTo)
            ))
            .toList();

        StringBuilder code = new StringBuilder();
        for (int index = 0; index < ordered.size(); index++) {
            SysEncodeRuleDetail detail = ordered.get(index);
            code.append(renderSegment(detail, serial, dateTime));
            if (index < ordered.size() - 1 && StringUtils.hasText(detail.getConnector())) {
                code.append(detail.getConnector());
            }
        }
        return code.toString();
    }

    private static String renderSegment(SysEncodeRuleDetail detail, long serial, LocalDateTime dateTime) {
        String type = detail.getSegmentType().trim().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "DATE" -> dateTime.format(DateTimeFormatter.ofPattern(resolveDateFormat(detail)));
            case "SEQ", "SERIAL", "SEQUENCE" -> formatSerial(resolveSequenceValue(detail, serial), detail);
            case "CUSTOM", "EXPRESSION", "VARIABLE", "FIXED" -> defaultText(detail.getSegmentValue());
            default -> defaultText(detail.getSegmentValue());
        };
    }

    private static String renderLegacy(SysEncodeRule rule, long serial, LocalDateTime dateTime) {
        StringBuilder code = new StringBuilder(defaultText(rule.getPrefix()));
        if (StringUtils.hasText(rule.getDateFormat())) {
            code.append(dateTime.format(DateTimeFormatter.ofPattern(rule.getDateFormat())));
        }
        code.append(leftPad(serial, defaultLength(rule.getSerialLength()), "0"));
        return code.toString();
    }

    private static long resolveSequenceValue(SysEncodeRuleDetail detail, long serial) {
        long start = detail.getSequenceStart() == null ? 1L : detail.getSequenceStart();
        return start + serial - 1L;
    }

    private static String formatSerial(long serial, SysEncodeRuleDetail detail) {
        int length = defaultLength(detail.getSegmentLength());
        String padding = StringUtils.hasText(detail.getPaddingChar()) ? detail.getPaddingChar() : "0";
        if ("RIGHT".equalsIgnoreCase(detail.getPaddingSide())) {
            return rightPad(serial, length, padding);
        }
        return leftPad(serial, length, padding);
    }

    private static String resolveDateFormat(SysEncodeRuleDetail detail) {
        if (StringUtils.hasText(detail.getDateFormat())) {
            return detail.getDateFormat();
        }
        return StringUtils.hasText(detail.getSegmentValue()) ? detail.getSegmentValue() : "yyyyMMdd";
    }

    private static int defaultLength(Integer length) {
        return length == null || length <= 0 ? DEFAULT_SERIAL_LENGTH : length;
    }

    private static String leftPad(long value, int length, String padding) {
        String text = String.valueOf(value);
        return repeatPadding(padding, length - text.length()) + text;
    }

    private static String rightPad(long value, int length, String padding) {
        String text = String.valueOf(value);
        return text + repeatPadding(padding, length - text.length());
    }

    private static String repeatPadding(String padding, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder result = new StringBuilder(count);
        while (result.length() < count) {
            result.append(padding);
        }
        return result.substring(0, count);
    }

    private static String defaultText(String value) {
        return value == null ? "" : value;
    }
}
