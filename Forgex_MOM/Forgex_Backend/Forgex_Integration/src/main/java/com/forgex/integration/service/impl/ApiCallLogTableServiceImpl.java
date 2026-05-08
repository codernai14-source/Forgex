package com.forgex.integration.service.impl;

import com.forgex.integration.mapper.ApiCallLogMapper;
import com.forgex.integration.service.IApiCallLogTableService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 接口调用日志月表维护服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCallLogTableServiceImpl implements IApiCallLogTableService {

    private static final Pattern LOG_TABLE_PATTERN = Pattern.compile("^fx_api_call_log_\\d{6}$");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String TABLE_PREFIX = "fx_api_call_log_";

    private final ApiCallLogMapper apiCallLogMapper;
    private final Map<String, Boolean> existingTableCache = new ConcurrentHashMap<>();

    /**
     * 服务启动后先确保当前月和下月表存在，避免运行首日缺表。
     */
    @PostConstruct
    @Override
    public void ensureCurrentAndNextMonthTables() {
        LocalDateTime now = LocalDateTime.now();
        ensureTable(now);
        ensureTable(now.plusMonths(1));
    }

    /**
     * 每天检查当月表；月末提前创建下月表。
     */
    @Scheduled(cron = "${forgex.integration.call-log-table.ensure-cron:0 5 0 * * ?}")
    public void scheduledEnsureTables() {
        LocalDate today = LocalDate.now();
        ensureTable(today.atStartOfDay());
        YearMonth currentMonth = YearMonth.from(today);
        if (today.getDayOfMonth() == currentMonth.lengthOfMonth()) {
            ensureTable(today.plusMonths(1).atStartOfDay());
        }
    }

    @Override
    public String ensureTable(LocalDateTime callTime) {
        String tableName = getMonthTableName(callTime);
        validateTableName(tableName);
        if (Boolean.TRUE.equals(existingTableCache.get(tableName))) {
            return tableName;
        }
        if (!tableExists(tableName)) {
            apiCallLogMapper.createMonthTable(tableName);
            log.info("Created api call log month table: {}", tableName);
        }
        existingTableCache.put(tableName, Boolean.TRUE);
        return tableName;
    }

    @Override
    public boolean tableExists(String tableName) {
        validateTableName(tableName);
        if (Boolean.TRUE.equals(existingTableCache.get(tableName))) {
            return true;
        }
        boolean exists = apiCallLogMapper.tableExists(tableName) > 0;
        if (exists) {
            existingTableCache.put(tableName, Boolean.TRUE);
        }
        return exists;
    }

    private void validateTableName(String tableName) {
        if (!StringUtils.hasText(tableName) || !LOG_TABLE_PATTERN.matcher(tableName).matches()) {
            throw new IllegalArgumentException("Invalid api call log table name: " + tableName);
        }
    }

    private String getMonthTableName(LocalDateTime callTime) {
        LocalDateTime safeTime = callTime == null ? LocalDateTime.now() : callTime;
        return TABLE_PREFIX + safeTime.format(MONTH_FORMATTER);
    }
}
