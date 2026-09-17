package com.forgex.common.security.desensitize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 按 {@link DesensitizeType} 对字符串做确定性遮蔽的 Jackson 序列化器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see Desensitize
 */
public class DesensitizeSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private final DesensitizeType type;

    /**
     * 默认按手机号规则遮蔽。
     */
    public DesensitizeSerializer() {
        this(DesensitizeType.PHONE);
    }

    /**
     * 指定遮蔽类型。
     *
     * @param type 脱敏类型
     */
    public DesensitizeSerializer(DesensitizeType type) {
        this.type = type;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) {
        if (property != null && property.getAnnotation(Desensitize.class) != null) {
            return new DesensitizeSerializer(property.getAnnotation(Desensitize.class).value());
        }
        return this;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(mask(value, type));
    }

    /**
     * 按类型遮蔽明文。
     *
     * @param value 原始值
     * @param type  脱敏类型
     * @return 遮蔽后的字符串
     */
    public static String mask(String value, DesensitizeType type) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return switch (type) {
            case EMAIL -> {
                int at = value.indexOf('@');
                yield at <= 1 ? "***" : value.charAt(0) + "***" + value.substring(at);
            }
            case PHONE -> value.length() <= 4
                    ? "****"
                    : value.substring(0, Math.min(3, value.length())) + "****"
                    + value.substring(Math.max(3, value.length() - 4));
            case ID_CARD, BANK_CARD -> value.length() <= 4 ? "****" : "****" + value.substring(value.length() - 4);
            case NAME -> value.length() <= 1 ? "*" : value.charAt(0) + "*".repeat(value.length() - 1);
            case ADDRESS -> value.length() <= 4
                    ? "****"
                    : value.substring(0, 2) + "****" + value.substring(value.length() - 2);
        };
    }
}
