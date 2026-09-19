package com.forgex.basic.workcalendar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.basic.workcalendar.domain.entity.BasicWorkCalendarDay;
import com.forgex.basic.workcalendar.domain.entity.BasicWorkCalendarTenantEvent;
import com.forgex.basic.workcalendar.domain.entity.BasicWorkCalendarUserEvent;
import com.forgex.basic.workcalendar.domain.param.WorkCalendarDayUpdateParam;
import com.forgex.basic.workcalendar.domain.param.WorkCalendarEventDeleteParam;
import com.forgex.basic.workcalendar.domain.param.WorkCalendarEventSaveParam;
import com.forgex.basic.workcalendar.domain.param.WorkCalendarMonthParam;
import com.forgex.basic.workcalendar.domain.vo.WorkCalendarEventVO;
import com.forgex.basic.workcalendar.domain.vo.WorkCalendarMonthVO;
import com.forgex.basic.workcalendar.mapper.BasicWorkCalendarDayMapper;
import com.forgex.basic.workcalendar.mapper.BasicWorkCalendarTenantEventMapper;
import com.forgex.basic.workcalendar.mapper.BasicWorkCalendarUserEventMapper;
import com.forgex.basic.workcalendar.service.IWorkCalendarService;
import com.forgex.common.api.dto.CalendarReminderCancelDTO;
import com.forgex.common.api.dto.CalendarReminderTaskSyncDTO;
import com.forgex.common.api.feign.CalendarReminderFeignClient;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.StatusCode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 工作日历服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkCalendarServiceImpl implements IWorkCalendarService {

    private static final Long PUBLIC_TENANT_ID = 0L;
    private static final String SCOPE_USER = "USER";
    private static final String SCOPE_TENANT = "TENANT";
    private static final String SOURCE_USER_EVENT = "USER_EVENT";
    private static final String SOURCE_TENANT_EVENT = "TENANT_EVENT";
    private static final String DEFAULT_TEMPLATE_CODE = "CALENDAR_REMINDER";
    private static final int DATE_TYPE_WORKDAY = 1;
    private static final int DATE_TYPE_PUBLIC_REST = 2;
    private static final int DATE_TYPE_STATUTORY_HOLIDAY = 3;
    private static final int DATE_TYPE_MAKEUP_WORKDAY = 4;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final Executor CALENDAR_GENERATE_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(2, Math.min(Runtime.getRuntime().availableProcessors(), 4)));

    private final BasicWorkCalendarDayMapper dayMapper;
    private final BasicWorkCalendarTenantEventMapper tenantEventMapper;
    private final BasicWorkCalendarUserEventMapper userEventMapper;
    private final CalendarReminderFeignClient reminderFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public WorkCalendarMonthVO month(WorkCalendarMonthParam param) {
        WorkCalendarMonthParam safeParam = param == null ? new WorkCalendarMonthParam() : param;
        YearMonth target = YearMonth.of(
                safeParam.getYear() == null ? LocalDate.now().getYear() : safeParam.getYear(),
                safeParam.getMonth() == null ? LocalDate.now().getMonthValue() : safeParam.getMonth());
        boolean syncHoliday = Boolean.TRUE.equals(safeParam.getSyncHoliday());
        ensureMonth(target, syncHoliday);

        LocalDate startDate = target.atDay(1);
        LocalDate endDate = target.atEndOfMonth();
        List<BasicWorkCalendarDay> days = dayMapper.selectList(new LambdaQueryWrapper<BasicWorkCalendarDay>()
                .between(BasicWorkCalendarDay::getCalendarDate, startDate, endDate)
                .orderByAsc(BasicWorkCalendarDay::getCalendarDate));

        WorkCalendarMonthVO vo = new WorkCalendarMonthVO();
        vo.setYear(target.getYear());
        vo.setMonth(target.getMonthValue());
        vo.setSyncHoliday(syncHoliday);
        vo.setDays(days);
        vo.setEvents(loadEvents(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay(), safeParam.getCalendarScopes()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveEvent(WorkCalendarEventSaveParam param) {
        validateEvent(param);
        String scope = normalizeScope(param.getScope());
        Long id = SCOPE_TENANT.equals(scope) ? saveTenantEvent(param) : saveUserEvent(param);
        syncReminder(scope, id);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteEvent(WorkCalendarEventDeleteParam param) {
        if (param == null || param.getId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        String scope = normalizeScope(param.getScope());
        if (SCOPE_TENANT.equals(scope)) {
            tenantEventMapper.deleteById(param.getId());
            cancelReminder(SOURCE_TENANT_EVENT, param.getId());
        } else {
            BasicWorkCalendarUserEvent event = userEventMapper.selectById(param.getId());
            Long userId = currentUserId();
            if (event == null || !Objects.equals(event.getOwnerUserId(), userId)) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED);
            }
            userEventMapper.deleteById(param.getId());
            cancelReminder(SOURCE_USER_EVENT, param.getId());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDay(WorkCalendarDayUpdateParam param) {
        if (param == null || param.getCalendarDate() == null || param.getDateType() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
        ensureMonth(YearMonth.from(param.getCalendarDate()), false);
        BasicWorkCalendarDay day = dayMapper.selectOne(new LambdaQueryWrapper<BasicWorkCalendarDay>()
                .eq(BasicWorkCalendarDay::getCalendarDate, param.getCalendarDate())
                .last("limit 1"));
        if (day == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED);
        }
        day.setDateType(param.getDateType());
        day.setHolidayName(param.getHolidayName());
        day.setCustomWeek(param.getCustomWeek());
        day.setPublicWeek(param.getPublicWeek());
        day.setRemark(param.getRemark());
        dayMapper.updateById(day);
        return true;
    }

    private void ensureMonth(YearMonth target, boolean syncHoliday) {
        long count = dayMapper.selectCount(new LambdaQueryWrapper<BasicWorkCalendarDay>()
                .eq(BasicWorkCalendarDay::getYearValue, target.getYear())
                .eq(BasicWorkCalendarDay::getMonthValue, target.getMonthValue()));
        if (count < target.lengthOfMonth()) {
            generateYearAsync(target.getYear(), syncHoliday);
        }
        if (syncHoliday) {
            CompletableFuture.runAsync(() -> ensureYear(target.getYear() - 1, true), CALENDAR_GENERATE_EXECUTOR);
            CompletableFuture.runAsync(() -> ensureYear(target.getYear() + 1, true), CALENDAR_GENERATE_EXECUTOR);
        }
    }

    private void generateYearAsync(int year, boolean syncHoliday) {
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> ensureYear(year - 1, syncHoliday), CALENDAR_GENERATE_EXECUTOR),
                CompletableFuture.runAsync(() -> ensureYear(year, syncHoliday), CALENDAR_GENERATE_EXECUTOR),
                CompletableFuture.runAsync(() -> ensureYear(year + 1, syncHoliday), CALENDAR_GENERATE_EXECUTOR)
        ).join();
    }

    private void ensureYear(int year, boolean syncHoliday) {
        Map<LocalDate, HolidayInfo> holidayMap = syncHoliday ? loadHolidayMap(year) : Map.of();
        for (int month = 1; month <= 12; month++) {
            YearMonth ym = YearMonth.of(year, month);
            for (int day = 1; day <= ym.lengthOfMonth(); day++) {
                LocalDate date = ym.atDay(day);
                if (exists(date)) {
                    if (syncHoliday && holidayMap.containsKey(date)) {
                        refreshHoliday(date, holidayMap.get(date), year);
                    }
                    continue;
                }
                insertDay(date, syncHoliday, holidayMap.get(date), year);
            }
        }
    }

    private boolean exists(LocalDate date) {
        return dayMapper.selectCount(new LambdaQueryWrapper<BasicWorkCalendarDay>()
                .eq(BasicWorkCalendarDay::getCalendarDate, date)) > 0;
    }

    private void insertDay(LocalDate date, boolean syncHoliday, HolidayInfo holiday, int sourceYear) {
        BasicWorkCalendarDay entity = new BasicWorkCalendarDay();
        entity.setTenantId(PUBLIC_TENANT_ID);
        entity.setCalendarDate(date);
        entity.setYearValue(date.getYear());
        entity.setMonthValue(date.getMonthValue());
        entity.setDayValue(date.getDayOfMonth());
        entity.setPublicWeek(publicWeek(date));
        entity.setHolidaySynced(syncHoliday && holiday != null);
        entity.setHolidaySourceYear(syncHoliday && holiday != null ? sourceYear : null);
        applyType(entity, holiday);
        try {
            dayMapper.insert(entity);
        } catch (Exception ex) {
            log.info("工作日历日期已存在 date={}", date);
        }
    }

    private void refreshHoliday(LocalDate date, HolidayInfo holiday, int sourceYear) {
        BasicWorkCalendarDay entity = dayMapper.selectOne(new LambdaQueryWrapper<BasicWorkCalendarDay>()
                .eq(BasicWorkCalendarDay::getCalendarDate, date)
                .last("limit 1"));
        if (entity == null) {
            return;
        }
        applyType(entity, holiday);
        entity.setHolidaySynced(true);
        entity.setHolidaySourceYear(sourceYear);
        dayMapper.updateById(entity);
    }

    private void applyType(BasicWorkCalendarDay entity, HolidayInfo holiday) {
        if (holiday != null) {
            entity.setHolidayName(holiday.getName());
            entity.setDateType(Boolean.TRUE.equals(holiday.getOffDay()) ? DATE_TYPE_STATUTORY_HOLIDAY : DATE_TYPE_MAKEUP_WORKDAY);
            return;
        }
        DayOfWeek dayOfWeek = entity.getCalendarDate().getDayOfWeek();
        entity.setHolidayName(null);
        entity.setDateType(dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
                ? DATE_TYPE_PUBLIC_REST : DATE_TYPE_WORKDAY);
    }

    private Map<LocalDate, HolidayInfo> loadHolidayMap(int year) {
        Map<LocalDate, HolidayInfo> result = new HashMap<>();
        for (int sourceYear = year - 1; sourceYear <= year + 1; sourceYear++) {
            try {
                URI uri = URI.create("https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/" + sourceYear + ".json");
                HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    continue;
                }
                JsonNode days = objectMapper.readTree(response.body()).path("days");
                if (!days.isArray()) {
                    continue;
                }
                for (JsonNode node : days) {
                    LocalDate date = LocalDate.parse(node.path("date").asText(), DATE_FORMATTER);
                    HolidayInfo info = new HolidayInfo();
                    info.setName(node.path("name").asText(null));
                    info.setOffDay(node.path("isOffDay").asBoolean());
                    result.put(date, info);
                }
            } catch (Exception ex) {
                log.error("同步中国法定节假日失败 year={}", sourceYear, ex);
            }
        }
        return result;
    }

    private List<WorkCalendarEventVO> loadEvents(LocalDateTime start, LocalDateTime end, List<String> scopes) {
        Set<String> enabledScopes = CollectionUtils.isEmpty(scopes) ? Set.of(SCOPE_USER, SCOPE_TENANT) : Set.copyOf(scopes);
        List<WorkCalendarEventVO> events = new ArrayList<>();
        if (enabledScopes.contains(SCOPE_TENANT)) {
            tenantEventMapper.selectList(new LambdaQueryWrapper<BasicWorkCalendarTenantEvent>()
                    .lt(BasicWorkCalendarTenantEvent::getStartTime, end)
                    .gt(BasicWorkCalendarTenantEvent::getEndTime, start)
                    .orderByAsc(BasicWorkCalendarTenantEvent::getStartTime))
                    .forEach(item -> events.add(toTenantVO(item)));
        }
        if (enabledScopes.contains(SCOPE_USER)) {
            Long userId = currentUserId();
            userEventMapper.selectList(new LambdaQueryWrapper<BasicWorkCalendarUserEvent>()
                    .eq(BasicWorkCalendarUserEvent::getOwnerUserId, userId)
                    .lt(BasicWorkCalendarUserEvent::getStartTime, end)
                    .gt(BasicWorkCalendarUserEvent::getEndTime, start)
                    .orderByAsc(BasicWorkCalendarUserEvent::getStartTime))
                    .forEach(item -> events.add(toUserVO(item)));
        }
        events.sort(Comparator.comparing(WorkCalendarEventVO::getStartTime));
        return events;
    }

    private Long saveUserEvent(WorkCalendarEventSaveParam param) {
        BasicWorkCalendarUserEvent entity = param.getId() == null ? new BasicWorkCalendarUserEvent() : userEventMapper.selectById(param.getId());
        if (entity == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED);
        }
        Long userId = currentUserId();
        if (param.getId() != null && !Objects.equals(entity.getOwnerUserId(), userId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED);
        }
        fillUserEvent(entity, param);
        entity.setTenantId(currentTenantId());
        entity.setOwnerUserId(userId);
        if (entity.getId() == null) {
            userEventMapper.insert(entity);
        } else {
            userEventMapper.updateById(entity);
        }
        return entity.getId();
    }

    private Long saveTenantEvent(WorkCalendarEventSaveParam param) {
        BasicWorkCalendarTenantEvent entity = param.getId() == null ? new BasicWorkCalendarTenantEvent() : tenantEventMapper.selectById(param.getId());
        if (entity == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.OPERATION_FAILED);
        }
        fillTenantEvent(entity, param);
        entity.setTenantId(currentTenantId());
        entity.setSourceUserId(currentUserId());
        if (entity.getId() == null) {
            tenantEventMapper.insert(entity);
        } else {
            tenantEventMapper.updateById(entity);
        }
        return entity.getId();
    }

    private void fillUserEvent(BasicWorkCalendarUserEvent entity, WorkCalendarEventSaveParam param) {
        entity.setRecordType(param.getRecordType());
        entity.setEventTitle(param.getEventTitle());
        entity.setEventContent(param.getEventContent());
        entity.setStartTime(param.getStartTime());
        entity.setEndTime(param.getEndTime());
        entity.setNotifyUserIds(toJson(param.getNotifyUserIds()));
        entity.setRemindMinutes(param.getRemindMinutes());
        entity.setRemindTime(resolveRemindTime(param));
        entity.setMessageTemplateCode(StringUtils.hasText(param.getMessageTemplateCode()) ? param.getMessageTemplateCode() : DEFAULT_TEMPLATE_CODE);
        entity.setRemark(param.getRemark());
    }

    private void fillTenantEvent(BasicWorkCalendarTenantEvent entity, WorkCalendarEventSaveParam param) {
        entity.setRecordType(param.getRecordType());
        entity.setEventTitle(param.getEventTitle());
        entity.setEventContent(param.getEventContent());
        entity.setStartTime(param.getStartTime());
        entity.setEndTime(param.getEndTime());
        entity.setNotifyUserIds(toJson(param.getNotifyUserIds()));
        entity.setRemindMinutes(param.getRemindMinutes());
        entity.setRemindTime(resolveRemindTime(param));
        entity.setMessageTemplateCode(StringUtils.hasText(param.getMessageTemplateCode()) ? param.getMessageTemplateCode() : DEFAULT_TEMPLATE_CODE);
        entity.setRemark(param.getRemark());
    }

    private LocalDateTime resolveRemindTime(WorkCalendarEventSaveParam param) {
        if (param.getRemindTime() != null) {
            return param.getRemindTime();
        }
        int minutes = param.getRemindMinutes() == null ? 0 : Math.max(param.getRemindMinutes(), 0);
        return param.getStartTime().minusMinutes(minutes);
    }

    private void syncReminder(String scope, Long id) {
        try {
            if (SCOPE_TENANT.equals(scope)) {
                BasicWorkCalendarTenantEvent event = tenantEventMapper.selectById(id);
                reminderFeignClient.sync(buildTenantReminder(event));
            } else {
                BasicWorkCalendarUserEvent event = userEventMapper.selectById(id);
                reminderFeignClient.sync(buildUserReminder(event));
            }
        } catch (Exception ex) {
            log.error("同步工作日历提醒任务失败 scope={}, id={}", scope, id, ex);
        }
    }

    private CalendarReminderTaskSyncDTO buildUserReminder(BasicWorkCalendarUserEvent event) {
        CalendarReminderTaskSyncDTO dto = new CalendarReminderTaskSyncDTO();
        dto.setSourceType(SOURCE_USER_EVENT);
        dto.setSourceId(event.getId());
        dto.setTenantId(event.getTenantId());
        dto.setOwnerUserId(event.getOwnerUserId());
        dto.setTitle(event.getEventTitle());
        dto.setRecordType(event.getRecordType());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setRemindTime(event.getRemindTime());
        dto.setNotifyUserIds(parseUserIds(event.getNotifyUserIds(), event.getOwnerUserId()));
        dto.setTemplateCode(event.getMessageTemplateCode());
        dto.setTemplateData(templateData(event.getEventTitle(), event.getRecordType(), event.getStartTime(), event.getEndTime()));
        return dto;
    }

    private CalendarReminderTaskSyncDTO buildTenantReminder(BasicWorkCalendarTenantEvent event) {
        CalendarReminderTaskSyncDTO dto = new CalendarReminderTaskSyncDTO();
        dto.setSourceType(SOURCE_TENANT_EVENT);
        dto.setSourceId(event.getId());
        dto.setTenantId(event.getTenantId());
        dto.setOwnerUserId(event.getSourceUserId());
        dto.setTitle(event.getEventTitle());
        dto.setRecordType(event.getRecordType());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setRemindTime(event.getRemindTime());
        dto.setNotifyUserIds(parseUserIds(event.getNotifyUserIds(), event.getSourceUserId()));
        dto.setTemplateCode(event.getMessageTemplateCode());
        dto.setTemplateData(templateData(event.getEventTitle(), event.getRecordType(), event.getStartTime(), event.getEndTime()));
        return dto;
    }

    private Map<String, Object> templateData(String title, String recordType, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", title);
        data.put("recordType", recordType);
        data.put("startTime", startTime == null ? "" : startTime.toString());
        data.put("endTime", endTime == null ? "" : endTime.toString());
        return data;
    }

    private void cancelReminder(String sourceType, Long sourceId) {
        try {
            CalendarReminderCancelDTO dto = new CalendarReminderCancelDTO();
            dto.setSourceType(sourceType);
            dto.setSourceId(sourceId);
            dto.setTenantId(currentTenantId());
            reminderFeignClient.cancel(dto);
        } catch (Exception ex) {
            log.error("取消工作日历提醒任务失败 sourceType={}, sourceId={}", sourceType, sourceId, ex);
        }
    }

    private WorkCalendarEventVO toUserVO(BasicWorkCalendarUserEvent entity) {
        WorkCalendarEventVO vo = new WorkCalendarEventVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setScope(SCOPE_USER);
        vo.setNotifyUserIds(parseUserIds(entity.getNotifyUserIds(), entity.getOwnerUserId()));
        return vo;
    }

    private WorkCalendarEventVO toTenantVO(BasicWorkCalendarTenantEvent entity) {
        WorkCalendarEventVO vo = new WorkCalendarEventVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setScope(SCOPE_TENANT);
        vo.setNotifyUserIds(parseUserIds(entity.getNotifyUserIds(), entity.getSourceUserId()));
        return vo;
    }

    private void validateEvent(WorkCalendarEventSaveParam param) {
        if (param == null || !StringUtils.hasText(param.getRecordType()) || !StringUtils.hasText(param.getEventTitle())
                || param.getStartTime() == null || param.getEndTime() == null || !param.getEndTime().isAfter(param.getStartTime())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, CommonPrompt.PARAM_EMPTY);
        }
    }

    private String normalizeScope(String scope) {
        return SCOPE_TENANT.equalsIgnoreCase(scope) ? SCOPE_TENANT : SCOPE_USER;
    }

    private Long currentTenantId() {
        Long tenantId = CurrentUserUtils.getTenantId();
        return tenantId == null ? PUBLIC_TENANT_ID : tenantId;
    }

    private Long currentUserId() {
        Long userId = CurrentUserUtils.getUserId();
        if (userId == null) {
            throw new I18nBusinessException(StatusCode.NOT_LOGIN, CommonPrompt.OPERATION_FAILED);
        }
        return userId;
    }

    private String toJson(List<Long> ids) {
        try {
            return objectMapper.writeValueAsString(ids == null ? List.of() : ids);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private List<Long> parseUserIds(String json, Long fallbackUserId) {
        try {
            List<Long> ids = StringUtils.hasText(json)
                    ? objectMapper.readValue(json, new TypeReference<List<Long>>() {})
                    : new ArrayList<>();
            if (ids.isEmpty() && fallbackUserId != null) {
                ids.add(fallbackUserId);
            }
            return ids;
        } catch (Exception ex) {
            return fallbackUserId == null ? List.of() : List.of(fallbackUserId);
        }
    }

    private String publicWeek(LocalDate date) {
        int week = (date.getDayOfMonth() - 1) / 7 + 1;
        return "W" + week;
    }

    @Data
    private static class HolidayInfo {
        private String name;
        private Boolean offDay;
    }
}
