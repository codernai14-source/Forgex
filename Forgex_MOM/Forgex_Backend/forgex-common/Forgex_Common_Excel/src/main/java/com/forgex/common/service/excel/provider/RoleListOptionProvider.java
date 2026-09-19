package com.forgex.common.service.excel.provider;

import com.forgex.common.domain.dto.excel.TemplateOption;

import java.util.List;

/**
 * 角色列表选项提供者接口
 * <p>
 * 由业务模块（如 Sys 模块）实现并注册到 Common 模块的注册中心。
 * 这样可以避免 Common 模块依赖业务模块，解决循环依赖问题。
 * </p>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @since 1.1.0
 */
public interface RoleListOptionProvider extends TemplateOptionProvider {
    
    /**
     * 获取 Provider 编码
     * <p>
     * 固定返回 "ROLE_LIST"
     * </p>
     *
     * @return Provider 编码
     */
    @Override
    default String getCode() {
        return "ROLE_LIST";
    }
    
    /**
     * 获取角色选项列表
     * <p>
     * 查询所有启用的角色，返回选项列表。
     * </p>
     *
     * @param context 上下文参数，包含租户 ID 等信息
     * @param dataSourceValue 数据源值（未使用）
     * @return 角色选项列表，value 为角色 ID，label 为角色名称
     */
    @Override
    List<TemplateOption> getOptions(TemplateOptionContext context, String dataSourceValue);
}
