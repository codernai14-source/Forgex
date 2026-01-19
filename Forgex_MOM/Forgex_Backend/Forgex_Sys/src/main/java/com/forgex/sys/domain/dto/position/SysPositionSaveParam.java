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
package com.forgex.sys.domain.dto.position;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;




/**
 * 职位保存参数
 * 
 * 用于职位新增和编辑的参数封装
 */
@Data
public class SysPositionSaveParam {
    
    /** 主键ID（编辑时必填） */
    private Long id;
    
    /** 租户ID */
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;
    
    /** 部门ID */
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;
    
    /** 职位名称 */
    @NotBlank(message = "职位名称不能为空")
    private String positionName;
    
    /** 职位编码 */
    @NotBlank(message = "职位编码不能为空")
    private String positionCode;
    
    /** 职位级别 */
    private Integer positionLevel;
    
    /** 排序号 */
    private Integer orderNum;
    
    /** 状态：false=禁用，true=启用 */
    private Boolean status;
    
    /** 备注 */
    private String remark;
}
