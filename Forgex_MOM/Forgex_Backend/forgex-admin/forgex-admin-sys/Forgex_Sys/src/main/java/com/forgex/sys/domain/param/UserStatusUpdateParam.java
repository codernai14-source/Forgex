package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 用户状态更新参数类
 * @author coder_nai@163.com
 * @date 2026年01月16日
 * @description: 用于接收用户状态更新的请求参数
 * @version: 1.0
 */
@Data
public class UserStatusUpdateParam {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户状态
     */
    private Boolean status;
}