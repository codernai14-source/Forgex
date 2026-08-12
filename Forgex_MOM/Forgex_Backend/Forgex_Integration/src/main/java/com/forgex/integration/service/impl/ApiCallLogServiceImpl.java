package com.forgex.integration.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiCallLogDTO;
import com.forgex.integration.domain.entity.ApiCallLog;
import com.forgex.integration.domain.param.ApiCallLogParam;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.mapper.ApiCallLogMapper;
import com.forgex.integration.service.IApiConfigService;
import com.forgex.integration.service.IApiCallLogService;
import com.forgex.integration.service.IApiCallLogTableService;
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
 * 接口调用日志服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCallLogServiceImpl extends ServiceImpl<ApiCallLogMapper, ApiCallLog>
    implements IApiCallLogService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String TABLE_PREFIX = "fx_api_call_log_";

    private final ApiCallLogMapper apiCallLogMapper;
    private final IApiCallLogTableService apiCallLogTableService;
    private final IApiConfigService apiConfigService;

    /**
     * 处理asyncsavelog。
     *
     * @param logEntity 日志实体
     * @return 是否处理成功
     */
    @Override
    @Async("integrationLogExecutor")
    public boolean asyncSaveLog(ApiCallLog logEntity) {
        try {
            if (logEntity == null) {
                return false;
            }
            LocalDateTime callTime = logEntity.getCallTime() != null ? logEntity.getCallTime() : LocalDateTime.now();
            apiCallLogTableService.ensureTable(callTime);
            String tableName = getMonthTableName(callTime);
            int result = apiCallLogMapper.insertToTable(tableName, logEntity);
            return result > 0;
        } catch (Exception e) {
            log.error("Failed to save api call log asynchronously, table={}, traceId={}, apiCode={}",
                logEntity == null ? null : getMonthTableName(logEntity.getCallTime()),
                logEntity == null ? null : logEntity.getTraceId(),
                logEntity == null ? null : logEntity.getApiCode(), e);
            return false;
        }
    }

    /**
     * 处理批量savelogs。
     *
     * @param logs logs
     */
    @Override
    public void batchSaveLogs(List<ApiCallLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        for (ApiCallLog logEntity : logs) {
            saveLogSync(logEntity);
        }
    }

    /**
     * 分页查询calllogs。
     *
     * @param param 请求参数
     * @return 分页结果
     */
    @Override
    public Page<ApiCallLogDTO> pageCallLogs(ApiCallLogParam param) {
        List<String> tableNames = getExistingMonthTableNames(param.getStartTime(), param.getEndTime());
        Page<ApiCallLogDTO> page = new Page<>(param.getPageNum(), param.getPageSize());
        if (tableNames.isEmpty()) {
            page.setTotal(0);
            page.setRecords(new ArrayList<>());
            return page;
        }

        long total = 0;
        List<Long> tableTotals = new ArrayList<>(tableNames.size());
        for (String tableName : tableNames) {
            long count = countFromTable(tableName, param);
            tableTotals.add(count);
            total += count;
        }

        page.setTotal(total);
        if (total == 0) {
            page.setRecords(new ArrayList<>());
            return page;
        }

        long globalOffset = Math.max(0L, (long) (param.getPageNum() - 1) * param.getPageSize());
        int remain = Math.max(param.getPageSize(), 0);
        List<ApiCallLogDTO> records = new ArrayList<>(param.getPageSize());

        for (int index = tableNames.size() - 1; index >= 0 && remain > 0; index--) {
            long tableCount = tableTotals.get(index);
            if (tableCount <= 0) {
                continue;
            }
            if (globalOffset >= tableCount) {
                globalOffset -= tableCount;
                continue;
            }

            int offset = (int) globalOffset;
            List<ApiCallLog> logs = apiCallLogMapper.selectFromTable(
                tableNames.get(index),
                param.getApiConfigId(),
                param.getCallDirection(),
                param.getCallStatus(),
                param.getCallerIp(),
                param.getStartTime(),
                param.getEndTime(),
                offset,
                remain
            );
            records.addAll(logs.stream().map(this::convertToDTO).collect(Collectors.toList()));
            remain = param.getPageSize() - records.size();
            globalOffset = 0;
        }

        page.setRecords(records);
        return page;
    }

    /**
     * 获取调用日志byID。
     *
     * @param id 主键 ID
     * @param callTime 调用时间
     * @return 处理结果
     */
    @Override
    public ApiCallLogDTO getCallLogById(Long id, LocalDateTime callTime) {
        if (id == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.CALL_LOG_ID_REQUIRED);
        }

        List<String> tableNames = callTime != null
            ? List.of(getMonthTableName(callTime))
            : getMonthTableNames(LocalDateTime.now().minusMonths(1), LocalDateTime.now());

        for (int index = tableNames.size() - 1; index >= 0; index--) {
            String tableName = tableNames.get(index);
            if (!apiCallLogTableService.tableExists(tableName)) {
                continue;
            }
            ApiCallLog logEntity = apiCallLogMapper.selectByIdFromTable(tableName, id);
            if (logEntity != null) {
                return convertToDTO(logEntity);
            }
        }
        return null;
    }

    /**
     * 查询calllogs列表。
     *
     * @param param 请求参数
     * @return 列表数据
     */
    @Override
    public List<ApiCallLogDTO> listCallLogs(ApiCallLogParam param) {
        List<String> tableNames = getExistingMonthTableNames(param.getStartTime(), param.getEndTime());
        if (tableNames.isEmpty()) {
            return new ArrayList<>();
        }

        List<ApiCallLogDTO> result = new ArrayList<>();
        for (int index = tableNames.size() - 1; index >= 0; index--) {
            List<ApiCallLog> logs = apiCallLogMapper.selectFromTable(
                tableNames.get(index),
                param.getApiConfigId(),
                param.getCallDirection(),
                param.getCallStatus(),
                param.getCallerIp(),
                param.getStartTime(),
                param.getEndTime(),
                null,
                null
            );
            result.addAll(logs.stream().map(this::convertToDTO).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * 处理countcalllogs。
     *
     * @param apiConfigId 接口配置 ID
     * @param startTime start时间
     * @param endTime end时间
     * @return 数据主键 ID
     */
    @Override
    public long countCallLogs(Long apiConfigId, LocalDateTime startTime, LocalDateTime endTime) {
        ApiCallLogParam param = new ApiCallLogParam();
        param.setApiConfigId(apiConfigId);
        param.setStartTime(startTime);
        param.setEndTime(endTime);
        return getExistingMonthTableNames(startTime, endTime).stream()
            .mapToLong(tableName -> countFromTable(tableName, param))
            .sum();
    }

    /**
     * 处理calculatesuccessrate。
     *
     * @param apiConfigId 接口配置 ID
     * @param startTime start时间
     * @param endTime end时间
     * @return 处理结果
     */
    @Override
    public double calculateSuccessRate(Long apiConfigId, LocalDateTime startTime, LocalDateTime endTime) {
        ApiCallLogParam totalParam = new ApiCallLogParam();
        totalParam.setApiConfigId(apiConfigId);
        totalParam.setStartTime(startTime);
        totalParam.setEndTime(endTime);

        ApiCallLogParam successParam = new ApiCallLogParam();
        successParam.setApiConfigId(apiConfigId);
        successParam.setStartTime(startTime);
        successParam.setEndTime(endTime);
        successParam.setCallStatus("SUCCESS");

        long totalCount = 0;
        long successCount = 0;
        List<String> tableNames = getExistingMonthTableNames(startTime, endTime);
        for (String tableName : tableNames) {
            totalCount += countFromTable(tableName, totalParam);
            successCount += countFromTable(tableName, successParam);
        }
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    /**
     * 获取month表格名称。
     *
     * @param date 日期
     * @return 字符串结果
     */
    @Override
    public String getMonthTableName(LocalDateTime date) {
        LocalDateTime safeDate = date != null ? date : LocalDateTime.now();
        return TABLE_PREFIX + safeDate.format(MONTH_FORMATTER);
    }

    private long countFromTable(String tableName, ApiCallLogParam param) {
        return apiCallLogMapper.countFromTable(
            tableName,
            param.getApiConfigId(),
            param.getCallDirection(),
            param.getCallStatus(),
            param.getCallerIp(),
            param.getStartTime(),
            param.getEndTime()
        );
    }

    private boolean saveLogSync(ApiCallLog logEntity) {
        try {
            if (logEntity == null) {
                return false;
            }
            LocalDateTime callTime = logEntity.getCallTime() != null ? logEntity.getCallTime() : LocalDateTime.now();
            apiCallLogTableService.ensureTable(callTime);
            String tableName = getMonthTableName(callTime);
            return apiCallLogMapper.insertToTable(tableName, logEntity) > 0;
        } catch (Exception e) {
            log.error("Failed to save api call log synchronously, table={}, traceId={}, apiCode={}",
                logEntity == null ? null : getMonthTableName(logEntity.getCallTime()),
                logEntity == null ? null : logEntity.getTraceId(),
                logEntity == null ? null : logEntity.getApiCode(), e);
            return false;
        }
    }

    private List<String> getExistingMonthTableNames(LocalDateTime startTime, LocalDateTime endTime) {
        return getMonthTableNames(startTime, endTime).stream()
            .filter(tableName -> {
                boolean exists = apiCallLogTableService.tableExists(tableName);
                if (!exists) {
                    log.error("Api call log month table does not exist, skip query: {}", tableName);
                }
                return exists;
            })
            .toList();
    }

    private List<String> getMonthTableNames(LocalDateTime startTime, LocalDateTime endTime) {
        List<String> tableNames = new ArrayList<>();
        if (startTime == null && endTime == null) {
            tableNames.add(getMonthTableName(LocalDateTime.now()));
            return tableNames;
        }

        LocalDateTime safeEndTime = endTime != null ? endTime : LocalDateTime.now();
        LocalDateTime safeStartTime = startTime != null ? startTime : safeEndTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime current = safeStartTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endMonth = safeEndTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        while (!current.isAfter(endMonth)) {
            tableNames.add(getMonthTableName(current));
            current = current.plusMonths(1);
        }
        return tableNames;
    }

    private ApiCallLogDTO convertToDTO(ApiCallLog entity) {
        ApiCallLogDTO dto = new ApiCallLogDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setApiName(resolveApiName(entity));
        return dto;
    }

    private String resolveApiName(ApiCallLog entity) {
        if (entity == null || entity.getApiConfigId() == null) {
            return null;
        }
        try {
            return apiConfigService.getApiConfigById(entity.getApiConfigId()).getApiName();
        } catch (Exception ex) {
            log.error("Resolve api name failed, apiConfigId={}", entity.getApiConfigId(), ex);
            return null;
        }
    }
}
