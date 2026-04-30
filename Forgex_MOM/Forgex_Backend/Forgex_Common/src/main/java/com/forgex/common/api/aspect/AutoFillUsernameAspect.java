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
package com.forgex.common.api.aspect;

import com.forgex.common.api.annotation.AutoFillUsername;
import com.forgex.common.api.feign.SysUserFeignClient;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自动填充用户名切面
 *
 * @author coder_nai@163.com
 * @date 2026-01-27
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AutoFillUsernameAspect {

    private final SysUserFeignClient sysUserFeignClient;

    @Around("execution(public * com.forgex..controller..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (result == null) {
            return null;
        }
        processResult(result);
        return result;
    }

    private void processResult(Object result) {
        try {
            Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            if (result instanceof R) {
                Object data = ((R<?>) result).getData();
                if (data != null) {
                    fillUsername(data, visited);
                }
                return;
            }
            fillUsername(result, visited);
        } catch (Exception e) {
            log.error("自动填充用户名失败", e);
        }
    }

    private void fillUsername(Object obj, Set<Object> visited) throws Exception {
        if (obj == null) {
            return;
        }

        if (obj instanceof Collection) {
            for (Object item : (Collection<?>) obj) {
                fillUsername(item, visited);
            }
            return;
        }

        if (obj instanceof Map) {
            for (Object value : ((Map<?, ?>) obj).values()) {
                fillUsername(value, visited);
            }
            return;
        }

        if (isPageObject(obj)) {
            fillPageRecords(obj, visited);
            return;
        }

        Class<?> clazz = obj.getClass();
        if (clazz.isPrimitive() || clazz.getName().startsWith("java.")) {
            return;
        }

        if (!visited.add(obj)) {
            return;
        }

        List<FieldFillInfo> fillInfos = collectFillInfos(obj, clazz);
        if (!fillInfos.isEmpty()) {
            fillFieldValues(obj, fillInfos);
        }
        fillNestedFields(obj, clazz, visited);
    }

    private boolean isPageObject(Object obj) {
        String className = obj.getClass().getName();
        return className.contains("IPage") || className.contains("Page");
    }

    private void fillPageRecords(Object pageObject, Set<Object> visited) throws Exception {
        try {
            Field recordsField = pageObject.getClass().getDeclaredField("records");
            recordsField.setAccessible(true);
            fillUsername(recordsField.get(pageObject), visited);
        } catch (NoSuchFieldException ignored) {
            // ignore
        }
    }

    private List<FieldFillInfo> collectFillInfos(Object obj, Class<?> clazz) throws IllegalAccessException {
        List<FieldFillInfo> fillInfos = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            AutoFillUsername annotation = field.getAnnotation(AutoFillUsername.class);
            if (annotation == null) {
                continue;
            }
            Long userId = resolveUserId(obj, clazz, annotation);
            if (userId != null) {
                field.setAccessible(true);
                fillInfos.add(new FieldFillInfo(field, userId, annotation.required()));
            }
        }
        return fillInfos;
    }

    private Long resolveUserId(Object obj, Class<?> clazz, AutoFillUsername annotation) throws IllegalAccessException {
        String userIdFieldName = annotation.userIdField();
        try {
            Field userIdField = clazz.getDeclaredField(userIdFieldName);
            userIdField.setAccessible(true);
            Object userIdValue = userIdField.get(obj);
            if (userIdValue == null) {
                if (annotation.required()) {
                    throw new I18nBusinessException(
                            StatusCode.BUSINESS_ERROR,
                            CommonPrompt.USER_ID_FIELD_CANNOT_BE_EMPTY,
                            userIdFieldName
                    );
                }
                return null;
            }
            if (userIdValue instanceof Long) {
                return (Long) userIdValue;
            }
            if (userIdValue instanceof Integer) {
                return ((Integer) userIdValue).longValue();
            }
            if (userIdValue instanceof String && !((String) userIdValue).isBlank()) {
                return Long.parseLong((String) userIdValue);
            }
            return null;
        } catch (NoSuchFieldException e) {
            log.error("找不到用户ID字段: {}", userIdFieldName);
            if (annotation.required()) {
                throw new I18nBusinessException(
                        StatusCode.BUSINESS_ERROR,
                        CommonPrompt.USER_ID_FIELD_NOT_FOUND,
                        userIdFieldName
                );
            }
            return null;
        }
    }

    private void fillFieldValues(Object obj, List<FieldFillInfo> fillInfos) throws IllegalAccessException {
        List<Long> userIds = fillInfos.stream()
                .map(FieldFillInfo::getUserId)
                .distinct()
                .collect(Collectors.toList());

        R<Map<Long, String>> response = sysUserFeignClient.getUsernameMap(userIds);
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            log.warn("批量查询用户名失败: {}", response);
            return;
        }

        Map<Long, String> usernameMap = response.getData();
        for (FieldFillInfo fillInfo : fillInfos) {
            String username = usernameMap.get(fillInfo.getUserId());
            if (username != null) {
                fillInfo.getField().set(obj, username);
            } else if (fillInfo.isRequired()) {
                throw new I18nBusinessException(
                        StatusCode.BUSINESS_ERROR,
                        CommonPrompt.USER_BY_ID_NOT_FOUND,
                        fillInfo.getUserId()
                );
            }
        }
    }

    private void fillNestedFields(Object obj, Class<?> clazz, Set<Object> visited) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            Object nestedValue = field.get(obj);
            if (nestedValue == null) {
                continue;
            }
            fillUsername(nestedValue, visited);
        }
    }

    private static class FieldFillInfo {
        private final Field field;
        private final Long userId;
        private final boolean required;

        FieldFillInfo(Field field, Long userId, boolean required) {
            this.field = field;
            this.userId = userId;
            this.required = required;
        }

        public Field getField() {
            return field;
        }

        public Long getUserId() {
            return userId;
        }

        public boolean isRequired() {
            return required;
        }
    }
}
