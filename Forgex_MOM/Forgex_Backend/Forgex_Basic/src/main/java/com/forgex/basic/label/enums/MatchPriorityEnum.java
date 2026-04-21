package com.forgex.basic.label.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板匹配优先级枚举
 * <p>
 * 定义标签模板匹配的优先级顺序，数值越小优先级越高
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Getter
@AllArgsConstructor
public enum MatchPriorityEnum {

    /**
     * 按物料匹配（最高优先级）
     */
    MATERIAL(1, "MATERIAL", "按物料匹配"),

    /**
     * 按供应商匹配
     */
    SUPPLIER(2, "SUPPLIER", "按供应商匹配"),

    /**
     * 按客户匹配
     */
    CUSTOMER(3, "CUSTOMER", "按客户匹配"),

    /**
     * 默认模板（最低优先级）
     */
    DEFAULT(99, "DEFAULT", "默认模板");

    /**
     * 优先级顺序（数值越小优先级越高）
     */
    private final Integer priority;

    /**
     * 绑定类型代码
     */
    private final String code;

    /**
     * 描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     *
     * @param code 绑定类型代码
     * @return 枚举值，未找到返回 null
     */
    public static MatchPriorityEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (MatchPriorityEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
