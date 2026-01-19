package com.forgex.common.exception;

import com.forgex.common.i18n.I18nPrompt;

/**
 * 国际化业务异常类
 * <p>
 * 自定义运行时异常，用于抛出支持国际化的业务错误信息。
 * 通过I18nPrompt接口，支持根据当前语言环境显示不同的错误消息。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>继承RuntimeException，支持非受检异常</li>
 *   <li>包含错误码，用于标识错误类型</li>
 *   <li>包含国际化提示，支持多语言错误消息</li>
 *   <li>支持消息参数，用于动态填充错误消息中的占位符</li>
 * </ul>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>业务逻辑验证失败时抛出</li>
 *   <li>数据不存在或冲突时抛出</li>
 *   <li>权限不足时抛出</li>
 *   <li>参数校验失败时抛出</li>
 * </ul>
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * // 抛出国际化业务异常
 * throw new I18nBusinessException(
 *     400, 
 *     I18nPrompt.of("USER_NOT_FOUND"),
 *     "用户ID", userId
 * );
 * }</pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see I18nPrompt
 * @see com.forgex.common.i18n.LangContext
 */
public class I18nBusinessException extends RuntimeException {
    /**
     * 错误码
     * <p>用于标识错误类型，如400=参数错误、404=资源不存在等。</p>
     */
    private final int code;

    /**
     * 国际化提示
     * <p>包含错误消息的国际化配置信息。</p>
     */
    private final I18nPrompt msg;

    /**
     * 消息参数
     * <p>用于动态填充错误消息中的占位符。</p>
     */
    private final Object[] msgArgs;

    /**
     * 构造函数
     * <p>
     * 创建国际化业务异常实例。
     * </p>
     * 
     * @param code 错误码
     * @param msg 国际化提示
     * @param msgArgs 消息参数
     */
    public I18nBusinessException(int code, I18nPrompt msg, Object... msgArgs) {
        // 调用父类构造函数，设置异常消息为提示代码
        super(msg == null ? null : msg.getPromptCode());
        this.code = code;
        this.msg = msg;
        this.msgArgs = msgArgs;
    }

    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取国际化提示
     * 
     * @return 国际化提示对象
     */
    public I18nPrompt getMsg() {
        return msg;
    }

    /**
     * 获取消息参数
     * 
     * @return 消息参数数组
     */
    public Object[] getMsgArgs() {
        return msgArgs;
    }
}

