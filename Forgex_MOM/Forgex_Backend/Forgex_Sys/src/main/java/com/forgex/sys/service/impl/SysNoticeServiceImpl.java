package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.TenantContextIgnore;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.dto.SysNoticeAttachmentDTO;
import com.forgex.sys.domain.dto.SysNoticeDTO;
import com.forgex.sys.domain.entity.SysNotice;
import com.forgex.sys.domain.entity.SysNoticeAttachment;
import com.forgex.sys.domain.entity.SysNoticeUserRecord;
import com.forgex.sys.domain.param.SysNoticePageParam;
import com.forgex.sys.domain.param.SysNoticeSaveParam;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.mapper.SysNoticeAttachmentMapper;
import com.forgex.sys.mapper.SysNoticeMapper;
import com.forgex.sys.mapper.SysNoticeUserRecordMapper;
import com.forgex.sys.service.ISysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 系统通知服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

    private static final int DEFAULT_ACTIVE_NOTICE_LIMIT = 20;
    private static final String SCOPE_PUBLIC = "PUBLIC";
    private static final String SCOPE_TENANT = "TENANT";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_DISABLED = "DISABLED";

    private final SysNoticeMapper noticeMapper;
    private final SysNoticeAttachmentMapper attachmentMapper;
    private final SysNoticeUserRecordMapper userRecordMapper;

    /**
     * 分页查询系统通知。
     *
     * @param page 分页对象
     * @param param 查询参数
     * @return 分页结果
     */
    @Override
    public IPage<SysNoticeDTO> pageNotices(Page<SysNotice> page, SysNoticePageParam param) {
        return runWithTenantIgnore(() -> {
            SysNoticePageParam safeParam = param == null ? new SysNoticePageParam() : param;
            Page<SysNotice> result = noticeMapper.selectPage(page, buildPageWrapper(safeParam));
            List<SysNoticeDTO> records = fillAttachments(result.getRecords());
            Page<SysNoticeDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
            dtoPage.setRecords(records);
            return dtoPage;
        });
    }

    /**
     * 查询系统通知详情。
     *
     * @param id 通知 ID
     * @return 通知详情
     */
    @Override
    public SysNoticeDTO detail(Long id) {
        return runWithTenantIgnore(() -> {
            SysNotice notice = requireNotice(id);
            assertReadable(notice);
            return toDTO(notice, listAttachments(id));
        });
    }

    /**
     * 保存系统通知。
     *
     * @param param 保存参数
     * @return 通知 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveNotice(SysNoticeSaveParam param) {
        return runWithTenantIgnore(() -> {
            validateSaveParam(param);
            SysNotice notice = param.getId() == null ? new SysNotice() : requireNotice(param.getId());
            if (param.getId() != null) {
                assertWritable(notice);
            }
            BeanUtils.copyProperties(param, notice, "attachments");
            notice.setScope(normalizeScope(param.getScope()));
            notice.setStatus(normalizeStatus(param.getStatus()));
            notice.setOrderNum(param.getOrderNum() == null ? 0 : param.getOrderNum());
            notice.setForceRemind(Boolean.TRUE.equals(param.getForceRemind()));
            if (SCOPE_PUBLIC.equals(notice.getScope())) {
                notice.setTenantId(0L);
            } else {
                notice.setTenantId(currentTenantId());
            }
            saveOrUpdate(notice);
            replaceAttachments(notice.getId(), notice.getTenantId(), param.getAttachments());
            return notice.getId();
        });
    }

    /**
     * 删除系统通知。
     *
     * @param id 通知 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        runWithTenantIgnore(() -> {
            SysNotice notice = requireNotice(id);
            assertWritable(notice);
            noticeMapper.deleteById(id);
            attachmentMapper.delete(new LambdaQueryWrapper<SysNoticeAttachment>()
                    .eq(SysNoticeAttachment::getNoticeId, id));
            return null;
        });
    }

    /**
     * 发布系统通知。
     *
     * @param id 通知 ID
     */
    @Override
    public void publish(Long id) {
        updateStatus(id, STATUS_PUBLISHED);
    }

    /**
     * 停用系统通知。
     *
     * @param id 通知 ID
     */
    @Override
    public void disable(Long id) {
        updateStatus(id, STATUS_DISABLED);
    }

    /**
     * 查询当前用户需要弹出的通知。
     *
     * @return 通知列表
     */
    @Override
    public List<SysNoticeDTO> listPopupNotices() {
        return runWithTenantIgnore(() -> {
            Long tenantId = currentTenantId();
            Long userId = currentUserId();
            List<SysNotice> notices = listEffectiveNoticeEntities(tenantId, null);
            if (notices == null || notices.isEmpty()) {
                return List.of();
            }

            Set<Long> ackedIds = userRecordMapper.selectList(new LambdaQueryWrapper<SysNoticeUserRecord>()
                            .eq(SysNoticeUserRecord::getUserId, userId)
                            .eq(SysNoticeUserRecord::getTenantId, tenantId)
                            .in(SysNoticeUserRecord::getNoticeId, notices.stream().map(SysNotice::getId).collect(Collectors.toSet())))
                    .stream()
                    .map(SysNoticeUserRecord::getNoticeId)
                    .collect(Collectors.toSet());
            List<SysNotice> pending = notices.stream()
                    .filter(item -> !ackedIds.contains(item.getId()))
                    .collect(Collectors.toList());
            return fillAttachments(pending);
        });
    }

    /**
     * 记录当前用户已弹出通知。
     *
     * @param noticeId 通知 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ackPopup(Long noticeId) {
        runWithTenantIgnore(() -> {
            SysNotice notice = requireNotice(noticeId);
            Long tenantId = currentTenantId();
            Long userId = currentUserId();
            if (!SCOPE_PUBLIC.equals(notice.getScope()) && !tenantId.equals(notice.getTenantId())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MSG_NO_PERMISSION);
            }

            SysNoticeUserRecord exists = userRecordMapper.selectOne(new LambdaQueryWrapper<SysNoticeUserRecord>()
                    .eq(SysNoticeUserRecord::getNoticeId, noticeId)
                    .eq(SysNoticeUserRecord::getUserId, userId)
                    .eq(SysNoticeUserRecord::getTenantId, tenantId)
                    .last("limit 1"));
            LocalDateTime now = LocalDateTime.now();
            if (exists == null) {
                SysNoticeUserRecord record = new SysNoticeUserRecord();
                record.setTenantId(tenantId);
                record.setNoticeId(noticeId);
                record.setUserId(userId);
                record.setPopupTime(now);
                record.setAckTime(now);
                record.setDeleted(false);
                userRecordMapper.insert(record);
                return null;
            }
            userRecordMapper.update(null, new LambdaUpdateWrapper<SysNoticeUserRecord>()
                    .eq(SysNoticeUserRecord::getId, exists.getId())
                    .set(SysNoticeUserRecord::getAckTime, now));
            return null;
        });
    }

    @Override
    public List<SysNoticeDTO> listActiveNotices(Integer maxCount) {
        return runWithTenantIgnore(() -> fillAttachments(listEffectiveNoticeEntities(currentTenantId(), maxCount)));
    }

    private LambdaQueryWrapper<SysNotice> buildPageWrapper(SysNoticePageParam param) {
        Long tenantId = currentTenantId();
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<SysNotice>()
                .and(w -> w.eq(SysNotice::getScope, SCOPE_PUBLIC)
                        .or()
                        .and(t -> t.eq(SysNotice::getScope, SCOPE_TENANT).eq(SysNotice::getTenantId, tenantId)))
                .like(StringUtils.hasText(param.getTitle()), SysNotice::getTitle, param.getTitle())
                .eq(StringUtils.hasText(param.getScope()), SysNotice::getScope, normalizeScope(param.getScope()))
                .eq(StringUtils.hasText(param.getStatus()), SysNotice::getStatus, normalizeStatus(param.getStatus()));
        LocalDateTime[] startTime = param.getStartTime();
        if (startTime != null && startTime.length == 2 && startTime[0] != null && startTime[1] != null) {
            wrapper.between(SysNotice::getStartTime, startTime[0], startTime[1]);
        }
        wrapper.orderByAsc(SysNotice::getOrderNum).orderByDesc(SysNotice::getCreateTime);
        return wrapper;
    }

    private void validateSaveParam(SysNoticeSaveParam param) {
        if (param == null || !StringUtils.hasText(param.getTitle()) || !StringUtils.hasText(param.getContentHtml())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MSG_TEMPLATE_PARAM_EMPTY);
        }
        String scope = normalizeScope(param.getScope());
        if (!SCOPE_PUBLIC.equals(scope) && !SCOPE_TENANT.equals(scope)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MSG_TEMPLATE_PARAM_EMPTY);
        }
    }

    private SysNotice requireNotice(Long id) {
        if (id == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MSG_TEMPLATE_PARAM_EMPTY);
        }
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.CONFIG_NOT_FOUND);
        }
        return notice;
    }

    private void updateStatus(Long id, String status) {
        runWithTenantIgnore(() -> {
            SysNotice notice = requireNotice(id);
            assertWritable(notice);
            noticeMapper.update(null, new LambdaUpdateWrapper<SysNotice>()
                    .eq(SysNotice::getId, id)
                    .set(SysNotice::getStatus, status));
            return null;
        });
    }

    private List<SysNotice> listEffectiveNoticeEntities(Long tenantId, Integer maxCount) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, STATUS_PUBLISHED)
                .and(w -> w.eq(SysNotice::getScope, SCOPE_PUBLIC)
                        .or()
                        .and(t -> t.eq(SysNotice::getScope, SCOPE_TENANT).eq(SysNotice::getTenantId, tenantId)))
                .and(w -> w.isNull(SysNotice::getStartTime).or().le(SysNotice::getStartTime, now))
                .and(w -> w.isNull(SysNotice::getEndTime).or().ge(SysNotice::getEndTime, now))
                .orderByAsc(SysNotice::getOrderNum)
                .orderByDesc(SysNotice::getCreateTime);
        Integer safeMaxCount = normalizeActiveNoticeMaxCount(maxCount);
        if (safeMaxCount != null) {
            wrapper.last("limit " + safeMaxCount);
        }
        List<SysNotice> notices = noticeMapper.selectList(wrapper);
        return notices == null ? List.of() : notices;
    }

    private Integer normalizeActiveNoticeMaxCount(Integer maxCount) {
        if (maxCount == null) {
            return DEFAULT_ACTIVE_NOTICE_LIMIT;
        }
        if (maxCount <= 0) {
            return null;
        }
        return Math.min(maxCount, 100);
    }

    private void replaceAttachments(Long noticeId, Long tenantId, List<SysNoticeAttachmentDTO> attachments) {
        attachmentMapper.delete(new LambdaQueryWrapper<SysNoticeAttachment>()
                .eq(SysNoticeAttachment::getNoticeId, noticeId));
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        for (SysNoticeAttachmentDTO item : attachments) {
            if (item == null || !StringUtils.hasText(item.getFileUrl())) {
                continue;
            }
            SysNoticeAttachment entity = new SysNoticeAttachment();
            BeanUtils.copyProperties(item, entity);
            entity.setId(null);
            entity.setNoticeId(noticeId);
            entity.setTenantId(tenantId);
            entity.setDeleted(false);
            attachmentMapper.insert(entity);
        }
    }

    private List<SysNoticeAttachmentDTO> listAttachments(Long noticeId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<SysNoticeAttachment>()
                        .eq(SysNoticeAttachment::getNoticeId, noticeId)
                        .orderByAsc(SysNoticeAttachment::getId))
                .stream()
                .map(this::toAttachmentDTO)
                .collect(Collectors.toList());
    }

    private List<SysNoticeDTO> fillAttachments(List<SysNotice> notices) {
        if (notices == null || notices.isEmpty()) {
            return List.of();
        }
        List<Long> ids = notices.stream().map(SysNotice::getId).collect(Collectors.toList());
        Map<Long, List<SysNoticeAttachmentDTO>> attachmentMap = attachmentMapper.selectList(
                        new LambdaQueryWrapper<SysNoticeAttachment>()
                                .in(SysNoticeAttachment::getNoticeId, ids)
                                .orderByAsc(SysNoticeAttachment::getId))
                .stream()
                .map(this::toAttachmentDTO)
                .collect(Collectors.groupingBy(SysNoticeAttachmentDTO::getNoticeId));
        List<SysNoticeDTO> result = new ArrayList<>(notices.size());
        for (SysNotice notice : notices) {
            result.add(toDTO(notice, attachmentMap.getOrDefault(notice.getId(), Collections.emptyList())));
        }
        return result;
    }

    private SysNoticeDTO toDTO(SysNotice notice, List<SysNoticeAttachmentDTO> attachments) {
        SysNoticeDTO dto = new SysNoticeDTO();
        BeanUtils.copyProperties(notice, dto);
        dto.setAttachments(new ArrayList<>(attachments));
        return dto;
    }

    private void assertReadable(SysNotice notice) {
        Long tenantId = currentTenantId();
        if (!SCOPE_PUBLIC.equals(notice.getScope()) && !tenantId.equals(notice.getTenantId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MSG_NO_PERMISSION);
        }
    }

    private void assertWritable(SysNotice notice) {
        if (SCOPE_PUBLIC.equals(notice.getScope())) {
            return;
        }
        Long tenantId = currentTenantId();
        if (!tenantId.equals(notice.getTenantId())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MSG_NO_PERMISSION);
        }
    }

    private <T> T runWithTenantIgnore(Supplier<T> action) {
        boolean oldIgnore = TenantContextIgnore.isIgnore();
        TenantContextIgnore.setIgnore(true);
        try {
            return action.get();
        } finally {
            if (!oldIgnore) {
                TenantContextIgnore.clear();
            }
        }
    }

    private SysNoticeAttachmentDTO toAttachmentDTO(SysNoticeAttachment attachment) {
        SysNoticeAttachmentDTO dto = new SysNoticeAttachmentDTO();
        BeanUtils.copyProperties(attachment, dto);
        return dto;
    }

    private String normalizeScope(String scope) {
        return StringUtils.hasText(scope) ? scope.trim().toUpperCase() : SCOPE_TENANT;
    }

    private String normalizeStatus(String status) {
        String value = StringUtils.hasText(status) ? status.trim().toUpperCase() : STATUS_DRAFT;
        if (STATUS_PUBLISHED.equals(value) || STATUS_DISABLED.equals(value)) {
            return value;
        }
        return STATUS_DRAFT;
    }

    private Long currentTenantId() {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MSG_NO_PERMISSION);
        }
        return tenantId;
    }

    private Long currentUserId() {
        Long userId = UserContext.get();
        if (userId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, SysPromptEnum.MSG_NO_PERMISSION);
        }
        return userId;
    }
}
