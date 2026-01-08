package com.forgex.common.domain.config;

import lombok.Data;

/**
 * 初始化状态配置。
 * <p>
 * 记录系统初始化使用次数与完成标志，供登录页判断是否跳转初始化向导。
 * 字段：
 * - {@code usageCount} 使用次数；
 * - {@code initCompleted} 是否已完成初始化。
 */
@Data
public class InitStatusConfig {
    /** 使用次数 */
    private Integer usageCount;
    /** 是否已完成初始化 */
    private Boolean initCompleted;
}
