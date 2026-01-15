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
    /** 返回数据：泛型类型，可为null */
    private T data;

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
        // 设置成功消息
        r.message = "OK";
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
        // 设置成功消息
        r.message = "OK";
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
}
