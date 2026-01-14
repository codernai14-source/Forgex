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
package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 数据字典 DTO
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 */
@Data
public class DictDTO {
    
    /**
     * 主键（更新时必填）
     */
    private Long id;
    
    /**
     * 父节点ID
     */
    private Long parentId;
    
    /**
     * 字典名称
     */
    private String dictName;
    
    /**
     * 字典编码（仅根节点）
     */
    private String dictCode;
    
    /**
     * 字典值（仅叶子节点）
     */
    private String dictValue;
    
    /**
     * 排序号
     */
    private Integer orderNum;
    
    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 租户ID
     */
    private Long tenantId;
}
