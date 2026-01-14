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
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.department.SysDepartmentDTO;
import com.forgex.sys.domain.dto.department.SysDepartmentQueryDTO;
import com.forgex.sys.domain.dto.department.SysDepartmentSaveParam;
import com.forgex.sys.service.SysDepartmentService;
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
 * 部门Controller
 * 
 * 提供部门管理的HTTP接口
 */
@Slf4j
@RestController
@RequestMapping("/sys/department")
@RequiredArgsConstructor
public class SysDepartmentController {
    
    private final SysDepartmentService departmentService;
    
    /**
     * 获取部门树
     * 
     * @param params 参数
     * @return 部门树列表
     */
    @PostMapping("/tree")
    public R<List<SysDepartmentDTO>> tree(@RequestBody Map<String, Object> params) {
        Long tenantId = TenantContext.get();
        List<SysDepartmentDTO> tree = departmentService.getDepartmentTree(tenantId);
        return R.ok(tree);
    }
    
    /**
     * 查询部门列表
     * 
     * @param queryDTO 查询参数
     * @return 部门列表
     */
    @PostMapping("/list")
    public R<List<SysDepartmentDTO>> list(@RequestBody SysDepartmentQueryDTO queryDTO) {
        queryDTO.setTenantId(TenantContext.get());
        List<SysDepartmentDTO> list = departmentService.list(queryDTO);
        return R.ok(list);
    }
    
    /**
     * 获取部门详情
     * 
     * @param params 参数（id）
     * @return 部门详情
     */
    @PostMapping("/get")
    public R<SysDepartmentDTO> get(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long tenantId = TenantContext.get();
        
        SysDepartmentDTO department = departmentService.getById(id, tenantId);
        
        if (department == null) {
            return R.fail("部门不存在");
        }
        
        return R.ok(department);
    }
    
    /**
     * 新增部门
     * 
     * @param param 部门参数
     * @return 部门ID
     */
    @PostMapping("/create")
    @RequirePerm("sys:department:create")
    public R<Long> create(@Validated @RequestBody SysDepartmentSaveParam param) {
        try {
            param.setTenantId(TenantContext.get());
            Long id = departmentService.create(param);
            return R.ok(id);
        } catch (Exception e) {
            log.error("新增部门失败", e);
            return R.fail(e.getMessage());
        }
    }
    
    /**
     * 更新部门
     * 
     * @param param 部门参数
     * @return 是否成功
     */
    @PostMapping("/update")
    @RequirePerm("sys:department:edit")
    public R<Boolean> update(@Validated @RequestBody SysDepartmentSaveParam param) {
        try {
            param.setTenantId(TenantContext.get());
            Boolean success = departmentService.update(param);
            return R.ok(success);
        } catch (Exception e) {
            log.error("更新部门失败", e);
            return R.fail(e.getMessage());
        }
    }
    
    /**
     * 删除部门
     * 
     * @param params 参数（id）
     * @return 是否成功
     */
    @PostMapping("/delete")
    @RequirePerm("sys:department:delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            Long tenantId = TenantContext.get();
            
            Boolean success = departmentService.delete(id, tenantId);
            return R.ok(success);
        } catch (Exception e) {
            log.error("删除部门失败", e);
            return R.fail(e.getMessage());
        }
    }
}
