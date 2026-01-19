package com.forgex.common.web;

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.i18n.I18nPrompt;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * 统一返回结果类
 * <p>
 * 用于封装所有API接口的返回结果，保证返回格式的一致性
 * </p>
 * <p>返回规范：</p>
 * <ul>
 *   <li>code：接口运行状态码，200表示成功，非200表示失败</li>
 *   <li>messageCode：国际化消息模板代码，通过I18nPrompt接口指定</li>
 *   <li>message：消息模板占位符参数，按占位符顺序用逗号分隔（如："订单A,开工"）</li>
 *   <li>data：返回数据，泛型类型，可为null</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 成功返回（无数据）
 * R.ok();
 * 
 * // 成功返回（带数据）
 * R.ok(data);
 * 
 * // 失败返回
 * R.fail(CommonPrompt.OPERATION_FAILED);
 * 
 * // 失败返回（带占位符参数）
 * // 模板：订单{0}处于{1}状态
 * R.fail(CommonPrompt.ORDER_STATUS, "订单A,开工");
 * }</pre>
 *
 * @param <T> 返回数据的类型
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class R<T> {

    /** 接口运行状态码 */
    private Integer code;

    /** 国际化消息模板代码，通过I18nPrompt接口指定 */
    @JsonIgnore
    private I18nPrompt messageCode;

    /** 消息模板占位符参数，按占位符顺序用逗号分隔 */
    private String message;

    /** 国际化元信息（可选） */
    private I18nMeta i18n;

    /** 返回数据：泛型类型，可为null */
    private T data;

    /**
     * 国际化元信息结构
     * <p>
     * 用于跨服务、网关或前端需要"二次翻译"的场景：当 {@link #messageCode} 无法直接序列化/反序列化时，
     * 仍可通过 module + code + args 反查语言库。
     * </p>
     *
     * @author Forgex Team
     * @version 1.0.0
     */
    @Data
    public static class I18nMeta {
        /** 模块标识 */
        private String module;
        /** 提示码 */
        private String code;
        /** 参数 */
        private Object[] args;
    }

    /**
     * 成功返回（无数据，无消息）
     * <p>
     * 返回状态码200，消息为空，数据为null
     * </p>
     *
     * @param <T> 返回数据的类型
     * @return R包装的成功结果
     */
    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.code = StatusCode.SUCCESS;
        r.messageCode = null;
        r.i18n = null;
        return r;
    }

    /**
     * 成功返回（带数据，无消息）
     * <p>
     * 返回状态码200，消息为空，携带指定数据
     * </p>
     *
     * @param <T>  返回数据的类型
     * @param data 返回数据
     * @return R包装的成功结果
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = StatusCode.SUCCESS;
        r.messageCode = null;
        r.i18n = null;
        r.data = data;
        return r;
    }

    /**
     * 成功返回（自定义消息，无数据）
     * <p>
     * 返回状态码200，自定义返回消息模板代码，数据为null
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param messageCode 自定义返回消息模板代码
     * @return R包装的成功结果
     * @see I18nPrompt
     */
    public static <T> R<T> ok(I18nPrompt messageCode) {
        R<T> r = new R<>();
        r.code = StatusCode.SUCCESS;
        r.messageCode = messageCode;
        r.i18n = buildI18nMeta(messageCode, null);
        return r;
    }

    /**
     * 成功返回（自定义消息，带数据）
     * <p>
     * 返回状态码200，自定义返回消息模板代码，携带指定数据
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param messageCode 自定义返回消息模板代码
     * @param data        返回数据
     * @return R包装的成功结果
     * @see I18nPrompt
     */
    public static <T> R<T> ok(I18nPrompt messageCode, T data) {
        R<T> r = new R<>();
        r.code = StatusCode.SUCCESS;
        r.messageCode = messageCode;
        r.i18n = buildI18nMeta(messageCode, null);
        r.data = data;
        return r;
    }
    
    /**
     * 成功返回（自定义消息和占位符参数，无数据）
     * <p>
     * 返回状态码200，自定义返回消息模板代码，message字段存储占位符参数（按占位符顺序用逗号分隔）
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param messageCode 自定义返回消息模板代码
     * @param args        占位符参数，按占位符顺序用逗号分隔
     * @return R包装的成功结果
     * @see StatusCode#SUCCESS
     * @see I18nPrompt
     */
    public static <T> R<T> ok(I18nPrompt messageCode, String args) {
        R<T> r = new R<>();
        r.code = StatusCode.SUCCESS;
        r.messageCode = messageCode;
        r.message = args;
        r.i18n = buildI18nMeta(messageCode, parseArgs(args));
        return r;
    }
    
    /**
     * 成功返回（自定义消息、占位符参数和数据）
     * <p>
     * 返回状态码200，自定义返回消息模板代码，message字段存储占位符参数（按占位符顺序用逗号分隔），携带指定数据
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param messageCode 自定义返回消息模板代码
     * @param args        占位符参数，按占位符顺序用逗号分隔
     * @param data        返回数据
     * @return R包装的成功结果
     * @see StatusCode#SUCCESS
     * @see I18nPrompt
     */
    public static <T> R<T> ok(I18nPrompt messageCode, String args, T data) {
        R<T> r = new R<>();
        r.code = StatusCode.SUCCESS;
        r.messageCode = messageCode;
        r.message = args;
        r.i18n = buildI18nMeta(messageCode, parseArgs(args));
        r.data = data;
        return r;
    }

    /**
     * 失败返回（默认状态码和消息，无数据）
     * <p>
     * 返回状态码500，消息代码为默认操作失败代码 {@link CommonPrompt#OPERATION_FAILED}，数据为null
     * </p>
     *
     * @param <T> 返回数据的类型
     * @return R包装的失败结果
     * @see StatusCode#BUSINESS_ERROR
     * @see CommonPrompt#OPERATION_FAILED
     */
    public static <T> R<T> fail() {
        R<T> r = new R<>();
        r.code = StatusCode.BUSINESS_ERROR;
        r.messageCode = CommonPrompt.OPERATION_FAILED;
        r.i18n = buildI18nMeta(CommonPrompt.OPERATION_FAILED, null);
        return r;
    }

    /**
     * 失败返回（自定义消息，默认状态码）
     * <p>
     * 返回状态码500，自定义返回错误消息模板代码，数据为null
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param messageCode 自定义返回错误消息模板代码
     * @return R包装的失败结果
     * @see StatusCode#BUSINESS_ERROR
     * @see I18nPrompt
     */
    public static <T> R<T> fail(I18nPrompt messageCode) {
        R<T> r = new R<>();
        r.code = StatusCode.BUSINESS_ERROR;
        r.messageCode = messageCode;
        r.i18n = buildI18nMeta(messageCode, null);
        return r;
    }

    /**
     * 失败返回（自定义状态码和消息）
     * <p>
     * 返回自定义状态码，自定义返回错误消息模板代码，数据为null
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param code        自定义状态码
     * @param messageCode 自定义返回错误消息模板代码
     * @return R包装的失败结果
     * @see StatusCode
     * @see I18nPrompt
     */
    public static <T> R<T> fail(Integer code, I18nPrompt messageCode) {
        R<T> r = new R<>();
        r.code = code;
        r.messageCode = messageCode;
        r.i18n = buildI18nMeta(messageCode, null);
        return r;
    }

    /**
     * 失败返回（自定义消息和数据，默认状态码）
     * <p>
     * 返回状态码500，自定义返回错误消息模板代码，携带指定数据
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param messageCode 自定义返回错误消息模板代码
     * @param data        返回数据
     * @return R包装的失败结果
     * @see StatusCode#BUSINESS_ERROR
     * @see I18nPrompt
     */
    public static <T> R<T> fail(I18nPrompt messageCode, T data) {
        R<T> r = new R<>();
        r.code = StatusCode.BUSINESS_ERROR;
        r.messageCode = messageCode;
        r.i18n = buildI18nMeta(messageCode, null);
        r.data = data;
        return r;
    }

    /**
     * 失败返回（自定义数据，默认状态码和消息）
     * <p>
     * 返回状态码500，消息代码为默认操作失败代码 {@link CommonPrompt#OPERATION_FAILED}，携带指定数据
     * </p>
     *
     * @param <T>  返回数据的类型
     * @param data 返回数据
     * @return R包装的失败结果
     * @see StatusCode#BUSINESS_ERROR
     * @see CommonPrompt#OPERATION_FAILED
     */
    public static <T> R<T> fail(T data) {
        R<T> r = new R<>();
        r.code = StatusCode.BUSINESS_ERROR;
        r.messageCode = CommonPrompt.OPERATION_FAILED;
        r.i18n = buildI18nMeta(CommonPrompt.OPERATION_FAILED, null);
        r.data = data;
        return r;
    }

    /**
     * 失败返回（自定义状态码、消息和数据）
     * <p>
     * 返回自定义状态码，自定义返回错误消息模板代码，携带指定数据
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param code        自定义状态码
     * @param messageCode 自定义返回错误消息模板代码
     * @param data        返回数据
     * @return R包装的失败结果
     * @see StatusCode
     * @see I18nPrompt
     */
    public static <T> R<T> fail(Integer code, I18nPrompt messageCode, T data) {
        R<T> r = new R<>();
        r.code = code;
        r.messageCode = messageCode;
        r.i18n = buildI18nMeta(messageCode, null);
        r.data = data;
        return r;
    }

    /**
     * 失败返回（自定义消息和占位符参数）
     * <p>
     * 返回状态码500，自定义返回错误消息模板代码，message字段存储占位符参数（按占位符顺序用逗号分隔）
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param messageCode 自定义返回错误消息模板代码
     * @param args        占位符参数，按占位符顺序用逗号分隔
     * @return R包装的失败结果
     * @see StatusCode#BUSINESS_ERROR
     * @see I18nPrompt
     */
    public static <T> R<T> fail(I18nPrompt messageCode, String args) {
        R<T> r = new R<>();
        r.code = StatusCode.BUSINESS_ERROR;
        r.messageCode = messageCode;
        r.message = args;
        r.i18n = buildI18nMeta(messageCode, parseArgs(args));
        return r;
    }

    /**
     * 失败返回（自定义状态码、消息和占位符参数）
     * <p>
     * 返回自定义状态码，自定义返回错误消息模板代码，message字段存储占位符参数（按占位符顺序用逗号分隔）
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param code        自定义状态码
     * @param messageCode 自定义返回错误消息模板代码
     * @param args        占位符参数，按占位符顺序用逗号分隔
     * @return R包装的失败结果
     * @see StatusCode
     * @see I18nPrompt
     */
    public static <T> R<T> fail(Integer code, I18nPrompt messageCode, String args) {
        R<T> r = new R<>();
        r.code = code;
        r.messageCode = messageCode;
        r.message = args;
        r.i18n = buildI18nMeta(messageCode, parseArgs(args));
        return r;
    }

    /**
     * 失败返回（自定义消息、占位符参数和数据）
     * <p>
     * 返回状态码500，自定义返回错误消息模板代码，message字段存储占位符参数（按占位符顺序用逗号分隔），携带指定数据
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param messageCode 自定义返回错误消息模板代码
     * @param args        占位符参数，按占位符顺序用逗号分隔
     * @param data        返回数据
     * @return R包装的失败结果
     * @see StatusCode#BUSINESS_ERROR
     * @see I18nPrompt
     */
    public static <T> R<T> fail(I18nPrompt messageCode, String args, T data) {
        R<T> r = new R<>();
        r.code = StatusCode.BUSINESS_ERROR;
        r.messageCode = messageCode;
        r.message = args;
        r.i18n = buildI18nMeta(messageCode, parseArgs(args));
        r.data = data;
        return r;
    }

    /**
     * 失败返回（自定义状态码、消息、占位符参数和数据）
     * <p>
     * 返回自定义状态码，自定义返回错误消息模板代码，message字段存储占位符参数（按占位符顺序用逗号分隔），携带指定数据
     * </p>
     *
     * @param <T>         返回数据的类型
     * @param code        自定义状态码
     * @param messageCode 自定义返回错误消息模板代码
     * @param args        占位符参数，按占位符顺序用逗号分隔
     * @param data        返回数据
     * @return R包装的失败结果
     * @see StatusCode
     * @see I18nPrompt
     */
    public static <T> R<T> fail(Integer code, I18nPrompt messageCode, String args, T data) {
        R<T> r = new R<>();
        r.code = code;
        r.messageCode = messageCode;
        r.message = args;
        r.i18n = buildI18nMeta(messageCode, parseArgs(args));
        r.data = data;
        return r;
    }

    /**
     * 解析占位符参数
     * <p>
     * 将逗号分隔的字符串解析为参数数组
     * </p>
     *
     * @param args 逗号分隔的参数字符串
     * @return 参数数组
     */
    private static Object[] parseArgs(String args) {
        if (args == null || args.isEmpty()) {
            return null;
        }
        return args.split(",");
    }

    /**
     * 构建国际化元信息
     * <p>
     * 根据消息代码和参数构建国际化元信息，用于跨服务或网关的二次翻译
     * </p>
     *
     * @param messageCode 国际化消息代码
     * @param args        消息参数
     * @return 国际化元信息，messageCode为空时返回null
     */
    private static I18nMeta buildI18nMeta(I18nPrompt messageCode, Object[] args) {
        if (messageCode == null) {
            return null;
        }

        I18nMeta meta = new I18nMeta();
        meta.setModule(messageCode.getModule());
        meta.setCode(messageCode.getPromptCode());
        meta.setArgs(args);
        return meta;
    }
}
