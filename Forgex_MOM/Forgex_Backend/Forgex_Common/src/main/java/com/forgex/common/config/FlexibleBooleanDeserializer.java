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
package com.forgex.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Boolean 兼容反序列化器
 * <p>
 * 兼容前端常见的布尔传值形式，支持：
 * 1. JSON 原生布尔值：{@code true}/{@code false}
 * 2. 数字：{@code 1}/{@code 0}
 * 3. 字符串：{@code "true"}/{@code "false"}/{@code "1"}/{@code "0"}
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-10
 */
public class FlexibleBooleanDeserializer extends JsonDeserializer<Boolean> {

    /**
     * 将多种布尔表示形式反序列化为 Boolean。
     *
     * @param parser 当前 JSON 解析器
     * @param context 反序列化上下文
     * @return 解析后的 Boolean 值
     * @throws IOException 当 JSON 读取失败或值格式不受支持时抛出
     */
    @Override
    public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }
        if (token == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }
        if (token == JsonToken.VALUE_NUMBER_INT) {
            return parseNumber(parser.getIntValue(), context);
        }
        if (token == JsonToken.VALUE_STRING) {
            return parseText(parser.getText(), context);
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        return (Boolean) context.handleUnexpectedToken(Boolean.class, parser);
    }

    /**
     * 解析数字形式的布尔值，仅允许 1 和 0。
     *
     * @param value 数字值
     * @param context 反序列化上下文
     * @return 对应的布尔值
     * @throws IOException 当值不是 1 或 0 时抛出
     */
    private Boolean parseNumber(int value, DeserializationContext context) throws IOException {
        if (value == 1) {
            return Boolean.TRUE;
        }
        if (value == 0) {
            return Boolean.FALSE;
        }
        return (Boolean) context.handleWeirdNumberValue(Boolean.class, value, "仅支持 1 或 0");
    }

    /**
     * 解析字符串形式的布尔值。
     *
     * @param text 原始字符串
     * @param context 反序列化上下文
     * @return 对应的布尔值
     * @throws IOException 当值不属于支持范围时抛出
     */
    private Boolean parseText(String text, DeserializationContext context) throws IOException {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        if (normalized.isEmpty() || "null".equalsIgnoreCase(normalized)) {
            return null;
        }
        if ("true".equalsIgnoreCase(normalized) || "1".equals(normalized)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(normalized) || "0".equals(normalized)) {
            return Boolean.FALSE;
        }
        return (Boolean) context.handleWeirdStringValue(Boolean.class, normalized, "仅支持 true/false 或 1/0");
    }
}
