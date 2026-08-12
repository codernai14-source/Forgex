package com.forgex.common.domain.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Excel 导入模板选项数据模型
 * <p>
 * 用于表示下拉选项的值和显示标签。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @since 1.1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateOption {

    /**
     * 选项值
     * <p>实际存储到 Excel 中的值。</p>
     */
    private String value;

    /**
     * 显示标签
     * <p>在 Excel 下拉框中显示的文本。</p>
     */
    private String label;

    /**
     * 父级选项值（用于级联下拉）
     * <p>当存在级联关系时，指定该选项属于哪个父级选项。</p>
     *
     * @since 1.1.0
     */
    private String parentValue;

    /**
     * 排序号
     * <p>用于控制选项显示顺序，数值越小越靠前。</p>
     *
     * @since 1.1.0
     */
    private Integer orderNum;

    /**
     * 构造函数（不包含父级值和排序号）
     *
     * @param value 选项值
     * @param label 显示标签
     */
    public TemplateOption(String value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 构造函数（包含排序号）
     *
     * @param value 选项值
     * @param label 显示标签
     * @param orderNum 排序号
     */
    public TemplateOption(String value, String label, Integer orderNum) {
        this.value = value;
        this.label = label;
        this.orderNum = orderNum;
    }
}
