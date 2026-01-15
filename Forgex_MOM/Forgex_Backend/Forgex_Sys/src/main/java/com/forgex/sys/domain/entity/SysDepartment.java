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
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import com.forgex.common.dict.DictI18n;
import lombok.Data;

/**
 * 部门实体
 * 
 * 映射表：sys_department
 * 用于组织架构管理，支持树状结构
 * 组织类型：group=集团, company=公司, subsidiary=子公司, department=部门, team=班组
 */
@Data
@TableName("sys_department")
public class SysDepartment extends BaseEntity {
    
    /** 父部门ID */
    private Long parentId;
    
    /** 父部门名称（关联查询结果） */
    private String parentName;
    
    /** 组织类型：group=集团, company=公司, subsidiary=子公司, department=部门, team=班组 */
    @DictI18n(nodePathConst = "org_type", targetField = "orgTypeText")
    private String orgType;
    
    /** 组织类型文本（字典翻译结果） */
    private String orgTypeText;
    
    /** 组织层级：1=集团, 2=公司, 3=子公司, 4=部门, 5=班组 */
    @DictI18n(nodePathConst = "org_level", targetField = "orgLevelText")
    private Integer orgLevel;
    
    /** 组织层级文本（字典翻译结果） */
    private String orgLevelText;
    
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
    
    /** 状态文本（字典翻译结果） */
    private String statusText;
}
