package com.forgex.integration.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.forgex.integration.domain.vo.IntegrationDashboardChartItemVO;
import com.forgex.integration.domain.vo.IntegrationDashboardFailureVO;
import com.forgex.integration.domain.vo.IntegrationDashboardTopApiVO;
import com.forgex.integration.domain.vo.IntegrationDashboardTrendItemVO;
import com.forgex.integration.domain.entity.ApiCallLog;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ApiCallLogMapper extends MPJBaseMapper<ApiCallLog> {

    @InterceptorIgnore(tenantLine = "true")
    int insertToTable(@Param("tableName") String tableName, @Param("log") ApiCallLog log);

    @InterceptorIgnore(tenantLine = "true")
    long tableExists(@Param("tableName") String tableName);

    @InterceptorIgnore(tenantLine = "true")
    int createMonthTable(@Param("tableName") String tableName);

    @InterceptorIgnore(tenantLine = "true")
    List<ApiCallLog> selectFromTable(
        @Param("tableName") String tableName,
        @Param("apiConfigId") Long apiConfigId,
        @Param("callDirection") String callDirection,
        @Param("callStatus") String callStatus,
        @Param("callerIp") String callerIp,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );

    @InterceptorIgnore(tenantLine = "true")
    ApiCallLog selectByIdFromTable(
        @Param("tableName") String tableName,
        @Param("id") Long id
    );

    @InterceptorIgnore(tenantLine = "true")
    long countFromTable(
        @Param("tableName") String tableName,
        @Param("apiConfigId") Long apiConfigId,
        @Param("callDirection") String callDirection,
        @Param("callStatus") String callStatus,
        @Param("callerIp") String callerIp,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @InterceptorIgnore(tenantLine = "true")
    List<IntegrationDashboardChartItemVO> countDashboardStatusFromTable(
        @Param("tableName") String tableName,
        @Param("tenantId") Long tenantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @InterceptorIgnore(tenantLine = "true")
    List<IntegrationDashboardTrendItemVO> countDashboardTrendFromTable(
        @Param("tableName") String tableName,
        @Param("tenantId") Long tenantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @InterceptorIgnore(tenantLine = "true")
    List<IntegrationDashboardTopApiVO> countDashboardTopApisFromTable(
        @Param("tableName") String tableName,
        @Param("tenantId") Long tenantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("limit") Integer limit
    );

    @InterceptorIgnore(tenantLine = "true")
    List<IntegrationDashboardFailureVO> selectDashboardFailuresFromTable(
        @Param("tableName") String tableName,
        @Param("tenantId") Long tenantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("limit") Integer limit
    );
}
