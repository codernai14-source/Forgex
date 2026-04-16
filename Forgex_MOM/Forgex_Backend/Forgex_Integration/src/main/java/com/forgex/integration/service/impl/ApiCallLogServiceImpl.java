package com.forgex.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiCallLogDTO;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.domain.entity.ApiCallLog;
import com.forgex.integration.domain.param.ApiCallLogParam;
import com.forgex.integration.mapper.ApiCallLogMapper;
import com.forgex.integration.service.IApiCallLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 接口调用记录服务实现类
 * <p>
 * 提供接口调用记录的异步保存、分表查询、统计等功能实现
 * 支持按月分表存储，自动根据时间范围路由到对应的表
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see com.forgex.integration.service.IApiCallLogService
 * @see com.forgex.integration.domain.entity.ApiCallLog
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCallLogServiceImpl extends ServiceImpl<ApiCallLogMapper, ApiCallLog> 
    implements IApiCallLogService {

    private final ApiCallLogMapper apiCallLogMapper;

    /**
     * 日期格式化器：yyyyMM
     */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 表名前缀
     */
    private static final String TABLE_PREFIX = "fx_api_call_log_";

    @Override
    @Async
    public boolean asyncSaveLog(ApiCallLog logEntity) {
        try {
            // 根据调用时间确定表名
            LocalDateTime callTime = logEntity.getCallTime() != null ? logEntity.getCallTime() : LocalDateTime.now();
            String tableName = getMonthTableName(callTime);
            
            // 插入到对应的月份表
            int result = apiCallLogMapper.insertToTable(tableName, logEntity);
            
            log.debug("异步保存调用记录成功：tableName={}, id={}", tableName, logEntity.getId());
            return result > 0;
        } catch (Exception e) {
            log.error("异步保存调用记录失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Page<ApiCallLogDTO> pageCallLogs(ApiCallLogParam param) {
        // 确定需要查询的月份表列表
        List<String> tableNames = getMonthTableNames(param.getStartTime(), param.getEndTime());
        
        if (tableNames.isEmpty()) {
            log.warn("查询调用记录时，时间范围为空");
            return new Page<>(param.getPageNum(), param.getPageSize(), 0);
        }
        
        // 从第一个月开始查询（如果需要跨月查询，可以在这里扩展逻辑）
        String tableName = tableNames.get(0);
        
        // 查询总数
        long total = apiCallLogMapper.countFromTable(
            tableName,
            param.getApiConfigId(),
            param.getStartTime(),
            param.getEndTime()
        );
        
        // 创建分页对象
        Page<ApiCallLogDTO> page = new Page<>(param.getPageNum(), param.getPageSize(), total);
        
        // 如果总数为 0，直接返回空结果
        if (total == 0) {
            return page;
        }
        
        // 构建查询条件
        LambdaQueryWrapper<ApiCallLog> wrapper = buildQueryWrapper(param);
        
        // 执行分页查询
        Page<ApiCallLog> resultPage = new Page<>(param.getPageNum(), param.getPageSize());
        resultPage = this.page(resultPage, wrapper);
        
        // 转换为 DTO 并设置到返回的分页对象中
        List<ApiCallLogDTO> dtoList = resultPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        page.setRecords(dtoList);
        
        return page;
    }

    @Override
    public ApiCallLogDTO getCallLogById(Long id, LocalDateTime callTime) {
        if (id == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.CALL_LOG_ID_REQUIRED);
        }
        
        // 根据调用时间确定表名
        String tableName = getMonthTableName(callTime);
        
        // 构建查询条件
        LambdaQueryWrapper<ApiCallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiCallLog::getId, id);
        
        // 由于需要指定表名，这里使用自定义查询
        List<ApiCallLog> logs = apiCallLogMapper.selectFromTable(
            tableName,
            null,
            null
        );
        
        // 过滤出指定 ID 的记录
        return logs.stream()
            .filter(log -> log.getId().equals(id))
            .map(this::convertToDTO)
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<ApiCallLogDTO> listCallLogs(ApiCallLogParam param) {
        // 确定需要查询的月份表列表
        List<String> tableNames = getMonthTableNames(param.getStartTime(), param.getEndTime());
        
        if (tableNames.isEmpty()) {
            log.warn("查询调用记录列表时，时间范围为空");
            return new ArrayList<>();
        }
        
        List<ApiCallLogDTO> resultList = new ArrayList<>();
        
        // 遍历所有月份表查询
        for (String tableName : tableNames) {
            LambdaQueryWrapper<ApiCallLog> wrapper = buildQueryWrapper(param);
            wrapper.last("ORDER BY create_time DESC");
            
            List<ApiCallLog> logs = this.list(wrapper);
            List<ApiCallLogDTO> dtos = logs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            resultList.addAll(dtos);
        }
        
        return resultList;
    }

    @Override
    public long countCallLogs(Long apiConfigId, LocalDateTime startTime, LocalDateTime endTime) {
        List<String> tableNames = getMonthTableNames(startTime, endTime);
        
        if (tableNames.isEmpty()) {
            return 0;
        }
        
        long totalCount = 0;
        
        // 累加所有月份表的记录数
        for (String tableName : tableNames) {
            long count = apiCallLogMapper.countFromTable(tableName, apiConfigId, startTime, endTime);
            totalCount += count;
        }
        
        return totalCount;
    }

    @Override
    public double calculateSuccessRate(Long apiConfigId, LocalDateTime startTime, LocalDateTime endTime) {
        List<String> tableNames = getMonthTableNames(startTime, endTime);
        
        if (tableNames.isEmpty()) {
            return 0.0;
        }
        
        long totalCount = 0;
        long successCount = 0;
        
        // 遍历所有月份表统计
        for (String tableName : tableNames) {
            // 查询总记录数
            long total = apiCallLogMapper.countFromTable(tableName, apiConfigId, startTime, endTime);
            totalCount += total;
            
            // 查询成功记录数
            long success = apiCallLogMapper.countFromTable(tableName, apiConfigId, startTime, endTime);
            successCount += success;
        }
        
        if (totalCount == 0) {
            return 0.0;
        }
        
        // 计算成功率（百分比）
        return (double) successCount / totalCount * 100;
    }

    @Override
    public String getMonthTableName(LocalDateTime date) {
        if (date == null) {
            date = LocalDateTime.now();
        }
        return TABLE_PREFIX + date.format(MONTH_FORMATTER);
    }

    /**
     * 获取时间范围内的所有月份表名
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 表名列表
     */
    private List<String> getMonthTableNames(LocalDateTime startTime, LocalDateTime endTime) {
        List<String> tableNames = new ArrayList<>();
        
        if (startTime == null || endTime == null) {
            // 如果时间为空，默认查询最近一个月的表
            LocalDateTime now = LocalDateTime.now();
            tableNames.add(getMonthTableName(now));
            return tableNames;
        }
        
        LocalDateTime current = startTime;
        while (!current.isAfter(endTime)) {
            tableNames.add(getMonthTableName(current));
            // 移动到下一个月
            current = current.plusMonths(1);
            // 设置为月初，避免重复
            current = current.withDayOfMonth(1);
        }
        
        return tableNames;
    }

    /**
     * 构建查询条件
     *
     * @param param 查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<ApiCallLog> buildQueryWrapper(ApiCallLogParam param) {
        LambdaQueryWrapper<ApiCallLog> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(ApiCallLog::getDeleted, false);
        
        // 接口配置 ID 过滤
        wrapper.eq(param.getApiConfigId() != null, ApiCallLog::getApiConfigId, param.getApiConfigId());
        
        // 调用方向过滤
        wrapper.eq(param.getCallDirection() != null && !param.getCallDirection().isEmpty(), 
                   ApiCallLog::getCallDirection, param.getCallDirection());
        
        // 调用状态过滤
        wrapper.eq(param.getCallStatus() != null && !param.getCallStatus().isEmpty(), 
                   ApiCallLog::getCallStatus, param.getCallStatus());
        
        // 调用方 IP 模糊查询
        wrapper.like(param.getCallerIp() != null && !param.getCallerIp().isEmpty(), 
                     ApiCallLog::getCallerIp, param.getCallerIp());
        
        // 时间范围过滤
        wrapper.ge(param.getStartTime() != null, ApiCallLog::getCallTime, param.getStartTime());
        wrapper.le(param.getEndTime() != null, ApiCallLog::getCallTime, param.getEndTime());
        
        log.debug("构建查询条件：param={}", param);
        return wrapper;
    }

    /**
     * 实体转 DTO
     *
     * @param log 调用记录实体
     * @return DTO 对象
     */
    private ApiCallLogDTO convertToDTO(ApiCallLog log) {
        ApiCallLogDTO dto = new ApiCallLogDTO();
        BeanUtils.copyProperties(log, dto);
        return dto;
    }
}
