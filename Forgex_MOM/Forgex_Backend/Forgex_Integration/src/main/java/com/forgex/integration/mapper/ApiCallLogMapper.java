package com.forgex.integration.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.github.yulichang.base.MPJBaseMapper;
import com.forgex.integration.domain.entity.ApiCallLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口调用记录 Mapper 接口
 * <p>
 * 负责接口调用记录的数据访问，支持按月分表查询
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Mapper
public interface ApiCallLogMapper extends MPJBaseMapper<ApiCallLog> {

    /**
     * 从指定表中保存调用记录
     * <p>
     * 由于调用记录表按月分表，需要动态指定表名
     * </p>
     *
     * @param tableName 表名（如：fx_api_call_log_202604）
     * @param log 调用记录
     * @return 影响行数
     */
    @InterceptorIgnore(tenantLine = "true")
    int insertToTable(@Param("tableName") String tableName, @Param("log") ApiCallLog log);

    /**
     * 从指定表中查询调用记录
     * <p>
     * 用于按时间范围查询
     * </p>
     *
     * @param tableName 表名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 调用记录列表
     */
    @InterceptorIgnore(tenantLine = "true")
    List<ApiCallLog> selectFromTable(
        @Param("tableName") String tableName,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计指定表中的记录数
     * <p>
     * 用于分页查询总数
     * </p>
     *
     * @param tableName 表名
     * @param apiConfigId 接口配置 ID（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录总数
     */
    @InterceptorIgnore(tenantLine = "true")
    long countFromTable(
        @Param("tableName") String tableName,
        @Param("apiConfigId") Long apiConfigId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
