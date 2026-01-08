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
 * 统一返回结果
 * code：0成功；非0失败
 */
@Data
public class R<T> {
    private int code;
    private String message;
    private T data;

    /**
     * 成功返回（无数据）
     * @return R包装的成功结果
     */
    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "OK";
        return r;
    }

    /**
     * 成功返回（带自定义消息）
     * @param message 成功消息
     * @return R包装的成功结果
     */
    public static <T> R<T> ok(String message) {
        R<T> r = new R<>();
        r.code = 200;
        r.message = message;
        return r;
    }

    /**
     * 成功返回（带消息和数据）
     * @param message 成功消息
     * @param data 数据
     * @return R包装的成功结果
     */
    public static <T> R<T> ok(String message, T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.message = message;
        r.data = data;
        return r;
    }

    /**
     * 成功返回（带数据）
     * @param data 数据
     * @return R包装的成功结果
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "OK";
        r.data = data;
        return r;
    }

    /**
     * 失败返回
     * @param message 错误信息
     * @return R包装的失败结果
     */
    public static <T> R<T> fail(String message) {
        R<T> r = new R<>();
        r.code = 500;
        r.message = message;
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
