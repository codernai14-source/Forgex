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

package com.forgex.sys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.dto.invitecode.*;
import com.forgex.sys.domain.entity.SysInviteCode;
import com.forgex.sys.domain.entity.SysInviteRegisterRecord;

import java.util.List;

/**
 * 邀请码Service接口
 *
 * 提供邀请码管理的业务逻辑接口
 */
public interface SysInviteCodeService {

    /**
     * 分页查询邀请码列表
     *
     * @param page     分页参数
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    IPage<SysInviteCodeDTO> pageInviteCodes(Page<SysInviteCode> page, SysInviteCodeQueryDTO queryDTO);

    /**
     * 根据ID获取邀请码详情
     *
     * @param id 邀请码ID
     * @param tenantId 租户ID
     * @return 邀请码详情
     */
    SysInviteCodeDTO getById(Long id, Long tenantId);

    /**
     * 新增邀请码
     *
     * @param param 邀请码参数
     * @return 新增的邀请码ID
     */
    Long create(SysInviteCodeSaveParam param);

    /**
     * 停用邀请码
     *
     * @param id 邀请码ID
     * @param tenantId 租户ID
     * @return 是否成功
     */
    Boolean disable(Long id, Long tenantId);

    /**
     * 删除邀请码
     *
     * @param id 邀请码ID
     * @param tenantId 租户ID
     * @return 是否成功
     */
    Boolean delete(Long id, Long tenantId);

    /**
     * 分页查询邀请码使用记录
     *
     * @param page     分页参数
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    IPage<SysInviteRecordDTO> pageRecords(Page<SysInviteRegisterRecord> page, SysInviteRecordQueryDTO queryDTO);

    /**
     * 根据邀请码字符串查询邀请码实体（用于注册校验）
     *
     * @param inviteCode 邀请码
     * @return 邀请码实体，不存在返回 null
     */
    SysInviteCode getByInviteCode(String inviteCode);

    /**
     * 增加邀请码使用次数（行级锁 + 事务）
     *
     * @param id 邀请码ID
     * @return 是否成功
     */
    Boolean incrementUsedCount(Long id);
}

