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

