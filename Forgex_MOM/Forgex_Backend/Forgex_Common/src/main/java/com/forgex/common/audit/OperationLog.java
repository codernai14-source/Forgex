package com.forgex.common.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * <p>
 * 用于标记需要记录操作日志的方法，通过AOP切面自动拦截并记录操作信息。
 * </p>
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * @OperationLog(
 *     module = "sys",
 *     menuPath = "/system/user",
 *     operationType = OperationType.ADD,
 *     detailTemplateCode = "USER_CREATE",
 *     detailFields = {"account", "username"}
 * )
 * public R<Long> createUser(UserDTO userDTO) {
 *     // 业务逻辑
 * }
 * }</pre>
 * <p><strong>属性说明：</strong></p>
 * <ul>
 *   <li>{@code module} - 模块标识，如"sys"、"auth"等</li>
 *   <li>{@code menuPath} - 菜单路径，用于定位功能模块</li>
 *   <li>{@code operationType} - 操作类型，从{@link OperationType}枚举中选择</li>
 *   <li>{@code detailTemplateCode} - 详情模板代码，用于国际化提示</li>
 *   <li>{@code detailFields} - 详情字段数组，用于从返回结果中提取特定字段值</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see OperationLogAspect
 * @see OperationType
 * @see OperationLogRecord
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    /**
     * 模块标识
     * <p>用于标识操作所属的功能模块，如"sys"、"auth"等。</p>
     * 
     * @return 模块标识字符串
     */
    String module();

    /**
     * 菜单路径
     * <p>用于定位操作所属的菜单或功能模块。</p>
     * 
     * @return 菜单路径字符串
     */
    String menuPath();

    /**
     * 操作类型
     * <p>指定操作的类型，从{@link OperationType}枚举中选择。</p>
     * 
     * @return 操作类型枚举值
     */
    OperationType operationType();

    /**
     * 详情模板代码
     * <p>用于国际化提示的模板代码，支持多语言显示。</p>
     * 
     * @return 模板代码字符串，默认为空字符串
     */
    String detailTemplateCode() default "";

    /**
     * 详情字段数组
     * <p>指定需要从返回结果中提取的字段名称，用于记录操作详情。</p>
     * 
     * @return 字段名称数组，默认为空数组
     */
    String[] detailFields() default {};

}

