package com.forgex.common.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.dict.DictI18n;
import com.forgex.common.dict.DictI18nResolver;
import com.forgex.common.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class RDictI18nAdvice implements ResponseBodyAdvice<Object> {
    private final DictI18nResolver dictI18nResolver;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            return body;
        }
        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        if (body instanceof R<?> r) {
            translateAny(r.getData(), tenantId, visited);
            return body;
        }
        translateAny(body, tenantId, visited);
        return body;
    }

    private void translateAny(Object obj, Long tenantId, IdentityHashMap<Object, Boolean> visited) {
        if (obj == null) {
            return;
        }
        if (visited.containsKey(obj)) {
            return;
        }
        visited.put(obj, Boolean.TRUE);

        if (obj instanceof IPage<?> page) {
            translateAny(page.getRecords(), tenantId, visited);
            return;
        }
        if (obj instanceof Iterable<?> it) {
            for (Object e : it) {
                translateAny(e, tenantId, visited);
            }
            return;
        }
        if (obj instanceof Map<?, ?> map) {
            for (Object v : map.values()) {
                translateAny(v, tenantId, visited);
            }
            return;
        }
        Class<?> cls = obj.getClass();
        if (cls.isPrimitive() || cls.isEnum()) {
            return;
        }
        String cn = cls.getName();
        if (cn.startsWith("java.") || cn.startsWith("javax.") || cn.startsWith("jakarta.")) {
            return;
        }

        Field[] fields = cls.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            DictI18n ann = f.getAnnotation(DictI18n.class);
            if (ann != null) {
                applyDictTranslation(obj, f, ann, tenantId);
                continue;
            }
            try {
                Object v = f.get(obj);
                if (v == null) {
                    continue;
                }
                Class<?> t = v.getClass();
                if (t.isPrimitive() || t.isEnum()) {
                    continue;
                }
                String tn = t.getName();
                if (tn.startsWith("java.") || tn.startsWith("javax.") || tn.startsWith("jakarta.")) {
                    continue;
                }
                translateAny(v, tenantId, visited);
            } catch (Exception ignored) {}
        }
    }

    private void applyDictTranslation(Object obj, Field annotatedField, DictI18n ann, Long tenantId) {
        String targetFieldName = ann.targetField();
        if (!StringUtils.hasText(targetFieldName)) {
            return;
        }
        String nodePath = ann.nodePathConst();
        if (!StringUtils.hasText(nodePath) && StringUtils.hasText(ann.nodePathField())) {
            Object v = readField(obj, ann.nodePathField());
            if (v != null) {
                nodePath = String.valueOf(v);
            }
        }
        if (!StringUtils.hasText(nodePath)) {
            return;
        }

        Object rawKey;
        if (StringUtils.hasText(ann.valueField())) {
            rawKey = readField(obj, ann.valueField());
        } else {
            rawKey = readField(obj, annotatedField.getName());
        }
        if (rawKey == null) {
            return;
        }
        String key = String.valueOf(rawKey);
        if (!StringUtils.hasText(key)) {
            return;
        }

        Map<String, String> map = dictI18nResolver.getChildLabelMap(tenantId, nodePath);
        if (map == null || map.isEmpty()) {
            return;
        }
        String label = map.get(key);
        if (!StringUtils.hasText(label)) {
            return;
        }
        writeField(obj, targetFieldName, label);
    }

    private Object readField(Object obj, String name) {
        if (obj == null || !StringUtils.hasText(name)) {
            return null;
        }
        Field f = findField(obj.getClass(), name);
        if (f == null) {
            return null;
        }
        try {
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeField(Object obj, String name, Object value) {
        Field f = findField(obj.getClass(), name);
        if (f == null) {
            return;
        }
        try {
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception ignored) {}
    }

    private Field findField(Class<?> cls, String name) {
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            }
        }
        return null;
    }
}

