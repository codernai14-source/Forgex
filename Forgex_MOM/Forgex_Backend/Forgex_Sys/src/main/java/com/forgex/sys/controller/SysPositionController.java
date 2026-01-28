/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.position.SysPositionDTO;
import com.forgex.sys.domain.dto.position.SysPositionQueryDTO;
import com.forgex.sys.domain.dto.position.SysPositionSaveParam;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.service.SysPositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 职位Controller
 * 
 * 提供职位管理的HTTP接口
 */
@Slf4j
@RestController
@RequestMapping("/sys/position")
@RequiredArgsConstructor
public class SysPositionController {
    
    private final SysPositionService positionService;
    
    /**
     * 查询职位列表
     * 
     * @param queryDTO 查询参数
     * @return 职位列表
     */
    @PostMapping("/list")
    public R<List<SysPositionDTO>> list(@RequestBody SysPositionQueryDTO queryDTO) {
        queryDTO.setTenantId(TenantContext.get());
        List<SysPositionDTO> list = positionService.list(queryDTO);
        return R.ok(list);
    }
    
    /**
     * 分页查询职位列表
     *
     * @param queryDTO 查询参数（分页参数来自 BaseGetParam：pageNum/pageSize）
     * @return 分页结果
     */
    @PostMapping("/page")
    public R<IPage<SysPositionDTO>> page(@RequestBody SysPositionQueryDTO queryDTO) {
        queryDTO.setTenantId(TenantContext.get());
        Page<SysPosition> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        return R.ok(positionService.pagePositions(page, queryDTO));
    }
    
    /**
     * 获取职位详情
     * 
     * @param params 参数（id）
     * @return 职位详情
     */
    @PostMapping("/get")
    public R<SysPositionDTO> get(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long tenantId = TenantContext.get();
        
        SysPositionDTO position = positionService.getById(id, tenantId);

        if (position == null) {
            return R.fail(CommonPrompt.POSITION_NOT_FOUND);
        }
        
        return R.ok(position);
    }
    
    /**
     * 新增职位
     *
     * @param param 职位参数
     * @return 操作结果
     */
    @PostMapping("/create")
    @RequirePerm("sys:position:create")
    public R<Void> create(@Validated @RequestBody SysPositionSaveParam param) {
        param.setTenantId(TenantContext.get());
        Long id = positionService.create(param);
        // 使用职位名称填充“新增成功”提示的占位符参数
        return R.okWithArgs(CommonPrompt.CREATE_SUCCESS, param.getPositionName());
    }

    /**
     * 更新职位
     *
     * @param param 职位参数
     * @return 操作结果
     */
    @PostMapping("/update")
    @RequirePerm("sys:position:edit")
    public R<Void> update(@Validated @RequestBody SysPositionSaveParam param) {
        param.setTenantId(TenantContext.get());
        Boolean success = positionService.update(param);
        // 使用职位名称填充“修改成功”提示的占位符参数
        return R.okWithArgs(CommonPrompt.UPDATE_SUCCESS, param.getPositionName());
    }

    /**
     * 删除职位
     *
     * @param params 参数（id）
     * @return 操作结果
     */
    @PostMapping("/delete")
    @RequirePerm("sys:position:delete")
    public R<Void> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long tenantId = TenantContext.get();
        
        // 删除前获取职位名称
        SysPositionDTO position = positionService.getById(id, tenantId);
        if (position == null) {
            return R.fail(CommonPrompt.POSITION_NOT_FOUND);
        }
        String positionName = position.getPositionName();
        
        Boolean success = positionService.delete(id, tenantId);
        // 使用职位名称填充“删除成功”提示的占位符参数
        return R.okWithArgs(CommonPrompt.DELETE_SUCCESS, positionName);
    }
}
