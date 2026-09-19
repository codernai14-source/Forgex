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
package com.forgex.sys.service;

import com.forgex.sys.domain.dto.department.SysDepartmentDTO;
import com.forgex.sys.domain.dto.department.SysDepartmentQueryDTO;
import com.forgex.sys.domain.dto.department.SysDepartmentSaveParam;

import java.util.List;

/**
 * 部门Service接口
 * 
 * 提供部门管理的业务逻辑接口
 */
public interface SysDepartmentService {
    
    /**
     * 获取部门树
     * 
     * @param tenantId 租户ID
     * @return 部门树列表
     */
    List<SysDepartmentDTO> getDepartmentTree(Long tenantId);
    
    /**
     * 查询部门列表
     * 
     * @param queryDTO 查询参数
     * @return 部门列表
     */
    List<SysDepartmentDTO> list(SysDepartmentQueryDTO queryDTO);
    
    /**
     * 根据ID获取部门详情
     * 
     * @param id 部门ID
     * @param tenantId 租户ID
     * @return 部门详情
     */
    SysDepartmentDTO getById(Long id, Long tenantId);
    
    /**
     * 新增部门
     * 
     * @param param 部门参数
     * @return 部门ID
     */
    Long create(SysDepartmentSaveParam param);
    
    /**
     * 更新部门
     * 
     * @param param 部门参数
     * @return 是否成功
     */
    Boolean update(SysDepartmentSaveParam param);
    
    /**
     * 删除部门
     * 
     * @param id 部门ID
     * @param tenantId 租户ID
     * @return 是否成功
     */
    Boolean delete(Long id, Long tenantId);
}
