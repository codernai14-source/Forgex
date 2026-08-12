/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgex.common.crypto;

import java.lang.annotation.*;

/**
 * 字段加密注解。
 * <p>
 * 标注在实体类的 String 字段上，MyBatis 拦截器
 * {@link com.forgex.common.crypto.FieldEncryptInterceptor}
 * 会在 INSERT/UPDATE 时自动加密、SELECT 时自动解密，实现透明加密。
 * <p>
 * 支持的加密算法：
 * <ul>
 *   <li>{@code "SM4"} - 国密 SM4 对称加密（默认）</li>
 *   <li>{@code "AES"} - AES-256-GCM 对称加密</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * public class SysUser extends BaseEntity {
 *
 *     &#64;FieldEncrypt(algorithm = "SM4")
 *     private String idCard;
 *
 *     &#64;FieldEncrypt(algorithm = "AES")
 *     private String bankCard;
 *
 *     &#64;FieldEncrypt  // 默认 SM4
 *     private String secretData;
 * }
 * </pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.crypto.FieldEncryptInterceptor
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldEncrypt {

    /**
     * 加密算法名称。
     * <p>
     * 支持值：{@code "SM4"}（默认）、{@code "AES"}
     *
     * @return 算法名称
     */
    String algorithm() default "SM4";
}



