package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 职位实体
 * 
 * 映射表：sys_position
 * 用于职位管理
 */
@Data
@TableName("sys_position")
public class SysPosition extends BaseEntity {
    
    /** 职位名称 */
    private String positionName;
    
    /** 职位编码 */
    private String positionCode;
    
    /** 职位级别 */
    private Integer positionLevel;
    
    /** 排序号 */
    private Integer orderNum;
    
    /** 状态（0=禁用，1=启用） */
    private Integer status;
    
    /** 备注 */
    private String remark;
}
