package com.forgex.common.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.dict.DictI18n;
import com.forgex.common.dict.DictI18nResolver;
import com.forgex.common.dict.DictItem;
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

/**
 * 字典国际化ResponseBodyAdvice
 * <p>
 * 实现Spring的ResponseBodyAdvice接口，用于在响应返回前自动翻译字典值。
 * 通过反射机制，自动识别并处理带有@DictI18n注解的字段，
 * 根据当前租户的语言环境，将字典值转换为对应的国际化文本。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>拦截所有Controller方法的返回值</li>
 *   <li>自动识别带有@DictI18n注解的字段</li>
 *   <li>根据租户ID和字典节点路径，查询字典标签映射</li>
 *   <li>将字典值替换为对应的国际化文本</li>
 *   <li>支持R包装类、IPage分页、Iterable集合、Map对象等多种返回类型</li>
 *   <li>递归处理嵌套对象和集合类型</li>
 * </ul>
 * <p><strong>处理流程：</strong></p>
 * <ol>
 *   <li>获取当前租户ID，未登录时直接返回原值</li>
 *   <li>创建访问标记Map，避免循环引用导致的重复处理</li>
 *   <li>判断返回值类型，分别处理不同类型</li>
 *   <li>对于R包装类，处理其data字段</li>
 *   <li>对于IPage分页，处理其records字段</li>
 *   <li>对于Iterable集合，递归处理每个元素</li>
 *   <li>对于Map对象，递归处理每个值</li>
 *   <li>通过反射获取类字段，查找@DictI18n注解</li>
 *   <li>根据注解配置，获取字典节点路径和字典值</li>
 *   <li>调用DictI18nResolver查询字典标签映射</li>
 *   <li>将字典值替换为对应的国际化文本</li>
 *   <li>通过反射设置目标字段的值</li>
 * </ol>
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * public class UserVO {
 *     private Long id;
 *     
 *     @DictI18n(
 *         nodePathField = "dictNodePath",
 *         valueField = "status",
 *         targetField = "statusText"
 *     )
 *     private Integer status;
 *     
 *     private String dictNodePath;
 *     private String statusText; // 自动填充国际化文本
 * }
 * 
 * // Controller方法返回UserVO时，status字段会自动被翻译为国际化文本
 * }</pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DictI18n
 * @see DictI18nResolver
 * @see com.forgex.common.tenant.TenantContext
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class RDictI18nAdvice implements ResponseBodyAdvice<Object> {
    /**
     * 字典国际化解析器
     * <p>用于查询字典标签映射，支持多语言回退机制。</p>
     */
    private final DictI18nResolver dictI18nResolver;

    /**
     * 判断是否支持该返回类型
     * <p>所有类型都支持字典国际化处理。</p>
     * 
     * @param returnType 返回值类型
     * @param selectedConverterType 选择的转换器类型
     * @return true
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 在响应体写入前处理字典国际化
     * <p>
     * 拦截Controller方法的返回值，自动翻译字典值为国际化文本。
     * </p>
     * <p><strong>处理流程：</strong></p>
     * <ol>
     *   <li>获取当前租户ID，未登录时直接返回原值</li>
     *   <li>创建访问标记Map，避免循环引用导致的重复处理</li>
     *   <li>判断返回值类型，分别处理不同类型</li>
     *   <li>对于R包装类，处理其data字段</li>
     *   <li>对于IPage分页，处理其records字段</li>
     *   <li>对于Iterable集合，递归处理每个元素</li>
     *   <li>对于Map对象，递归处理每个值</li>
     * </ol>
     * 
     * @param body 返回值对象
     * @param returnType 返回值类型参数
     * @param selectedContentType 选择的媒体类型
     * @param selectedConverterType 选择的转换器类型
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @return 处理后的返回值对象
     */
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        // 获取当前租户ID
        Long tenantId = TenantContext.get();
        
        // 未登录时直接返回原值
        if (tenantId == null) {
            return body;
        }

        // 创建访问标记Map，避免循环引用导致的重复处理
        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        
        // 如果是R包装类，处理其data字段
        if (body instanceof R<?> r) {
            translateAny(r.getData(), tenantId, visited);
            return body;
        }
        
        // 处理其他类型的返回值
        translateAny(body, tenantId, visited);
        return body;
    }

    /**
     * 递归翻译对象中的字典值
     * <p>
     * 根据对象类型，递归遍历并翻译所有字典值。
     * 支持IPage、Iterable、Map和普通对象类型。
     * </p>
     * <p><strong>处理流程：</strong></p>
     * <ol>
     *   <li>对象为空，直接返回</li>
     *   <li>检查是否已处理，避免循环引用</li>
     *   <li>对于IPage分页，处理其records字段</li>
     *   <li>对于Iterable集合，递归处理每个元素</li>
     *   <li>对于Map对象，递归处理每个值</li>
     *   <li>对于普通对象，通过反射处理字段</li>
     * </ol>
     * 
     * @param obj 要翻译的对象
     * @param tenantId 租户ID
     * @param visited 访问标记Map，用于避免循环引用
     */
    private void translateAny(Object obj, Long tenantId, IdentityHashMap<Object, Boolean> visited) {
        // 对象为空，直接返回
        if (obj == null) {
            return;
        }
        
        // 检查是否已处理，避免循环引用
        if (visited.containsKey(obj)) {
            return;
        }
        
        // 标记为已处理
        visited.put(obj, Boolean.TRUE);

        // 对于IPage分页，处理其records字段
        if (obj instanceof IPage<?> page) {
            translateAny(page.getRecords(), tenantId, visited);
            return;
        }
        
        // 对于Iterable集合，递归处理每个元素
        if (obj instanceof Iterable<?> it) {
            for (Object e : it) {
                translateAny(e, tenantId, visited);
            }
            return;
        }
        
        // 对于Map对象，递归处理每个值
        if (obj instanceof Map<?, ?> map) {
            for (Object v : map.values()) {
                translateAny(v, tenantId, visited);
            }
            return;
        }
        
        // 获取对象类型
        Class<?> cls = obj.getClass();
        
        // 基本类型和枚举类型不需要处理
        if (cls.isPrimitive() || cls.isEnum()) {
            return;
        }
        
        // 跳过Java标准类
        String cn = cls.getName();
        if (cn.startsWith("java.") || cn.startsWith("javax.") || cn.startsWith("jakarta.")) {
            return;
        }
        
        // 获取所有字段
        Field[] fields = cls.getDeclaredFields();
        
        // 遍历所有字段，查找@DictI18n注解
        for (Field f : fields) {
            f.setAccessible(true);
            DictI18n ann = f.getAnnotation(DictI18n.class);
            
            // 找到@DictI18n注解，应用字典翻译
            if (ann != null) {
                applyDictTranslation(obj, f, ann, tenantId);
                continue;
            }
            
            // 尝试处理字段值
            try {
                Object v = f.get(obj);
                if (v == null) {
                    continue;
                }
                
                // 跳过基本类型和枚举类型
                Class<?> t = v.getClass();
                if (t.isPrimitive() || t.isEnum()) {
                    continue;
                }
                
                // 跳过Java标准类
                String tn = t.getName();
                if (tn.startsWith("java.") || tn.startsWith("javax.") || tn.startsWith("jakarta.")) {
                    continue;
                }
                
                // 递归处理字段值
                translateAny(v, tenantId, visited);
            } catch (Exception ignored) {}
        }
    }

    /**
     * 应用字典翻译到字段
     * <p>
     * 根据注解配置，获取字典节点路径和字典值，
     * 查询字典标签映射，将字典值替换为国际化文本。
     * </p>
     * <p><strong>处理流程：</strong></p>
     * <ol>
     *   <li>获取目标字段名称</li>
     *   <li>目标字段名为空，直接返回</li>
     *   <li>获取标签样式字段名称</li>
     *   <li>获取字典节点路径</li>
     *   <li>如果注解指定了节点路径常量，直接使用</li>
     *   <li>如果注解指定了节点路径字段，从对象中读取</li>
     *   <li>节点路径为空，直接返回</li>
     *   <li>获取字典值字段名称</li>
     *   <li>如果注解指定了字典值字段，从对象中读取</li>
     *   <li>否则使用字段名作为字典值字段</li>
     *   <li>字典值字段为空，直接返回</li>
     *   <li>将字典值转换为字符串</li>
     *   <li>字典值为空，直接返回</li>
     *   <li>如果有标签样式字段，查询字典项映射</li>
     *   <li>否则查询字典标签映射</li>
     *   <li>映射为空或为空，直接返回</li>
     *   <li>获取对应的国际化文本</li>
     *   <li>国际化文本为空，直接返回</li>
     *   <li>通过反射设置目标字段的值</li>
     *   <li>如果有标签样式字段，设置标签样式</li>
     * </ol>
     * 
     * @param obj 包含字典字段的对象
     * @param annotatedField 带有@DictI18n注解的字段
     * @param ann @DictI18n注解实例
     * @param tenantId 租户ID
     */
    private void applyDictTranslation(Object obj, Field annotatedField, DictI18n ann, Long tenantId) {
        // 获取目标字段名称
        String targetFieldName = ann.targetField();
        if (!StringUtils.hasText(targetFieldName)) {
            return;
        }
        
        // 获取字典节点路径
        String nodePath = ann.nodePathConst();
        if (!StringUtils.hasText(nodePath) && StringUtils.hasText(ann.nodePathField())) {
            Object v = readField(obj, ann.nodePathField());
            if (v != null) {
                nodePath = String.valueOf(v);
            }
        }
        
        // 节点路径为空，直接返回
        if (!StringUtils.hasText(nodePath)) {
            return;
        }
        
        // 获取字典值字段名称
        Object fieldValue;
        if (StringUtils.hasText(ann.valueField())) {
            fieldValue = readField(obj, ann.valueField());
        } else {
            fieldValue = readField(obj, annotatedField.getName());
        }
        
        // 字典值字段为空，直接返回
        if (fieldValue == null) {
            return;
        }
        
        // 将字段值转换为字符串
        String rawKey = fieldValue.toString();
        
        // 将字典值转换为字符串
        String key = String.valueOf(rawKey);
        if (!StringUtils.hasText(key)) {
            return;
        }
        
        // 查询字典项映射
        Map<String, DictItem> itemMap = dictI18nResolver.getChildItemMap(tenantId, nodePath);
        
        // 映射为空或为空，直接返回
        if (itemMap == null || itemMap.isEmpty()) {
            return;
        }
        
        // 获取对应的字典项
        DictItem item = itemMap.get(key);
        if (item == null) {
            return;
        }
        
        // 通过反射设置目标字段的值（JSON字符串，包含label和tagStyle）
        writeField(obj, targetFieldName, item.toJson());
    }

    /**
     * 读取字段值
     * <p>
     * 通过反射获取对象的字段值。
     * </p>
     * 
     * @param obj 对象实例
     * @param name 字段名称
     * @return 字段值，读取失败返回null
     */
    private Object readField(Object obj, String name) {
        if (obj == null || !StringUtils.hasText(name)) {
            return null;
        }
        
        // 查找字段
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

    /**
     * 设置字段值
     * <p>
     * 通过反射设置对象的字段值。
     * </p>
     * 
     * @param obj 对象实例
     * @param name 字段名称
     * @param value 要设置的值
     */
    private void writeField(Object obj, String name, Object value) {
        // 查找字段
        Field f = findField(obj.getClass(), name);
        if (f == null) {
            return;
        }
        
        try {
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception ignored) {}
    }

    /**
     * 查找字段
     * <p>
     * 在类及其父类中递归查找指定名称的字段。
     * </p>
     * 
     * @param cls 要查找的类
     * @param name 字段名称
     * @return 字段对象，未找到返回null
     */
    private Field findField(Class<?> cls, String name) {
        // 从当前类开始查找
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            try {
                // 尝试获取字段
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // 未找到，继续查找父类
                c = c.getSuperclass();
            }
        }
        return null;
    }
}

