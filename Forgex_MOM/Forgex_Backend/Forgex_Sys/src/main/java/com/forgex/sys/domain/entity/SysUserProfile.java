package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

import java.util.List;

/**
 * 用户附属信息实体
 * 
 * 映射表：sys_user_profile
 * 用于存储用户的扩展信息，如政治面貌、家庭住址、工作经历等
 */
@Data
@TableName(value = "sys_user_profile", autoResultMap = true)
public class SysUserProfile extends BaseEntity {
    
    /** 用户ID */
    private Long userId;
    
    /** 政治面貌 */
    private String politicalStatus;
    
    /** 家庭住址 */
    private String homeAddress;
    
    /** 紧急联系人 */
    private String emergencyContact;
    
    /** 紧急联系人电话 */
    private String emergencyPhone;
    
    /** 引荐人 */
    private String referrer;
    
    /** 学历 */
    private String education;
    
    /** 工作经历（JSON格式） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<WorkHistory> workHistory;
    
    /**
     * 工作经历内部类
     */
    @Data
    public static class WorkHistory {
        /** 公司名称 */
        private String company;
        /** 职位 */
        private String position;
        /** 开始时间 */
        private String startDate;
        /** 结束时间 */
        private String endDate;
        /** 工作描述 */
        private String description;
    }
}
