package com.forgex.basic.label.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板类型枚举
 * <p>
 * 定义标签模板的业务类型，不同场景使用不同类型的模板
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Getter
@AllArgsConstructor
public enum TemplateTypeEnum {

    /**
     * 来料标签
     */
    INCOMING("INCOMING", "来料标签", "用于原材料入库时的标签"),

    /**
     * 半成品标签
     */
    SEMI_FINISHED("SEMI_FINISHED", "半成品标签", "用于生产过程中的半成品标签"),

    /**
     * 成品标签
     */
    FINISHED("FINISHED", "成品标签", "用于成品出库时的标签"),

    /**
     * 出库标签
     */
    OUTBOUND("OUTBOUND", "出库标签", "用于产品出库时的标签"),

    /**
     * 退货标签
     */
    RETURN("RETURN", "退货标签", "用于退货处理的标签"),

    /**
     * 不良品标签
     */
    DEFECTIVE("DEFECTIVE", "不良品标签", "用于不良品标识的标签"),

    /**
     * 库存标签
     */
    INVENTORY("INVENTORY", "库存标签", "用于库存盘点的标签"),

    /**
     * 通用标签
     */
    GENERAL("GENERAL", "通用标签", "通用场景的标签");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 枚举值，未找到返回 null
     */
    public static TemplateTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (TemplateTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 判断代码是否有效
     *
     * @param code 类型代码
     * @return true-有效，false-无效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }

    /**
     * 获取所有类型代码列表
     *
     * @return 类型代码数组
     */
    public static String[] getAllCodes() {
        TemplateTypeEnum[] values = values();
        String[] codes = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].getCode();
        }
        return codes;
    }
}
