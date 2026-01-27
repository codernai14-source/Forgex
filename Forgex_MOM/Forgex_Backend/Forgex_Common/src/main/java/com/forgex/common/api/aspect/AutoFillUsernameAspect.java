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
import com.forgex.common.api.dto.UserInfoDTO;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自动填充用户名切面
 * <p>拦截方法返回值，自动填充带有 @AutoFillUsername 注解的字段</p>
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
    
    /**
     * 拦截所有 Controller 方法的返回值
     */
    @Around("execution(public * com.forgex..controller..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 执行原方法
        Object result = joinPoint.proceed();
        
        // 如果返回值为空，直接返回
        if (result == null) {
            return result;
        }
        
        // 处理返回值
        processResult(result);
        
        return result;
    }
    
    /**
     * 处理返回结果
     */
    private void processResult(Object result) {
        try {
            // 如果是 R 类型，处理其 data 字段
            if (result instanceof R) {
                R<?> r = (R<?>) result;
                Object data = r.getData();
                if (data != null) {
                    fillUsername(data);
                }
            } else {
                // 直接处理对象
                fillUsername(result);
            }
        } catch (Exception e) {
            log.error("自动填充用户名失败", e);
        }
    }
    
    /**
     * 填充用户名
     */
    private void fillUsername(Object obj) throws Exception {
        if (obj == null) {
            return;
        }
        
        // 处理集合类型
        if (obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            for (Object item : collection) {
                fillUsername(item);
            }
            return;
        }
        
        // 处理分页对象
        if (obj.getClass().getName().contains("IPage") || obj.getClass().getName().contains("Page")) {
            try {
                Field recordsField = obj.getClass().getDeclaredField("records");
                recordsField.setAccessible(true);
                Object records = recordsField.get(obj);
                if (records instanceof Collection) {
                    fillUsername(records);
                }
            } catch (NoSuchFieldException e) {
                // 忽略
            }
            return;
        }
        
        // 处理普通对象
        Class<?> clazz = obj.getClass();
        
        // 跳过基本类型和常见类型
        if (clazz.isPrimitive() || clazz.getName().startsWith("java.")) {
            return;
        }
        
        // 收集需要填充的字段信息
        List<FieldFillInfo> fillInfos = new ArrayList<>();
        
        for (Field field : clazz.getDeclaredFields()) {
            AutoFillUsername annotation = field.getAnnotation(AutoFillUsername.class);
            if (annotation != null) {
                field.setAccessible(true);
                
                // 获取用户ID字段
                String userIdFieldName = annotation.userIdField();
                try {
                    Field userIdField = clazz.getDeclaredField(userIdFieldName);
                    userIdField.setAccessible(true);
                    Object userIdValue = userIdField.get(obj);
                    
                    if (userIdValue != null) {
                        Long userId = null;
                        if (userIdValue instanceof Long) {
                            userId = (Long) userIdValue;
                        } else if (userIdValue instanceof Integer) {
                            userId = ((Integer) userIdValue).longValue();
                        } else if (userIdValue instanceof String) {
                            userId = Long.parseLong((String) userIdValue);
                        }
                        
                        if (userId != null) {
                            fillInfos.add(new FieldFillInfo(field, userId, annotation.required()));
                        }
                    } else if (annotation.required()) {
                        throw new BusinessException("用户ID字段 " + userIdFieldName + " 不能为空");
                    }
                } catch (NoSuchFieldException e) {
                    log.error("找不到用户ID字段: {}", userIdFieldName);
                    if (annotation.required()) {
                        throw new BusinessException("找不到用户ID字段: " + userIdFieldName);
                    }
                }
            }
        }
        
        // 批量查询用户名
        if (!fillInfos.isEmpty()) {
            List<Long> userIds = fillInfos.stream()
                    .map(FieldFillInfo::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            R<Map<Long, String>> response = sysUserFeignClient.getUsernameMap(userIds);
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                Map<Long, String> usernameMap = response.getData();
                
                // 填充用户名
                for (FieldFillInfo fillInfo : fillInfos) {
                    String username = usernameMap.get(fillInfo.getUserId());
                    if (username != null) {
                        fillInfo.getField().set(obj, username);
                    } else if (fillInfo.isRequired()) {
                        throw new BusinessException("找不到用户ID为 " + fillInfo.getUserId() + " 的用户");
                    }
                }
            } else {
                log.warn("批量查询用户名失败: {}", response);
            }
        }
    }
    
    /**
     * 字段填充信息
     */
    private static class FieldFillInfo {
        private final Field field;
        private final Long userId;
        private final boolean required;
        
        public FieldFillInfo(Field field, Long userId, boolean required) {
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

