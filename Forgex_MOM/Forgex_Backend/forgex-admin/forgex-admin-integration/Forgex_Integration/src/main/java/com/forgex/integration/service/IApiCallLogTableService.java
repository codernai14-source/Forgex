package com.forgex.integration.service;

import java.time.LocalDateTime;

/**
 * 接口调用日志月表维护服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IApiCallLogTableService {

    /**
     * 确保指定调用时间对应的月表存在。
     *
     * @param callTime 调用时间
     * @return 月表名称
     */
    String ensureTable(LocalDateTime callTime);

    /**
     * 判断月表是否存在。
     *
     * @param tableName 月表名称
     * @return true=存在，false=不存在
     */
    boolean tableExists(String tableName);

    /**
     * 启动时创建当月与下月日志表。
     */
    void ensureCurrentAndNextMonthTables();
}
