package com.forgex.job.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.CalendarReminderCancelDTO;
import com.forgex.common.api.dto.CalendarReminderTaskSyncDTO;
import com.forgex.common.service.TemplateMessageService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.job.domain.entity.SysCalendarReminderTask;
import com.forgex.job.mapper.SysCalendarReminderTaskMapper;
import com.forgex.job.service.ICalendarReminderTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 日历提醒任务服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Slf4j
@Service
@DS("job")
@RequiredArgsConstructor
public class CalendarReminderTaskServiceImpl extends ServiceImpl<SysCalendarReminderTaskMapper, SysCalendarReminderTask>
        implements ICalendarReminderTaskService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PROCESSING = 1;
    private static final int STATUS_SENT = 2;
    private static final int STATUS_FAILED = 3;
    private static final int STATUS_CANCELED = 4;
    private static final int DEFAULT_MAX_RETRY = 3;
    private static final String DEFAULT_TEMPLATE_CODE = "CALENDAR_REMINDER";

    private final SysCalendarReminderTaskMapper reminderTaskMapper;
    private final TemplateMessageService templateMessageService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sync(CalendarReminderTaskSyncDTO param) {
        if (param == null || !StringUtils.hasText(param.getSourceType()) || param.getSourceId() == null) {
            return false;
        }
        cancelBySource(param.getSourceType(), param.getSourceId(), param.getTenantId());
        if (param.getRemindTime() == null || !param.getRemindTime().isAfter(LocalDateTime.now())) {
            return true;
        }
        if (CollectionUtils.isEmpty(param.getNotifyUserIds())) {
            return true;
        }
        SysCalendarReminderTask task = new SysCalendarReminderTask();
        task.setTenantId(param.getTenantId() == null ? 0L : param.getTenantId());
        task.setSourceType(param.getSourceType());
        task.setSourceId(param.getSourceId());
        task.setOwnerUserId(param.getOwnerUserId());
        task.setTitle(param.getTitle());
        task.setRecordType(param.getRecordType());
        task.setStartTime(param.getStartTime());
        task.setEndTime(param.getEndTime());
        task.setRemindTime(param.getRemindTime());
        task.setNotifyUserIds(writeJson(new ArrayList<>(new LinkedHashSet<>(param.getNotifyUserIds()))));
        task.setTemplateCode(StringUtils.hasText(param.getTemplateCode()) ? param.getTemplateCode() : DEFAULT_TEMPLATE_CODE);
        task.setTemplateData(writeJson(param.getTemplateData()));
        task.setStatus(STATUS_PENDING);
        task.setSendCount(0);
        task.setMaxRetryCount(DEFAULT_MAX_RETRY);
        task.setNextRetryTime(param.getRemindTime());
        reminderTaskMapper.insert(task);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(CalendarReminderCancelDTO param) {
        if (param == null || !StringUtils.hasText(param.getSourceType()) || param.getSourceId() == null) {
            return false;
        }
        cancelBySource(param.getSourceType(), param.getSourceId(), param.getTenantId());
        return true;
    }

    @Override
    public void scanDueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<SysCalendarReminderTask> tasks = reminderTaskMapper.selectList(new LambdaQueryWrapper<SysCalendarReminderTask>()
                .in(SysCalendarReminderTask::getStatus, List.of(STATUS_PENDING, STATUS_FAILED))
                .le(SysCalendarReminderTask::getRemindTime, now)
                .le(SysCalendarReminderTask::getNextRetryTime, now)
                .orderByAsc(SysCalendarReminderTask::getRemindTime)
                .last("limit 100"));
        for (SysCalendarReminderTask task : tasks) {
            processTask(task);
        }
    }

    private void processTask(SysCalendarReminderTask task) {
        boolean locked = reminderTaskMapper.update(null, new LambdaUpdateWrapper<SysCalendarReminderTask>()
                .set(SysCalendarReminderTask::getStatus, STATUS_PROCESSING)
                .eq(SysCalendarReminderTask::getId, task.getId())
                .in(SysCalendarReminderTask::getStatus, List.of(STATUS_PENDING, STATUS_FAILED))) > 0;
        if (!locked) {
            return;
        }
        TenantContext.set(task.getTenantId());
        UserContext.set(task.getOwnerUserId());
        try {
            List<Long> receivers = readUserIds(task.getNotifyUserIds());
            Map<String, Object> data = readData(task.getTemplateData());
            int sentCount = templateMessageService.sendByTemplate(task.getTemplateCode(), receivers, data, "CALENDAR");
            task.setStatus(STATUS_SENT);
            task.setSentTime(LocalDateTime.now());
            task.setSendCount((task.getSendCount() == null ? 0 : task.getSendCount()) + 1);
            task.setFailReason(null);
            reminderTaskMapper.updateById(task);
            log.info("日历提醒发送完成 taskId={}, sentCount={}", task.getId(), sentCount);
        } catch (Exception ex) {
            markFailed(task, ex);
        } finally {
            UserContext.clear();
            TenantContext.clear();
        }
    }

    private void markFailed(SysCalendarReminderTask task, Exception ex) {
        int sendCount = (task.getSendCount() == null ? 0 : task.getSendCount()) + 1;
        task.setSendCount(sendCount);
        task.setFailReason(ex.getMessage());
        if (sendCount >= (task.getMaxRetryCount() == null ? DEFAULT_MAX_RETRY : task.getMaxRetryCount())) {
            task.setStatus(STATUS_FAILED);
            task.setNextRetryTime(LocalDateTime.now().plusYears(100));
        } else {
            task.setStatus(STATUS_FAILED);
            task.setNextRetryTime(LocalDateTime.now().plusMinutes(Math.max(1, sendCount * 5L)));
        }
        reminderTaskMapper.updateById(task);
        log.warn("日历提醒发送失败 taskId={}", task.getId(), ex);
    }

    private void cancelBySource(String sourceType, Long sourceId, Long tenantId) {
        reminderTaskMapper.update(null, new LambdaUpdateWrapper<SysCalendarReminderTask>()
                .set(SysCalendarReminderTask::getStatus, STATUS_CANCELED)
                .eq(SysCalendarReminderTask::getSourceType, sourceType)
                .eq(SysCalendarReminderTask::getSourceId, sourceId)
                .eq(tenantId != null, SysCalendarReminderTask::getTenantId, tenantId)
                .in(SysCalendarReminderTask::getStatus, List.of(STATUS_PENDING, STATUS_FAILED, STATUS_PROCESSING)));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private List<Long> readUserIds(String json) {
        try {
            Set<Long> values = objectMapper.readValue(json, new TypeReference<LinkedHashSet<Long>>() {});
            return values.stream().filter(item -> item != null).toList();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private Map<String, Object> readData(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return Map.of();
        }
    }
}
