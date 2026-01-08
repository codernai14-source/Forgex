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
