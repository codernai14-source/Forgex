package com.forgex.sys.domain.param;

import lombok.Data;

import java.util.List;

/**
 * 批量ID参数类
 * @author coder_nai@163.com
 * @date 2026年01月16日
 * @description: 用于接收批量ID参数的请求
 * @version: 1.0
 */
@Data
public class BatchIdsParam {
    /**
     * ID列表
     */
    private List<Long> ids;
}