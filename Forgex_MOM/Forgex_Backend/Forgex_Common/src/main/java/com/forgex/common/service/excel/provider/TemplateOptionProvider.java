package com.forgex.common.service.excel.provider;

import com.forgex.common.domain.dto.excel.TemplateOption;

import java.util.List;

/**
 * Excel 导入模板选项提供者接口
 * <p>
 * 用于动态提供 Excel 导入模板中的下拉选项数据。
 * 支持工厂列表、部门列表、角色列表等动态数据源。
 * </p>
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * // 注册 Provider
 * registry.register("FACTORY_LIST", new FactoryListOptionProvider(factoryMapper));
 *
 * // 获取选项列表
 * List<TemplateOption> options = provider.getOptions("FACTORY_LIST", null, null);
 * }</pre>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @since 1.1.0
 * @see TemplateOption
 * @see TemplateOptionProviderRegistry
 */
public interface TemplateOptionProvider {

    /**
     * 获取 Provider 编码
     * <p>
     * 用于唯一标识一个 Provider，例如 "FACTORY_LIST"、"ROLE_LIST" 等。
     * </p>
     *
     * @return Provider 编码
     */
    String getCode();

    /**
     * 获取选项列表
     * <p>
     * 根据提供的参数动态生成下拉选项列表。
     * </p>
     *
     * @param context 上下文参数，包含当前已填写的字段值，用于级联查询
     * @param dataSourceValue 数据源值，Provider 特定的配置参数
     * @return 选项列表，包含 value（选项值）和 label（显示标签）
     * @throws IllegalArgumentException 当参数不合法时抛出
     * @see TemplateOption
     */
    List<TemplateOption> getOptions(TemplateOptionContext context, String dataSourceValue);
}
