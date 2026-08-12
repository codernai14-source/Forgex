package com.forgex.sys.service.impl;

import com.forgex.common.exception.I18nBusinessException;
import com.forgex.sys.domain.param.EncodeRuleDetailSaveParam;
import com.forgex.sys.enums.SysPromptEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 编码规则明细校验测试。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
class EncodeRuleDetailValidatorTest {

    @Test
    void shouldRejectMissingDetails() {
        I18nBusinessException exception = assertThrows(
            I18nBusinessException.class,
            () -> EncodeRuleDetailValidator.validateAndNormalize(List.of())
        );

        assertEquals(SysPromptEnum.ENCODE_RULE_DETAIL_REQUIRED, exception.getMsg());
    }

    @Test
    void shouldRejectNonContinuousSegmentOrder() {
        List<EncodeRuleDetailSaveParam> details = List.of(
            detail(1, "FIXED", "SO"),
            detail(3, "SEQ", null)
        );

        I18nBusinessException exception = assertThrows(
            I18nBusinessException.class,
            () -> EncodeRuleDetailValidator.validateAndNormalize(details)
        );

        assertEquals(SysPromptEnum.ENCODE_RULE_DETAIL_INVALID, exception.getMsg());
    }

    @Test
    void shouldRejectDetailsWithoutSequenceSegment() {
        List<EncodeRuleDetailSaveParam> details = List.of(
            detail(1, "FIXED", "SO"),
            detail(2, "DATE", "yyyyMMdd")
        );

        I18nBusinessException exception = assertThrows(
            I18nBusinessException.class,
            () -> EncodeRuleDetailValidator.validateAndNormalize(details)
        );

        assertEquals(SysPromptEnum.ENCODE_RULE_DETAIL_INVALID, exception.getMsg());
    }

    @Test
    void shouldRejectInvalidDateFormat() {
        EncodeRuleDetailSaveParam invalidDate = detail(1, "DATE", null);
        invalidDate.setDateFormat("yyyy'");
        assertThrows(
            I18nBusinessException.class,
            () -> EncodeRuleDetailValidator.validateAndNormalize(List.of(invalidDate))
        );
    }

    @Test
    void shouldRejectDateFormatThatRequiresUnavailableTemporalFields() {
        EncodeRuleDetailSaveParam invalidDate = detail(1, "DATE", null);
        invalidDate.setDateFormat("VV");

        assertThrows(
            I18nBusinessException.class,
            () -> EncodeRuleDetailValidator.validateAndNormalize(List.of(invalidDate))
        );
    }

    @Test
    void shouldRejectInvalidSequenceConfig() {
        EncodeRuleDetailSaveParam invalidSequence = detail(1, "SEQ", null);
        invalidSequence.setSequenceStart(-1);
        invalidSequence.setSegmentLength(0);
        assertThrows(
            I18nBusinessException.class,
            () -> EncodeRuleDetailValidator.validateAndNormalize(List.of(invalidSequence))
        );
    }

    @Test
    void shouldNormalizeHistoricalAliasesAndDefaults() {
        EncodeRuleDetailSaveParam variable = detail(1, "variable", "CUS");
        EncodeRuleDetailSaveParam sequence = detail(2, "sequence", null);

        EncodeRuleDetailValidator.validateAndNormalize(List.of(variable, sequence));

        assertEquals("VARIABLE", variable.getSegmentType());
        assertEquals("SEQUENCE", sequence.getSegmentType());
        assertEquals(1, sequence.getSequenceStart());
        assertEquals(4, sequence.getSegmentLength());
        assertEquals("0", sequence.getPaddingChar());
        assertEquals("LEFT", sequence.getPaddingSide());
    }

    private EncodeRuleDetailSaveParam detail(int order, String type, String value) {
        EncodeRuleDetailSaveParam detail = new EncodeRuleDetailSaveParam();
        detail.setSegmentOrder(order);
        detail.setSegmentType(type);
        detail.setSegmentValue(value);
        return detail;
    }
}
