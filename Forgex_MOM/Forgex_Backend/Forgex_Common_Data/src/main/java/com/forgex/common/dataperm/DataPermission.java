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
package com.forgex.common.dataperm;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>标记在Mapper方法上，表示该方法需要进行数据权限控制。</p>
 * 
 * <p>使用示例：</p>
 * <pre>
 * {@code @DataPermission(deptAlias = "d", userAlias = "u")}
 * List&lt;User&gt; selectUserList(@Param("param") UserParam param);
 * </pre>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
    
    /**
     * 部门表别名
     * <p>用于在SQL中定位部门字段，如果为空则使用默认字段名dept_id</p>
     */
    String deptAlias() default "";
    
    /**
     * 用户表别名
     * <p>用于在SQL中定位用户字段，如果为空则使用默认字段名create_by</p>
     */
    String userAlias() default "";
    
    /**
     * 部门字段名
     * <p>默认为dept_id</p>
     */
    String deptColumn() default "dept_id";
    
    /**
     * 用户字段名
     * <p>默认为create_by</p>
     */
    String userColumn() default "create_by";
    
    /**
     * 是否启用数据权限
     * <p>默认为true，设置为false可以临时禁用数据权限</p>
     */
    boolean enabled() default true;
}

