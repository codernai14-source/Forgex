package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 表编码参数类
 * @author coder_nai@163.com
 * @date 2026年01月16日
 * @description: 用于接收表编码参数的请求
 * @version: 1.0
 */
@Data
public class TableCodeParam {
    /**
     * 表编码
     */
    private String tableCode;
}