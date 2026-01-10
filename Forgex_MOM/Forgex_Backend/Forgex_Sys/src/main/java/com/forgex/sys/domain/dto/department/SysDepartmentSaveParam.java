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

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 部门保存参数
 * 
 * 用于部门新增和编辑的参数封装
 */
@Data
public class SysDepartmentSaveParam {
    
    /** 主键ID（编辑时必填） */
    private Long id;
    
    /** 租户ID */
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;
    
    /** 父部门ID */
    private Long parentId;
    
    /** 组织类型：group=集团, company=公司, subsidiary=子公司, department=部门, team=班组 */
    @NotBlank(message = "组织类型不能为空")
    private String orgType;
    
    /** 组织层级：1=集团, 2=公司, 3=子公司, 4=部门, 5=班组 */
    @NotNull(message = "组织层级不能为空")
    private Integer orgLevel;
    
    /** 部门名称 */
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    
    /** 部门编码 */
    @NotBlank(message = "部门编码不能为空")
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
}
