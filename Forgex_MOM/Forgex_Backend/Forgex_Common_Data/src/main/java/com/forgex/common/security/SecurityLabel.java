package com.forgex.common.security;

/**
 * 客体安全标记。
 * <p>
 * 数值越大密级越高。强制访问控制采用“下读上写”：
 * 读取要求客体密级不超过主体密级，写入要求客体密级不低于主体密级。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public enum SecurityLabel {

    /** 公开 */
    PUBLIC(0),

    /** 内部 */
    INTERNAL(1),

    /** 秘密 */
    SECRET(2),

    /** 机密 */
    CONFIDENTIAL(3);

    private final int level;

    SecurityLabel(int level) {
        this.level = level;
    }

    /**
     * 返回密级数值。
     *
     * @return 密级
     */
    public int getLevel() {
        return level;
    }

    /**
     * 将数值转换为枚举，无法识别时回退为公开。
     *
     * @param value 数值
     * @return 安全标记
     */
    public static SecurityLabel fromLevel(Integer value) {
        if (value == null) {
            return PUBLIC;
        }
        for (SecurityLabel label : values()) {
            if (label.level == value) {
                return label;
            }
        }
        return PUBLIC;
    }
}
