package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 通用ID参数类
 * @author coder_nai@163.com
 * @date 2026年01月16日
 * @description: 用于接收单个ID参数的请求
 * @version: 1.0
 */
@Data
public class IdParam {
    /**
     * ID值
     */
    private Long id;
}