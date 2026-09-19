package com.forgex.job.annotation;

import org.springframework.stereotype.Component;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Job 处理器标记注解。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface FxJobHandler {

    /**
     * Spring Bean 名称。
     *
     * @return Bean 名称
     */
    @AliasFor(annotation = Component.class, attribute = "value")
    String value() default "";
}
