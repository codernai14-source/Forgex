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
package com.forgex.common.exception;

/**
 * 业务异常类
 * <p>
 * 用于业务逻辑中的异常处理，继承自 {@link RuntimeException}
 * </p>
 * <p>使用场景：</p>
 * <ul>
 *   <li>业务逻辑校验失败</li>
 *   <li>数据不存在</li>
 *   <li>权限不足</li>
 *   <li>其他业务异常情况</li>
 * </ul>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 抛出默认状态码500的业务异常
 * throw new BusinessException("业务处理失败");
 * 
 * // 抛出自定义状态码的业务异常
 * throw new BusinessException(400, "参数错误");
 * 
 * // 抛出带原因的业务异常
 * throw new BusinessException("业务处理失败", cause);
 * }</pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see RuntimeException
 */
public class BusinessException extends RuntimeException {
    
    /** 异常状态码，默认为500 */
    private Integer code;
    
    /**
     * 构造业务异常（默认状态码500）
     *
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super(message);
        // 设置默认状态码为500
        this.code = 500;
    }
    
    /**
     * 构造业务异常（自定义状态码）
     *
     * @param code 状态码
     * @param message 异常消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        // 设置自定义状态码
        this.code = code;
    }
    
    /**
     * 构造业务异常（默认状态码500，带原因）
     *
     * @param message 异常消息
     * @param cause 异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        // 设置默认状态码为500
        this.code = 500;
    }
    
    /**
     * 构造业务异常（自定义状态码，带原因）
     *
     * @param code 状态码
     * @param message 异常消息
     * @param cause 异常原因
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        // 设置自定义状态码
        this.code = code;
    }
    
    /**
     * 获取异常状态码
     *
     * @return 状态码
     */
    public Integer getCode() {
        return code;
    }
    
    /**
     * 设置异常状态码
     *
     * @param code 状态码
     */
    public void setCode(Integer code) {
        this.code = code;
    }
}
