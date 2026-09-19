package com.forgex.common.security;

/**
 * 强制访问控制规则：下读上写。
 * <p>
 * SQL 改写仍由数据权限拦截器一次性完成，本类提供可单测的判定入口。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public final class MandatoryAccessControlHandler {

    private MandatoryAccessControlHandler() {
    }

    /**
     * 读取要求数据密级不超过主体密级。
     *
     * @param userLevel 用户密级
     * @param dataLabel 数据密级
     * @return true 表示可读
     */
    public static boolean canRead(int userLevel, int dataLabel) {
        return dataLabel <= userLevel;
    }

    /**
     * 写入要求数据密级不低于主体密级。
     *
     * @param userLevel 用户密级
     * @param dataLabel 数据密级
     * @return true 表示可写
     */
    public static boolean canWrite(int userLevel, int dataLabel) {
        return dataLabel >= userLevel;
    }
}
