package com.forgex.common.dataperm;

/**
 * 主体密级提供者。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface SubjectSecurityLevelProvider {

    /**
     * 当前环境是否启用强制访问控制。
     *
     * @return true 表示启用
     */
    default boolean mandatoryAccessEnabled() {
        return false;
    }

    /**
     * 解析用户密级。
     *
     * @param userId 用户 ID
     * @return 密级数值
     */
    int resolve(Long userId);
}
