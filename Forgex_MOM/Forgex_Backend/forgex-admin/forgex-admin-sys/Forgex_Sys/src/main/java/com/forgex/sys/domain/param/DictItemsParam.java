package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 字典项查询参数类
 * @author coder_nai@163.com
 * @date 2026年01月16日
 * @description: 用于接收字典项查询参数的请求
 * @version: 1.0
 */
@Data
public class DictItemsParam {
    /**
     * 字典编码
     */
    private String dictCode;
}