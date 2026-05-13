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

import com.baomidou.mybatisplus.core.metadata.IPage;
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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自动填充用户名切面。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2026-01-27
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AutoFillUsernameAspect {

    private final SysUserFeignClient sysUserFeignClient;

    /**
     * 拦截 Controller 响应并自动填充用户名。
     *
     * @param joinPoint joinpoint
     * @return 处理结果
     */
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
            List<FieldFillInfo> fillInfos = new ArrayList<>();
            if (result instanceof R) {
                Object data = ((R<?>) result).getData();
                if (data != null) {
                    collectUsernameFillInfos(data, visited, fillInfos);
                }
            } else {
                collectUsernameFillInfos(result, visited, fillInfos);
            }
            fillFieldValues(fillInfos);
        } catch (Exception e) {
            log.error("自动填充用户名失败", e);
        }
    }

    private void collectUsernameFillInfos(Object obj, Set<Object> visited, List<FieldFillInfo> fillInfos) throws Exception {
        if (obj == null) {
            return;
        }

        if (obj instanceof Collection) {
            for (Object item : (Collection<?>) obj) {
                collectUsernameFillInfos(item, visited, fillInfos);
            }
            return;
        }

        if (obj instanceof Map) {
            for (Object value : ((Map<?, ?>) obj).values()) {
                collectUsernameFillInfos(value, visited, fillInfos);
            }
            return;
        }

        if (obj instanceof IPage) {
            collectUsernameFillInfos(((IPage<?>) obj).getRecords(), visited, fillInfos);
            return;
        }

        Class<?> clazz = obj.getClass();
        if (!isBusinessObjectType(clazz)) {
            return;
        }

        if (isPageObject(obj)) {
            collectPageRecordFillInfos(obj, visited, fillInfos);
            return;
        }

        if (!visited.add(obj)) {
            return;
        }

        collectFillInfos(obj, clazz, fillInfos);
        collectNestedFillInfos(obj, clazz, visited, fillInfos);
    }

    private boolean isPageObject(Object obj) {
        String className = obj.getClass().getName();
        return className.contains("IPage") || className.contains("Page");
    }

    private void collectPageRecordFillInfos(Object pageObject, Set<Object> visited, List<FieldFillInfo> fillInfos) throws Exception {
        try {
            Field recordsField = findField(pageObject.getClass(), "records");
            recordsField.setAccessible(true);
            collectUsernameFillInfos(recordsField.get(pageObject), visited, fillInfos);
        } catch (NoSuchFieldException ignored) {
            Method getRecordsMethod = pageObject.getClass().getMethod("getRecords");
            collectUsernameFillInfos(getRecordsMethod.invoke(pageObject), visited, fillInfos);
        }
    }

    private void collectFillInfos(Object obj, Class<?> clazz, List<FieldFillInfo> fillInfos) throws IllegalAccessException {
        for (Field field : getAllFields(clazz)) {
            AutoFillUsername annotation = field.getAnnotation(AutoFillUsername.class);
            if (annotation == null) {
                continue;
            }
            Long userId = resolveUserId(obj, clazz, annotation);
            if (userId != null) {
                makeAccessible(field);
                fillInfos.add(new FieldFillInfo(obj, field, userId, annotation.required()));
            }
        }
    }

    private Long resolveUserId(Object obj, Class<?> clazz, AutoFillUsername annotation) throws IllegalAccessException {
        String userIdFieldName = annotation.userIdField();
        try {
            Field userIdField = findField(clazz, userIdFieldName);
            makeAccessible(userIdField);
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
                try {
                    return Long.parseLong(((String) userIdValue).trim());
                } catch (NumberFormatException e) {
                    if (annotation.required()) {
                        log.warn("用户ID字段 {} 的值不是数字: {}", userIdFieldName, userIdValue);
                    }
                    return null;
                }
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

    private void fillFieldValues(List<FieldFillInfo> fillInfos) throws IllegalAccessException {
        if (fillInfos.isEmpty()) {
            return;
        }

        List<Long> userIds = fillInfos.stream()
                .map(FieldFillInfo::getUserId)
                .distinct()
                .collect(Collectors.toList());

        R<Map<Long, String>> response = sysUserFeignClient.getUsernameMap(userIds);
        if (response == null || response.getCode() != 200 || response.getData() == null) {
            log.warn("批量查询用户名失败: {}", response);
            return;
        }

        Map<?, ?> usernameMap = response.getData();
        for (FieldFillInfo fillInfo : fillInfos) {
            String username = resolveUsername(usernameMap, fillInfo.getUserId());
            if (username != null) {
                fillInfo.getField().set(fillInfo.getTarget(), username);
            } else if (fillInfo.isRequired()) {
                throw new I18nBusinessException(
                        StatusCode.BUSINESS_ERROR,
                        CommonPrompt.USER_BY_ID_NOT_FOUND,
                        fillInfo.getUserId()
                );
            }
        }
    }

    private void collectNestedFillInfos(Object obj, Class<?> clazz, Set<Object> visited, List<FieldFillInfo> fillInfos) throws Exception {
        for (Field field : getAllFields(clazz)) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            if (!canInspectField(field)) {
                continue;
            }
            makeAccessible(field);
            Object nestedValue = field.get(obj);
            if (nestedValue == null) {
                continue;
            }
            collectUsernameFillInfos(nestedValue, visited, fillInfos);
        }
    }

    private String resolveUsername(Map<?, ?> usernameMap, Long userId) {
        Object username = usernameMap.get(userId);
        if (username == null) {
            username = usernameMap.get(String.valueOf(userId));
        }
        if (username == null) {
            String userIdText = String.valueOf(userId);
            for (Map.Entry<?, ?> entry : usernameMap.entrySet()) {
                if (userIdText.equals(String.valueOf(entry.getKey()))) {
                    username = entry.getValue();
                    break;
                }
            }
        }
        return username == null ? null : String.valueOf(username);
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            Collections.addAll(fields, currentClass.getDeclaredFields());
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                currentClass = currentClass.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private void makeAccessible(Field field) {
        field.setAccessible(true);
    }

    private boolean canInspectField(Field field) {
        Class<?> type = field.getType();
        if (isSimpleValueType(type)) {
            return false;
        }
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type) || IPage.class.isAssignableFrom(type)) {
            return true;
        }
        return isBusinessObjectType(type);
    }

    private boolean isBusinessObjectType(Class<?> clazz) {
        return !isSimpleValueType(clazz)
                && !clazz.isArray()
                && !clazz.getName().startsWith("java.")
                && !clazz.getName().startsWith("javax.")
                && !clazz.getName().startsWith("jakarta.")
                && !clazz.getName().startsWith("sun.")
                && !clazz.getName().startsWith("jdk.")
                && !clazz.getName().startsWith("com.sun.");
    }

    private boolean isSimpleValueType(Class<?> clazz) {
        return clazz == null
                || clazz.isPrimitive()
                || clazz.isEnum()
                || CharSequence.class.isAssignableFrom(clazz)
                || Number.class.isAssignableFrom(clazz)
                || Boolean.class == clazz
                || Character.class == clazz
                || Date.class.isAssignableFrom(clazz)
                || Temporal.class.isAssignableFrom(clazz)
                || Optional.class.isAssignableFrom(clazz)
                || Class.class == clazz;
    }

    private static class FieldFillInfo {
        private final Object target;
        private final Field field;
        private final Long userId;
        private final boolean required;

        FieldFillInfo(Object target, Field field, Long userId, boolean required) {
            this.target = target;
            this.field = field;
            this.userId = userId;
            this.required = required;
        }

        public Object getTarget() {
            return target;
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
