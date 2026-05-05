package com.forgex.common.service.excel.provider;

import com.forgex.common.domain.dto.excel.TemplateOption;
import com.forgex.common.enums.ExcelPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Excel 导入模板选项提供者注册中心
 * <p>
 * 用于注册和管理所有的 TemplateOptionProvider 实现。
 * 支持通过 Provider 编码获取对应的 Provider 实例。
 * </p>
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * // 注册 Provider
 * registry.register("FACTORY_LIST", factoryListProvider);
 * registry.register("DEPT_BY_FACTORY", deptByFactoryProvider);
 *
 * // 获取选项
 * List<TemplateOption> options = registry.getOptions(
 *     "FACTORY_LIST",
 *     context,
 *     null
 * );
 * }</pre>
 *
 * @author Forgex Team
 * @version 1.1.0
 * @since 1.1.0
 * @see TemplateOptionProvider
 * @see TemplateOption
 */
@Slf4j
@Component
public class TemplateOptionProviderRegistry {

    /**
     * Provider 注册表
     * <p>key: Provider 编码，value: Provider 实例</p>
     */
    private final Map<String, TemplateOptionProvider> providers = new ConcurrentHashMap<>();

    /**
     * 注册 Provider
     * <p>
     * 将 Provider 实例注册到注册中心，如果已存在相同编码的 Provider，
     * 则会覆盖原有的 Provider。
     * </p>
     *
     * @param code Provider 编码，用于唯一标识一个 Provider
     * @param provider Provider 实例
     * @throws IllegalArgumentException 当 code 或 provider 为 null 时抛出
     * @see TemplateOptionProvider
     */
    public void register(String code, TemplateOptionProvider provider) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider code cannot be null or empty");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Provider instance cannot be null");
        }

        log.info("Register TemplateOptionProvider: code={}", code);
        providers.put(code, provider);
    }

    /**
     * 获取 Provider 实例
     * <p>
     * 根据 Provider 编码获取对应的 Provider 实例。
     * </p>
     *
     * @param code Provider 编码
     * @return Provider 实例
     * @throws I18nBusinessException 当 Provider 不存在时抛出
     * @see TemplateOptionProvider
     */
    public TemplateOptionProvider getProvider(String code) {
        TemplateOptionProvider provider = providers.get(code);
        if (provider == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.TEMPLATE_OPTION_PROVIDER_NOT_FOUND, code);
        }
        return provider;
    }

    /**
     * 获取选项列表
     * <p>
     * 根据 Provider 编码获取对应的选项列表。
     * </p>
     *
     * @param code Provider 编码
     * @param context 上下文参数，包含当前已填写的字段值
     * @param dataSourceValue 数据源值，Provider 特定的配置参数
     * @return 选项列表
     * @throws I18nBusinessException 当 Provider 不存在时抛出
     * @see TemplateOption
     * @see TemplateOptionContext
     */
    public List<TemplateOption> getOptions(String code, TemplateOptionContext context, String dataSourceValue) {
        TemplateOptionProvider provider = getProvider(code);
        try {
            return provider.getOptions(context, dataSourceValue);
        } catch (Exception e) {
            log.error("Get options failed, code={}, context={}, dataSourceValue={}",
                     code, context, dataSourceValue, e);
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ExcelPromptEnum.TEMPLATE_OPTION_LIST_GET_FAILED, e.getMessage());
        }
    }

    /**
     * 检查 Provider 是否存在
     * <p>
     * 根据 Provider 编码检查是否已注册。
     * </p>
     *
     * @param code Provider 编码
     * @return 如果存在返回 true，否则返回 false
     */
    public boolean hasProvider(String code) {
        return providers.containsKey(code);
    }

    /**
     * 获取所有已注册的 Provider 编码
     * <p>
     * 返回所有已注册的 Provider 编码列表，用于前端配置时展示可选的 Provider。
     * </p>
     *
     * @return Provider 编码列表
     */
    public List<String> getAllProviderCodes() {
        return providers.keySet().stream().toList();
    }
}
