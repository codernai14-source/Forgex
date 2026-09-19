package com.forgex.common.security.desensitize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 审计与接口响应共用的脱敏 Jackson 模块。
 * <p>
 * 优先识别 {@link Desensitize} 注解；对未标注但字段名属于敏感词的属性按名称兜底遮蔽。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DesensitizeSerializer
 */
public class DesensitizeModule extends SimpleModule {

    private static final Set<String> NAME_HINTS = Set.of(
            "password", "token", "secret", "phone", "mobile", "email",
            "idcard", "bankaccount", "recoverycode"
    );

    /**
     * 创建脱敏模块。
     */
    public DesensitizeModule() {
        super("forgex-desensitize");
        setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                             BeanDescription beanDesc,
                                                             List<BeanPropertyWriter> beanProperties) {
                for (BeanPropertyWriter writer : beanProperties) {
                    if (writer.getAnnotation(Desensitize.class) != null) {
                        continue;
                    }
                    String name = writer.getName() == null ? "" : writer.getName().toLowerCase(Locale.ROOT);
                    if (matchesHint(name)) {
                        writer.assignSerializer(new NameHintSerializer(name));
                    }
                }
                return beanProperties;
            }
        });
    }

    private static boolean matchesHint(String name) {
        for (String hint : NAME_HINTS) {
            if (name.contains(hint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按字段名兜底遮蔽。
     */
    private static final class NameHintSerializer extends JsonSerializer<Object> {

        private final String name;

        private NameHintSerializer(String name) {
            this.name = name;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            DesensitizeType type = DesensitizeType.PHONE;
            if (name.contains("email")) {
                type = DesensitizeType.EMAIL;
            } else if (name.contains("idcard")) {
                type = DesensitizeType.ID_CARD;
            } else if (name.contains("bank")) {
                type = DesensitizeType.BANK_CARD;
            } else if (name.contains("password") || name.contains("token") || name.contains("secret") || name.contains("recovery")) {
                gen.writeString("***");
                return;
            }
            gen.writeString(DesensitizeSerializer.mask(String.valueOf(value), type));
        }
    }
}
