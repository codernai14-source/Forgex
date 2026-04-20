package com.forgex.basic.label.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 模板类型枚举
 * <p>
 * 定义标签模板的业务类型，与数据库 label_template.template_type 字段保持一致
 * 覆盖 MES 现场全部标签场景：物料、客户/供应商、生产标识、包装出货、工程卡延伸
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Getter
@AllArgsConstructor
public enum TemplateTypeEnum {

    // ==================== 3.1 物料相关标签 ====================
    /**
     * 物料标签
     */
    MATERIAL("MATERIAL", "物料标签", "用于物料标识的通用标签"),

    /**
     * 产品标签
     */
    PRODUCT("PRODUCT", "产品标签", "用于产品标识的标签"),

    /**
     * LOT批次标签
     */
    LOT("LOT", "LOT批次标签", "用于批次追溯的标签"),

    /**
     * 来料标签
     */
    INCOMING("INCOMING", "来料标签", "用于原材料入库时的标签"),

    // ==================== 3.2 客户/供应商相关标签 ====================
    /**
     * 供应商标签
     */
    SUPPLIER("SUPPLIER", "供应商标签", "用于供应商标识的标签"),

    /**
     * 客户唛头标签
     */
    CUSTOMER_MARK("CUSTOMER_MARK", "客户唛头", "用于客户外箱唛头的标签"),

    /**
     * 客户定制标签
     */
    CUSTOMER_LABEL("CUSTOMER_LABEL", "客户定制标签", "客户定制的特殊标签"),

    // ==================== 3.3 生产现场标识类标签 ====================
    /**
     * 工位标识标签
     */
    WORKSTATION("WORKSTATION", "工位标识标签", "用于产线工位标识的标签"),

    /**
     * 人员标识标签
     */
    PERSONNEL("PERSONNEL", "人员标识标签", "用于员工工牌/权限标识的标签"),

    /**
     * 设备标识标签
     */
    EQUIPMENT("EQUIPMENT", "设备标识标签", "用于设备资产管理的标识标签"),

    /**
     * 工步标签
     */
    PROCESS_STEP("PROCESS_STEP", "工步标签", "用于工艺工步标识的标签"),

    /**
     * 库位标识标签
     */
    LOCATION("LOCATION", "库位标识标签", "用于仓库库位/货架标识的标签"),

    // ==================== 3.4 包装/出货类标签 ====================
    /**
     * SPQ内箱标签
     */
    SPQ_INNER("SPQ_INNER", "SPQ内箱标签", "用于SPQ内箱包装的标签"),

    /**
     * PQ外箱标签
     */
    PQ_OUTER("PQ_OUTER", "PQ外箱标签", "用于PQ外箱包装的标签"),

    /**
     * 海外外箱标签
     */
    OVERSEAS_OUTER("OVERSEAS_OUTER", "海外外箱标签", "用于海外出货外箱的标签"),

    /**
     * 出货箱数标签
     */
    SHIPPING_BOX("SHIPPING_BOX", "出货箱数标签", "用于出货箱数标识的标签"),

    // ==================== 3.5 工程卡延伸标签 ====================
    /**
     * 工程卡包装标签
     */
    ENG_CARD_PACKAGE("ENG_CARD_PACKAGE", "工程卡包装标签", "用于工程卡相关包装的标签"),

    // ==================== 其他通用标签 ====================
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
     * 类型代码（与数据库 template_type 字段值对应）
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
