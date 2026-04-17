package com.forgex.integration.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
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
    long countFromTable(
        @Param("tableName") String tableName,
        @Param("apiConfigId") Long apiConfigId,
        @Param("callDirection") String callDirection,
        @Param("callStatus") String callStatus,
        @Param("callerIp") String callerIp,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
