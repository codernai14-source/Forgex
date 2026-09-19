package com.forgex.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.integration.domain.entity.ApiConfig;
import com.forgex.integration.domain.entity.ThirdSystem;
import com.forgex.integration.domain.vo.IntegrationDashboardChartItemVO;
import com.forgex.integration.domain.vo.IntegrationDashboardFailureVO;
import com.forgex.integration.domain.vo.IntegrationDashboardOverviewVO;
import com.forgex.integration.domain.vo.IntegrationDashboardSummaryVO;
import com.forgex.integration.domain.vo.IntegrationDashboardTopApiVO;
import com.forgex.integration.domain.vo.IntegrationDashboardTrendItemVO;
import com.forgex.integration.enums.ApiTaskStatusEnum;
import com.forgex.integration.mapper.ApiCallLogMapper;
import com.forgex.integration.mapper.ApiConfigMapper;
import com.forgex.integration.mapper.ThirdSystemMapper;
import com.forgex.integration.service.IApiCallLogTableService;
import com.forgex.integration.service.IIntegrationDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 接口平台首页统计服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationDashboardServiceImpl implements IIntegrationDashboardService {

    private static final int ENABLED_STATUS = 1;
    private static final int DEFAULT_DAYS = 14;
    private static final int TOP_API_LIMIT = 6;
    private static final int FAILURE_LIMIT = 6;
    private static final String DIRECTION_INBOUND = "INBOUND";
    private static final String DIRECTION_OUTBOUND = "OUTBOUND";
    private static final String TABLE_PREFIX = "fx_api_call_log_";
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ThirdSystemMapper thirdSystemMapper;
    private final ApiConfigMapper apiConfigMapper;
    private final ApiCallLogMapper apiCallLogMapper;
    private final IApiCallLogTableService apiCallLogTableService;

    /**
     * 查询接口平台首页概览数据。
     *
     * @return 首页概览数据
     */
    @Override
    public IntegrationDashboardOverviewVO getOverview() {
        Long tenantId = resolveTenantId();
        LocalDate today = LocalDate.now();
        LocalDateTime startTime = today.minusDays(DEFAULT_DAYS - 1L).atStartOfDay();
        LocalDateTime endTime = today.plusDays(1L).atStartOfDay().minusNanos(1L);
        List<String> tableNames = getExistingMonthTableNames(startTime, endTime);

        Map<String, Long> statusMap = aggregateStatus(tableNames, tenantId, startTime, endTime);
        long successCalls = statusMap.getOrDefault(ApiTaskStatusEnum.SUCCESS.name(), 0L);
        long failCalls = statusMap.getOrDefault(ApiTaskStatusEnum.FAIL.name(), 0L);
        long totalCalls = statusMap.values().stream().mapToLong(Long::longValue).sum();
        long todayCalls = countCalls(tableNames, tenantId, today.atStartOfDay(), endTime);

        IntegrationDashboardSummaryVO summary = buildSummary(tenantId, todayCalls, totalCalls, successCalls, failCalls);

        IntegrationDashboardOverviewVO overview = new IntegrationDashboardOverviewVO();
        overview.setSummary(summary);
        overview.setDirectionStats(buildDirectionStats(summary));
        overview.setStatusComparison(buildStatusComparison(successCalls, failCalls));
        overview.setStatusPie(buildStatusPie(statusMap));
        overview.setCallTrend(buildTrend(tableNames, tenantId, startTime, endTime));
        overview.setTopApis(buildTopApis(tableNames, tenantId, startTime, endTime));
        overview.setRecentFailures(buildRecentFailures(tableNames, tenantId, startTime, endTime));
        return overview;
    }

    private IntegrationDashboardSummaryVO buildSummary(
        Long tenantId,
        long todayCalls,
        long totalCalls,
        long successCalls,
        long failCalls
    ) {
        IntegrationDashboardSummaryVO summary = new IntegrationDashboardSummaryVO();
        summary.setTotalThirdSystems(countThirdSystems(tenantId, null));
        summary.setEnabledThirdSystems(countThirdSystems(tenantId, ENABLED_STATUS));
        summary.setTotalApis(countApis(tenantId, null, null));
        summary.setEnabledApis(countApis(tenantId, null, ENABLED_STATUS));
        summary.setInboundApis(countApis(tenantId, DIRECTION_INBOUND, null));
        summary.setOutboundApis(countApis(tenantId, DIRECTION_OUTBOUND, null));
        summary.setTodayCalls(todayCalls);
        summary.setTotalCalls(totalCalls);
        summary.setSuccessCalls(successCalls);
        summary.setFailCalls(failCalls);
        summary.setSuccessRate(calculateRate(successCalls, totalCalls));
        return summary;
    }

    private long countThirdSystems(Long tenantId, Integer status) {
        LambdaQueryWrapper<ThirdSystem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdSystem::getTenantId, tenantId)
            .eq(ThirdSystem::getDeleted, false);
        if (status != null) {
            wrapper.eq(ThirdSystem::getStatus, status);
        }
        Long count = thirdSystemMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private long countApis(Long tenantId, String direction, Integer status) {
        LambdaQueryWrapper<ApiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiConfig::getTenantId, tenantId)
            .eq(ApiConfig::getDeleted, false);
        if (direction != null) {
            wrapper.eq(ApiConfig::getDirection, direction);
        }
        if (status != null) {
            wrapper.eq(ApiConfig::getStatus, status);
        }
        Long count = apiConfigMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private Map<String, Long> aggregateStatus(List<String> tableNames, Long tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (String tableName : tableNames) {
            List<IntegrationDashboardChartItemVO> rows = apiCallLogMapper.countDashboardStatusFromTable(
                tableName,
                tenantId,
                startTime,
                endTime
            );
            for (IntegrationDashboardChartItemVO row : rows) {
                if (row.getName() == null) {
                    continue;
                }
                statusMap.merge(row.getName(), safeLong(row.getValue()), Long::sum);
            }
        }
        return statusMap;
    }

    private long countCalls(List<String> tableNames, Long tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        long total = 0L;
        for (String tableName : tableNames) {
            List<IntegrationDashboardChartItemVO> rows = apiCallLogMapper.countDashboardStatusFromTable(
                tableName,
                tenantId,
                startTime,
                endTime
            );
            total += rows.stream()
                .map(IntegrationDashboardChartItemVO::getValue)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        }
        return total;
    }

    private List<IntegrationDashboardChartItemVO> buildDirectionStats(IntegrationDashboardSummaryVO summary) {
        return List.of(
            new IntegrationDashboardChartItemVO(DIRECTION_INBOUND, safeLong(summary.getInboundApis())),
            new IntegrationDashboardChartItemVO(DIRECTION_OUTBOUND, safeLong(summary.getOutboundApis()))
        );
    }

    private List<IntegrationDashboardChartItemVO> buildStatusComparison(long successCalls, long failCalls) {
        return List.of(
            new IntegrationDashboardChartItemVO(ApiTaskStatusEnum.SUCCESS.name(), successCalls),
            new IntegrationDashboardChartItemVO(ApiTaskStatusEnum.FAIL.name(), failCalls)
        );
    }

    private List<IntegrationDashboardChartItemVO> buildStatusPie(Map<String, Long> statusMap) {
        if (statusMap.isEmpty()) {
            return buildStatusComparison(0L, 0L);
        }
        return statusMap.entrySet().stream()
            .map(entry -> new IntegrationDashboardChartItemVO(entry.getKey(), entry.getValue()))
            .toList();
    }

    private List<IntegrationDashboardTrendItemVO> buildTrend(
        List<String> tableNames,
        Long tenantId,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        Map<String, IntegrationDashboardTrendItemVO> trendMap = new LinkedHashMap<>();
        LocalDate startDate = startTime.toLocalDate();
        for (int i = 0; i < DEFAULT_DAYS; i++) {
            String date = startDate.plusDays(i).format(DAY_FORMATTER);
            IntegrationDashboardTrendItemVO item = new IntegrationDashboardTrendItemVO();
            item.setDate(date);
            item.setTotal(0L);
            item.setSuccess(0L);
            item.setFail(0L);
            trendMap.put(date, item);
        }

        for (String tableName : tableNames) {
            List<IntegrationDashboardTrendItemVO> rows = apiCallLogMapper.countDashboardTrendFromTable(
                tableName,
                tenantId,
                startTime,
                endTime
            );
            for (IntegrationDashboardTrendItemVO row : rows) {
                IntegrationDashboardTrendItemVO item = trendMap.get(row.getDate());
                if (item == null) {
                    continue;
                }
                item.setTotal(safeLong(item.getTotal()) + safeLong(row.getTotal()));
                item.setSuccess(safeLong(item.getSuccess()) + safeLong(row.getSuccess()));
                item.setFail(safeLong(item.getFail()) + safeLong(row.getFail()));
            }
        }
        return new ArrayList<>(trendMap.values());
    }

    private List<IntegrationDashboardTopApiVO> buildTopApis(
        List<String> tableNames,
        Long tenantId,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        Map<String, IntegrationDashboardTopApiVO> topApiMap = new HashMap<>();
        for (String tableName : tableNames) {
            List<IntegrationDashboardTopApiVO> rows = apiCallLogMapper.countDashboardTopApisFromTable(
                tableName,
                tenantId,
                startTime,
                endTime,
                TOP_API_LIMIT
            );
            for (IntegrationDashboardTopApiVO row : rows) {
                String key = buildTopApiKey(row);
                IntegrationDashboardTopApiVO item = topApiMap.computeIfAbsent(key, ignored -> copyTopApiBase(row));
                item.setTotalCalls(safeLong(item.getTotalCalls()) + safeLong(row.getTotalCalls()));
                item.setSuccessCalls(safeLong(item.getSuccessCalls()) + safeLong(row.getSuccessCalls()));
                item.setFailCalls(safeLong(item.getFailCalls()) + safeLong(row.getFailCalls()));
            }
        }

        List<IntegrationDashboardTopApiVO> topApis = topApiMap.values().stream()
            .sorted(Comparator.comparing(IntegrationDashboardTopApiVO::getTotalCalls, Comparator.nullsLast(Long::compareTo)).reversed())
            .limit(TOP_API_LIMIT)
            .toList();
        fillApiNames(topApis);
        topApis.forEach(item -> item.setSuccessRate(calculateRate(safeLong(item.getSuccessCalls()), safeLong(item.getTotalCalls()))));
        return topApis;
    }

    private List<IntegrationDashboardFailureVO> buildRecentFailures(
        List<String> tableNames,
        Long tenantId,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        List<IntegrationDashboardFailureVO> failures = new ArrayList<>();
        for (int index = tableNames.size() - 1; index >= 0; index--) {
            failures.addAll(apiCallLogMapper.selectDashboardFailuresFromTable(
                tableNames.get(index),
                tenantId,
                startTime,
                endTime,
                FAILURE_LIMIT
            ));
        }

        List<IntegrationDashboardFailureVO> recentFailures = failures.stream()
            .sorted(Comparator
                .comparing(
                    IntegrationDashboardFailureVO::getCallTime,
                    Comparator.nullsLast(LocalDateTime::compareTo)
                )
                .reversed()
                .thenComparing(Comparator
                    .comparing(IntegrationDashboardFailureVO::getId, Comparator.nullsLast(Long::compareTo))
                    .reversed()))
            .limit(FAILURE_LIMIT)
            .toList();
        fillFailureApiNames(recentFailures);
        return recentFailures;
    }

    private IntegrationDashboardTopApiVO copyTopApiBase(IntegrationDashboardTopApiVO row) {
        IntegrationDashboardTopApiVO item = new IntegrationDashboardTopApiVO();
        item.setApiConfigId(row.getApiConfigId());
        item.setApiCode(row.getApiCode());
        item.setApiName(row.getApiName());
        item.setCallDirection(row.getCallDirection());
        item.setTotalCalls(0L);
        item.setSuccessCalls(0L);
        item.setFailCalls(0L);
        return item;
    }

    private String buildTopApiKey(IntegrationDashboardTopApiVO row) {
        return String.join(
            "|",
            String.valueOf(row.getApiConfigId()),
            String.valueOf(row.getApiCode()),
            String.valueOf(row.getCallDirection())
        );
    }

    private void fillApiNames(List<IntegrationDashboardTopApiVO> topApis) {
        Map<Long, ApiConfig> apiConfigMap = loadApiConfigMap(
            topApis.stream()
                .map(IntegrationDashboardTopApiVO::getApiConfigId)
                .filter(Objects::nonNull)
                .distinct()
                .toList()
        );
        for (IntegrationDashboardTopApiVO item : topApis) {
            ApiConfig apiConfig = apiConfigMap.get(item.getApiConfigId());
            if (apiConfig != null && apiConfig.getApiName() != null) {
                item.setApiName(apiConfig.getApiName());
            }
            if (item.getApiName() == null || item.getApiName().isBlank()) {
                item.setApiName(item.getApiCode());
            }
        }
    }

    private void fillFailureApiNames(List<IntegrationDashboardFailureVO> failures) {
        Map<Long, ApiConfig> apiConfigMap = loadApiConfigMap(
            failures.stream()
                .map(IntegrationDashboardFailureVO::getApiConfigId)
                .filter(Objects::nonNull)
                .distinct()
                .toList()
        );
        for (IntegrationDashboardFailureVO item : failures) {
            ApiConfig apiConfig = apiConfigMap.get(item.getApiConfigId());
            if (apiConfig != null && apiConfig.getApiName() != null) {
                item.setApiName(apiConfig.getApiName());
            }
            if (item.getApiName() == null || item.getApiName().isBlank()) {
                item.setApiName(item.getApiCode());
            }
        }
    }

    private Map<Long, ApiConfig> loadApiConfigMap(List<Long> apiConfigIds) {
        if (apiConfigIds.isEmpty()) {
            return Map.of();
        }
        return apiConfigMapper.selectBatchIds(apiConfigIds).stream()
            .collect(Collectors.toMap(ApiConfig::getId, Function.identity(), (left, right) -> left));
    }

    private List<String> getExistingMonthTableNames(LocalDateTime startTime, LocalDateTime endTime) {
        return getMonthTableNames(startTime, endTime).stream()
            .filter(tableName -> {
                boolean exists = apiCallLogTableService.tableExists(tableName);
                if (!exists) {
                    log.error("Api call log month table does not exist, skip dashboard query: {}", tableName);
                }
                return exists;
            })
            .toList();
    }

    private List<String> getMonthTableNames(LocalDateTime startTime, LocalDateTime endTime) {
        List<String> tableNames = new ArrayList<>();
        LocalDateTime current = startTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endMonth = endTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        while (!current.isAfter(endMonth)) {
            tableNames.add(TABLE_PREFIX + current.format(MONTH_FORMATTER));
            current = current.plusMonths(1);
        }
        return tableNames;
    }

    private Long resolveTenantId() {
        Long tenantId = TenantContext.get();
        return tenantId == null ? 0L : tenantId;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private double calculateRate(long success, long total) {
        if (total <= 0) {
            return 0.0D;
        }
        return BigDecimal.valueOf(success)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
            .doubleValue();
    }
}
