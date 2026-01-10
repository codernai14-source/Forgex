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

import com.forgex.sys.domain.dto.position.SysPositionDTO;
import com.forgex.sys.domain.dto.position.SysPositionQueryDTO;
import com.forgex.sys.domain.dto.position.SysPositionSaveParam;

import java.util.List;

/**
 * 职位Service接口
 * 
 * 提供职位管理的业务逻辑接口
 */
public interface SysPositionService {
    
    /**
     * 查询职位列表
     * 
     * @param queryDTO 查询参数
     * @return 职位列表
     */
    List<SysPositionDTO> list(SysPositionQueryDTO queryDTO);
    
    /**
     * 根据ID获取职位详情
     * 
     * @param id 职位ID
     * @param tenantId 租户ID
     * @return 职位详情
     */
    SysPositionDTO getById(Long id, Long tenantId);
    
    /**
     * 新增职位
     * 
     * @param param 职位参数
     * @return 职位ID
     */
    Long create(SysPositionSaveParam param);
    
    /**
     * 更新职位
     * 
     * @param param 职位参数
     * @return 是否成功
     */
    Boolean update(SysPositionSaveParam param);
    
    /**
     * 删除职位
     * 
     * @param id 职位ID
     * @param tenantId 租户ID
     * @return 是否成功
     */
    Boolean delete(Long id, Long tenantId);
}
