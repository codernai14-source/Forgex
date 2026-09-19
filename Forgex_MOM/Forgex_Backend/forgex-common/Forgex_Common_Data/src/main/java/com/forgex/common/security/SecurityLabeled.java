package com.forgex.common.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 Mapper 方法需要叠加强制访问控制条件。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.dataperm.DataPermissionInterceptor
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecurityLabeled {

    /**
     * 安全标记列名。
     *
     * @return 列名
     */
    String column() default "security_label";

    /**
     * 表别名，可为空。
     *
     * @return 别名
     */
    String alias() default "";

    /**
     * 是否启用强制访问控制。
     *
     * @return true 表示启用
     */
    boolean enabled() default true;
}
