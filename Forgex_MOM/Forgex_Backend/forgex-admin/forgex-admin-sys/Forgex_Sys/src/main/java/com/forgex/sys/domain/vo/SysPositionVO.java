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
package com.forgex.sys.domain.vo;

import com.forgex.common.base.BaseEntity;
import com.forgex.common.dict.DictI18n;
import lombok.Data;

/**
 * 职位视图对象
 * 用于向前端返回职位数据，包含展示需要的衍生字段
 *
 * @author coder_nai
 * @version 1.0.0
 */
@Data
public class SysPositionVO extends BaseEntity {
    
    /** 职位名称 */
    private String positionName;
    
    /** 职位编码 */
    private String positionCode;
    
    /** 职位级别 */
    @DictI18n(nodePathConst = "position_level", targetField = "positionLevelText")
    private Integer positionLevel;
    
    /** 职位级别文本（字典翻译结果） */
    private String positionLevelText;
    
    /** 排序号 */
    private Integer orderNum;
    
    /** 状态：false=禁用，true=启用 */
    @DictI18n(nodePathConst = "status", targetField = "statusText")
    private Boolean status;
    
    /** 状态文本（字典翻译结果） */
    private String statusText;
    
    /** 备注 */
    private String remark;
    
    /** 部门ID */
    private Long departmentId;
    
    /** 部门名称（关联查询结果） */
    private String departmentName;
}