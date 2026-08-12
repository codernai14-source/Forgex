package com.forgex.sys.service.impl;

import com.forgex.sys.domain.entity.SysEncodeRule;
import com.forgex.sys.domain.entity.SysEncodeRuleDetail;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 编码规则明细渲染测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class EncodeRuleRendererTest {

    @Test
    void shouldRenderCodeFromOrderedDetailSegments() {
        SysEncodeRule rule = legacyRule();

        SysEncodeRuleDetail serial = detail(3, "SEQ", null, null);
        serial.setSegmentLength(5);
        serial.setSequenceStart(10);
        serial.setPaddingChar("0");
        serial.setPaddingSide("LEFT");

        List<SysEncodeRuleDetail> details = List.of(
            detail(2, "DATE", "yyyyMM", "-"),
            serial,
            detail(1, "FIXED", "SO", "-")
        );

        String code = EncodeRuleRenderer.render(rule, details, 2L, dateTime());

        assertEquals("SO-202608-00011", code);
    }

    @Test
    void shouldSupportHistoricalSegmentAliasesAndPaddingDirection() {
        SysEncodeRuleDetail sequence = detail(2, "SEQUENCE", null, null);
        sequence.setSegmentLength(4);
        sequence.setSequenceStart(1);
        sequence.setPaddingChar("X");
        sequence.setPaddingSide("RIGHT");

        List<SysEncodeRuleDetail> details = List.of(
            detail(1, "VARIABLE", "CUS", "/"),
            sequence
        );

        String code = EncodeRuleRenderer.render(legacyRule(), details, 7L, dateTime());

        assertEquals("CUS/7XXX", code);
    }

    @Test
    void shouldFallBackToLegacyMainTableFieldsWhenDetailsAreMissing() {
        String code = EncodeRuleRenderer.render(legacyRule(), List.of(), 3L, dateTime());

        assertEquals("OLD202608120003", code);
    }

    @Test
    void shouldRenderTimeFieldsInDateSegment() {
        SysEncodeRuleDetail date = detail(1, "DATE", "yyyyMMddHHmmss", null);

        String code = EncodeRuleRenderer.render(legacyRule(), List.of(date), 1L, dateTime());

        assertEquals("20260812140506", code);
    }

    private LocalDateTime dateTime() {
        return LocalDateTime.of(2026, 8, 12, 14, 5, 6);
    }

    private SysEncodeRule legacyRule() {
        SysEncodeRule rule = new SysEncodeRule();
        rule.setPrefix("OLD");
        rule.setDateFormat("yyyyMMdd");
        rule.setSerialLength(4);
        return rule;
    }

    private SysEncodeRuleDetail detail(int order, String type, String value, String connector) {
        SysEncodeRuleDetail detail = new SysEncodeRuleDetail();
        detail.setSegmentOrder(order);
        detail.setSegmentType(type);
        detail.setSegmentValue(value);
        detail.setConnector(connector);
        return detail;
    }
}
