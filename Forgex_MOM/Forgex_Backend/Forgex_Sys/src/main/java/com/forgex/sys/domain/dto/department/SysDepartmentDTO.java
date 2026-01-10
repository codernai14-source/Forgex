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
package com.forgex.sys.domain.dto.department;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门DTO
 * 
 * 用于部门列表展示和树形结构
 */
@Data
public class SysDepartmentDTO {
    
    /** 主键ID */
    private Long id;
    
    /** 父部门ID */
    private Long parentId;
    
    /** 组织类型：group=集团, company=公司, subsidiary=子公司, department=部门, team=班组 */
    private String orgType;
    
    /** 组织层级：1=集团, 2=公司, 3=子公司, 4=部门, 5=班组 */
    private Integer orgLevel;
    
    /** 部门名称 */
    private String deptName;
    
    /** 部门编码 */
    private String deptCode;
    
    /** 部门负责人 */
    private String leader;
    
    /** 联系电话 */
    private String phone;
    
    /** 邮箱 */
    private String email;
    
    /** 排序号 */
    private Integer orderNum;
    
    /** 状态：false=禁用，true=启用 */
    private Boolean status;
    
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
    
    /** 子部门列表（用于树形结构） */
    private List<SysDepartmentDTO> children;
}
