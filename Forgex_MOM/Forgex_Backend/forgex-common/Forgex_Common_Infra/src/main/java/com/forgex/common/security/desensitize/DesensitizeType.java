package com.forgex.common.security.desensitize;

/**
 * 展示层脱敏策略。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public enum DesensitizeType {

    /** 手机号 */
    PHONE,

    /** 邮箱 */
    EMAIL,

    /** 身份证 */
    ID_CARD,

    /** 银行卡 */
    BANK_CARD,

    /** 姓名 */
    NAME,

    /** 地址 */
    ADDRESS
}
