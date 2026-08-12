package com.forgex.common.security.perm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限校验注解（按钮级）。
 * <p>
 * 标注在 Controller 类或方法上，表示调用该接口需要具备指定的 permKey（按钮权限键）。
 * 实际校验由 {@link PermissionInterceptor} 完成。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see PermissionInterceptor
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePerm {

    /**
     * 需要的权限键列表。
     *
     * @return 权限键数组
     */
    String[] value();
}

