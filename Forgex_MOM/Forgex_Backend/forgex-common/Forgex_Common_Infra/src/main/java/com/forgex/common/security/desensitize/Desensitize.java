package com.forgex.common.security.desensitize;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口响应层脱敏注解。
 * <p>
 * 仅影响 JSON 展示，不改变数据库存储值。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DesensitizeSerializer
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizeSerializer.class)
public @interface Desensitize {

    /**
     * 脱敏策略。
     *
     * @return 策略类型
     */
    DesensitizeType value();
}
