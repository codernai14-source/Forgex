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

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位DTO
 * 
 * 用于职位信息的数据传输
 */
@Data
public class SysPositionDTO {
    
    /** 主键ID */
    private Long id;
    
    /** 职位名称 */
    private String positionName;
    
    /** 职位编码 */
    private String positionCode;
    
    /** 职位级别 */
    private Integer positionLevel;
    
    /** 排序号 */
    private Integer orderNum;
    
    /** 状态：false=禁用，true=启用 */
    private Boolean status;
    
    /** 备注 */
    private String remark;
    
    /** 租户ID */
    private Long tenantId;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /** 创建人 */
    private String createBy;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /** 更新人 */
    private String updateBy;
}
