package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
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
    
    /** 状态（0=禁用，1=启用） */
    private Integer status;
}
