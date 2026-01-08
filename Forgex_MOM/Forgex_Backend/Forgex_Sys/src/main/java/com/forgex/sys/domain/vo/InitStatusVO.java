package com.forgex.sys.domain.vo;
import lombok.Data;

/**
 * 初始化状态返回对象。
 * <p>
 * 字段：
 * - {@code firstUse} 是否首次使用（需要进入初始化向导）；
 * - {@code usageCount} 使用次数计数（0 表示从未初始化）。
 */
@Data
public class InitStatusVO {
    /** 是否首次使用 */
    private boolean firstUse;
    /** 使用次数计数 */
    private int usageCount;
}
