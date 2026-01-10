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


import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.position.SysPositionDTO;
import com.forgex.sys.domain.dto.position.SysPositionQueryDTO;
import com.forgex.sys.domain.dto.position.SysPositionSaveParam;
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
            return R.fail("职位不存在");
        }
        
        return R.ok(position);
    }
    
    /**
     * 新增职位
     * 
     * @param param 职位参数
     * @return 职位ID
     */
    @PostMapping("/create")
    public R<Long> create(@Validated @RequestBody SysPositionSaveParam param) {
        try {
            param.setTenantId(TenantContext.get());
            Long id = positionService.create(param);
            return R.ok(id);
        } catch (Exception e) {
            log.error("新增职位失败", e);
            return R.fail(e.getMessage());
        }
    }
    
    /**
     * 更新职位
     * 
     * @param param 职位参数
     * @return 是否成功
     */
    @PostMapping("/update")
    public R<Boolean> update(@Validated @RequestBody SysPositionSaveParam param) {
        try {
            param.setTenantId(TenantContext.get());
            Boolean success = positionService.update(param);
            return R.ok(success);
        } catch (Exception e) {
            log.error("更新职位失败", e);
            return R.fail(e.getMessage());
        }
    }
    
    /**
     * 删除职位
     * 
     * @param params 参数（id）
     * @return 是否成功
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Long tenantId = TenantContext.get();
            
            Boolean success = positionService.delete(id, tenantId);
            return R.ok(success);
        } catch (Exception e) {
            log.error("删除职位失败", e);
            return R.fail(e.getMessage());
        }
    }
}
