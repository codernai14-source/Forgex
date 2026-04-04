package com.forgex.common.i18n;

/**
 * 国际化提示接口
 * <p>
 * 定义国际化提示消息的基本结构，用于支持多语言的错误消息和提示文本。
 * 实现此接口的类可以提供模块标识、提示代码和默认模板。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>定义模块标识，用于区分不同功能模块的消息</li>
 *   <li>定义提示代码，用于唯一标识一条消息</li>
 *   <li>定义默认模板，用于消息格式化</li>
 * </ul>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>业务异常消息的国际化</li>
 *   <li>系统提示消息的国际化</li>
 *   <li>验证错误消息的国际化</li>
 * </ul>
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * public enum CommonPrompt implements I18nPrompt {
 *     NOT_LOGIN("sys", "NOT_LOGIN", "未登录或登录已过期"),
 *     PARAM_EMPTY("sys", "PARAM_EMPTY", "参数不能为空");
 *     
 *     private final String module;
 *     private final String promptCode;
 *     private final String defaultTemplate;
 *     
 *     // 构造函数和getter方法
 * }
 * }</pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.exception.I18nBusinessException
 */
public interface I18nPrompt {
    /**
     * 获取模块标识
     * <p>
     * 用于区分不同功能模块的消息，如"sys"（系统模块）、"auth"（认证模块）等。
     * </p>
     * 
     * @return 模块标识字符串
     */
    String getModule();

    /**
     * 获取提示代码
     * <p>
     * 用于唯一标识一条消息，如"NOT_LOGIN"、"PARAM_EMPTY"等。
     * </p>
     * 
     * @return 提示代码字符串
     */
    String getPromptCode();

    /**
     * 获取默认模板
     * <p>
     * 当国际化消息不存在时使用的默认文本模板。
     * 支持使用占位符（如{0}、{1}）进行参数替换。
     * </p>
     * 
     * @return 默认模板字符串
     */
    String getDefaultTemplate();
}

