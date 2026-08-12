package com.forgex.common.service.excel.provider;

import com.forgex.common.domain.dto.excel.TemplateOption;

import java.util.List;

/**
 * 部门列表选项提供者接口（级联工厂）
 * <p>
 * 由业务模块（如 Sys 模块）实现并注册到 Common 模块的注册中心。
 * 这样可以避免 Common 模块依赖业务模块，解决循环依赖问题。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @since 1.1.0
 */
public interface DeptByFactoryOptionProvider extends TemplateOptionProvider {
    
    /**
     * 获取 Provider 编码
     * <p>
     * 固定返回 "DEPT_BY_FACTORY"
     * </p>
     *
     * @return Provider 编码
     */
    @Override
    default String getCode() {
        return "DEPT_BY_FACTORY";
    }
    
    /**
     * 获取部门选项列表
     * <p>
     * 根据已选择的工厂 ID，查询该工厂下的部门列表。
     * </p>
     *
     * @param context 上下文参数，包含已选择的工厂 ID（通过 context.getFieldValue("factory") 获取）
     * @param dataSourceValue 数据源值（未使用）
     * @return 部门选项列表，value 为部门 ID，label 为部门名称
     */
    @Override
    List<TemplateOption> getOptions(TemplateOptionContext context, String dataSourceValue);
}
