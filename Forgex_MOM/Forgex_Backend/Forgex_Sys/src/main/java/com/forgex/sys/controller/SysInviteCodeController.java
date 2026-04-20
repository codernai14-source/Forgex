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

package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.invitecode.*;
import com.forgex.sys.domain.entity.SysInviteCode;
import com.forgex.sys.domain.entity.SysInviteRegisterRecord;
import com.forgex.sys.service.SysInviteCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 邀请码管理 Controller
 *
 * 提供邀请码的增删改查、停用、使用记录查询等接口
 * 菜单位置：系统管理 -> 组织架构 -> 邀请注册
 */
@Slf4j
@RestController
@RequestMapping("/sys/invite-code")
@RequiredArgsConstructor
public class SysInviteCodeController {

    private final SysInviteCodeService inviteCodeService;

    /**
     * 分页查询邀请码列表
     */
    @PostMapping("/page")
    public R<IPage<SysInviteCodeDTO>> page(@RequestBody SysInviteCodeQueryDTO queryDTO) {
        queryDTO.setTenantId(TenantContext.get());
        Page<SysInviteCode> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return R.ok(inviteCodeService.pageInviteCodes(page, queryDTO));
    }

    /**
     * 获取邀请码详情
     */
    @PostMapping("/get")
    public R<SysInviteCodeDTO> get(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long tenantId = TenantContext.get();
        SysInviteCodeDTO dto = inviteCodeService.getById(id, tenantId);
        if (dto == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }
        return R.ok(dto);
    }

    /**
     * 新增邀请码
     */
    @PostMapping("/create")
    @RequirePerm("sys:invite-code:add")
    public R<SysInviteCodeDTO> create(@Validated @RequestBody SysInviteCodeSaveParam param) {
        param.setTenantId(TenantContext.get());
        Long id = inviteCodeService.create(param);
        // 返回新建的邀请码详情（包含生成的邀请码字符串）
        SysInviteCodeDTO dto = inviteCodeService.getById(id, param.getTenantId());
        return R.ok(CommonPrompt.CREATE_SUCCESS, dto);
    }

    /**
     * 停用邀请码
     */
    @PostMapping("/disable")
    @RequirePerm("sys:invite-code:edit")
    public R<Void> disable(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long tenantId = TenantContext.get();
        Boolean success = inviteCodeService.disable(id, tenantId);
        if (!success) {
            return R.fail(CommonPrompt.OPERATION_FAILED);
        }
        return R.ok(CommonPrompt.DISABLE_SUCCESS);
    }

    /**
     * 删除邀请码
     */
    @PostMapping("/delete")
    @RequirePerm("sys:invite-code:delete")
    public R<Void> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long tenantId = TenantContext.get();

        SysInviteCodeDTO dto = inviteCodeService.getById(id, tenantId);
        if (dto == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }

        Boolean success = inviteCodeService.delete(id, tenantId);
        if (!success) {
            return R.fail(CommonPrompt.OPERATION_FAILED);
        }
        return R.okWithArgs(CommonPrompt.DELETE_SUCCESS, dto.getInviteCode());
    }

    /**
     * 分页查询邀请码使用记录
     */
    @PostMapping("/record/page")
    public R<IPage<SysInviteRecordDTO>> recordPage(@RequestBody SysInviteRecordQueryDTO queryDTO) {
        queryDTO.setTenantId(TenantContext.get());
        Page<SysInviteRegisterRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return R.ok(inviteCodeService.pageRecords(page, queryDTO));
    }
}

