/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.common.web;

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
 *   <li>code：状态码，200表示成功，非200表示失败</li>
 *   <li>message：返回消息，成功时为"OK"，失败时为错误信息</li>
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
 * R.fail("错误信息");
 * }</pre>
 *
 * @param <T> 返回数据的类型
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class R<T> {
    /** 状态码：200表示成功，非200表示失败 */
    private int code;
    /** 返回消息：成功时为"OK"，失败时为错误信息 */
    private String message;
    @JsonIgnore
    private I18nPrompt msg;
    @JsonIgnore
    private Object[] msgArgs;
    /**
     * 国际化元信息（可选）
     * <p>
     * 用于跨服务、网关或前端需要“二次翻译”的场景：当 {@link #msg} 无法直接序列化/反序列化时，
     * 仍可通过 module + code + args 反查语言库。
     * </p>
     */
    private I18nMeta i18n;
    /** 返回数据：泛型类型，可为null */
    private T data;

    /**
     * 国际化元信息结构
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
     * 成功返回（无数据）
     * <p>返回状态码200，消息为"OK"，数据为null</p>
     *
     * @param <T> 返回数据的类型
     * @return R包装的成功结果
     */
    public static <T> R<T> ok() {
        // 创建R对象
        R<T> r = new R<>();
        // 设置成功状态码
        r.code = 200;
        return r;
    }

    public static <T> R<T> ok(I18nPrompt msg, Object... msgArgs) {
        R<T> r = new R<>();
        r.code = 200;
        r.msg = msg;
        r.msgArgs = msgArgs;
        r.i18n = buildI18nMeta(msg, msgArgs);
        return r;
    }

    /**
     * 成功返回（带自定义消息）
     * <p>返回状态码200，消息为自定义消息，数据为null</p>
     *
     * @param <T> 返回数据的类型
     * @param message 成功消息
     * @return R包装的成功结果
     */
    public static <T> R<T> ok(String message) {
        // 创建R对象
        R<T> r = new R<>();
        // 设置成功状态码
        r.code = 200;
        // 设置自定义成功消息
        r.message = message;
        return r;
    }

    /**
     * 成功返回（带消息和数据）
     * <p>返回状态码200，消息为自定义消息，数据为指定数据</p>
     *
     * @param <T> 返回数据的类型
     * @param message 成功消息
     * @param data 数据
     * @return R包装的成功结果
     */
    public static <T> R<T> ok(String message, T data) {
        // 创建R对象
        R<T> r = new R<>();
        // 设置成功状态码
        r.code = 200;
        // 设置自定义成功消息
        r.message = message;
        // 设置返回数据
        r.data = data;
        return r;
    }

    /**
     * 成功返回（带数据）
     * <p>返回状态码200，消息为"OK"，数据为指定数据</p>
     *
     * @param <T> 返回数据的类型
     * @param data 数据
     * @return R包装的成功结果
     */
    public static <T> R<T> ok(T data) {
        // 创建R对象
        R<T> r = new R<>();
        // 设置成功状态码
        r.code = 200;
        // 设置返回数据
        r.data = data;
        return r;
    }

    /**
     * 失败返回（默认状态码500）
     * <p>返回状态码500，消息为错误信息，数据为null</p>
     *
     * @param <T> 返回数据的类型
     * @param message 错误信息
     * @return R包装的失败结果
     */
    public static <T> R<T> fail(String message) {
        // 创建R对象
        R<T> r = new R<>();
        // 设置失败状态码
        r.code = 500;
        // 设置错误信息
        r.message = message;
        return r;
    }

    public static <T> R<T> fail(I18nPrompt msg, Object... msgArgs) {
        R<T> r = new R<>();
        r.code = 500;
        r.msg = msg;
        r.msgArgs = msgArgs;
        r.i18n = buildI18nMeta(msg, msgArgs);
        return r;
    }

    /**
     * 失败返回（自定义状态码）
     * <p>返回自定义状态码，消息为错误信息，数据为null</p>
     *
     * @param <T> 返回数据的类型
     * @param code 状态码
     * @param message 错误信息
     * @return R包装的失败结果
     */
    public static <T> R<T> fail(int code, String message) {
        // 创建R对象
        R<T> r = new R<>();
        // 设置自定义状态码
        r.code = code;
        // 设置错误信息
        r.message = message;
        return r;
    }

    public static <T> R<T> fail(int code, I18nPrompt msg, Object... msgArgs) {
        R<T> r = new R<>();
        r.code = code;
        r.msg = msg;
        r.msgArgs = msgArgs;
        r.i18n = buildI18nMeta(msg, msgArgs);
        return r;
    }

    /**
     * 构建国际化元信息
     *
     * @param msg 国际化提示
     * @param msgArgs 参数
     * @return 国际化元信息；msg 为空时返回 null
     */
    private static I18nMeta buildI18nMeta(I18nPrompt msg, Object[] msgArgs) {
        if (msg == null) {
            return null;
        }

        I18nMeta meta = new I18nMeta();
        meta.setModule(msg.getModule());
        meta.setCode(msg.getPromptCode());
        meta.setArgs(msgArgs);
        return meta;
    }
}
