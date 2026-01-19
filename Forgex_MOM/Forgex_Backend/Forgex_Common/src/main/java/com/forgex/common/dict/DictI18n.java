package com.forgex.common.dict;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字典国际化注解
 * <p>
 * 用于标记需要进行字典国际化处理的字段，配合AOP切面自动解析字典值的多语言文本。
 * </p>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>实体类中的字典字段，需要根据当前语言环境显示不同语言的文本</li>
 *   <li>DTO/VO类中的字典字段，需要将字典值转换为国际化文本</li>
 *   <li>支持从字典节点路径或字典值字段获取国际化文本</li>
 * </ul>
 * <p><strong>属性说明：</strong></p>
 * <ul>
 *   <li>{@code nodePathField} - 指定字典节点路径字段名</li>
 *   <li>{@code nodePathConst} - 指定字典节点路径常量值</li>
 *   <li>{@code valueField} - 指定字典值字段名</li>
 *   <li>{@code targetField} - 指定目标字段名，用于存储解析后的国际化文本</li>
 * </ul>
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
 * }</pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DictI18nResolver
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DictI18n {
    /**
     * 字典节点路径字段名
     * <p>
     * 指定实体类中存储字典节点路径的字段名称，
     * 通过该字段的值定位字典节点，再根据当前语言解析国际化文本。
     * </p>
     * <p>
     * 与{@code nodePathConst}互斥，优先使用{@code nodePathField}。
     * </p>
     * 
     * @return 字典节点路径字段名，默认为空字符串
     */
    String nodePathField() default "";

    /**
     * 字典节点路径常量值
     * <p>
     * 直接指定字典节点路径的常量值，
     * 适用于字典节点路径固定不变的场景。
     * </p>
     * <p>
     * 与{@code nodePathField}互斥，优先使用{@code nodePathField}。
     * </p>
     * 
     * @return 字典节点路径常量值，默认为空字符串
     */
    String nodePathConst() default "";

    /**
     * 字典值字段名
     * <p>
     * 指定实体类中存储字典值的字段名称，
     * 该字段的值用于在字典节点下查找对应的国际化文本。
     * </p>
     * 
     * @return 字典值字段名，默认为空字符串
     */
    String valueField() default "";

    /**
     * 目标字段名
     * <p>
     * 指定用于存储解析后国际化文本的字段名称，
     * AOP切面会将解析后的文本自动填充到该字段。
     * </p>
     * <p>
     * 该字段必须在实体类中存在，且类型为String。
     * </p>
     * 
     * @return 目标字段名，默认为空字符串
     */
    String targetField() default "";
}

