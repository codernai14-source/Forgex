package com.forgex.common.tenant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * 租户隔离忽略切面
 * 基于配置表注册的服务类或Mapper方法，动态跳过租户隔离
 */
@Aspect
@Component
public class TenantIgnoreAspect {
    @Around("execution(* com.forgex..service..*(..)) || execution(* com.forgex..mapper..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        boolean setIgnore = false;
        try {
            MethodSignature ms = (MethodSignature) pjp.getSignature();
            Class<?> clazz = ms.getDeclaringType();
            String className = clazz.getName();
            String methodName = ms.getName();
            String methodFqn = className + "#" + methodName;

            if (TenantIgnoreRegistry.isServiceIgnored(className) || TenantIgnoreRegistry.isMapperMethodIgnored(methodFqn)) {
                TenantContextIgnore.setIgnore(true);
                setIgnore = true;
            }
            return pjp.proceed();
        } finally {
            if (setIgnore) {
                TenantContextIgnore.clear();
            }
        }
    }
}

