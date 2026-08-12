package com.forgex.common.service.excel.provider;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Excel 导入模板选项上下文
 * <p>
 * 用于传递模板选项查询的上下文信息，包括当前已填写的字段值等。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @since 1.1.0
 * @see TemplateOptionProvider
 */
@Data
public class TemplateOptionContext {

    /**
     * 当前租户 ID
     * <p>用于多租户数据隔离。</p>
     */
    private Long tenantId;

    /**
     * 当前已填写的字段值 Map
     * <p>
     * key: 字段的 importField
     * value: 字段已选择的值
     * </p>
     * <p>用途：用于级联下拉，例如部门字段依赖工厂字段，
     * 则可以通过 context.getFieldValue("factory") 获取已选择的工厂值。</p>
     */
    private Map<String, String> fieldValues;

    /**
     * 获取指定字段的值
     *
     * @param fieldKey 字段 key（importField）
     * @return 字段值，如果不存在返回 null
     */
    public String getFieldValue(String fieldKey) {
        if (fieldValues == null) {
            return null;
        }
        return fieldValues.get(fieldKey);
    }

    /**
     * 设置字段值
     *
     * @param fieldKey 字段 key（importField）
     * @param value 字段值
     */
    public void setFieldValue(String fieldKey, String value) {
        if (fieldValues == null) {
            fieldValues = new HashMap<>();
        }
        fieldValues.put(fieldKey, value);
    }
}
