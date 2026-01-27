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
package com.forgex.common.api.annotation;

import java.lang.annotation.*;

/**
 * 自动填充用户名注解
 * <p>在字段上使用此注解，可以自动根据用户ID查询并填充用户名</p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * public class SomeDTO {
 *     private Long userId;
 *     
 *     &#64;AutoFillUsername(userIdField = "userId")
 *     private String username;
 * }
 * </pre>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-27
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoFillUsername {
    
    /**
     * 用户ID字段名称
     * <p>指定当前对象中哪个字段是用户ID，用于查询用户名</p>
     * 
     * @return 用户ID字段名
     */
    String userIdField() default "userId";
    
    /**
     * 是否必填
     * <p>如果为true，当用户ID为空或查询不到用户时会抛出异常</p>
     * 
     * @return 是否必填
     */
    boolean required() default false;
}

