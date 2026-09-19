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
 * 数据字典实体（单表树形结构）
 * <p>
 * 映射表：{@code sys_dict}。用于管理系统字典数据。
 * 字段：
 * <ul>
 *   <li>{@code parentId} 父节点ID（0表示根节点）</li>
 *   <li>{@code dictName} 字典名称</li>
 *   <li>{@code dictCode} 字典编码（仅根节点）</li>
 *   <li>{@code dictValue} 字典值（仅叶子节点）</li>
 *   <li>{@code orderNum} 排序号</li>
 *   <li>{@code status} 状态（1-启用，0-禁用）</li>
 *   <li>{@code remark} 备注</li>
 * </ul>
 * </p>
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 */
@Data
@TableName("sys_dict")
public class SysDict extends BaseEntity {
    
    /**
     * 父节点ID（0表示根节点）
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

    private Long moduleId;

    /**
     * 字典值国际化JSON
     * 存储多语言字典值，格式：{"zh":"男","en":"Male"}
     */
    private String dictValueI18nJson;

    /**
     * 标签样式配置JSON
     * 用于字典值标签的样式配置，支持颜色和图标
     * 格式：{"color":"success","icon":"CheckCircleOutlined"}
     * 
     * @see com.forgex.sys.domain.vo.TagStyleVO
     */
    private String tagStyleJson;

    private String nodePath;

    private Integer level;

    private Integer childrenCount;
    
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
}
