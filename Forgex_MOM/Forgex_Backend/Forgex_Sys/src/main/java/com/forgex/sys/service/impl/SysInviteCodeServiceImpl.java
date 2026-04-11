/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.dto.invitecode.*;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.domain.entity.SysInviteCode;
import com.forgex.sys.domain.entity.SysInviteRegisterRecord;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.mapper.SysInviteCodeMapper;
import com.forgex.sys.mapper.SysInviteRegisterRecordMapper;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.service.SysInviteCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 邀请码Service实现
 *
 * 提供邀请码管理的业务逻辑实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysInviteCodeServiceImpl implements SysInviteCodeService {

    private final SysInviteCodeMapper inviteCodeMapper;
    private final SysInviteRegisterRecordMapper registerRecordMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysPositionMapper positionMapper;

    @Override
    public IPage<SysInviteCodeDTO> pageInviteCodes(Page<SysInviteCode> page, SysInviteCodeQueryDTO queryDTO) {
        LambdaQueryWrapper<SysInviteCode> wrapper = buildQueryWrapper(queryDTO);
        wrapper.orderByDesc(SysInviteCode::getCreateTime);
        IPage<SysInviteCode> inviteCodePage = inviteCodeMapper.selectPage(page, wrapper);
        return inviteCodePage.convert(this::convertToDTO);
    }

    @Override
    public SysInviteCodeDTO getById(Long id, Long tenantId) {
        LambdaQueryWrapper<SysInviteCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysInviteCode::getId, id)
               .eq(SysInviteCode::getDeleted, false);
        if (tenantId != null) {
            wrapper.eq(SysInviteCode::getTenantId, tenantId);
        }
        SysInviteCode inviteCode = inviteCodeMapper.selectOne(wrapper);
        if (inviteCode == null) {
            return null;
        }
        return convertToDTO(inviteCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SysInviteCodeSaveParam param) {
        // 校验部门是否存在
        SysDepartment dept = departmentMapper.selectById(param.getDepartmentId());
        if (dept == null || Boolean.TRUE.equals(dept.getDeleted())) {
            throw new RuntimeException("部门不存在");
        }

        // 校验职位是否存在（可选）
        if (param.getPositionId() != null) {
            SysPosition pos = positionMapper.selectById(param.getPositionId());
            if (pos == null || Boolean.TRUE.equals(pos.getDeleted())) {
                throw new RuntimeException("职位不存在");
            }
        }

        // 生成邀请码
        String code = generateInviteCode();

        SysInviteCode inviteCode = new SysInviteCode();
        BeanUtils.copyProperties(param, inviteCode);
        inviteCode.setInviteCode(code);
        inviteCode.setUsedCount(0);
        inviteCode.setStatus(true);

        inviteCodeMapper.insert(inviteCode);

        log.info("创建邀请码成功，ID：{}，邀请码：{}", inviteCode.getId(), code);
        return inviteCode.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disable(Long id, Long tenantId) {
        LambdaUpdateWrapper<SysInviteCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysInviteCode::getId, id)
               .eq(SysInviteCode::getDeleted, false)
               .set(SysInviteCode::getStatus, false);
        if (tenantId != null) {
            wrapper.eq(SysInviteCode::getTenantId, tenantId);
        }
        int rows = inviteCodeMapper.update(null, wrapper);
        log.info("停用邀请码，ID：{}，结果：{}", id, rows > 0);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id, Long tenantId) {
        LambdaQueryWrapper<SysInviteCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysInviteCode::getId, id);
        if (tenantId != null) {
            wrapper.eq(SysInviteCode::getTenantId, tenantId);
        }
        int rows = inviteCodeMapper.delete(wrapper);
        log.info("删除邀请码，ID：{}", id);
        return rows > 0;
    }

    @Override
    public IPage<SysInviteRecordDTO> pageRecords(Page<SysInviteRegisterRecord> page, SysInviteRecordQueryDTO queryDTO) {
        LambdaQueryWrapper<SysInviteRegisterRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysInviteRegisterRecord::getDeleted, false);

        if (queryDTO.getTenantId() != null) {
            wrapper.eq(SysInviteRegisterRecord::getTenantId, queryDTO.getTenantId());
        }
        if (queryDTO.getInviteId() != null) {
            wrapper.eq(SysInviteRegisterRecord::getInviteId, queryDTO.getInviteId());
        }
        if (StringUtils.hasText(queryDTO.getInviteCode())) {
            wrapper.eq(SysInviteRegisterRecord::getInviteCode, queryDTO.getInviteCode());
        }

        wrapper.orderByDesc(SysInviteRegisterRecord::getRegisterTime);

        IPage<SysInviteRegisterRecord> recordPage = registerRecordMapper.selectPage(page, wrapper);
        return recordPage.convert(this::convertRecordToDTO);
    }

    @Override
    public SysInviteCode getByInviteCode(String inviteCode) {
        LambdaQueryWrapper<SysInviteCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysInviteCode::getInviteCode, inviteCode)
               .eq(SysInviteCode::getDeleted, false);
        return inviteCodeMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean incrementUsedCount(Long id) {
        // 使用数据库级别的原子操作，避免并发超发
        LambdaUpdateWrapper<SysInviteCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysInviteCode::getId, id)
               .setSql("used_count = used_count + 1");
        int rows = inviteCodeMapper.update(null, wrapper);
        return rows > 0;
    }

    // ==================== 私有方法 ====================

    /**
     * 生成邀请码：8位大写字母+数字
     */
    private String generateInviteCode() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return uuid.substring(0, 8);
    }

    /**
     * 实体转DTO
     */
    private SysInviteCodeDTO convertToDTO(SysInviteCode entity) {
        SysInviteCodeDTO dto = new SysInviteCodeDTO();
        BeanUtils.copyProperties(entity, dto);

        // 计算剩余可用人数
        int remain = entity.getMaxRegisterCount() - entity.getUsedCount();
        dto.setRemainCount(Math.max(remain, 0));

        // 计算状态标签
        dto.setStatusLabel(computeStatusLabel(entity));

        // 查询部门名称
        if (entity.getDepartmentId() != null) {
            SysDepartment dept = departmentMapper.selectById(entity.getDepartmentId());
            if (dept != null) {
                dto.setDepartmentName(dept.getDeptName());
            }
        }

        // 查询职位名称
        if (entity.getPositionId() != null) {
            SysPosition pos = positionMapper.selectById(entity.getPositionId());
            if (pos != null) {
                dto.setPositionName(pos.getPositionName());
            }
        }

        return dto;
    }

    /**
     * 计算邀请码的综合状态标签
     */
    private String computeStatusLabel(SysInviteCode entity) {
        if (Boolean.FALSE.equals(entity.getStatus())) {
            return "DISABLED";
        }
        if (entity.getExpireTime() != null && LocalDateTime.now().isAfter(entity.getExpireTime())) {
            return "EXPIRED";
        }
        if (entity.getUsedCount() >= entity.getMaxRegisterCount()) {
            return "USED_UP";
        }
        return "ACTIVE";
    }

    /**
     * 注册记录实体转DTO
     */
    private SysInviteRecordDTO convertRecordToDTO(SysInviteRegisterRecord record) {
        SysInviteRecordDTO dto = new SysInviteRecordDTO();
        BeanUtils.copyProperties(record, dto);
        return dto;
    }

    /**
     * 构建邀请码查询条件
     */
    private LambdaQueryWrapper<SysInviteCode> buildQueryWrapper(SysInviteCodeQueryDTO queryDTO) {
        LambdaQueryWrapper<SysInviteCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysInviteCode::getDeleted, false);

        if (queryDTO.getTenantId() != null) {
            wrapper.eq(SysInviteCode::getTenantId, queryDTO.getTenantId());
        }
        if (StringUtils.hasText(queryDTO.getInviteCode())) {
            wrapper.like(SysInviteCode::getInviteCode, queryDTO.getInviteCode());
        }
        if (queryDTO.getDepartmentId() != null) {
            wrapper.eq(SysInviteCode::getDepartmentId, queryDTO.getDepartmentId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysInviteCode::getStatus, queryDTO.getStatus());
        }

        return wrapper;
    }
}

